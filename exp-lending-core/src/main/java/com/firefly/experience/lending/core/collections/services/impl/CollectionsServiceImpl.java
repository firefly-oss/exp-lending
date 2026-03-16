package com.firefly.experience.lending.core.collections.services.impl;

import com.firefly.domain.lending.loan.servicing.sdk.api.LoanServicingApi;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link CollectionsService}, delegating to the domain Loan Servicing
 * SDK's {@code LoanServicingApi} for collection case retrieval and payment promise registration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionsServiceImpl implements CollectionsService {

    private final LoanServicingApi loanServicingApi;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<CollectionCaseSummaryDTO> listCollectionCases() {
        // MVP: the domain SDK does not expose a list-all-servicing-cases endpoint.
        // Returns an empty list. Replace when the domain layer surfaces this endpoint.
        log.debug("Listing collection cases");
        return Flux.empty();
    }

    @Override
    public Mono<CollectionCaseDetailDTO> getCollectionCase(UUID caseId) {
        log.debug("Getting collection case caseId={}", caseId);
        return loanServicingApi.getLoanDetails(caseId.toString(), null)
                .map(item -> {
                    var jsonMap = objectMapper.convertValue(item, Map.class);
                    UUID id = jsonMap.get("loanServicingCaseId") != null
                            ? UUID.fromString(jsonMap.get("loanServicingCaseId").toString()) : caseId;
                    String status = jsonMap.get("servicingStatus") != null
                            ? jsonMap.get("servicingStatus").toString() : null;
                    BigDecimal principal = jsonMap.get("principalAmount") != null
                            ? new BigDecimal(jsonMap.get("principalAmount").toString()) : null;
                    return CollectionCaseDetailDTO.builder()
                            .caseId(id)
                            .loanId(id)
                            .status(status)
                            .overdueAmount(principal)
                            .daysPastDue(0) // MVP: no DPD field in domain SDK
                            .actions(List.<CollectionActionDTO>of())
                            .build();
                });
    }

    @Override
    public Mono<PaymentPromiseDTO> registerPaymentPromise(UUID caseId, RegisterPaymentPromiseCommand command) {
        // MVP: no dedicated payment promise resource in domain-lending-loan-servicing-sdk.
        // Returns a stub promise record. Replace when upstream exposes a promise sub-resource.
        log.debug("Registering payment promise caseId={}", caseId);
        return Mono.just(PaymentPromiseDTO.builder()
                .promiseId(UUID.randomUUID())
                .promisedAmount(command.getPromisedAmount())
                .promiseDate(command.getPromiseDate())
                .status("PENDING")
                .build());
    }
}
