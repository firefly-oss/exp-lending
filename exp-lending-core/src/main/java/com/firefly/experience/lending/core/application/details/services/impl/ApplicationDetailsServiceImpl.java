package com.firefly.experience.lending.core.application.details.services.impl;

import com.firefly.core.lending.origination.sdk.api.ApplicationConditionApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationFeeApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationTaskApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationVerificationApi;
import com.firefly.experience.lending.core.application.details.queries.ConditionDTO;
import com.firefly.experience.lending.core.application.details.queries.FeeDTO;
import com.firefly.experience.lending.core.application.details.queries.TaskDTO;
import com.firefly.experience.lending.core.application.details.queries.VerificationDTO;
import com.firefly.experience.lending.core.application.details.services.ApplicationDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link ApplicationDetailsService}, delegating to the
 * Loan Origination SDK APIs for conditions, tasks, fees, and verifications.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link ApplicationConditionApi},
 * {@link ApplicationFeeApi}, {@link ApplicationTaskApi}, or {@link ApplicationVerificationApi};
 * direct core-lending-origination-sdk usage is temporary until the domain layer surfaces
 * these endpoints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationDetailsServiceImpl implements ApplicationDetailsService {

    private final ApplicationConditionApi applicationConditionApi;
    private final ApplicationFeeApi applicationFeeApi;
    private final ApplicationTaskApi applicationTaskApi;
    private final ApplicationVerificationApi applicationVerificationApi;

    @Override
    public Flux<ConditionDTO> getConditions(UUID applicationId) {
        log.debug("Listing conditions for applicationId={}", applicationId);
        return applicationConditionApi
                .findAllConditions(applicationId, null, null, null, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToConditionDTO);
    }

    @Override
    public Flux<TaskDTO> getTasks(UUID applicationId) {
        log.debug("Listing tasks for applicationId={}", applicationId);
        return applicationTaskApi
                .findAllTasks(applicationId, null, null, null, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToTaskDTO);
    }

    @Override
    public Mono<TaskDTO> completeTask(UUID applicationId, UUID taskId) {
        log.debug("Completing taskId={} for applicationId={}", taskId, applicationId);
        return applicationTaskApi.getTask(applicationId, taskId, UUID.randomUUID().toString())
                .flatMap(existing -> {
                    // Build a fresh update DTO with only the changed fields to avoid live-object mutation.
                    // ARCH-EXCEPTION: core-lending-origination-sdk provides no dedicated UpdateTaskCommand;
                    // ApplicationTaskDTO is reused as the update body with only taskStatus and completedAt set.
                    var update = new com.firefly.core.lending.origination.sdk.model.ApplicationTaskDTO()
                            .taskStatus("COMPLETED")
                            .completedAt(LocalDateTime.now());
                    return applicationTaskApi.updateTask(
                            applicationId, taskId, update, UUID.randomUUID().toString());
                })
                .map(this::mapToTaskDTO);
    }

    @Override
    public Flux<FeeDTO> getFees(UUID applicationId) {
        log.debug("Listing fees for applicationId={}", applicationId);
        return applicationFeeApi
                .findAllFees(applicationId, null, null, null, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToFeeDTO);
    }

    @Override
    public Flux<VerificationDTO> getVerifications(UUID applicationId) {
        log.debug("Listing verifications for applicationId={}", applicationId);
        return applicationVerificationApi
                .findAllVerifications(applicationId, null, null, null, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToVerificationDTO);
    }

    private ConditionDTO mapToConditionDTO(
            com.firefly.core.lending.origination.sdk.model.ApplicationConditionDTO src) {
        return ConditionDTO.builder()
                .conditionId(src.getConditionId())
                .description(src.getDescription())
                .status(src.getStatus())
                .type(src.getConditionType())
                .build();
    }

    private TaskDTO mapToTaskDTO(
            com.firefly.core.lending.origination.sdk.model.ApplicationTaskDTO src) {
        return TaskDTO.builder()
                .taskId(src.getTaskId())
                .description(src.getDescription())
                .status(src.getTaskStatus())
                .dueDate(src.getDueDate() != null ? src.getDueDate().toLocalDate() : null)
                .build();
    }

    private FeeDTO mapToFeeDTO(
            com.firefly.core.lending.origination.sdk.model.ApplicationFeeDTO src) {
        // MVP: the core SDK does not expose a currency field; defaulting to EUR.
        return FeeDTO.builder()
                .feeId(src.getFeeId())
                .feeName(src.getFeeName())
                .amount(src.getFeeAmount())
                .currency("EUR")
                .type(src.getFeeType())
                .build();
    }

    private VerificationDTO mapToVerificationDTO(
            com.firefly.core.lending.origination.sdk.model.ApplicationVerificationDTO src) {
        return VerificationDTO.builder()
                .verificationId(src.getVerificationId())
                .type(src.getVerificationType())
                .status(src.getVerificationStatus())
                .completedAt(src.getVerifiedAt())
                .build();
    }
}
