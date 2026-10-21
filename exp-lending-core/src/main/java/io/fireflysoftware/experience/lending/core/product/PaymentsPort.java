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


package io.fireflysoftware.experience.lending.core.product;

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Payments for a loan disbursement or installment. Try, then confirm.
 * SandboxPaymentsAdapter is the class an installer replaces with the payments journey.
 * This port does not run SCA, TCC, the ledger, library-rails, or library-psps.
 */
public interface PaymentsPort {

    Mono<PaymentTry> tryPayment(PaymentInstruction instruction);

    /**
     * @param sandboxResponse the {@code challengeValue} returned by try; omitted for an internal try
     */
    Mono<PaymentReceipt> confirm(UUID paymentId, String sandboxResponse);
}
