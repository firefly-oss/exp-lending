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


package io.fireflysoftware.experience.lending.core.application.details.services;

import io.fireflysoftware.experience.lending.core.application.details.queries.ConditionDTO;
import io.fireflysoftware.experience.lending.core.application.details.queries.FeeDTO;
import io.fireflysoftware.experience.lending.core.application.details.queries.TaskDTO;
import io.fireflysoftware.experience.lending.core.application.details.queries.VerificationDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for querying and managing application detail sub-resources: conditions,
 * tasks, fees, and verifications attached to a loan application.
 */
public interface ApplicationDetailsService {

    Flux<ConditionDTO> getConditions(UUID applicationId);

    Flux<TaskDTO> getTasks(UUID applicationId);

    Mono<TaskDTO> completeTask(UUID applicationId, UUID taskId);

    Flux<FeeDTO> getFees(UUID applicationId);

    Flux<VerificationDTO> getVerifications(UUID applicationId);
}
