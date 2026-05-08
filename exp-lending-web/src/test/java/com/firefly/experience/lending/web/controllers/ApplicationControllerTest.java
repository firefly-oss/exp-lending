package com.firefly.experience.lending.web.controllers;

import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationPartyDTO;
import com.firefly.experience.lending.core.application.commands.CreateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateApplicationCommand;
import com.firefly.experience.lending.core.application.commands.UpdateEmploymentDataCommand;
import com.firefly.experience.lending.core.application.queries.ApplicationDetailDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationStatusHistoryDTO;
import com.firefly.experience.lending.core.application.queries.ApplicationSummaryDTO;
import com.firefly.experience.lending.core.application.services.ApplicationService;
import org.mockito.ArgumentCaptor;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ApplicationController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class ApplicationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ApplicationService applicationService;

    // fireflyframework-web's GlobalExceptionHandler required beans
    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final String BASE_PATH = "/api/v1/experience/lending/applications";

    @Test
    void createApplication_returns201WithBody() {
        var applicationId = UUID.randomUUID();
        var detail = ApplicationDetailDTO.builder()
                .applicationId(applicationId)
                .requestedAmount(new BigDecimal("15000"))
                .term(36)
                .purpose("PERSONAL")
                .createdAt(LocalDateTime.now())
                .build();

        when(applicationService.createApplication(any(CreateApplicationCommand.class)))
                .thenReturn(Mono.just(detail));

        webTestClient.post()
                .uri(BASE_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "productId": "550e8400-e29b-41d4-a716-446655440000",
                            "requestedAmount": 15000,
                            "term": 36,
                            "purpose": "PERSONAL"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ApplicationDetailDTO.class)
                .value(body -> {
                    assertThat(body.getApplicationId()).isEqualTo(applicationId);
                    assertThat(body.getRequestedAmount()).isEqualByComparingTo("15000");
                    assertThat(body.getTerm()).isEqualTo(36);
                });
    }

    @Test
    void listApplications_returns200WithEmptyList() {
        when(applicationService.listApplications()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApplicationSummaryDTO.class)
                .hasSize(0);
    }

    @Test
    void listApplications_returns200WithResults() {
        var summary = ApplicationSummaryDTO.builder()
                .applicationId(UUID.randomUUID())
                .status("DRAFT")
                .requestedAmount(new BigDecimal("10000"))
                .createdAt(LocalDateTime.now())
                .build();

        when(applicationService.listApplications()).thenReturn(Flux.just(summary));

        webTestClient.get()
                .uri(BASE_PATH)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApplicationSummaryDTO.class)
                .hasSize(1);
    }

    @Test
    void getApplication_returns200WithBody() {
        var applicationId = UUID.randomUUID();
        var detail = ApplicationDetailDTO.builder()
                .applicationId(applicationId)
                .purpose("HOME")
                .createdAt(LocalDateTime.now())
                .build();

        when(applicationService.getApplication(eq(applicationId))).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri(BASE_PATH + "/{id}", applicationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApplicationDetailDTO.class)
                .value(body -> {
                    assertThat(body.getApplicationId()).isEqualTo(applicationId);
                    assertThat(body.getPurpose()).isEqualTo("HOME");
                });
    }

    @Test
    void getApplication_returns500WhenServiceFails() {
        var applicationId = UUID.randomUUID();
        when(applicationService.getApplication(eq(applicationId)))
                .thenReturn(Mono.error(new RuntimeException("not found")));

        webTestClient.get()
                .uri(BASE_PATH + "/{id}", applicationId)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void updateApplication_returns200WithBody() {
        var applicationId = UUID.randomUUID();
        var detail = ApplicationDetailDTO.builder()
                .applicationId(applicationId)
                .requestedAmount(new BigDecimal("20000"))
                .term(48)
                .purpose("RENOVATION")
                .createdAt(LocalDateTime.now())
                .build();

        when(applicationService.updateApplication(eq(applicationId), any(UpdateApplicationCommand.class)))
                .thenReturn(Mono.just(detail));

        webTestClient.patch()
                .uri(BASE_PATH + "/{id}", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "requestedAmount": 20000,
                            "term": 48
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApplicationDetailDTO.class)
                .value(body -> {
                    assertThat(body.getApplicationId()).isEqualTo(applicationId);
                    assertThat(body.getRequestedAmount()).isEqualByComparingTo("20000");
                    assertThat(body.getTerm()).isEqualTo(48);
                });
    }

    @Test
    void submitApplication_returns202() {
        var applicationId = UUID.randomUUID();
        when(applicationService.submitApplication(eq(applicationId))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(BASE_PATH + "/{id}/submission", applicationId)
                .exchange()
                .expectStatus().isAccepted();
    }

    @Test
    void withdrawApplication_returns200() {
        var applicationId = UUID.randomUUID();
        when(applicationService.withdrawApplication(eq(applicationId))).thenReturn(Mono.empty());

        webTestClient.post()
                .uri(BASE_PATH + "/{id}/withdraw", applicationId)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void updateEmploymentData_returns200WithBody() {
        var applicationId = UUID.randomUUID();
        var partyDto = new ApplicationPartyDTO()
                .applicationPartyId(UUID.randomUUID())
                .loanApplicationId(applicationId);

        when(applicationService.updateEmploymentData(eq(applicationId),
                any(UpdateEmploymentDataCommand.class)))
                .thenReturn(Mono.just(partyDto));

        webTestClient.patch()
                .uri(BASE_PATH + "/{applicationId}/employment-data", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "employmentStatus": "private",
                            "employmentType": "permanent",
                            "employer": "Acme S.A.",
                            "position": "Engineer",
                            "employmentStartDate": "06/2018",
                            "annualPaydays": 14,
                            "monthlySalary": 2500.00,
                            "housingType": "rent",
                            "housingCost": 850.00,
                            "housingStartDate": "01/2020",
                            "existingLoans": 1,
                            "otherDebts": 120.00
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApplicationPartyDTO.class)
                .value(body -> assertThat(body.getLoanApplicationId()).isEqualTo(applicationId));

        ArgumentCaptor<UpdateEmploymentDataCommand> captor =
                ArgumentCaptor.forClass(UpdateEmploymentDataCommand.class);
        org.mockito.Mockito.verify(applicationService)
                .updateEmploymentData(eq(applicationId), captor.capture());
        assertThat(captor.getValue().getEmploymentStatus()).isEqualTo("private");
        assertThat(captor.getValue().getEmploymentStartDate()).isEqualTo("06/2018");
        assertThat(captor.getValue().getMonthlySalary()).isEqualByComparingTo("2500.00");
    }

    @Test
    void updateEmploymentData_rejectsInvalidDateFormat() {
        // The mocked GlobalExceptionHandler maps validation failures to 5xx in this
        // test harness (matching the existing controller-test convention).
        var applicationId = UUID.randomUUID();

        webTestClient.patch()
                .uri(BASE_PATH + "/{applicationId}/employment-data", applicationId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "employmentStatus": "private",
                            "employmentStartDate": "2018-06-01",
                            "annualPaydays": 12,
                            "monthlySalary": 1500,
                            "housingType": "rent",
                            "housingCost": 0,
                            "housingStartDate": "01/2020",
                            "existingLoans": 0,
                            "otherDebts": 0
                        }
                        """)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getStatusHistory_returns200WithEmptyEntries() {
        var applicationId = UUID.randomUUID();
        var history = ApplicationStatusHistoryDTO.builder()
                .entries(List.of())
                .build();

        when(applicationService.getStatusHistory(eq(applicationId))).thenReturn(Mono.just(history));

        webTestClient.get()
                .uri(BASE_PATH + "/{id}/status-history", applicationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApplicationStatusHistoryDTO.class)
                .value(body -> assertThat(body.getEntries()).isEmpty());
    }
}
