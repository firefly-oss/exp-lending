package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.details.queries.ConditionDTO;
import com.firefly.experience.lending.core.application.details.queries.FeeDTO;
import com.firefly.experience.lending.core.application.details.queries.TaskDTO;
import com.firefly.experience.lending.core.application.details.queries.VerificationDTO;
import com.firefly.experience.lending.core.application.details.services.ApplicationDetailsService;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ApplicationDetailsController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class ApplicationDetailsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ApplicationDetailsService applicationDetailsService;

    // fireflyframework-web's GlobalExceptionHandler required beans
    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID TASK_ID = UUID.randomUUID();
    private static final String BASE_PATH = "/api/v1/experience/lending/applications/{id}";

    // --- GET /conditions ---

    @Test
    void getConditions_returns200WithList() {
        var condition = ConditionDTO.builder()
                .conditionId(UUID.randomUUID())
                .type("INCOME_PROOF")
                .description("Provide payslips")
                .status("PENDING")
                .build();

        when(applicationDetailsService.getConditions(eq(APPLICATION_ID)))
                .thenReturn(Flux.just(condition));

        webTestClient.get()
                .uri(BASE_PATH + "/conditions", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(ConditionDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getType()).isEqualTo("INCOME_PROOF");
                    assertThat(list.get(0).getStatus()).isEqualTo("PENDING");
                });
    }

    @Test
    void getConditions_returns200WithEmptyList() {
        when(applicationDetailsService.getConditions(eq(APPLICATION_ID)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH + "/conditions", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ConditionDTO.class)
                .hasSize(0);
    }

    // --- GET /tasks ---

    @Test
    void getTasks_returns200WithList() {
        var task = TaskDTO.builder()
                .taskId(TASK_ID)
                .description("Submit proof of income")
                .status("PENDING")
                .dueDate(LocalDate.of(2026, 4, 1))
                .build();

        when(applicationDetailsService.getTasks(eq(APPLICATION_ID)))
                .thenReturn(Flux.just(task));

        webTestClient.get()
                .uri(BASE_PATH + "/tasks", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getTaskId()).isEqualTo(TASK_ID);
                    assertThat(list.get(0).getStatus()).isEqualTo("PENDING");
                });
    }

    @Test
    void getTasks_returns200WithEmptyList() {
        when(applicationDetailsService.getTasks(eq(APPLICATION_ID)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH + "/tasks", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(TaskDTO.class)
                .hasSize(0);
    }

    // --- PATCH /tasks/{taskId} ---

    @Test
    void completeTask_returns200WithUpdatedTask() {
        var completed = TaskDTO.builder()
                .taskId(TASK_ID)
                .description("Submit proof of income")
                .status("COMPLETED")
                .dueDate(LocalDate.of(2026, 4, 1))
                .build();

        when(applicationDetailsService.completeTask(eq(APPLICATION_ID), eq(TASK_ID)))
                .thenReturn(Mono.just(completed));

        webTestClient.patch()
                .uri(BASE_PATH + "/tasks/{taskId}", APPLICATION_ID, TASK_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TaskDTO.class)
                .value(body -> {
                    assertThat(body.getTaskId()).isEqualTo(TASK_ID);
                    assertThat(body.getStatus()).isEqualTo("COMPLETED");
                });
    }

    @Test
    void completeTask_returns500WhenServiceFails() {
        when(applicationDetailsService.completeTask(eq(APPLICATION_ID), eq(TASK_ID)))
                .thenReturn(Mono.error(new RuntimeException("task not found")));

        webTestClient.patch()
                .uri(BASE_PATH + "/tasks/{taskId}", APPLICATION_ID, TASK_ID)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // --- GET /fees ---

    @Test
    void getFees_returns200WithList() {
        var fee = FeeDTO.builder()
                .feeId(UUID.randomUUID())
                .feeName("Origination Fee")
                .amount(BigDecimal.valueOf(200.00))
                .currency("EUR")
                .type("ORIGINATION")
                .build();

        when(applicationDetailsService.getFees(eq(APPLICATION_ID)))
                .thenReturn(Flux.just(fee));

        webTestClient.get()
                .uri(BASE_PATH + "/fees", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FeeDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getFeeName()).isEqualTo("Origination Fee");
                    assertThat(list.get(0).getCurrency()).isEqualTo("EUR");
                });
    }

    @Test
    void getFees_returns200WithEmptyList() {
        when(applicationDetailsService.getFees(eq(APPLICATION_ID)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH + "/fees", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(FeeDTO.class)
                .hasSize(0);
    }

    // --- GET /verifications ---

    @Test
    void getVerifications_returns200WithList() {
        var verif = VerificationDTO.builder()
                .verificationId(UUID.randomUUID())
                .type("IDENTITY")
                .status("VERIFIED")
                .completedAt(LocalDateTime.of(2026, 3, 10, 9, 30))
                .build();

        when(applicationDetailsService.getVerifications(eq(APPLICATION_ID)))
                .thenReturn(Flux.just(verif));

        webTestClient.get()
                .uri(BASE_PATH + "/verifications", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VerificationDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getType()).isEqualTo("IDENTITY");
                    assertThat(list.get(0).getStatus()).isEqualTo("VERIFIED");
                });
    }

    @Test
    void getVerifications_returns200WithEmptyList() {
        when(applicationDetailsService.getVerifications(eq(APPLICATION_ID)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH + "/verifications", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(VerificationDTO.class)
                .hasSize(0);
    }
}
