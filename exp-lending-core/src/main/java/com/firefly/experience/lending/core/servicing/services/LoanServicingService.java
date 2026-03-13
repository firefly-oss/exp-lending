package com.firefly.experience.lending.core.servicing.services;

import com.firefly.experience.lending.core.servicing.commands.RequestEarlyRepaymentCommand;
import com.firefly.experience.lending.core.servicing.commands.RequestRestructuringCommand;
import com.firefly.experience.lending.core.servicing.queries.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for active loan servicing operations, including balance queries, repayment schedules,
 * installments, disbursements, rate changes, accruals, and restructuring requests.
 */
public interface LoanServicingService {

    Flux<LoanSummaryDTO> listLoans();

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

    Flux<LoanDocumentDTO> getDocuments(UUID loanId);

    Flux<LoanEventDTO> getEvents(UUID loanId);

    Flux<LoanNotificationDTO> getNotifications(UUID loanId);
}
