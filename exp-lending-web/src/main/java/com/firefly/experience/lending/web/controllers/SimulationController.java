package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.simulation.commands.CheckEligibilityCommand;
import com.firefly.experience.lending.core.simulation.commands.CreateSimulationCommand;
import com.firefly.experience.lending.core.simulation.queries.EligibilityResultDTO;
import com.firefly.experience.lending.core.simulation.queries.SimulationResultDTO;
import com.firefly.experience.lending.core.simulation.services.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * REST controller exposing the loan simulation and product eligibility evaluation endpoints.
 */
@RestController
@RequestMapping("/api/v1/experience/lending")
@RequiredArgsConstructor
@Tag(name = "Lending - Simulation & Eligibility")
public class SimulationController {

    private final SimulationService simulationService;

    @PostMapping(value = "/simulations",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create Simulation",
            description = "Computes a loan simulation for the given amount, term, and product type.")
    public Mono<ResponseEntity<SimulationResultDTO>> createSimulation(
            @RequestBody CreateSimulationCommand command) {
        return simulationService.createSimulation(command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
    }

    @GetMapping(value = "/simulations/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Get Simulation",
            description = "Retrieves a previously created loan simulation by its identifier.")
    public Mono<ResponseEntity<SimulationResultDTO>> getSimulation(@PathVariable UUID id) {
        return simulationService.getSimulation(id)
                .map(ResponseEntity::ok);
    }

    @PostMapping(value = "/eligibility",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Check Eligibility",
            description = "Evaluates whether a party is eligible for a given product and requested amount.")
    public Mono<ResponseEntity<EligibilityResultDTO>> checkEligibility(
            @RequestBody CheckEligibilityCommand command) {
        return simulationService.checkEligibility(command)
                .map(ResponseEntity::ok);
    }
}
