package com.firefly.experience.lending.core.servicing.services.impl;

import com.firefly.core.lending.servicing.sdk.api.*;
import com.firefly.core.lending.servicing.sdk.model.LoanAccrualDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanBalanceDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanDisbursementDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanInstallmentPlanDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanInstallmentRecordDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanRateChangeDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanRepaymentRecordDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanRepaymentScheduleDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanRestructuringDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanServicingCaseDTO;
import com.firefly.core.lending.servicing.sdk.model.LoanServicingEventDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanAccrualDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanBalanceDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanDisbursementDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanInstallmentPlanDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanInstallmentRecordDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanRateChangeDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanRepaymentRecordDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanRepaymentScheduleDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanRestructuringDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanServicingCaseDTO;
import com.firefly.core.lending.servicing.sdk.model.PaginationResponseLoanServicingEventDTO;
import com.firefly.experience.lending.core.servicing.commands.RequestEarlyRepaymentCommand;
import com.firefly.experience.lending.core.servicing.commands.RequestRestructuringCommand;
import com.firefly.experience.lending.core.servicing.queries.*;
import com.firefly.experience.lending.core.servicing.services.LoanServicingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link LoanServicingService}, delegating to the Loan Servicing SDK
 * for balance, schedule, installment, disbursement, rate change, accrual, and restructuring APIs.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link LoanBalanceApi},
 * {@link LoanRepaymentScheduleApi}, {@link LoanInstallmentPlanApi},
 * {@link LoanInstallmentRecordApi}, {@link LoanDisbursementApi}, {@link LoanDisbursementPlanApi},
 * {@link LoanRepaymentRecordApi}, {@link LoanRateChangeApi}, {@link LoanAccrualApi},
 * {@link LoanEscrowApi}, {@link LoanRebateApi}, {@link LoanRestructuringApi},
 * {@link LoanServicingEventApi}, or {@link LoanNotificationApi}; direct
 * core-lending-loan-servicing-sdk usage is temporary until the domain layer surfaces
 * these endpoints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanServicingServiceImpl implements LoanServicingService {

    private final LoanServicingCaseApi loanServicingCaseApi;
    private final LoanBalanceApi loanBalanceApi;
    private final LoanRepaymentScheduleApi loanRepaymentScheduleApi;
    private final LoanInstallmentPlanApi loanInstallmentPlanApi;
    private final LoanInstallmentRecordApi loanInstallmentRecordApi;
    private final LoanDisbursementApi loanDisbursementApi;
    private final LoanDisbursementPlanApi loanDisbursementPlanApi;
    private final LoanRepaymentRecordApi loanRepaymentRecordApi;
    private final LoanRateChangeApi loanRateChangeApi;
    private final LoanAccrualApi loanAccrualApi;
    private final LoanEscrowApi loanEscrowApi;
    private final LoanRebateApi loanRebateApi;
    private final LoanRestructuringApi loanRestructuringApi;
    private final LoanServicingEventApi loanServicingEventApi;
    private final LoanNotificationApi loanNotificationApi;

    @Override
    public Flux<LoanSummaryDTO> listLoans() {
        log.debug("Listing loan servicing cases");
        return loanServicingCaseApi
                .findAllServicingCases(null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toSummary);
    }

    @Override
    public Mono<LoanDetailDTO> getLoan(UUID loanId) {
        log.debug("Getting loan loanId={}", loanId);
        return loanServicingCaseApi.getServicingCaseById(loanId, UUID.randomUUID().toString())
                .map(this::toDetail);
    }

    @Override
    public Mono<BalanceDTO> getBalance(UUID loanId) {
        log.debug("Getting current balance loanId={}", loanId);
        return loanBalanceApi
                .findAllBalances(loanId, null, UUID.randomUUID().toString())
                .map(page -> {
                    List<LoanBalanceDTO> content = page.getContent() != null ? page.getContent() : List.of();
                    // Prefer current snapshot; fall back to first entry
                    return content.stream()
                            .filter(b -> Boolean.TRUE.equals(b.getIsCurrent()))
                            .findFirst()
                            .or(() -> content.stream().findFirst())
                            .map(this::toBalance)
                            .orElse(BalanceDTO.builder().loanId(loanId).build());
                });
    }

    @Override
    public Mono<BalanceHistoryDTO> getBalanceHistory(UUID loanId) {
        log.debug("Getting balance history loanId={}", loanId);
        return loanBalanceApi
                .findAllBalances(loanId, null, UUID.randomUUID().toString())
                .map(page -> {
                    List<BalanceDTO> entries = page.getContent() != null
                            ? page.getContent().stream().map(this::toBalance).toList()
                            : List.of();
                    return BalanceHistoryDTO.builder().entries(entries).build();
                });
    }

    @Override
    public Flux<ScheduleEntryDTO> getRepaymentSchedule(UUID loanId) {
        log.debug("Getting repayment schedule loanId={}", loanId);
        return loanRepaymentScheduleApi
                .findAllRepaymentSchedules(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toScheduleEntry);
    }

    @Override
    public Flux<InstallmentDTO> getInstallments(UUID loanId) {
        log.debug("Getting installments loanId={}", loanId);
        return loanInstallmentPlanApi
                .findAllInstallmentPlans(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toInstallment);
    }

    @Override
    public Mono<InstallmentDTO> getInstallment(UUID loanId, UUID installmentId) {
        log.debug("Getting installment loanId={} installmentId={}", loanId, installmentId);
        return loanInstallmentPlanApi.getInstallmentPlanById(loanId, installmentId, UUID.randomUUID().toString())
                .map(this::toInstallment);
    }

    @Override
    public Flux<InstallmentPaymentDTO> getInstallmentPayments(UUID loanId, UUID installmentId) {
        log.debug("Getting payments for installmentId={} loanId={}", installmentId, loanId);
        return loanInstallmentRecordApi
                .findAllInstallmentRecords(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toInstallmentPayment);
    }

    @Override
    public Flux<DisbursementDTO> getDisbursements(UUID loanId) {
        log.debug("Getting disbursements loanId={}", loanId);
        return loanDisbursementApi
                .findAllDisbursements(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toDisbursement);
    }

    @Override
    public Mono<DisbursementDTO> getDisbursement(UUID loanId, UUID disbursementId) {
        log.debug("Getting disbursement loanId={} disbursementId={}", loanId, disbursementId);
        return loanDisbursementApi.getDisbursementById(loanId, disbursementId, UUID.randomUUID().toString())
                .map(this::toDisbursement);
    }

    @Override
    public Mono<DisbursementPlanDTO> getDisbursementPlan(UUID loanId) {
        // MVP: findAllDisbursementPlans returns untyped PaginationResponse; returning empty plan.
        // Replace when the SDK exposes a typed pagination response for disbursement plans.
        log.debug("Getting disbursement plan loanId={}", loanId);
        return Mono.just(DisbursementPlanDTO.builder().entries(List.of()).build());
    }

    @Override
    public Flux<RepaymentDTO> getRepayments(UUID loanId) {
        log.debug("Getting repayments loanId={}", loanId);
        return loanRepaymentRecordApi
                .findAllRepaymentRecords(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toRepayment);
    }

    @Override
    public Mono<Void> requestEarlyRepayment(UUID loanId, RequestEarlyRepaymentCommand command) {
        log.debug("Requesting early repayment loanId={}", loanId);
        var dto = new LoanRepaymentRecordDTO()
                .loanServicingCaseId(loanId)
                .paymentAmount(command.getAmount())
                .isPartialPayment("PARTIAL".equalsIgnoreCase(command.getType()))
                .paymentDate(LocalDate.now());
        return loanRepaymentRecordApi
                .createRepaymentRecord(loanId, dto, UUID.randomUUID().toString())
                .then();
    }

    @Override
    public Mono<EarlyRepaymentSimulationDTO> simulateEarlyRepayment(UUID loanId,
                                                                     RequestEarlyRepaymentCommand command) {
        // MVP: no dedicated simulation endpoint in the core SDK.
        // Returns a stub response. Replace when upstream exposes a simulation resource.
        log.debug("Simulating early repayment loanId={}", loanId);
        BigDecimal amount = command.getAmount() != null ? command.getAmount() : BigDecimal.ZERO;
        return Mono.just(EarlyRepaymentSimulationDTO.builder()
                .totalAmount(amount)
                .penaltyAmount(BigDecimal.ZERO)
                .savings(BigDecimal.ZERO)
                .build());
    }

    @Override
    public Mono<RateInfoDTO> getRateInfo(UUID loanId) {
        log.debug("Getting rate info loanId={}", loanId);
        return loanServicingCaseApi.getServicingCaseById(loanId, UUID.randomUUID().toString())
                .map(c -> RateInfoDTO.builder()
                        .currentRate(c.getInterestRate())
                        .rateType("FIXED")   // MVP: rate type not stored on LoanServicingCaseDTO
                        .nextReviewDate(null) // MVP: no review schedule in core SDK
                        .build());
    }

    @Override
    public Flux<RateChangeDTO> getRateChanges(UUID loanId) {
        log.debug("Getting rate changes loanId={}", loanId);
        return loanRateChangeApi
                .findAllRateChanges(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toRateChange);
    }

    @Override
    public Flux<AccrualDTO> getAccruals(UUID loanId) {
        log.debug("Getting accruals loanId={}", loanId);
        return loanAccrualApi
                .findAllAccruals(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toAccrual);
    }

    @Override
    public Flux<EscrowDTO> getEscrow(UUID loanId) {
        // MVP: findAllEscrows returns untyped PaginationResponse; returning empty list.
        // Replace when the SDK exposes a typed pagination response for escrows.
        log.debug("Getting escrow loanId={}", loanId);
        return Flux.empty();
    }

    @Override
    public Flux<RebateDTO> getRebates(UUID loanId) {
        // MVP: findAllRebates returns untyped PaginationResponse; returning empty list.
        // Replace when the SDK exposes a typed pagination response for rebates.
        log.debug("Getting rebates loanId={}", loanId);
        return Flux.empty();
    }

    @Override
    public Mono<RestructuringDTO> requestRestructuring(UUID loanId, RequestRestructuringCommand command) {
        log.debug("Requesting restructuring loanId={}", loanId);
        var dto = new LoanRestructuringDTO()
                .loanServicingCaseId(loanId)
                .reason(command.getReason())
                .restructuringDate(LocalDate.now());
        return loanRestructuringApi
                .createRestructuring(loanId, dto, UUID.randomUUID().toString())
                .map(this::toRestructuring);
    }

    @Override
    public Flux<RestructuringDTO> getRestructurings(UUID loanId) {
        log.debug("Getting restructurings loanId={}", loanId);
        return loanRestructuringApi
                .findAllRestructurings(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toRestructuring);
    }

    @Override
    public Flux<LoanDocumentDTO> getDocuments(UUID loanId) {
        // MVP: no document resource in core-lending-loan-servicing-sdk.
        // Replace when upstream exposes a loan document sub-resource.
        log.debug("Getting documents loanId={}", loanId);
        return Flux.empty();
    }

    @Override
    public Flux<LoanEventDTO> getEvents(UUID loanId) {
        log.debug("Getting servicing events loanId={}", loanId);
        return loanServicingEventApi
                .findAllServicingEvents(loanId, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::toEvent);
    }

    @Override
    public Flux<LoanNotificationDTO> getNotifications(UUID loanId) {
        // MVP: findAllNotifications returns untyped PaginationResponse; returning empty list.
        // Replace when the SDK exposes a typed pagination response for loan notifications.
        log.debug("Getting notifications loanId={}", loanId);
        return Flux.empty();
    }

    // --- Mappers ---

    private LoanSummaryDTO toSummary(LoanServicingCaseDTO dto) {
        return LoanSummaryDTO.builder()
                .loanId(dto.getLoanServicingCaseId())
                .productType(dto.getProductCatalogId() != null ? dto.getProductCatalogId().toString() : null)
                .status(dto.getServicingStatus() != null ? dto.getServicingStatus().getValue() : null)
                .outstandingBalance(null) // MVP: requires separate balance query
                .nextPaymentDate(null)    // MVP: requires schedule query
                .nextPaymentAmount(null)  // MVP: requires schedule query
                .build();
    }

    private LoanDetailDTO toDetail(LoanServicingCaseDTO dto) {
        return LoanDetailDTO.builder()
                .loanId(dto.getLoanServicingCaseId())
                .productType(dto.getProductCatalogId() != null ? dto.getProductCatalogId().toString() : null)
                .status(dto.getServicingStatus() != null ? dto.getServicingStatus().getValue() : null)
                .outstandingBalance(null)
                .nextPaymentDate(null)
                .nextPaymentAmount(null)
                .originalAmount(dto.getPrincipalAmount())
                .term(dto.getLoanTerm())
                .interestRate(dto.getInterestRate())
                .disbursementDate(dto.getOriginationDate())
                .maturityDate(dto.getMaturityDate())
                .build();
    }

    private BalanceDTO toBalance(LoanBalanceDTO dto) {
        return BalanceDTO.builder()
                .loanId(dto.getLoanServicingCaseId())
                .outstandingPrincipal(dto.getPrincipalOutstanding())
                .outstandingInterest(dto.getInterestOutstanding())
                .totalOutstanding(dto.getTotalOutstanding())
                .currency("EUR") // MVP: currency not stored on LoanBalanceDTO
                .asOfDate(dto.getCreatedAt())
                .build();
    }

    private ScheduleEntryDTO toScheduleEntry(LoanRepaymentScheduleDTO dto) {
        String status = Boolean.TRUE.equals(dto.getIsPaid()) ? "PAID" : "SCHEDULED";
        return ScheduleEntryDTO.builder()
                .installmentNumber(dto.getInstallmentNumber())
                .dueDate(dto.getDueDate())
                .principal(dto.getPrincipalDue())
                .interest(dto.getInterestDue())
                .totalAmount(dto.getTotalDue())
                .status(status)
                .build();
    }

    private InstallmentDTO toInstallment(LoanInstallmentPlanDTO dto) {
        String status;
        if (Boolean.TRUE.equals(dto.getIsPaid())) {
            status = "PAID";
        } else if (dto.getDueDate() != null && dto.getDueDate().isBefore(LocalDate.now())) {
            status = "OVERDUE";
        } else {
            status = "SCHEDULED";
        }
        return InstallmentDTO.builder()
                .installmentId(dto.getLoanInstallmentPlanId())
                .installmentNumber(dto.getInstallmentNumber())
                .dueDate(dto.getDueDate())
                .amount(dto.getTotalDue())
                .status(status)
                .build();
    }

    private InstallmentPaymentDTO toInstallmentPayment(LoanInstallmentRecordDTO dto) {
        return InstallmentPaymentDTO.builder()
                .paymentId(dto.getLoanInstallmentRecordId())
                .amount(dto.getPaymentAmount())
                .paidAt(dto.getCreatedAt())
                .build();
    }

    private DisbursementDTO toDisbursement(LoanDisbursementDTO dto) {
        return DisbursementDTO.builder()
                .disbursementId(dto.getLoanDisbursementId())
                .amount(dto.getDisbursementAmount())
                .status(dto.getDisbursementStatus() != null ? dto.getDisbursementStatus().getValue() : null)
                .disbursedAt(dto.getDisbursementDate() != null ? dto.getDisbursementDate().atStartOfDay() : null)
                .build();
    }

    private RepaymentDTO toRepayment(LoanRepaymentRecordDTO dto) {
        return RepaymentDTO.builder()
                .repaymentId(dto.getLoanRepaymentRecordId())
                .amount(dto.getPaymentAmount())
                .type(Boolean.TRUE.equals(dto.getIsPartialPayment()) ? "EARLY" : "REGULAR")
                .appliedAt(dto.getPaymentDate() != null ? dto.getPaymentDate().atStartOfDay() : null)
                .build();
    }

    private RateChangeDTO toRateChange(LoanRateChangeDTO dto) {
        return RateChangeDTO.builder()
                .changeId(dto.getLoanRateChangeId())
                .previousRate(dto.getOldInterestRate())
                .newRate(dto.getNewInterestRate())
                .effectiveDate(dto.getEffectiveDate())
                .build();
    }

    private AccrualDTO toAccrual(LoanAccrualDTO dto) {
        return AccrualDTO.builder()
                .accrualId(dto.getLoanAccrualId())
                .period(dto.getAccrualDate() != null ? dto.getAccrualDate().toString() : null)
                .amount(dto.getAccrualAmount())
                .calculatedAt(dto.getCreatedAt())
                .build();
    }

    private RestructuringDTO toRestructuring(LoanRestructuringDTO dto) {
        return RestructuringDTO.builder()
                .restructuringId(dto.getLoanRestructuringId())
                .status("APPROVED") // MVP: LoanRestructuringDTO has no status field
                .requestedAt(dto.getRestructuringDate() != null
                        ? dto.getRestructuringDate().atStartOfDay() : null)
                .newTerms(dto.getReason()) // MVP: map reason as summary of new terms
                .build();
    }

    private LoanEventDTO toEvent(LoanServicingEventDTO dto) {
        return LoanEventDTO.builder()
                .eventId(dto.getLoanServicingEventId())
                .eventType(dto.getEventType() != null ? dto.getEventType().getValue() : null)
                .description(dto.getDescription())
                .timestamp(dto.getCreatedAt())
                .build();
    }
}
