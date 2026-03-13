package com.firefly.experience.lending.core.servicing.services;

import com.firefly.core.lending.servicing.sdk.api.*;
import com.firefly.core.lending.servicing.sdk.model.*;
import com.firefly.experience.lending.core.servicing.commands.RequestEarlyRepaymentCommand;
import com.firefly.experience.lending.core.servicing.commands.RequestRestructuringCommand;
import com.firefly.experience.lending.core.servicing.services.impl.LoanServicingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServicingServiceImplTest {

    @Mock private LoanServicingCaseApi loanServicingCaseApi;
    @Mock private LoanBalanceApi loanBalanceApi;
    @Mock private LoanRepaymentScheduleApi loanRepaymentScheduleApi;
    @Mock private LoanInstallmentPlanApi loanInstallmentPlanApi;
    @Mock private LoanInstallmentRecordApi loanInstallmentRecordApi;
    @Mock private LoanDisbursementApi loanDisbursementApi;
    @Mock private LoanDisbursementPlanApi loanDisbursementPlanApi;
    @Mock private LoanRepaymentRecordApi loanRepaymentRecordApi;
    @Mock private LoanRateChangeApi loanRateChangeApi;
    @Mock private LoanAccrualApi loanAccrualApi;
    @Mock private LoanEscrowApi loanEscrowApi;
    @Mock private LoanRebateApi loanRebateApi;
    @Mock private LoanRestructuringApi loanRestructuringApi;
    @Mock private LoanServicingEventApi loanServicingEventApi;
    @Mock private LoanNotificationApi loanNotificationApi;

    private LoanServicingServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new LoanServicingServiceImpl(
                loanServicingCaseApi, loanBalanceApi, loanRepaymentScheduleApi,
                loanInstallmentPlanApi, loanInstallmentRecordApi, loanDisbursementApi,
                loanDisbursementPlanApi, loanRepaymentRecordApi, loanRateChangeApi,
                loanAccrualApi, loanEscrowApi, loanRebateApi, loanRestructuringApi,
                loanServicingEventApi, loanNotificationApi);
    }

    // --- listLoans ---

    @Test
    void listLoans_mapsServicingCasesToSummaries() {
        var caseId = UUID.randomUUID();
        var dto = new LoanServicingCaseDTO(caseId)
                .servicingStatus(LoanServicingCaseDTO.ServicingStatusEnum.ACTIVE)
                .principalAmount(new BigDecimal("10000"));

        var page = new PaginationResponseLoanServicingCaseDTO().content(List.of(dto));
        when(loanServicingCaseApi.findAllServicingCases(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listLoans())
                .assertNext(summary -> {
                    assertThat(summary.getLoanId()).isEqualTo(caseId);
                    assertThat(summary.getStatus()).isEqualTo("ACTIVE");
                })
                .verifyComplete();
    }

    @Test
    void listLoans_returnsEmpty_whenPageContentIsNull() {
        var page = new PaginationResponseLoanServicingCaseDTO();
        when(loanServicingCaseApi.findAllServicingCases(
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listLoans()).verifyComplete();
    }

    // --- getLoan ---

    @Test
    void getLoan_mapsServicingCaseToDetail() {
        var loanId = UUID.randomUUID();
        var maturity = LocalDate.of(2030, 1, 1);
        var dto = new LoanServicingCaseDTO(loanId)
                .servicingStatus(LoanServicingCaseDTO.ServicingStatusEnum.ACTIVE)
                .principalAmount(new BigDecimal("50000"))
                .loanTerm(60)
                .interestRate(new BigDecimal("4.5"))
                .originationDate(LocalDate.of(2025, 1, 1))
                .maturityDate(maturity);

        when(loanServicingCaseApi.getServicingCaseById(eq(loanId), isNull()))
                .thenReturn(Mono.just(dto));

        StepVerifier.create(service.getLoan(loanId))
                .assertNext(detail -> {
                    assertThat(detail.getLoanId()).isEqualTo(loanId);
                    assertThat(detail.getOriginalAmount()).isEqualByComparingTo("50000");
                    assertThat(detail.getTerm()).isEqualTo(60);
                    assertThat(detail.getInterestRate()).isEqualByComparingTo("4.5");
                    assertThat(detail.getMaturityDate()).isEqualTo(maturity);
                })
                .verifyComplete();
    }

    @Test
    void getLoan_propagatesUpstreamError() {
        var loanId = UUID.randomUUID();
        when(loanServicingCaseApi.getServicingCaseById(eq(loanId), isNull()))
                .thenReturn(Mono.error(new RuntimeException("not found")));

        StepVerifier.create(service.getLoan(loanId))
                .expectErrorMessage("not found")
                .verify();
    }

    // --- getBalance ---

    @Test
    void getBalance_prefersCurrentSnapshot() {
        var loanId = UUID.randomUUID();
        var stale = new LoanBalanceDTO()
                .loanServicingCaseId(loanId)
                .principalOutstanding(new BigDecimal("9000"))
                .totalOutstanding(new BigDecimal("9200"))
                .isCurrent(false)
                .createdAt(LocalDateTime.now().minusDays(5));
        var current = new LoanBalanceDTO()
                .loanServicingCaseId(loanId)
                .principalOutstanding(new BigDecimal("8500"))
                .totalOutstanding(new BigDecimal("8700"))
                .isCurrent(true)
                .createdAt(LocalDateTime.now());

        var page = new PaginationResponseLoanBalanceDTO().content(List.of(stale, current));
        when(loanBalanceApi.findAllBalances(eq(loanId), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getBalance(loanId))
                .assertNext(balance -> {
                    assertThat(balance.getOutstandingPrincipal()).isEqualByComparingTo("8500");
                    assertThat(balance.getTotalOutstanding()).isEqualByComparingTo("8700");
                    assertThat(balance.getCurrency()).isEqualTo("EUR");
                })
                .verifyComplete();
    }

    // --- getRepaymentSchedule ---

    @Test
    void getRepaymentSchedule_mapsToPaidStatus() {
        var loanId = UUID.randomUUID();
        var entry = new LoanRepaymentScheduleDTO()
                .installmentNumber(1)
                .dueDate(LocalDate.of(2025, 2, 1))
                .principalDue(new BigDecimal("800"))
                .interestDue(new BigDecimal("37.50"))
                .totalDue(new BigDecimal("837.50"))
                .isPaid(true);

        var page = new PaginationResponseLoanRepaymentScheduleDTO().content(List.of(entry));
        when(loanRepaymentScheduleApi.findAllRepaymentSchedules(eq(loanId), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getRepaymentSchedule(loanId))
                .assertNext(e -> {
                    assertThat(e.getInstallmentNumber()).isEqualTo(1);
                    assertThat(e.getStatus()).isEqualTo("PAID");
                    assertThat(e.getTotalAmount()).isEqualByComparingTo("837.50");
                })
                .verifyComplete();
    }

    // --- getInstallments ---

    @Test
    void getInstallments_mapsOverdueStatus_whenDueDatePast() {
        var loanId = UUID.randomUUID();
        var planId = UUID.randomUUID();
        var plan = new LoanInstallmentPlanDTO(planId, null, null)
                .installmentNumber(3)
                .dueDate(LocalDate.of(2024, 1, 1))  // past date → OVERDUE
                .totalDue(new BigDecimal("450"))
                .isPaid(false);

        var page = new PaginationResponseLoanInstallmentPlanDTO().content(List.of(plan));
        when(loanInstallmentPlanApi.findAllInstallmentPlans(eq(loanId), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getInstallments(loanId))
                .assertNext(inst -> {
                    assertThat(inst.getInstallmentId()).isEqualTo(planId);
                    assertThat(inst.getStatus()).isEqualTo("OVERDUE");
                })
                .verifyComplete();
    }

    @Test
    void getInstallment_delegatesToGetById() {
        var loanId = UUID.randomUUID();
        var installmentId = UUID.randomUUID();
        var plan = new LoanInstallmentPlanDTO(installmentId, null, null)
                .installmentNumber(2)
                .dueDate(LocalDate.now().plusMonths(1))
                .totalDue(new BigDecimal("500"))
                .isPaid(false);

        when(loanInstallmentPlanApi.getInstallmentPlanById(eq(loanId), eq(installmentId), isNull()))
                .thenReturn(Mono.just(plan));

        StepVerifier.create(service.getInstallment(loanId, installmentId))
                .assertNext(inst -> {
                    assertThat(inst.getInstallmentId()).isEqualTo(installmentId);
                    assertThat(inst.getStatus()).isEqualTo("SCHEDULED");
                })
                .verifyComplete();
    }

    // --- getDisbursements ---

    @Test
    void getDisbursements_mapsDisbursementStatus() {
        var loanId = UUID.randomUUID();
        var disbId = UUID.randomUUID();
        var disb = new LoanDisbursementDTO(disbId, null, null)
                .disbursementAmount(new BigDecimal("25000"))
                .disbursementStatus(LoanDisbursementDTO.DisbursementStatusEnum.COMPLETED)
                .disbursementDate(LocalDate.of(2025, 3, 1));

        var page = new PaginationResponseLoanDisbursementDTO().content(List.of(disb));
        when(loanDisbursementApi.findAllDisbursements(eq(loanId), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getDisbursements(loanId))
                .assertNext(d -> {
                    assertThat(d.getDisbursementId()).isEqualTo(disbId);
                    assertThat(d.getAmount()).isEqualByComparingTo("25000");
                    assertThat(d.getStatus()).isEqualTo("COMPLETED");
                })
                .verifyComplete();
    }

    // --- requestEarlyRepayment ---

    @Test
    void requestEarlyRepayment_createsRepaymentRecord() {
        var loanId = UUID.randomUUID();
        when(loanRepaymentRecordApi.createRepaymentRecord(eq(loanId), any(LoanRepaymentRecordDTO.class), any()))
                .thenReturn(Mono.just(new LoanRepaymentRecordDTO()));

        var cmd = new RequestEarlyRepaymentCommand();
        cmd.setAmount(new BigDecimal("5000"));
        cmd.setType("PARTIAL");

        StepVerifier.create(service.requestEarlyRepayment(loanId, cmd))
                .verifyComplete();
    }

    // --- simulateEarlyRepayment ---

    @Test
    void simulateEarlyRepayment_returnsStubWithZeroPenalty() {
        var loanId = UUID.randomUUID();
        var cmd = new RequestEarlyRepaymentCommand();
        cmd.setAmount(new BigDecimal("10000"));
        cmd.setType("FULL");

        StepVerifier.create(service.simulateEarlyRepayment(loanId, cmd))
                .assertNext(sim -> {
                    assertThat(sim.getTotalAmount()).isEqualByComparingTo("10000");
                    assertThat(sim.getPenaltyAmount()).isEqualByComparingTo("0");
                    assertThat(sim.getSavings()).isEqualByComparingTo("0");
                })
                .verifyComplete();
    }

    // --- getRateInfo ---

    @Test
    void getRateInfo_derivesRateFromServicingCase() {
        var loanId = UUID.randomUUID();
        var dto = new LoanServicingCaseDTO(loanId)
                .interestRate(new BigDecimal("3.75"));

        when(loanServicingCaseApi.getServicingCaseById(eq(loanId), isNull()))
                .thenReturn(Mono.just(dto));

        StepVerifier.create(service.getRateInfo(loanId))
                .assertNext(info -> {
                    assertThat(info.getCurrentRate()).isEqualByComparingTo("3.75");
                    assertThat(info.getRateType()).isEqualTo("FIXED");
                    assertThat(info.getNextReviewDate()).isNull();
                })
                .verifyComplete();
    }

    // --- getAccruals ---

    @Test
    void getAccruals_mapsAccrualFields() {
        var loanId = UUID.randomUUID();
        var accrualId = UUID.randomUUID();
        var accrual = new LoanAccrualDTO(accrualId)
                .accrualAmount(new BigDecimal("12.50"))
                .accrualDate(LocalDate.of(2025, 3, 31))
                .createdAt(LocalDateTime.now());

        var page = new PaginationResponseLoanAccrualDTO().content(List.of(accrual));
        when(loanAccrualApi.findAllAccruals(eq(loanId), any(), any(), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any(), any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getAccruals(loanId))
                .assertNext(a -> {
                    assertThat(a.getAccrualId()).isEqualTo(accrualId);
                    assertThat(a.getAmount()).isEqualByComparingTo("12.50");
                    assertThat(a.getPeriod()).isEqualTo("2025-03-31");
                })
                .verifyComplete();
    }

    // --- MVP stubs ---

    @Test
    void getDisbursementPlan_returnsEmptyPlan() {
        var loanId = UUID.randomUUID();
        StepVerifier.create(service.getDisbursementPlan(loanId))
                .assertNext(plan -> assertThat(plan.getEntries()).isEmpty())
                .verifyComplete();
    }

    @Test
    void getEscrow_returnsEmpty() {
        StepVerifier.create(service.getEscrow(UUID.randomUUID())).verifyComplete();
    }

    @Test
    void getRebates_returnsEmpty() {
        StepVerifier.create(service.getRebates(UUID.randomUUID())).verifyComplete();
    }

    @Test
    void getDocuments_returnsEmpty() {
        StepVerifier.create(service.getDocuments(UUID.randomUUID())).verifyComplete();
    }

    @Test
    void getNotifications_returnsEmpty() {
        StepVerifier.create(service.getNotifications(UUID.randomUUID())).verifyComplete();
    }

    // --- requestRestructuring ---

    @Test
    void requestRestructuring_createsRestructuringRecord() {
        var loanId = UUID.randomUUID();
        var restructuringId = UUID.randomUUID();
        var sdkDto = new LoanRestructuringDTO(restructuringId)
                .reason("Financial hardship")
                .restructuringDate(LocalDate.now());

        when(loanRestructuringApi.createRestructuring(eq(loanId), any(LoanRestructuringDTO.class), any()))
                .thenReturn(Mono.just(sdkDto));

        var cmd = new RequestRestructuringCommand();
        cmd.setReason("Financial hardship");
        cmd.setRequestedTermChanges("Extend by 12 months");

        StepVerifier.create(service.requestRestructuring(loanId, cmd))
                .assertNext(r -> {
                    assertThat(r.getRestructuringId()).isEqualTo(restructuringId);
                    assertThat(r.getStatus()).isEqualTo("APPROVED");
                })
                .verifyComplete();
    }

    // --- getEvents ---

    @Test
    void getEvents_mapsServicingEvents() {
        var loanId = UUID.randomUUID();
        var eventId = UUID.randomUUID();
        var event = new LoanServicingEventDTO(eventId)
                .eventType(LoanServicingEventDTO.EventTypeEnum.RESTRUCTURE)
                .description("Restructure applied")
                .createdAt(LocalDateTime.now());

        var page = new PaginationResponseLoanServicingEventDTO().content(List.of(event));
        when(loanServicingEventApi.findAllServicingEvents(eq(loanId), any(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(),
                any()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getEvents(loanId))
                .assertNext(e -> {
                    assertThat(e.getEventId()).isEqualTo(eventId);
                    assertThat(e.getEventType()).isEqualTo("RESTRUCTURE");
                    assertThat(e.getDescription()).isEqualTo("Restructure applied");
                })
                .verifyComplete();
    }
}
