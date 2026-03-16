package com.firefly.experience.lending.core.application.details.services.impl;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationConditionDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationFeeDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationTaskDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationVerificationDTO;
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
 * domain Loan Origination SDK's {@code LoanOriginationApi} for conditions, tasks,
 * fees, and verifications.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationDetailsServiceImpl implements ApplicationDetailsService {

    private final LoanOriginationApi loanOriginationApi;

    @Override
    public Flux<ConditionDTO> getConditions(UUID applicationId) {
        log.debug("Listing conditions for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationConditions(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToConditionDTO);
    }

    @Override
    public Flux<TaskDTO> getTasks(UUID applicationId) {
        log.debug("Listing tasks for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationTasks(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToTaskDTO);
    }

    @Override
    public Mono<TaskDTO> completeTask(UUID applicationId, UUID taskId) {
        log.debug("Completing taskId={} for applicationId={}", taskId, applicationId);
        // MVP: the domain SDK does not expose a dedicated update-task endpoint.
        // Returns the existing task with COMPLETED status. Replace when the domain layer
        // surfaces this endpoint.
        return loanOriginationApi
                .getApplicationTasks(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .filter(t -> taskId.equals(t.getTaskId()))
                .next()
                .map(existing -> TaskDTO.builder()
                        .taskId(existing.getTaskId())
                        .description(existing.getDescription())
                        .status("COMPLETED")
                        .dueDate(existing.getDueDate() != null ? existing.getDueDate().toLocalDate() : null)
                        .build());
    }

    @Override
    public Flux<FeeDTO> getFees(UUID applicationId) {
        log.debug("Listing fees for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationFees(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToFeeDTO);
    }

    @Override
    public Flux<VerificationDTO> getVerifications(UUID applicationId) {
        log.debug("Listing verifications for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationVerifications(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToVerificationDTO);
    }

    private ConditionDTO mapToConditionDTO(ApplicationConditionDTO src) {
        return ConditionDTO.builder()
                .conditionId(src.getConditionId())
                .description(src.getDescription())
                .status(src.getStatus())
                .type(src.getConditionType())
                .build();
    }

    private TaskDTO mapToTaskDTO(ApplicationTaskDTO src) {
        return TaskDTO.builder()
                .taskId(src.getTaskId())
                .description(src.getDescription())
                .status(src.getTaskStatus())
                .dueDate(src.getDueDate() != null ? src.getDueDate().toLocalDate() : null)
                .build();
    }

    private FeeDTO mapToFeeDTO(ApplicationFeeDTO src) {
        // MVP: the domain SDK does not expose a currency field; defaulting to EUR.
        return FeeDTO.builder()
                .feeId(src.getFeeId())
                .feeName(src.getFeeName())
                .amount(src.getFeeAmount())
                .currency("EUR")
                .type(src.getFeeType())
                .build();
    }

    private VerificationDTO mapToVerificationDTO(ApplicationVerificationDTO src) {
        return VerificationDTO.builder()
                .verificationId(src.getVerificationId())
                .type(src.getVerificationType())
                .status(src.getVerificationStatus())
                .completedAt(src.getVerifiedAt())
                .build();
    }
}
