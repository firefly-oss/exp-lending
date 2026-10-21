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

import io.fireflysoftware.experience.lending.core.product.PaymentInstruction;
import io.fireflysoftware.experience.lending.core.product.PaymentKind;
import io.fireflysoftware.experience.lending.core.product.PaymentPath;
import io.fireflysoftware.experience.lending.core.product.PaymentReceipt;
import io.fireflysoftware.experience.lending.core.product.PaymentTry;
import io.fireflysoftware.experience.lending.core.product.PaymentsPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Live payments port. Calls the payments hub try, confirm, and cancel routes.
 * Selected with {@code firefly.experience.payments.adapter=hub}. The sandbox adapter stays the default.
 * This class does not generate a challenge and does not treat a fixed word as a valid code.
 */
@Component
@ConditionalOnProperty(name = "firefly.experience.payments.adapter", havingValue = "hub")
public class HttpPaymentsAdapter implements PaymentsPort {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };

    private final WebClient webClient;
    private final ConcurrentHashMap<UUID, PaymentInstruction> started = new ConcurrentHashMap<>();

    @Autowired
    public HttpPaymentsAdapter(
            @Value("${api-configuration.domain-platform.payments.base-path:http://localhost:8092}") String basePath) {
        this(WebClient.builder().baseUrl(basePath).build());
    }

    HttpPaymentsAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<PaymentTry> tryPayment(PaymentInstruction instruction) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("scheme", instruction.path() == PaymentPath.OUTBOUND ? "DOMESTIC_OUTBOUND" : "INTERNAL_TRANSFER");
        body.put("idempotencyKey", instruction.loanId() + ":" + instruction.kind().name() + ":" + instruction.amount());
        body.put("amount", instruction.amount());
        body.put("thirdPartyReference", instruction.installmentId() == null
                ? instruction.loanId().toString()
                : instruction.installmentId().toString());
        return webClient.post().uri("/api/v1/payments/tries").bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.createException()
                        .flatMap(error -> Mono.error(new IllegalStateException(
                                "The payments hub returned HTTP " + response.statusCode().value(), error))))
                .bodyToMono(MAP)
                .map(payload -> {
                    UUID paymentId = uuid(payload.get("paymentId"));
                    started.put(paymentId, instruction);
                    return new PaymentTry(
                            paymentId,
                            instruction.kind(),
                            instruction.path(),
                            uuid(payload.get("challengeId")),
                            text(payload.get("challengeValue")));
                });
    }

    @Override
    public Mono<PaymentReceipt> confirm(UUID paymentId, String challengeResponse) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("response", challengeResponse);
        return webClient.post().uri("/api/v1/payments/{paymentId}/confirmations", paymentId).bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.createException()
                        .flatMap(error -> Mono.error(new IllegalStateException(
                                "The payments hub returned HTTP " + response.statusCode().value(), error))))
                .bodyToMono(MAP)
                .map(payload -> receipt(paymentId, payload));
    }

    @Override
    public Mono<Void> cancel(UUID paymentId) {
        return webClient.post().uri("/api/v1/payments/{paymentId}/cancellations", paymentId)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response -> response.createException()
                        .flatMap(error -> Mono.error(new IllegalStateException(
                                "The payments hub returned HTTP " + response.statusCode().value(), error))))
                .bodyToMono(MAP)
                .then();
    }

    private PaymentReceipt receipt(UUID paymentId, Map<String, Object> payload) {
        PaymentInstruction instruction = started.remove(paymentId);
        PaymentKind kind = instruction == null ? PaymentKind.DISBURSEMENT : instruction.kind();
        BigDecimal amount = instruction == null ? decimal(payload.get("amount")) : instruction.amount();
        UUID id = uuid(payload.get("paymentId"));
        return new PaymentReceipt(id == null ? paymentId : id, kind, amount, "hub", false);
    }

    private static UUID uuid(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        if (text.isBlank()) {
            return null;
        }
        return UUID.fromString(text);
    }

    private static String text(Object value) {
        if (value == null) {
            return null;
        }
        String text = value.toString();
        return text.isBlank() ? null : text;
    }

    private static BigDecimal decimal(Object value) {
        if (value == null) {
            return null;
        }
        return new BigDecimal(value.toString());
    }
}
