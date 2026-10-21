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


package io.fireflysoftware.experience.lending.core.servicing.services.impl;

import io.fireflysoftware.experience.lending.core.ports.AccessAction;
import io.fireflysoftware.experience.lending.core.ports.AccessGuard;
import io.fireflysoftware.experience.lending.core.ports.IdentityPort;
import io.fireflysoftware.experience.lending.core.ports.AccessPort;
import io.fireflysoftware.experience.lending.core.ports.AccessRequest;
import io.fireflysoftware.experience.lending.core.product.PaymentInstruction;
import io.fireflysoftware.experience.lending.core.product.PaymentKind;
import io.fireflysoftware.experience.lending.core.product.PaymentsPort;
import io.fireflysoftware.experience.lending.core.servicing.commands.RequestEarlyRepaymentCommand;
import io.fireflysoftware.experience.lending.core.servicing.commands.RequestRestructuringCommand;
import io.fireflysoftware.experience.lending.core.servicing.ports.LoanServicingPort;
import io.fireflysoftware.experience.lending.core.servicing.queries.AccrualDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.BalanceDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.BalanceHistoryDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.DisbursementDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.DisbursementPlanDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.EarlyRepaymentSimulationDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.EscrowDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.InstallmentDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.InstallmentPaymentDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.LoanDetailDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.LoanDocumentDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.LoanEventDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.LoanNotificationDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.LoanSummaryDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.RateChangeDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.RateInfoDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.RebateDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.RepaymentDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.RestructuringDTO;
import io.fireflysoftware.experience.lending.core.servicing.queries.ScheduleEntryDTO;
import io.fireflysoftware.experience.lending.core.servicing.services.LoanServicingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Loan servicing journey. Access is checked before the domain servicing port.
 */
@Service
@RequiredArgsConstructor
public class LoanServicingServiceImpl implements LoanServicingService {

    private final IdentityPort identityPort;
    private final AccessPort accessPort;
    private final LoanServicingPort loanServicingPort;
    private final PaymentsPort paymentsPort;

    @Override
    public Mono<LoanDetailDTO> getLoan(UUID loanId) {
        return read(loanId, () -> loanServicingPort.getLoan(loanId));
    }

    @Override
    public Mono<BalanceDTO> getBalance(UUID loanId) {
        return read(loanId, () -> loanServicingPort.getBalance(loanId));
    }

    @Override
    public Mono<BalanceHistoryDTO> getBalanceHistory(UUID loanId) {
        return read(loanId, () -> loanServicingPort.getBalanceHistory(loanId));
    }

    @Override
    public Flux<ScheduleEntryDTO> getRepaymentSchedule(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getRepaymentSchedule(loanId));
    }

    @Override
    public Flux<InstallmentDTO> getInstallments(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getInstallments(loanId));
    }

    @Override
    public Mono<InstallmentDTO> getInstallment(UUID loanId, UUID installmentId) {
        return read(loanId, () -> loanServicingPort.getInstallment(loanId, installmentId));
    }

    @Override
    public Flux<InstallmentPaymentDTO> getInstallmentPayments(UUID loanId, UUID installmentId) {
        return readMany(loanId, () -> loanServicingPort.getInstallmentPayments(loanId, installmentId));
    }

    @Override
    public Flux<DisbursementDTO> getDisbursements(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getDisbursements(loanId));
    }

    @Override
    public Mono<DisbursementDTO> getDisbursement(UUID loanId, UUID disbursementId) {
        return read(loanId, () -> loanServicingPort.getDisbursement(loanId, disbursementId));
    }

    @Override
    public Mono<DisbursementPlanDTO> getDisbursementPlan(UUID loanId) {
        return read(loanId, () -> loanServicingPort.getDisbursementPlan(loanId));
    }

    @Override
    public Flux<RepaymentDTO> getRepayments(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getRepayments(loanId));
    }

    @Override
    public Mono<Void> requestEarlyRepayment(UUID loanId, RequestEarlyRepaymentCommand command) {
        PaymentInstruction instruction = new PaymentInstruction(
                loanId, null, command.getAmount(), PaymentKind.INSTALLMENT);
        return update(loanId, () -> paymentsPort.tryPayment(instruction).flatMap(tried ->
                paymentsPort.confirm(tried.paymentId(), tried.challengeId() == null
                        ? null
                        : io.fireflysoftware.experience.lending.core.adapter.SandboxPaymentsAdapter.SANDBOX_RESPONSE)).then());
    }

    @Override
    public Mono<EarlyRepaymentSimulationDTO> simulateEarlyRepayment(UUID loanId, RequestEarlyRepaymentCommand command) {
        return read(loanId, () -> loanServicingPort.simulateEarlyRepayment(loanId, command));
    }

    @Override
    public Mono<RateInfoDTO> getRateInfo(UUID loanId) {
        return read(loanId, () -> loanServicingPort.getRateInfo(loanId));
    }

    @Override
    public Flux<RateChangeDTO> getRateChanges(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getRateChanges(loanId));
    }

    @Override
    public Flux<AccrualDTO> getAccruals(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getAccruals(loanId));
    }

    @Override
    public Flux<EscrowDTO> getEscrow(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getEscrow(loanId));
    }

    @Override
    public Flux<RebateDTO> getRebates(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getRebates(loanId));
    }

    @Override
    public Mono<RestructuringDTO> requestRestructuring(UUID loanId, RequestRestructuringCommand command) {
        return update(loanId, () -> loanServicingPort.requestRestructuring(loanId, command));
    }

    @Override
    public Flux<RestructuringDTO> getRestructurings(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getRestructurings(loanId));
    }

    @Override
    public Flux<LoanEventDTO> getEvents(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getEvents(loanId));
    }

    @Override
    public Flux<LoanNotificationDTO> getNotifications(UUID loanId) {
        return readMany(loanId, () -> loanServicingPort.getNotifications(loanId));
    }

    private <T> Mono<T> read(UUID loanId, Supplier<Mono<T>> call) {
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(loanId, AccessAction.READ, "APPLICATION", loanId), call);
    }

    private <T> Flux<T> readMany(UUID loanId, Supplier<Flux<T>> call) {
        return AccessGuard.requireMany(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(loanId, AccessAction.READ, "APPLICATION", loanId), call);
    }

    private <T> Mono<T> update(UUID loanId, Supplier<Mono<T>> call) {
        return AccessGuard.require(identityPort, accessPort, IdentityPort.SAMPLE_USERNAME,
                AccessRequest.of(loanId, AccessAction.UPDATE, "APPLICATION", loanId), call);
    }
}
