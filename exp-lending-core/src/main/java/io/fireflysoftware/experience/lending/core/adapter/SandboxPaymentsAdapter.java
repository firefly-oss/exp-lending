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
import io.fireflysoftware.experience.lending.core.product.PaymentPath;
import io.fireflysoftware.experience.lending.core.product.PaymentReceipt;
import io.fireflysoftware.experience.lending.core.product.PaymentTry;
import io.fireflysoftware.experience.lending.core.product.PaymentsPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sandbox shape for a payment: try, then confirm. An installer replaces this class
 * with the payments journey. It does not run SCA or TCC, and it does not call the ledger.
 */
@Component
@ConditionalOnProperty(name = "firefly.experience.payments.adapter", havingValue = "sandbox", matchIfMissing = true)
public class SandboxPaymentsAdapter implements PaymentsPort {

    /**
     * Response an outbound confirm must submit. A different value does not confirm.
     */
    public static final String SANDBOX_RESPONSE = "sandbox";

    private final ConcurrentHashMap<UUID, Pending> pending = new ConcurrentHashMap<>();

    @Override
    public Mono<PaymentTry> tryPayment(PaymentInstruction instruction) {
        UUID paymentId = UUID.nameUUIDFromBytes((instruction.loanId() + ":" + instruction.kind().name()
                + ":" + instruction.amount() + ":" + instruction.path().name()).getBytes(StandardCharsets.UTF_8));
        UUID challengeId = instruction.path() == PaymentPath.OUTBOUND
                ? UUID.nameUUIDFromBytes(("challenge:" + paymentId).getBytes(StandardCharsets.UTF_8))
                : null;
        pending.put(paymentId, new Pending(instruction, challengeId));
        return Mono.just(new PaymentTry(paymentId, instruction.kind(), instruction.path(), challengeId));
    }

    @Override
    public Mono<PaymentReceipt> confirm(UUID paymentId, String sandboxResponse) {
        Pending held = pending.get(paymentId);
        if (held == null) {
            return Mono.error(new PaymentNotConfirmedException("PAYMENT_NOT_CONFIRMED"));
        }
        if (held.challengeId() != null && !SANDBOX_RESPONSE.equals(sandboxResponse)) {
            return Mono.error(new PaymentNotConfirmedException("PAYMENT_NOT_CONFIRMED"));
        }
        pending.remove(paymentId, held);
        return Mono.just(new PaymentReceipt(paymentId, held.instruction().kind(), held.instruction().amount(), "sandbox", true));
    }

    private record Pending(PaymentInstruction instruction, UUID challengeId) {
    }

    /**
     * Raised when confirm does not complete the try.
     */
    public static final class PaymentNotConfirmedException extends RuntimeException {

        public PaymentNotConfirmedException(String message) {
            super(message);
        }
    }
}
