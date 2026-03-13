package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.servicing.commands.RequestEarlyRepaymentCommand;
import com.firefly.experience.lending.core.servicing.commands.RequestRestructuringCommand;
import com.firefly.experience.lending.core.servicing.queries.*;
import com.firefly.experience.lending.core.servicing.services.LoanServicingService;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = LoanServicingController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class LoanServicingControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private LoanServicingService loanServicingService;

    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final String BASE = "/api/v1/experience/lending/loans";

    // --- GET /loans ---

    @Test
    void listLoans_returns200WithSummaries() {
        var loanId = UUID.randomUUID();
        var summary = LoanSummaryDTO.builder()
                .loanId(loanId)
                .status("ACTIVE")
                .build();

        when(loanServicingService.listLoans()).thenReturn(Flux.just(summary));

        webTestClient.get()
                .uri(BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LoanSummaryDTO.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).getLoanId()).isEqualTo(loanId);
                    assertThat(list.get(0).getStatus()).isEqualTo("ACTIVE");
                });
    }

    // --- GET /loans/{id} ---

    @Test
    void getLoan_returns200WithDetail() {
        var loanId = UUID.randomUUID();
        var detail = LoanDetailDTO.builder()
                .loanId(loanId)
                .status("ACTIVE")
                .originalAmount(new BigDecimal("50000"))
                .term(60)
                .interestRate(new BigDecimal("4.5"))
                .maturityDate(LocalDate.of(2030, 1, 1))
                .build();

        when(loanServicingService.getLoan(eq(loanId))).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri(BASE + "/{id}", loanId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoanDetailDTO.class)
                .value(body -> {
                    assertThat(body.getLoanId()).isEqualTo(loanId);
                    assertThat(body.getTerm()).isEqualTo(60);
                    assertThat(body.getInterestRate()).isEqualByComparingTo("4.5");
                });
    }

    @Test
    void getLoan_returns500WhenServiceFails() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getLoan(eq(loanId)))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        webTestClient.get()
                .uri(BASE + "/{id}", loanId)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // --- GET /loans/{id}/balance ---

    @Test
    void getBalance_returns200WithBalance() {
        var loanId = UUID.randomUUID();
        var balance = BalanceDTO.builder()
                .loanId(loanId)
                .outstandingPrincipal(new BigDecimal("8500"))
                .totalOutstanding(new BigDecimal("8700"))
                .currency("EUR")
                .build();

        when(loanServicingService.getBalance(eq(loanId))).thenReturn(Mono.just(balance));

        webTestClient.get()
                .uri(BASE + "/{id}/balance", loanId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceDTO.class)
                .value(body -> {
                    assertThat(body.getOutstandingPrincipal()).isEqualByComparingTo("8500");
                    assertThat(body.getCurrency()).isEqualTo("EUR");
                });
    }

    // --- GET /loans/{id}/balance/history ---

    @Test
    void getBalanceHistory_returns200WithHistory() {
        var loanId = UUID.randomUUID();
        var history = BalanceHistoryDTO.builder()
                .entries(java.util.List.of())
                .build();

        when(loanServicingService.getBalanceHistory(eq(loanId))).thenReturn(Mono.just(history));

        webTestClient.get()
                .uri(BASE + "/{id}/balance/history", loanId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceHistoryDTO.class)
                .value(body -> assertThat(body.getEntries()).isEmpty());
    }

    // --- GET /loans/{id}/schedule ---

    @Test
    void getRepaymentSchedule_returns200WithEntries() {
        var loanId = UUID.randomUUID();
        var entry = ScheduleEntryDTO.builder()
                .installmentNumber(1)
                .dueDate(LocalDate.of(2025, 4, 1))
                .totalAmount(new BigDecimal("837.50"))
                .status("SCHEDULED")
                .build();

        when(loanServicingService.getRepaymentSchedule(eq(loanId))).thenReturn(Flux.just(entry));

        webTestClient.get()
                .uri(BASE + "/{id}/schedule", loanId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ScheduleEntryDTO.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).getStatus()).isEqualTo("SCHEDULED");
                });
    }

    // --- GET /loans/{id}/installments ---

    @Test
    void getInstallments_returns200WithInstallments() {
        var loanId = UUID.randomUUID();
        var installment = InstallmentDTO.builder()
                .installmentId(UUID.randomUUID())
                .installmentNumber(2)
                .dueDate(LocalDate.now().plusMonths(1))
                .amount(new BigDecimal("500"))
                .status("SCHEDULED")
                .build();

        when(loanServicingService.getInstallments(eq(loanId))).thenReturn(Flux.just(installment));

        webTestClient.get()
                .uri(BASE + "/{id}/installments", loanId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InstallmentDTO.class)
                .value(list -> assertThat(list).hasSize(1));
    }

    // --- GET /loans/{id}/installments/{instId} ---

    @Test
    void getInstallment_returns200WithInstallment() {
        var loanId = UUID.randomUUID();
        var instId = UUID.randomUUID();
        var installment = InstallmentDTO.builder()
                .installmentId(instId)
                .installmentNumber(3)
                .status("PAID")
                .build();

        when(loanServicingService.getInstallment(eq(loanId), eq(instId))).thenReturn(Mono.just(installment));

        webTestClient.get()
                .uri(BASE + "/{id}/installments/{instId}", loanId, instId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(InstallmentDTO.class)
                .value(body -> assertThat(body.getStatus()).isEqualTo("PAID"));
    }

    // --- GET /loans/{id}/installments/{instId}/payments ---

    @Test
    void getInstallmentPayments_returns200WithPayments() {
        var loanId = UUID.randomUUID();
        var instId = UUID.randomUUID();
        var payment = InstallmentPaymentDTO.builder()
                .paymentId(UUID.randomUUID())
                .amount(new BigDecimal("500"))
                .build();

        when(loanServicingService.getInstallmentPayments(eq(loanId), eq(instId)))
                .thenReturn(Flux.just(payment));

        webTestClient.get()
                .uri(BASE + "/{id}/installments/{instId}/payments", loanId, instId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(InstallmentPaymentDTO.class)
                .value(list -> assertThat(list).hasSize(1));
    }

    // --- GET /loans/{id}/disbursements ---

    @Test
    void getDisbursements_returns200WithDisbursements() {
        var loanId = UUID.randomUUID();
        var disbursement = DisbursementDTO.builder()
                .disbursementId(UUID.randomUUID())
                .amount(new BigDecimal("25000"))
                .status("COMPLETED")
                .build();

        when(loanServicingService.getDisbursements(eq(loanId))).thenReturn(Flux.just(disbursement));

        webTestClient.get()
                .uri(BASE + "/{id}/disbursements", loanId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DisbursementDTO.class)
                .value(list -> assertThat(list.get(0).getStatus()).isEqualTo("COMPLETED"));
    }

    // --- GET /loans/{id}/disbursements/{dId} ---

    @Test
    void getDisbursement_returns200() {
        var loanId = UUID.randomUUID();
        var dId = UUID.randomUUID();
        var disbursement = DisbursementDTO.builder().disbursementId(dId).amount(new BigDecimal("5000")).build();

        when(loanServicingService.getDisbursement(eq(loanId), eq(dId))).thenReturn(Mono.just(disbursement));

        webTestClient.get()
                .uri(BASE + "/{id}/disbursements/{dId}", loanId, dId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- GET /loans/{id}/disbursement-plan ---

    @Test
    void getDisbursementPlan_returns200() {
        var loanId = UUID.randomUUID();
        var plan = DisbursementPlanDTO.builder().entries(java.util.List.of()).build();
        when(loanServicingService.getDisbursementPlan(eq(loanId))).thenReturn(Mono.just(plan));

        webTestClient.get()
                .uri(BASE + "/{id}/disbursement-plan", loanId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- GET /loans/{id}/repayments ---

    @Test
    void getRepayments_returns200() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getRepayments(eq(loanId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/repayments", loanId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- POST /loans/{id}/early-repayment ---

    @Test
    void requestEarlyRepayment_returns202() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.requestEarlyRepayment(eq(loanId), any(RequestEarlyRepaymentCommand.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(BASE + "/{id}/early-repayment", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"amount": 5000, "type": "PARTIAL"}
                        """)
                .exchange()
                .expectStatus().isAccepted();
    }

    // --- POST /loans/{id}/early-repayment/simulation ---

    @Test
    void simulateEarlyRepayment_returns200WithSimulation() {
        var loanId = UUID.randomUUID();
        var sim = EarlyRepaymentSimulationDTO.builder()
                .totalAmount(new BigDecimal("10000"))
                .penaltyAmount(BigDecimal.ZERO)
                .savings(BigDecimal.ZERO)
                .build();

        when(loanServicingService.simulateEarlyRepayment(eq(loanId), any(RequestEarlyRepaymentCommand.class)))
                .thenReturn(Mono.just(sim));

        webTestClient.post()
                .uri(BASE + "/{id}/early-repayment/simulation", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"amount": 10000, "type": "FULL"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EarlyRepaymentSimulationDTO.class)
                .value(body -> assertThat(body.getTotalAmount()).isEqualByComparingTo("10000"));
    }

    // --- GET /loans/{id}/rate-info ---

    @Test
    void getRateInfo_returns200() {
        var loanId = UUID.randomUUID();
        var info = RateInfoDTO.builder().currentRate(new BigDecimal("3.75")).rateType("FIXED").build();
        when(loanServicingService.getRateInfo(eq(loanId))).thenReturn(Mono.just(info));

        webTestClient.get()
                .uri(BASE + "/{id}/rate-info", loanId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RateInfoDTO.class)
                .value(body -> assertThat(body.getRateType()).isEqualTo("FIXED"));
    }

    // --- GET /loans/{id}/rate-changes ---

    @Test
    void getRateChanges_returns200() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getRateChanges(eq(loanId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/rate-changes", loanId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- GET /loans/{id}/accruals ---

    @Test
    void getAccruals_returns200() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getAccruals(eq(loanId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/accruals", loanId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- GET /loans/{id}/escrow ---

    @Test
    void getEscrow_returns200() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getEscrow(eq(loanId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/escrow", loanId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- GET /loans/{id}/rebates ---

    @Test
    void getRebates_returns200() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getRebates(eq(loanId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/rebates", loanId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- POST /loans/{id}/restructuring ---

    @Test
    void requestRestructuring_returns202WithRestructuringDTO() {
        var loanId = UUID.randomUUID();
        var restructuringId = UUID.randomUUID();
        var result = RestructuringDTO.builder()
                .restructuringId(restructuringId)
                .status("APPROVED")
                .build();

        when(loanServicingService.requestRestructuring(eq(loanId), any(RequestRestructuringCommand.class)))
                .thenReturn(Mono.just(result));

        webTestClient.post()
                .uri(BASE + "/{id}/restructuring", loanId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"reason": "Financial hardship", "requestedTermChanges": "Extend 12 months"}
                        """)
                .exchange()
                .expectStatus().isAccepted()
                .expectBody(RestructuringDTO.class)
                .value(body -> assertThat(body.getStatus()).isEqualTo("APPROVED"));
    }

    // --- GET /loans/{id}/restructurings ---

    @Test
    void getRestructurings_returns200() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getRestructurings(eq(loanId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/restructurings", loanId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- GET /loans/{id}/documents ---

    @Test
    void getDocuments_returns200() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getDocuments(eq(loanId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/documents", loanId)
                .exchange()
                .expectStatus().isOk();
    }

    // --- GET /loans/{id}/events ---

    @Test
    void getEvents_returns200WithEvents() {
        var loanId = UUID.randomUUID();
        var event = LoanEventDTO.builder()
                .eventId(UUID.randomUUID())
                .eventType("RESTRUCTURE")
                .description("Restructure applied")
                .build();

        when(loanServicingService.getEvents(eq(loanId))).thenReturn(Flux.just(event));

        webTestClient.get()
                .uri(BASE + "/{id}/events", loanId)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(LoanEventDTO.class)
                .value(list -> assertThat(list.get(0).getEventType()).isEqualTo("RESTRUCTURE"));
    }

    // --- GET /loans/{id}/notifications ---

    @Test
    void getNotifications_returns200() {
        var loanId = UUID.randomUUID();
        when(loanServicingService.getNotifications(eq(loanId))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/{id}/notifications", loanId)
                .exchange()
                .expectStatus().isOk();
    }
}
