package com.firefly.experience.lending.core.simulation.services;

import com.firefly.experience.lending.core.simulation.commands.ConfigureSimulationCommand;
import com.firefly.experience.lending.core.simulation.queries.ConfiguredSimulationDTO;
import reactor.core.publisher.Mono;

/**
 * Orchestrates the simulation configuration flow:
 * (1) calculate via pricing-engine, then (2) persist via domain-lending-loan-origination.
 */
public interface SimulationConfigurationService {

    /**
     * Configures and persists a lending simulation in a single reactive flow.
     *
     * @param command validated input from the caller
     * @return a {@link Mono} emitting the persisted, fully-priced simulation
     */
    Mono<ConfiguredSimulationDTO> configure(ConfigureSimulationCommand command);
}
