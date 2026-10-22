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


package io.fireflysoftware.experience.lending.core.application.decision.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fireflysoftware.domain.common.contracts.sdk.api.ContractsApi;
import io.fireflysoftware.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DecisionServiceImplTest {

    @Test
    void getContractUsesTheIdTheDomainReturned() {
        UUID applicationId = UUID.randomUUID();
        UUID contractId = UUID.randomUUID();
        ContractsApi contracts = mock(ContractsApi.class);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("contractId", contractId.toString());
        body.put("contractStatus", "ACTIVE");
        body.put("contractNumber", "C-19");
        when(contracts.getContractDetail(applicationId, null)).thenReturn(reactor.core.publisher.Mono.just(body));
        DecisionServiceImpl service = new DecisionServiceImpl(mock(LoanOriginationApi.class), contracts, new ObjectMapper());

        StepVerifier.create(service.getContract(applicationId))
                .assertNext(contract -> {
                    org.assertj.core.api.Assertions.assertThat(contract.getContractId()).isEqualTo(contractId);
                    org.assertj.core.api.Assertions.assertThat(contract.getStatus()).isEqualTo("ACTIVE");
                })
                .verifyComplete();
    }

    @Test
    void getContractDoesNotSubstituteTheApplicationId() {
        UUID applicationId = UUID.randomUUID();
        ContractsApi contracts = mock(ContractsApi.class);
        when(contracts.getContractDetail(applicationId, null))
                .thenReturn(reactor.core.publisher.Mono.just(Map.of("contractStatus", "DRAFT")));
        DecisionServiceImpl service = new DecisionServiceImpl(mock(LoanOriginationApi.class), contracts, new ObjectMapper());

        StepVerifier.create(service.getContract(applicationId))
                .expectErrorMatches(error -> error instanceof IllegalStateException
                        && "Contract id was not returned".equals(error.getMessage()))
                .verify();
    }
}
