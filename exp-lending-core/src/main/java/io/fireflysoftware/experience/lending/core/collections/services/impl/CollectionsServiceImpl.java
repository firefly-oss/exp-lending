/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.lending.core.collections.services.impl;

import io.fireflysoftware.domain.lending.loan.servicing.sdk.api.LoanServicingApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fireflysoftware.experience.lending.core.collections.commands.RegisterPaymentPromiseCommand;
import io.fireflysoftware.experience.lending.core.collections.queries.CollectionActionDTO;
import io.fireflysoftware.experience.lending.core.collections.queries.CollectionCaseDetailDTO;
import io.fireflysoftware.experience.lending.core.collections.queries.CollectionCaseSummaryDTO;
import io.fireflysoftware.experience.lending.core.collections.queries.PaymentPromiseDTO;
import io.fireflysoftware.experience.lending.core.collections.services.CollectionsService;
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
