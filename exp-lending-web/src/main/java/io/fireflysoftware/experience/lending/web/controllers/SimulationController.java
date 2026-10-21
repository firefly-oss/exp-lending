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


package io.fireflysoftware.experience.lending.web.controllers;

import io.fireflysoftware.experience.lending.core.simulation.commands.CheckEligibilityCommand;
import io.fireflysoftware.experience.lending.core.simulation.commands.ConfigureSimulationCommand;
import io.fireflysoftware.experience.lending.core.simulation.commands.CreateSimulationCommand;
import io.fireflysoftware.experience.lending.core.simulation.queries.ConfiguredSimulationDTO;
import io.fireflysoftware.experience.lending.core.simulation.queries.EligibilityResultDTO;
import io.fireflysoftware.experience.lending.core.simulation.queries.SimulationResultDTO;
import io.fireflysoftware.experience.lending.core.simulation.services.SimulationConfigurationService;
import io.fireflysoftware.experience.lending.core.simulation.services.SimulationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    private final SimulationConfigurationService simulationConfigurationService;

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

    @PostMapping(value = "/simulations/configure",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "configureLendingSimulation",
            summary = "Configure Simulation",
            description = "Calculates a lending simulation via the pricing engine and persists the "
                    + "result through domain-lending-loan-origination.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Simulation configured and persisted"),
            @ApiResponse(responseCode = "400", description = "Validation failed")
    })
    public Mono<ResponseEntity<ConfiguredSimulationDTO>> configureSimulation(
            @Valid @RequestBody ConfigureSimulationCommand command) {
        return simulationConfigurationService.configure(command)
                .map(result -> ResponseEntity.status(HttpStatus.CREATED).body(result));
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
