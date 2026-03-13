package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.servicing.commands.RequestEarlyRepaymentCommand;
import com.firefly.experience.lending.core.servicing.commands.RequestRestructuringCommand;
import com.firefly.experience.lending.core.servicing.queries.*;
import com.firefly.experience.lending.core.servicing.services.LoanServicingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing active loan servicing endpoints: balances, repayment schedules,
 * installments, disbursements, rate changes, accruals, restructuring, documents, and events.
 */
@RestController
@RequestMapping("/api/v1/experience/lending/loans")
@RequiredArgsConstructor
@Tag(name = "Lending - Active Loans")
public class LoanServicingController {

    private final LoanServicingService loanServicingService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Active Loans",
            description = "Returns all active loan servicing cases accessible to the caller.")
    public Flux<LoanSummaryDTO> listLoans() {
        return loanServicingService.listLoans();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Loan",
            description = "Retrieves full details of an active loan by its identifier.")
    public Mono<ResponseEntity<LoanDetailDTO>> getLoan(@PathVariable UUID id) {
        return loanServicingService.getLoan(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Balance",
            description = "Returns the current outstanding balance for the loan.")
    public Mono<ResponseEntity<BalanceDTO>> getBalance(@PathVariable UUID id) {
        return loanServicingService.getBalance(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/balance/history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Balance History",
            description = "Returns the full balance snapshot history for the loan.")
    public Mono<ResponseEntity<BalanceHistoryDTO>> getBalanceHistory(@PathVariable UUID id) {
        return loanServicingService.getBalanceHistory(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Repayment Schedule",
            description = "Returns the full amortisation schedule for the loan.")
    public Flux<ScheduleEntryDTO> getRepaymentSchedule(@PathVariable UUID id) {
        return loanServicingService.getRepaymentSchedule(id);
    }

    @GetMapping(value = "/{id}/installments", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Installments",
            description = "Returns all installment plan entries for the loan.")
    public Flux<InstallmentDTO> getInstallments(@PathVariable UUID id) {
        return loanServicingService.getInstallments(id);
    }

    @GetMapping(value = "/{id}/installments/{instId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Installment",
            description = "Returns a single installment plan entry.")
    public Mono<ResponseEntity<InstallmentDTO>> getInstallment(
            @PathVariable UUID id,
            @PathVariable UUID instId) {
        return loanServicingService.getInstallment(id, instId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/installments/{instId}/payments", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Installment Payments",
            description = "Returns payment records linked to a specific installment.")
    public Flux<InstallmentPaymentDTO> getInstallmentPayments(
            @PathVariable UUID id,
            @PathVariable UUID instId) {
        return loanServicingService.getInstallmentPayments(id, instId);
    }

    @GetMapping(value = "/{id}/disbursements", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Disbursements",
            description = "Returns all disbursement records for the loan.")
    public Flux<DisbursementDTO> getDisbursements(@PathVariable UUID id) {
        return loanServicingService.getDisbursements(id);
    }

    @GetMapping(value = "/{id}/disbursements/{dId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Disbursement",
            description = "Returns a single disbursement record.")
    public Mono<ResponseEntity<DisbursementDTO>> getDisbursement(
            @PathVariable UUID id,
            @PathVariable UUID dId) {
        return loanServicingService.getDisbursement(id, dId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/disbursement-plan", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Disbursement Plan",
            description = "Returns the disbursement plan for the loan.")
    public Mono<ResponseEntity<DisbursementPlanDTO>> getDisbursementPlan(@PathVariable UUID id) {
        return loanServicingService.getDisbursementPlan(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/repayments", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Repayments",
            description = "Returns all repayment records for the loan.")
    public Flux<RepaymentDTO> getRepayments(@PathVariable UUID id) {
        return loanServicingService.getRepayments(id);
    }

    @PostMapping(value = "/{id}/early-repayment",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Request Early Repayment",
            description = "Registers a partial or full early repayment against the loan.")
    public Mono<ResponseEntity<Void>> requestEarlyRepayment(
            @PathVariable UUID id,
            @RequestBody RequestEarlyRepaymentCommand command) {
        return loanServicingService.requestEarlyRepayment(id, command)
                .thenReturn(ResponseEntity.<Void>accepted().build());
    }

    @PostMapping(value = "/{id}/early-repayment/simulation",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Simulate Early Repayment",
            description = "Returns a cost breakdown for a hypothetical early repayment.")
    public Mono<ResponseEntity<EarlyRepaymentSimulationDTO>> simulateEarlyRepayment(
            @PathVariable UUID id,
            @RequestBody RequestEarlyRepaymentCommand command) {
        return loanServicingService.simulateEarlyRepayment(id, command)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/rate-info", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Rate Info",
            description = "Returns the current interest rate and type for the loan.")
    public Mono<ResponseEntity<RateInfoDTO>> getRateInfo(@PathVariable UUID id) {
        return loanServicingService.getRateInfo(id)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/{id}/rate-changes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Rate Changes",
            description = "Returns the history of interest rate changes for the loan.")
    public Flux<RateChangeDTO> getRateChanges(@PathVariable UUID id) {
        return loanServicingService.getRateChanges(id);
    }

    @GetMapping(value = "/{id}/accruals", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Accruals",
            description = "Returns all interest accrual records for the loan.")
    public Flux<AccrualDTO> getAccruals(@PathVariable UUID id) {
        return loanServicingService.getAccruals(id);
    }

    @GetMapping(value = "/{id}/escrow", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Escrow Accounts",
            description = "Returns all escrow accounts associated with the loan.")
    public Flux<EscrowDTO> getEscrow(@PathVariable UUID id) {
        return loanServicingService.getEscrow(id);
    }

    @GetMapping(value = "/{id}/rebates", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Rebates",
            description = "Returns all rebate records applied to the loan.")
    public Flux<RebateDTO> getRebates(@PathVariable UUID id) {
        return loanServicingService.getRebates(id);
    }

    @PostMapping(value = "/{id}/restructuring",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Request Restructuring",
            description = "Submits a restructuring request for the loan.")
    public Mono<ResponseEntity<RestructuringDTO>> requestRestructuring(
            @PathVariable UUID id,
            @RequestBody RequestRestructuringCommand command) {
        return loanServicingService.requestRestructuring(id, command)
                .map(result -> ResponseEntity.status(HttpStatus.ACCEPTED).body(result));
    }

    @GetMapping(value = "/{id}/restructurings", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Restructurings",
            description = "Returns the history of restructuring requests for the loan.")
    public Flux<RestructuringDTO> getRestructurings(@PathVariable UUID id) {
        return loanServicingService.getRestructurings(id);
    }

    @GetMapping(value = "/{id}/documents", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Documents",
            description = "Returns all documents attached to the loan.")
    public Flux<LoanDocumentDTO> getDocuments(@PathVariable UUID id) {
        return loanServicingService.getDocuments(id);
    }

    @GetMapping(value = "/{id}/events", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Events",
            description = "Returns the servicing event log for the loan.")
    public Flux<LoanEventDTO> getEvents(@PathVariable UUID id) {
        return loanServicingService.getEvents(id);
    }

    @GetMapping(value = "/{id}/notifications", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Notifications",
            description = "Returns all notifications sent in connection with the loan.")
    public Flux<LoanNotificationDTO> getNotifications(@PathVariable UUID id) {
        return loanServicingService.getNotifications(id);
    }
}
