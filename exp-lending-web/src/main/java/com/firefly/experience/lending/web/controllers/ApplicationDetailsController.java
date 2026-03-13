package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.details.queries.ConditionDTO;
import com.firefly.experience.lending.core.application.details.queries.FeeDTO;
import com.firefly.experience.lending.core.application.details.queries.TaskDTO;
import com.firefly.experience.lending.core.application.details.queries.VerificationDTO;
import com.firefly.experience.lending.core.application.details.services.ApplicationDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing application detail sub-resource endpoints:
 * conditions, tasks, fees, and verifications.
 */
@RestController
@RequestMapping("/api/v1/experience/lending/applications/{id}")
@RequiredArgsConstructor
@Tag(name = "Lending - Application Details")
public class ApplicationDetailsController {

    private final ApplicationDetailsService applicationDetailsService;

    @GetMapping(value = "/conditions", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Conditions",
            description = "Returns all conditions associated with a loan application.")
    public Flux<ConditionDTO> getConditions(@PathVariable UUID id) {
        return applicationDetailsService.getConditions(id);
    }

    @GetMapping(value = "/tasks", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Tasks",
            description = "Returns all tasks associated with a loan application.")
    public Flux<TaskDTO> getTasks(@PathVariable UUID id) {
        return applicationDetailsService.getTasks(id);
    }

    @PatchMapping(value = "/tasks/{taskId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Complete Task",
            description = "Marks an application task as COMPLETED.")
    public Mono<ResponseEntity<TaskDTO>> completeTask(
            @PathVariable UUID id,
            @PathVariable UUID taskId) {
        return applicationDetailsService.completeTask(id, taskId)
                .map(ResponseEntity::ok);
    }

    @GetMapping(value = "/fees", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Fees",
            description = "Returns all fees associated with a loan application.")
    public Flux<FeeDTO> getFees(@PathVariable UUID id) {
        return applicationDetailsService.getFees(id);
    }

    @GetMapping(value = "/verifications", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "List Verifications",
            description = "Returns all verifications associated with a loan application.")
    public Flux<VerificationDTO> getVerifications(@PathVariable UUID id) {
        return applicationDetailsService.getVerifications(id);
    }
}
