package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.disbursement.commands.ConfigureDisbursementAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.commands.RegisterExternalAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.queries.DisbursementAccountDTO;
import com.firefly.experience.lending.core.application.disbursement.queries.ExternalAccountDTO;
import com.firefly.experience.lending.core.application.disbursement.services.DisbursementAccountService;
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
 * REST controller exposing disbursement account configuration and external bank account
 * management endpoints for a loan application.
 */
@RestController
@RequestMapping("/api/v1/experience/lending/applications/{id}")
@RequiredArgsConstructor
@Tag(name = "Lending - Disbursement Account")
public class DisbursementAccountController {

    private final DisbursementAccountService disbursementAccountService;

    @GetMapping(value = "/disbursement-account", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Disbursement Account",
            description = "Returns the configured disbursement account for a loan application.")
    public Mono<ResponseEntity<DisbursementAccountDTO>> getDisbursementAccount(@PathVariable UUID id) {
        return disbursementAccountService.getDisbursementAccount(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping(value = "/disbursement-account",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Configure Disbursement Account",
            description = "Sets the disbursement account (internal or external) for a loan application.")
    public Mono<ResponseEntity<DisbursementAccountDTO>> configureDisbursementAccount(
            @PathVariable UUID id,
            @RequestBody ConfigureDisbursementAccountCommand command) {
        return disbursementAccountService.configureDisbursementAccount(id, command)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/external-accounts",
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Register External Account",
            description = "Registers a new external bank account for disbursement on a loan application.")
    public Mono<ResponseEntity<ExternalAccountDTO>> registerExternalAccount(
            @PathVariable UUID id,
            @RequestBody RegisterExternalAccountCommand command) {
        return disbursementAccountService.registerExternalAccount(id, command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @GetMapping(value = "/external-accounts", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List External Accounts",
            description = "Returns all external bank accounts registered for a loan application.")
    public Flux<ExternalAccountDTO> listExternalAccounts(@PathVariable UUID id) {
        return disbursementAccountService.listExternalAccounts(id);
    }

    @DeleteMapping("/external-accounts/{accId}")
    @Operation(summary = "Delete External Account",
            description = "Removes an external bank account from a loan application.")
    public Mono<ResponseEntity<Void>> deleteExternalAccount(
            @PathVariable UUID id,
            @PathVariable UUID accId) {
        return disbursementAccountService.deleteExternalAccount(id, accId)
                .thenReturn(ResponseEntity.<Void>noContent().build());
    }
}
