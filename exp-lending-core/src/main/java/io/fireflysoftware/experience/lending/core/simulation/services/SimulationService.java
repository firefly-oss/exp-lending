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


package io.fireflysoftware.experience.lending.core.simulation.services;

import io.fireflysoftware.experience.lending.core.simulation.commands.CheckEligibilityCommand;
import io.fireflysoftware.experience.lending.core.simulation.commands.CreateSimulationCommand;
import io.fireflysoftware.experience.lending.core.simulation.queries.EligibilityResultDTO;
import io.fireflysoftware.experience.lending.core.simulation.queries.SimulationResultDTO;
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
