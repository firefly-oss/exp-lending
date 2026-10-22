/*
 * Copyright 2026 Firefly Software Foundation
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


package io.fireflysoftware.experience.lending.core.servicing.services;

import io.fireflysoftware.experience.lending.core.ports.AccessDecision;
import io.fireflysoftware.experience.lending.core.ports.AccessPort;
import io.fireflysoftware.experience.lending.core.ports.IdentityPort;
import io.fireflysoftware.experience.lending.core.ports.SubjectRef;
import io.fireflysoftware.experience.lending.core.product.PaymentKind;
import io.fireflysoftware.experience.lending.core.product.PaymentPath;
import io.fireflysoftware.experience.lending.core.product.PaymentReceipt;
import io.fireflysoftware.experience.lending.core.product.PaymentTry;
import io.fireflysoftware.experience.lending.core.product.PaymentsPort;
import io.fireflysoftware.experience.lending.core.servicing.commands.RequestEarlyRepaymentCommand;
import io.fireflysoftware.experience.lending.core.servicing.ports.LoanServicingPort;
import io.fireflysoftware.experience.lending.core.servicing.services.impl.LoanServicingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EarlyRepaymentJourneyTest {

    @Mock private IdentityPort identityPort;
    @Mock private AccessPort accessPort;
    @Mock private LoanServicingPort loanServicingPort;
    @Mock private PaymentsPort paymentsPort;

    private LoanServicingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LoanServicingServiceImpl(identityPort, accessPort, loanServicingPort, paymentsPort);
        lenient().when(identityPort.resolve(any()))
                .thenReturn(Mono.just(new SubjectRef(IdentityPort.SAMPLE_ACTOR, "sandbox")));
        lenient().when(accessPort.check(any()))
                .thenReturn(Mono.just(AccessDecision.permit("SANDBOX_GRANT")));
    }

    @Test
    void recordsThePaymentIdReturnedByThePaymentsPort() {
        UUID loanId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        when(paymentsPort.tryPayment(any())).thenReturn(Mono.just(
                new PaymentTry(paymentId, PaymentKind.INSTALLMENT, PaymentPath.INTERNAL, null, "challenge")));
        when(paymentsPort.confirm(paymentId, "challenge")).thenReturn(Mono.just(
                new PaymentReceipt(paymentId, PaymentKind.INSTALLMENT, new BigDecimal("5000"), "hub", false)));
        when(loanServicingPort.requestEarlyRepayment(eq(loanId), any())).thenReturn(Mono.empty());

        RequestEarlyRepaymentCommand command = new RequestEarlyRepaymentCommand();
        command.setAmount(new BigDecimal("5000"));
        command.setType("PARTIAL");

        StepVerifier.create(service.requestEarlyRepayment(loanId, command)).verifyComplete();

        ArgumentCaptor<RequestEarlyRepaymentCommand> recorded = ArgumentCaptor.forClass(RequestEarlyRepaymentCommand.class);
        verify(loanServicingPort).requestEarlyRepayment(eq(loanId), recorded.capture());
        assertThat(recorded.getValue().getTransactionId()).isEqualTo(paymentId);
    }
}
