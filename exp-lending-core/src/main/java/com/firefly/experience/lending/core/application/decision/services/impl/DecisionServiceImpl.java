package com.firefly.experience.lending.core.application.decision.services.impl;

import com.firefly.domain.common.contracts.sdk.api.ContractsApi;
import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.ProposedOfferDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.UnderwritingDecisionDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.experience.lending.core.application.decision.commands.RejectOfferCommand;
import com.firefly.experience.lending.core.application.decision.commands.SignContractCommand;
import com.firefly.experience.lending.core.application.decision.queries.ContractDTO;
import com.firefly.experience.lending.core.application.decision.queries.DecisionDTO;
import com.firefly.experience.lending.core.application.decision.queries.OfferDetailDTO;
import com.firefly.experience.lending.core.application.decision.queries.OfferSummaryDTO;
import com.firefly.experience.lending.core.application.decision.queries.ScoringStatusDTO;
import com.firefly.experience.lending.core.application.decision.services.DecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link DecisionService}, orchestrating the underwriting decision
 * flow via the domain Loan Origination SDK and domain Contracts SDK.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {

    private final LoanOriginationApi loanOriginationApi;
    private final ContractsApi contractsApi;
    private final ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Scoring
    // -------------------------------------------------------------------------

    @Override
    public Mono<ScoringStatusDTO> getScoringStatus(UUID applicationId) {
        log.debug("Fetching scoring status for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationDecisions(applicationId, null)
                .map(page -> {
                    boolean hasDecision = page.getContent() != null && !page.getContent().isEmpty();
                    return ScoringStatusDTO.builder()
                            .applicationId(applicationId)
                            .status(hasDecision ? "COMPLETED" : "PENDING")
                            // Score is sourced from core-lending-credit-scoring in production;
                            // not available through the underwriting decision for the MVP.
                            .score(null)
                            .build();
                });
    }

    // -------------------------------------------------------------------------
    // Decision
    // -------------------------------------------------------------------------

    @Override
    public Mono<DecisionDTO> getDecision(UUID applicationId) {
        log.debug("Fetching underwriting decision for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationDecisions(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .next()
                .map(decision -> mapToDecisionDTO(applicationId, decision));
    }

    // -------------------------------------------------------------------------
    // Offers
    // -------------------------------------------------------------------------

    @Override
    public Flux<OfferSummaryDTO> getOffers(UUID applicationId) {
        log.debug("Listing offers for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationOffers(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToOfferSummaryDTO);
    }

    @Override
    public Mono<OfferDetailDTO> getOffer(UUID applicationId, UUID offerId) {
        log.debug("Fetching offer offerId={} for applicationId={}", offerId, applicationId);
        return loanOriginationApi
                .getApplicationOffers(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .filter(o -> offerId.equals(o.getProposedOfferId()))
                .next()
                .map(this::mapToOfferDetailDTO);
    }

    @Override
    public Mono<Void> acceptOffer(UUID applicationId, UUID offerId) {
        log.debug("Accepting offer offerId={} for applicationId={}", offerId, applicationId);
        // MVP: the domain SDK does not expose a dedicated update-offer endpoint.
        // Offer acceptance is handled as part of the application approval flow via approveApplication.
        return loanOriginationApi
                .approveApplication(applicationId, UUID.randomUUID().toString())
                .then();
    }

    @Override
    public Mono<Void> rejectOffer(UUID applicationId, UUID offerId, RejectOfferCommand command) {
        log.debug("Rejecting offer offerId={} for applicationId={}", offerId, applicationId);
        // MVP: the domain SDK does not expose a dedicated update-offer endpoint.
        // Offer rejection is handled as part of the application rejection flow via rejectApplication.
        return loanOriginationApi
                .rejectApplication(applicationId, UUID.randomUUID().toString())
                .then();
    }

    // -------------------------------------------------------------------------
    // Contract
    // -------------------------------------------------------------------------

    @Override
    public Mono<ContractDTO> getContract(UUID applicationId) {
        log.debug("Fetching contract for applicationId={}", applicationId);
        // MVP convention: contract is created with contractNumber = applicationId.toString()
        // by the domain service when the offer is accepted and a contract saga completes.
        // Use listByParty or getContractDetail to retrieve the contract.
        // Since no filterContracts method exists in domain SDK, use getContractDetail with applicationId as contractId.
        return contractsApi.getContractDetail(applicationId, null)
                .map(item -> {
                    // The domain SDK returns Object; Jackson deserializes to a LinkedHashMap.
                    var jsonMap = objectMapper.convertValue(item, java.util.Map.class);
                    return ContractDTO.builder()
                            .contractId(jsonMap.get("contractId") != null
                                    ? UUID.fromString(jsonMap.get("contractId").toString()) : applicationId)
                            .applicationId(applicationId)
                            .status(jsonMap.get("contractStatus") != null
                                    ? jsonMap.get("contractStatus").toString() : null)
                            .documentUrl(jsonMap.get("contractNumber") != null
                                    ? jsonMap.get("contractNumber").toString() : null)
                            .build();
                });
    }

    @Override
    public Mono<ContractDTO> signContract(UUID applicationId, SignContractCommand command) {
        log.debug("Initiating contract signing for applicationId={}", applicationId);
        // Step 1: Retrieve the contract associated with this application.
        return getContract(applicationId)
                // Step 2: The domain SDK does not expose an updateContract endpoint.
                // Contract signing is recorded by returning the contract with ACTIVE status.
                // Full signature completion will be handled when the domain SDK surfaces
                // a dedicated signing operation.
                .map(contract -> ContractDTO.builder()
                        .contractId(contract.getContractId())
                        .applicationId(applicationId)
                        .status("ACTIVE")
                        .documentUrl(contract.getDocumentUrl())
                        .signedAt(LocalDateTime.now())
                        .build());
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private DecisionDTO mapToDecisionDTO(UUID applicationId, UnderwritingDecisionDTO src) {
        // Derive result from approved amount: positive amount signals APPROVED.
        String result = (src.getApprovedAmount() != null
                && src.getApprovedAmount().compareTo(BigDecimal.ZERO) > 0)
                ? "APPROVED" : "REJECTED";
        return DecisionDTO.builder()
                .applicationId(applicationId)
                .result(result)
                .reasons(List.of())
                .decidedAt(src.getCreatedAt() != null ? src.getCreatedAt() : LocalDateTime.now())
                .build();
    }

    private OfferSummaryDTO mapToOfferSummaryDTO(ProposedOfferDTO src) {
        return OfferSummaryDTO.builder()
                .offerId(src.getProposedOfferId())
                .amount(src.getRequestedAmount())
                .term(src.getRequestedTenorMonths())
                .monthlyPayment(src.getMonthlyPayment())
                .annualRate(src.getRequestedInterestRate())
                .status(src.getOfferStatus())
                .build();
    }

    private OfferDetailDTO mapToOfferDetailDTO(ProposedOfferDTO src) {
        BigDecimal totalCost = (src.getRequestedAmount() != null && src.getTotalInterest() != null)
                ? src.getRequestedAmount().add(src.getTotalInterest())
                : src.getRequestedAmount();
        return OfferDetailDTO.builder()
                .offerId(src.getProposedOfferId())
                .amount(src.getRequestedAmount())
                .term(src.getRequestedTenorMonths())
                .monthlyPayment(src.getMonthlyPayment())
                .annualRate(src.getRequestedInterestRate())
                .status(src.getOfferStatus())
                .totalCost(totalCost)
                .fees(List.of())
                .conditions(List.of())
                .build();
    }
}
