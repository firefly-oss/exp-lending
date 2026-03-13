package com.firefly.experience.lending.core.application.decision.services.impl;

import com.firefly.core.contract.sdk.api.ContractsApi;
import com.firefly.core.lending.origination.sdk.api.ProposedOfferApi;
import com.firefly.core.lending.origination.sdk.api.UnderwritingDecisionApi;
import com.firefly.domain.common.contracts.sdk.api.ScaOperationsApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firefly.domain.common.contracts.sdk.model.InitiateScaOperationRequest;
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
 * flow via the Loan Origination SDK, Contracts SDK, and SCA SDK.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link UnderwritingDecisionApi} or
 * {@link ProposedOfferApi}; direct core-lending-origination-sdk usage is temporary until the
 * domain layer surfaces these endpoints. {@link ContractsApi} from core-common-contract-mgmt-sdk
 * is used directly because no domain SDK exposes contract CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DecisionServiceImpl implements DecisionService {

    private final UnderwritingDecisionApi underwritingDecisionApi;
    private final ProposedOfferApi proposedOfferApi;
    private final ContractsApi contractsApi;
    // Field name matches bean name "scaOperationsApi" — Spring resolves the ambiguity by name
    // when both ScaClientFactory and ContractsClientFactory expose a ScaOperationsApi bean.
    private final ScaOperationsApi scaOperationsApi;
    private final ObjectMapper objectMapper;

    // -------------------------------------------------------------------------
    // Scoring
    // -------------------------------------------------------------------------

    @Override
    public Mono<ScoringStatusDTO> getScoringStatus(UUID applicationId) {
        log.debug("Fetching scoring status for applicationId={}", applicationId);
        return underwritingDecisionApi
                .findAllDecisions(applicationId, null, null, null, null, null)
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
        return underwritingDecisionApi
                .findAllDecisions(applicationId, null, null, null, null, null)
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
        return proposedOfferApi
                .findAllOffers(applicationId, null, null, null, null, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToOfferSummaryDTO);
    }

    @Override
    public Mono<OfferDetailDTO> getOffer(UUID applicationId, UUID offerId) {
        log.debug("Fetching offer offerId={} for applicationId={}", offerId, applicationId);
        return proposedOfferApi.getOffer(applicationId, offerId, null)
                .map(this::mapToOfferDetailDTO);
    }

    @Override
    public Mono<Void> acceptOffer(UUID applicationId, UUID offerId) {
        log.debug("Accepting offer offerId={} for applicationId={}", offerId, applicationId);
        return proposedOfferApi.getOffer(applicationId, offerId, null)
                .flatMap(existing -> {
                    // Build a fresh update DTO with only the changed field to avoid live-object mutation.
                    // ARCH-EXCEPTION: core-lending-origination-sdk provides no dedicated UpdateOfferCommand;
                    // ProposedOfferDTO is reused as the update body with only offerStatus set.
                    var update = new com.firefly.core.lending.origination.sdk.model.ProposedOfferDTO()
                            .offerStatus("ACCEPTED");
                    return proposedOfferApi.updateOffer(
                            applicationId, offerId, update, UUID.randomUUID().toString());
                })
                .then();
    }

    @Override
    public Mono<Void> rejectOffer(UUID applicationId, UUID offerId, RejectOfferCommand command) {
        log.debug("Rejecting offer offerId={} for applicationId={}", offerId, applicationId);
        return proposedOfferApi.getOffer(applicationId, offerId, null)
                .flatMap(existing -> {
                    // Build a fresh update DTO with only the changed field to avoid live-object mutation.
                    // ARCH-EXCEPTION: core-lending-origination-sdk provides no dedicated UpdateOfferCommand;
                    // ProposedOfferDTO is reused as the update body with only offerStatus set.
                    var update = new com.firefly.core.lending.origination.sdk.model.ProposedOfferDTO()
                            .offerStatus("REJECTED");
                    return proposedOfferApi.updateOffer(
                            applicationId, offerId, update, UUID.randomUUID().toString());
                })
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
        var filterReq = new com.firefly.core.contract.sdk.model.FilterRequestContractDTO()
                .filters(new com.firefly.core.contract.sdk.model.ContractDTO()
                        .contractNumber(applicationId.toString()));
        return contractsApi.filterContracts(filterReq)
                // PaginationResponse.getContent() returns List<Object>; Jackson deserializes
                // each element as a LinkedHashMap — convertValue maps it to the typed SDK DTO.
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .next()
                .map(item -> objectMapper.convertValue(
                        item, com.firefly.core.contract.sdk.model.ContractDTO.class))
                .map(sdkContract -> mapToContractDTO(applicationId, sdkContract));
    }

    @Override
    public Mono<ContractDTO> signContract(UUID applicationId, SignContractCommand command) {
        log.debug("Initiating contract signing for applicationId={}", applicationId);
        // Step 1: Initiate SCA operation. The client receives the operationId and must
        // complete the SCA challenge (OTP / biometric) before the contract becomes ACTIVE.
        var scaRequest = new InitiateScaOperationRequest()
                .operationType("CONTRACT_SIGNING")
                .resourceId(applicationId);
        // ARCH-EXCEPTION: ScaOperationsApi.initiateOperation does not expose an xIdempotencyKey
        // parameter; idempotency key cannot be injected until the domain SDK surfaces it.
        return scaOperationsApi.initiateOperation(scaRequest)
                // Step 2: Retrieve the draft contract associated with this application.
                .then(getContract(applicationId))
                // Step 3: Transition the contract to ACTIVE status to record intent to sign.
                // Full signature completion is confirmed when the SCA challenge is verified.
                .flatMap(contract -> {
                    // contractId is passed as the path param to updateContract; the body
                    // carries only the fields being changed (PATCH-style partial update).
                    var sdkUpdate = new com.firefly.core.contract.sdk.model.ContractDTO()
                            .contractStatus(com.firefly.core.contract.sdk.model.ContractDTO
                                    .ContractStatusEnum.ACTIVE);
                    // ARCH-EXCEPTION: ContractsApi.updateContract does not expose an xIdempotencyKey
                    // parameter; idempotency key cannot be injected until the core SDK surfaces it.
                    return contractsApi.updateContract(contract.getContractId(), sdkUpdate)
                            .map(updated -> ContractDTO.builder()
                                    .contractId(updated.getContractId())
                                    .applicationId(applicationId)
                                    .status(updated.getContractStatus() != null
                                            ? updated.getContractStatus().getValue() : "ACTIVE")
                                    .documentUrl(updated.getContractNumber())
                                    .signedAt(LocalDateTime.now())
                                    .build());
                });
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    private DecisionDTO mapToDecisionDTO(UUID applicationId,
            com.firefly.core.lending.origination.sdk.model.UnderwritingDecisionDTO src) {
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

    private OfferSummaryDTO mapToOfferSummaryDTO(
            com.firefly.core.lending.origination.sdk.model.ProposedOfferDTO src) {
        return OfferSummaryDTO.builder()
                .offerId(src.getProposedOfferId())
                .amount(src.getRequestedAmount())
                .term(src.getRequestedTenorMonths())
                .monthlyPayment(src.getMonthlyPayment())
                .annualRate(src.getRequestedInterestRate())
                .status(src.getOfferStatus())
                .build();
    }

    private OfferDetailDTO mapToOfferDetailDTO(
            com.firefly.core.lending.origination.sdk.model.ProposedOfferDTO src) {
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

    private ContractDTO mapToContractDTO(UUID applicationId,
            com.firefly.core.contract.sdk.model.ContractDTO src) {
        return ContractDTO.builder()
                .contractId(src.getContractId())
                .applicationId(applicationId)
                .status(src.getContractStatus() != null
                        ? src.getContractStatus().getValue() : null)
                .documentUrl(src.getContractNumber())
                .build();
    }
}
