package com.firefly.experience.lending.core.application.details.services;

import com.firefly.experience.lending.core.application.details.queries.ConditionDTO;
import com.firefly.experience.lending.core.application.details.queries.FeeDTO;
import com.firefly.experience.lending.core.application.details.queries.TaskDTO;
import com.firefly.experience.lending.core.application.details.queries.VerificationDTO;
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
