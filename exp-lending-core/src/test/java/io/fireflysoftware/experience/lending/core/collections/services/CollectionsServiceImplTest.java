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


package io.fireflysoftware.experience.lending.core.collections.services;

import io.fireflysoftware.domain.lending.collections.sdk.api.CollectionCasesApi;
import io.fireflysoftware.domain.lending.collections.sdk.model.CollectionCaseDTO;
import io.fireflysoftware.domain.lending.collections.sdk.model.CollectionPromiseDTO;
import io.fireflysoftware.domain.lending.collections.sdk.model.PaginationResponseCollectionCaseDTO;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.api.LoanServicingApi;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fireflysoftware.experience.lending.core.collections.commands.RegisterPaymentPromiseCommand;
import io.fireflysoftware.experience.lending.core.collections.services.impl.CollectionsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionsServiceImplTest {

    @Mock
    private CollectionCasesApi collectionCasesApi;

    @Mock
    private LoanServicingApi loanServicingApi;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private CollectionsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CollectionsServiceImpl(collectionCasesApi, loanServicingApi, objectMapper);
    }

    // --- listCollectionCases ---

    @Test
    void listCollectionCases_mapsStoredCases() {
        var caseId = UUID.randomUUID();
        var loanId = UUID.randomUUID();
        var stored = new CollectionCaseDTO()
                .collectionCaseId(caseId)
                .loanServicingCaseId(loanId)
                .collectionStatus(CollectionCaseDTO.CollectionStatusEnum.OPEN)
                .totalDue(new BigDecimal("3200.00"))
                .daysPastDue(12);
        when(collectionCasesApi.callList(any()))
                .thenReturn(Mono.just(new PaginationResponseCollectionCaseDTO().content(List.of(stored))));

        StepVerifier.create(service.listCollectionCases())
                .assertNext(summary -> {
                    assertThat(summary.getCaseId()).isEqualTo(caseId);
                    assertThat(summary.getLoanId()).isEqualTo(loanId);
                    assertThat(summary.getStatus()).isEqualTo("OPEN");
                    assertThat(summary.getOverdueAmount()).isEqualByComparingTo("3200.00");
                    assertThat(summary.getDaysPastDue()).isEqualTo(12);
                })
                .verifyComplete();
    }

    // --- getCollectionCase ---

    @Test
    void getCollectionCase_mapsServicingCaseToDetail() {
        var caseId = UUID.randomUUID();
        var responseMap = Map.of(
                "loanServicingCaseId", caseId.toString(),
                "servicingStatus", "DELINQUENT",
                "principalAmount", "3200.00"
        );

        when(loanServicingApi.getLoanDetails(eq(caseId.toString()), any()))
                .thenReturn(Mono.just(responseMap));

        StepVerifier.create(service.getCollectionCase(caseId))
                .assertNext(detail -> {
                    assertThat(detail.getCaseId()).isEqualTo(caseId);
                    assertThat(detail.getStatus()).isEqualTo("DELINQUENT");
                    assertThat(detail.getOverdueAmount()).isEqualByComparingTo(new BigDecimal("3200.00"));
                    assertThat(detail.getActions()).isEmpty();
                })
                .verifyComplete();
    }

    // --- registerPaymentPromise ---

    @Test
    void registerPaymentPromise_persistsThroughCollections() {
        var caseId = UUID.randomUUID();
        var promiseId = UUID.randomUUID();
        var command = new RegisterPaymentPromiseCommand();
        command.setPromisedAmount(new BigDecimal("1500.00"));
        command.setPromiseDate(LocalDate.of(2026, 3, 31));
        var saved = new CollectionPromiseDTO()
                .collectionPromiseId(promiseId)
                .promisedAmount(new BigDecimal("1500.00"))
                .promisedDate(LocalDateTime.of(2026, 3, 31, 0, 0));
        when(collectionCasesApi.registerPaymentPromise(eq(caseId), any(), any()))
                .thenReturn(Mono.just(saved));

        StepVerifier.create(service.registerPaymentPromise(caseId, command))
                .assertNext(promise -> {
                    assertThat(promise.getPromiseId()).isEqualTo(promiseId);
                    assertThat(promise.getPromisedAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
                    assertThat(promise.getPromiseDate()).isEqualTo(LocalDate.of(2026, 3, 31));
                    assertThat(promise.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();
    }
}
