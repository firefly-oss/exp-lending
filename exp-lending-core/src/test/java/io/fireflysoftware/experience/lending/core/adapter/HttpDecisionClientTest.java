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


package io.fireflysoftware.experience.lending.core.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.fireflysoftware.experience.lending.core.ports.DecisionOutcome;
import io.fireflysoftware.experience.lending.core.ports.DecisionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HttpDecisionClientTest {

    @Test
    void decide_postsJourneyContract() {
        UUID partyId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        WebClient client = WebClient.builder().exchangeFunction(request -> {
            assertThat(request.method().name()).isEqualTo("POST");
            assertThat(request.url().getPath()).isEqualTo("/api/v1/decisions");
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{\"outcome\":\"REFER\",\"reasonCodes\":[\"CREDIT_SCORE_BELOW_THRESHOLD\"]}")
                    .build());
        }).build();

        StepVerifier.create(new DomainDecisionConfiguration.HttpDecisionClient(client).decide(
                        new DecisionRequest(partyId, productId, "LENDING_APPLICATION", Map.of())))
                .assertNext(result -> {
                    assertThat(result.outcome()).isEqualTo(DecisionOutcome.REFER);
                    assertThat(result.reasonCodes()).containsExactly("CREDIT_SCORE_BELOW_THRESHOLD");
                })
                .verifyComplete();
    }
}
