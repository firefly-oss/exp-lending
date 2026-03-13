package com.firefly.experience.lending.core.application.details.services;

import com.firefly.core.lending.origination.sdk.api.ApplicationConditionApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationFeeApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationTaskApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationVerificationApi;
import com.firefly.core.lending.origination.sdk.model.ApplicationConditionDTO;
import com.firefly.core.lending.origination.sdk.model.ApplicationFeeDTO;
import com.firefly.core.lending.origination.sdk.model.ApplicationTaskDTO;
import com.firefly.core.lending.origination.sdk.model.ApplicationVerificationDTO;
import com.firefly.core.lending.origination.sdk.model.PaginationResponseApplicationConditionDTO;
import com.firefly.core.lending.origination.sdk.model.PaginationResponseApplicationFeeDTO;
import com.firefly.core.lending.origination.sdk.model.PaginationResponseApplicationTaskDTO;
import com.firefly.core.lending.origination.sdk.model.PaginationResponseApplicationVerificationDTO;
import com.firefly.experience.lending.core.application.details.services.impl.ApplicationDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDetailsServiceImplTest {

    @Mock
    private ApplicationConditionApi applicationConditionApi;

    @Mock
    private ApplicationFeeApi applicationFeeApi;

    @Mock
    private ApplicationTaskApi applicationTaskApi;

    @Mock
    private ApplicationVerificationApi applicationVerificationApi;

    @InjectMocks
    private ApplicationDetailsServiceImpl service;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID CONDITION_ID = UUID.randomUUID();
    private static final UUID TASK_ID = UUID.randomUUID();
    private static final UUID FEE_ID = UUID.randomUUID();
    private static final UUID VERIFICATION_ID = UUID.randomUUID();

    // --- getConditions ---

    @Test
    void getConditions_returnsMappedConditions() {
        var sdkCondition = new ApplicationConditionDTO(CONDITION_ID)
                .loanApplicationId(APPLICATION_ID)
                .conditionType("INCOME_PROOF")
                .description("Provide last 3 payslips")
                .status("PENDING");

        var page = new PaginationResponseApplicationConditionDTO().content(List.of(sdkCondition));

        when(applicationConditionApi.findAllConditions(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getConditions(APPLICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto.getConditionId()).isEqualTo(CONDITION_ID);
                    assertThat(dto.getType()).isEqualTo("INCOME_PROOF");
                    assertThat(dto.getDescription()).isEqualTo("Provide last 3 payslips");
                    assertThat(dto.getStatus()).isEqualTo("PENDING");
                })
                .verifyComplete();
    }

    @Test
    void getConditions_returnsEmptyWhenPageContentIsNull() {
        var page = new PaginationResponseApplicationConditionDTO().content(null);

        when(applicationConditionApi.findAllConditions(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getConditions(APPLICATION_ID))
                .verifyComplete();
    }

    @Test
    void getConditions_propagatesUpstreamError() {
        when(applicationConditionApi.findAllConditions(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        StepVerifier.create(service.getConditions(APPLICATION_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- getTasks ---

    @Test
    void getTasks_returnsMappedTasks() {
        var dueDate = LocalDateTime.of(2026, 4, 1, 12, 0);
        var sdkTask = new ApplicationTaskDTO(TASK_ID)
                .loanApplicationId(APPLICATION_ID)
                .taskType("DOCUMENT_COLLECTION")
                .taskStatus("PENDING")
                .description("Collect income documents")
                .dueDate(dueDate);

        var page = new PaginationResponseApplicationTaskDTO().content(List.of(sdkTask));

        when(applicationTaskApi.findAllTasks(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getTasks(APPLICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto.getTaskId()).isEqualTo(TASK_ID);
                    assertThat(dto.getStatus()).isEqualTo("PENDING");
                    assertThat(dto.getDescription()).isEqualTo("Collect income documents");
                    assertThat(dto.getDueDate()).isEqualTo(dueDate.toLocalDate());
                })
                .verifyComplete();
    }

    @Test
    void getTasks_returnsEmptyWhenPageContentIsNull() {
        var page = new PaginationResponseApplicationTaskDTO().content(null);

        when(applicationTaskApi.findAllTasks(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getTasks(APPLICATION_ID))
                .verifyComplete();
    }

    @Test
    void getTasks_propagatesUpstreamError() {
        when(applicationTaskApi.findAllTasks(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.error(new RuntimeException("tasks error")));

        StepVerifier.create(service.getTasks(APPLICATION_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- completeTask ---

    @Test
    void completeTask_fetchesThenUpdatesWithCompletedStatus() {
        var existing = new ApplicationTaskDTO(TASK_ID)
                .loanApplicationId(APPLICATION_ID)
                .taskType("DOCUMENT_COLLECTION")
                .taskStatus("PENDING")
                .description("Collect income documents");

        var updated = new ApplicationTaskDTO(TASK_ID)
                .loanApplicationId(APPLICATION_ID)
                .taskType("DOCUMENT_COLLECTION")
                .taskStatus("COMPLETED")
                .description("Collect income documents");

        when(applicationTaskApi.getTask(eq(APPLICATION_ID), eq(TASK_ID), isNull()))
                .thenReturn(Mono.just(existing));

        when(applicationTaskApi.updateTask(
                eq(APPLICATION_ID), eq(TASK_ID), any(ApplicationTaskDTO.class), any(String.class)))
                .thenReturn(Mono.just(updated));

        StepVerifier.create(service.completeTask(APPLICATION_ID, TASK_ID))
                .assertNext(dto -> {
                    assertThat(dto.getTaskId()).isEqualTo(TASK_ID);
                    assertThat(dto.getStatus()).isEqualTo("COMPLETED");
                })
                .verifyComplete();
    }

    @Test
    void completeTask_propagatesGetTaskError() {
        when(applicationTaskApi.getTask(eq(APPLICATION_ID), eq(TASK_ID), isNull()))
                .thenReturn(Mono.error(new RuntimeException("not found")));

        StepVerifier.create(service.completeTask(APPLICATION_ID, TASK_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void completeTask_propagatesUpdateTaskError() {
        var existing = new ApplicationTaskDTO(TASK_ID)
                .loanApplicationId(APPLICATION_ID)
                .taskType("DOCUMENT_COLLECTION")
                .taskStatus("PENDING");

        when(applicationTaskApi.getTask(eq(APPLICATION_ID), eq(TASK_ID), isNull()))
                .thenReturn(Mono.just(existing));

        when(applicationTaskApi.updateTask(
                eq(APPLICATION_ID), eq(TASK_ID), any(ApplicationTaskDTO.class), any(String.class)))
                .thenReturn(Mono.error(new RuntimeException("update failed")));

        StepVerifier.create(service.completeTask(APPLICATION_ID, TASK_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- getFees ---

    @Test
    void getFees_returnsMappedFees() {
        var sdkFee = new ApplicationFeeDTO(FEE_ID)
                .loanApplicationId(APPLICATION_ID)
                .feeType("ORIGINATION")
                .feeName("Origination Fee")
                .feeAmount(BigDecimal.valueOf(250.00));

        var page = new PaginationResponseApplicationFeeDTO().content(List.of(sdkFee));

        when(applicationFeeApi.findAllFees(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getFees(APPLICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto.getFeeId()).isEqualTo(FEE_ID);
                    assertThat(dto.getFeeName()).isEqualTo("Origination Fee");
                    assertThat(dto.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(250.00));
                    assertThat(dto.getCurrency()).isEqualTo("EUR");
                    assertThat(dto.getType()).isEqualTo("ORIGINATION");
                })
                .verifyComplete();
    }

    @Test
    void getFees_returnsEmptyWhenPageContentIsNull() {
        var page = new PaginationResponseApplicationFeeDTO().content(null);

        when(applicationFeeApi.findAllFees(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getFees(APPLICATION_ID))
                .verifyComplete();
    }

    @Test
    void getFees_propagatesUpstreamError() {
        when(applicationFeeApi.findAllFees(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.error(new RuntimeException("fees error")));

        StepVerifier.create(service.getFees(APPLICATION_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    // --- getVerifications ---

    @Test
    void getVerifications_returnsMappedVerifications() {
        var completedAt = LocalDateTime.of(2026, 3, 10, 9, 30);
        var sdkVerif = new ApplicationVerificationDTO(VERIFICATION_ID)
                .loanApplicationId(APPLICATION_ID)
                .verificationType("IDENTITY")
                .verificationStatus("VERIFIED")
                .verifiedAt(completedAt);

        var page = new PaginationResponseApplicationVerificationDTO().content(List.of(sdkVerif));

        when(applicationVerificationApi.findAllVerifications(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getVerifications(APPLICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto.getVerificationId()).isEqualTo(VERIFICATION_ID);
                    assertThat(dto.getType()).isEqualTo("IDENTITY");
                    assertThat(dto.getStatus()).isEqualTo("VERIFIED");
                    assertThat(dto.getCompletedAt()).isEqualTo(completedAt);
                })
                .verifyComplete();
    }

    @Test
    void getVerifications_returnsEmptyWhenPageContentIsNull() {
        var page = new PaginationResponseApplicationVerificationDTO().content(null);

        when(applicationVerificationApi.findAllVerifications(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.getVerifications(APPLICATION_ID))
                .verifyComplete();
    }

    @Test
    void getVerifications_propagatesUpstreamError() {
        when(applicationVerificationApi.findAllVerifications(
                eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.error(new RuntimeException("verifications error")));

        StepVerifier.create(service.getVerifications(APPLICATION_ID))
                .expectError(RuntimeException.class)
                .verify();
    }
}
