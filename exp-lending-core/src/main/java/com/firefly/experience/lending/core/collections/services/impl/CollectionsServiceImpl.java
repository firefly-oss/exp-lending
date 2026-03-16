package com.firefly.experience.lending.core.collections.services.impl;

import com.firefly.core.lending.servicing.sdk.api.LoanServicingCaseApi;
import com.firefly.core.lending.servicing.sdk.model.LoanServicingCaseDTO;
import com.firefly.experience.lending.core.collections.commands.RegisterPaymentPromiseCommand;
import com.firefly.experience.lending.core.collections.queries.CollectionActionDTO;
import com.firefly.experience.lending.core.collections.queries.CollectionCaseDetailDTO;
import com.firefly.experience.lending.core.collections.queries.CollectionCaseSummaryDTO;
import com.firefly.experience.lending.core.collections.queries.PaymentPromiseDTO;
import com.firefly.experience.lending.core.collections.services.CollectionsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link CollectionsService}, delegating to the Loan Servicing SDK's
 * {@code LoanServicingCaseApi} for collection case retrieval and payment promise registration.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link LoanServicingCaseApi}; direct
 * core-lending-loan-servicing-sdk usage is temporary until the domain layer surfaces this endpoint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionsServiceImpl implements CollectionsService {

    private final LoanServicingCaseApi loanServicingCaseApi;

    @Override
    public Flux<CollectionCaseSummaryDTO> listCollectionCases() {
        log.debug("Listing collection cases");
        return loanServicingCaseApi
                .findAllServicingCases(null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toSummary);
    }

    @Override
    public Mono<CollectionCaseDetailDTO> getCollectionCase(UUID caseId) {
        log.debug("Getting collection case caseId={}", caseId);
        return loanServicingCaseApi.getServicingCaseById(caseId, UUID.randomUUID().toString())
                .map(this::toDetail);
    }

    @Override
    public Mono<PaymentPromiseDTO> registerPaymentPromise(UUID caseId, RegisterPaymentPromiseCommand command) {
        // MVP: no dedicated payment promise resource in core-lending-loan-servicing-sdk.
        // Returns a stub promise record. Replace when upstream exposes a promise sub-resource.
        log.debug("Registering payment promise caseId={}", caseId);
        return Mono.just(PaymentPromiseDTO.builder()
                .promiseId(UUID.randomUUID())
                .promisedAmount(command.getPromisedAmount())
                .promiseDate(command.getPromiseDate())
                .status("PENDING")
                .build());
    }

    // --- Mappers ---

    private CollectionCaseSummaryDTO toSummary(LoanServicingCaseDTO dto) {
        return CollectionCaseSummaryDTO.builder()
                .caseId(dto.getLoanServicingCaseId())
                .loanId(dto.getLoanServicingCaseId())
                .status(dto.getServicingStatus() != null ? dto.getServicingStatus().getValue() : null)
                .overdueAmount(dto.getPrincipalAmount())
                .daysPastDue(0) // MVP: no DPD field in core-lending-loan-servicing-sdk
                .build();
    }

    private CollectionCaseDetailDTO toDetail(LoanServicingCaseDTO dto) {
        return CollectionCaseDetailDTO.builder()
                .caseId(dto.getLoanServicingCaseId())
                .loanId(dto.getLoanServicingCaseId())
                .status(dto.getServicingStatus() != null ? dto.getServicingStatus().getValue() : null)
                .overdueAmount(dto.getPrincipalAmount())
                .daysPastDue(0) // MVP: no DPD field in core-lending-loan-servicing-sdk
                .actions(List.<CollectionActionDTO>of())
                .build();
    }
}
