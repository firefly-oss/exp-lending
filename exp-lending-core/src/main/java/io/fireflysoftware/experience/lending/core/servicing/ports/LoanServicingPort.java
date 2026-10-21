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


package io.fireflysoftware.experience.lending.core.servicing.ports;

import io.fireflysoftware.experience.lending.core.servicing.commands.RequestEarlyRepaymentCommand;
import io.fireflysoftware.experience.lending.core.servicing.commands.RequestRestructuringCommand;
import io.fireflysoftware.experience.lending.core.servicing.queries.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for active loan servicing operations, including balance queries, repayment schedules,
 * installments, disbursements, rate changes, accruals, and restructuring requests.
 */
public interface LoanServicingPort {

    Mono<LoanDetailDTO> getLoan(UUID loanId);

    Mono<BalanceDTO> getBalance(UUID loanId);

    Mono<BalanceHistoryDTO> getBalanceHistory(UUID loanId);

    Flux<ScheduleEntryDTO> getRepaymentSchedule(UUID loanId);

    Flux<InstallmentDTO> getInstallments(UUID loanId);

    Mono<InstallmentDTO> getInstallment(UUID loanId, UUID installmentId);

    Flux<InstallmentPaymentDTO> getInstallmentPayments(UUID loanId, UUID installmentId);

    Flux<DisbursementDTO> getDisbursements(UUID loanId);

    Mono<DisbursementDTO> getDisbursement(UUID loanId, UUID disbursementId);

    Mono<DisbursementPlanDTO> getDisbursementPlan(UUID loanId);

    Flux<RepaymentDTO> getRepayments(UUID loanId);

    Mono<Void> requestEarlyRepayment(UUID loanId, RequestEarlyRepaymentCommand command);

    Mono<EarlyRepaymentSimulationDTO> simulateEarlyRepayment(UUID loanId, RequestEarlyRepaymentCommand command);

    Mono<RateInfoDTO> getRateInfo(UUID loanId);

    Flux<RateChangeDTO> getRateChanges(UUID loanId);

    Flux<AccrualDTO> getAccruals(UUID loanId);

    Flux<EscrowDTO> getEscrow(UUID loanId);

    Flux<RebateDTO> getRebates(UUID loanId);

    Mono<RestructuringDTO> requestRestructuring(UUID loanId, RequestRestructuringCommand command);

    Flux<RestructuringDTO> getRestructurings(UUID loanId);

    Flux<LoanEventDTO> getEvents(UUID loanId);

    Flux<LoanNotificationDTO> getNotifications(UUID loanId);
}
