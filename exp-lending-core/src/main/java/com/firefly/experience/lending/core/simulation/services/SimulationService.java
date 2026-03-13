package com.firefly.experience.lending.core.simulation.services;

import com.firefly.experience.lending.core.simulation.commands.CheckEligibilityCommand;
import com.firefly.experience.lending.core.simulation.commands.CreateSimulationCommand;
import com.firefly.experience.lending.core.simulation.queries.EligibilityResultDTO;
import com.firefly.experience.lending.core.simulation.queries.SimulationResultDTO;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for loan simulation and product eligibility evaluation operations.
 */
public interface SimulationService {

    Mono<SimulationResultDTO> createSimulation(CreateSimulationCommand command);

    Mono<SimulationResultDTO> getSimulation(UUID simulationId);

    Mono<EligibilityResultDTO> checkEligibility(CheckEligibilityCommand command);
}
