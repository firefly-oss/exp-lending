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
import io.fireflysoftware.experience.lending.core.product.PaymentReceipt;
import io.fireflysoftware.experience.lending.core.product.PaymentsPort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Illustrative payments adapter. An installer replaces this class with the payments journey.
 * It does not call the ledger, library-rails, or library-psps.
 */
@Component
@ConditionalOnProperty(name = "firefly.experience.payments.adapter", havingValue = "sandbox", matchIfMissing = true)
public class SandboxPaymentsAdapter implements PaymentsPort {

    @Override
    public Mono<PaymentReceipt> disburse(PaymentInstruction instruction) {
        return Mono.just(receipt(instruction, PaymentKind.DISBURSEMENT));
    }

    @Override
    public Mono<PaymentReceipt> payInstallment(PaymentInstruction instruction) {
        return Mono.just(receipt(instruction, PaymentKind.INSTALLMENT));
    }

    private static PaymentReceipt receipt(PaymentInstruction instruction, PaymentKind kind) {
        String seed = instruction.loanId() + ":" + kind.name() + ":" + instruction.amount();
        UUID paymentId = UUID.nameUUIDFromBytes(seed.getBytes(StandardCharsets.UTF_8));
        return new PaymentReceipt(paymentId, kind, instruction.amount(), "sandbox", true);
    }
}
