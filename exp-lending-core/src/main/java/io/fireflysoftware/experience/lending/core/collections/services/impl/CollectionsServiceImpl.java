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

import io.fireflysoftware.domain.lending.collections.sdk.api.CollectionCasesApi;
import io.fireflysoftware.domain.lending.collections.sdk.model.CollectionPromiseDTO;
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
 * Collection cases and payment promises are stored by the delinquency collections domain.
 * Case detail still reads the linked servicing record.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionsServiceImpl implements CollectionsService {

    private final CollectionCasesApi collectionCasesApi;
    private final LoanServicingApi loanServicingApi;
    private final ObjectMapper objectMapper;

    @Override
    public Flux<CollectionCaseSummaryDTO> listCollectionCases() {
        log.debug("Listing collection cases");
        return collectionCasesApi.callList(UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(item -> CollectionCaseSummaryDTO.builder()
                        .caseId(item.getCollectionCaseId())
                        .loanId(item.getLoanServicingCaseId())
                        .status(item.getCollectionStatus() != null ? item.getCollectionStatus().getValue() : null)
                        .overdueAmount(item.getTotalDue())
                        .daysPastDue(item.getDaysPastDue())
                        .build());
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
        log.debug("Registering payment promise caseId={}", caseId);
        var body = new CollectionPromiseDTO()
                .collectionCaseId(caseId)
                .promisedAmount(command.getPromisedAmount())
                .promisedDate(command.getPromiseDate() != null ? command.getPromiseDate().atStartOfDay() : null);
        return collectionCasesApi.registerPaymentPromise(caseId, body, UUID.randomUUID().toString())
                .map(saved -> PaymentPromiseDTO.builder()
                        .promiseId(saved.getCollectionPromiseId())
                        .promisedAmount(saved.getPromisedAmount())
                        .promiseDate(saved.getPromisedDate() != null ? saved.getPromisedDate().toLocalDate() : null)
                        .status(promiseStatus(saved.getIsKept()))
                        .build());
    }

    private static String promiseStatus(Boolean kept) {
        if (kept == null) {
            return "PENDING";
        }
        return Boolean.TRUE.equals(kept) ? "KEPT" : "BROKEN";
    }
}
