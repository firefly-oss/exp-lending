package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.commands.CreateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateApplicationCommand;
import com.firefly.experience.lending.core.application.queries.ApplicationDetailDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationStatusHistoryDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationSummaryDTO;
import com.firefly.experience.lending.core.application.services.ApplicationService;
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
 * REST controller exposing loan application lifecycle endpoints (create, list, get, update,
 * submit, withdraw, and status history).
 */
@RestController
@RequestMapping("/api/v1/experience/lending/applications")
@RequiredArgsConstructor
@Tag(name = "Lending - Applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Application",
            description = "Creates a new loan application for the given product, amount, term, and purpose.")
    public Mono<ResponseEntity<ApplicationDetailDTO>> createApplication(
            @RequestBody CreateApplicationCommand command) {
        return applicationService.createApplication(command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Applications",
            description = "Returns all loan applications accessible to the caller.")
    public Flux<ApplicationSummaryDTO> listApplications() {
        return applicationService.listApplications();
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Application",
            description = "Retrieves the full details of a loan application by its identifier.")
    public Mono<ResponseEntity<ApplicationDetailDTO>> getApplication(@PathVariable UUID id) {
        return applicationService.getApplication(id)
                .map(ResponseEntity::ok);
    }

    @PatchMapping(value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update Application",
            description = "Updates editable fields of a loan application.")
    public Mono<ResponseEntity<ApplicationDetailDTO>> updateApplication(
            @PathVariable UUID id,
            @RequestBody UpdateApplicationCommand command) {
        return applicationService.updateApplication(id, command)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/{id}/submission", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Submit Application",
            description = "Submits the application for underwriting review.")
    public Mono<ResponseEntity<Void>> submitApplication(@PathVariable UUID id) {
        return applicationService.submitApplication(id)
                .thenReturn(ResponseEntity.<Void>accepted().build());
    }

    @PostMapping(value = "/{id}/withdraw", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Withdraw Application",
            description = "Withdraws the application at the applicant's request.")
    public Mono<ResponseEntity<Void>> withdrawApplication(@PathVariable UUID id) {
        return applicationService.withdrawApplication(id)
                .thenReturn(ResponseEntity.<Void>ok().build());
    }

    @GetMapping(value = "/{id}/status-history", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Status History",
            description = "Returns the full status transition history for a loan application.")
    public Mono<ResponseEntity<ApplicationStatusHistoryDTO>> getStatusHistory(@PathVariable UUID id) {
        return applicationService.getStatusHistory(id)
                .map(ResponseEntity::ok);
    }
}
