package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.simulation.commands.CheckEligibilityCommand;
import com.firefly.experience.lending.core.simulation.commands.CreateSimulationCommand;
import com.firefly.experience.lending.core.simulation.queries.EligibilityResultDTO;
import com.firefly.experience.lending.core.simulation.queries.SimulationResultDTO;
import com.firefly.experience.lending.core.simulation.services.SimulationService;
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
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = SimulationController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class SimulationControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private SimulationService simulationService;

    // fireflyframework-web's GlobalExceptionHandler is component-scanned into this context
    // because ExpLendingApplication scans org.fireflyframework.web; mock its required deps.
    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    @Test
    void createSimulation_returns201WithBody() {
        var simulationId = UUID.randomUUID();
        var result = SimulationResultDTO.builder()
                .simulationId(simulationId)
                .annualRate(new BigDecimal("5.5"))
                .monthlyPayment(new BigDecimal("450.00"))
                .totalCost(new BigDecimal("10800.00"))
                .term(24)
                .productType("PERSONAL_LOAN")
                .build();

        when(simulationService.createSimulation(any(CreateSimulationCommand.class)))
                .thenReturn(Mono.just(result));

        webTestClient.post()
                .uri("/api/v1/experience/lending/simulations")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "amount": 10000,
                            "term": 24,
                            "productType": "PERSONAL_LOAN"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(SimulationResultDTO.class)
                .value(body -> {
                    assertThat(body.getSimulationId()).isEqualTo(simulationId);
                    assertThat(body.getAnnualRate()).isEqualByComparingTo("5.5");
                });
    }

    @Test
    void getSimulation_returns200WithBody() {
        var simulationId = UUID.randomUUID();
        var result = SimulationResultDTO.builder()
                .simulationId(simulationId)
                .annualRate(new BigDecimal("6.0"))
                .term(12)
                .productType("CAR_LOAN")
                .build();

        when(simulationService.getSimulation(eq(simulationId))).thenReturn(Mono.just(result));

        webTestClient.get()
                .uri("/api/v1/experience/lending/simulations/{id}", simulationId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(SimulationResultDTO.class)
                .value(body -> {
                    assertThat(body.getSimulationId()).isEqualTo(simulationId);
                    assertThat(body.getTerm()).isEqualTo(12);
                });
    }

    @Test
    void getSimulation_returns500WhenServiceFails() {
        var simulationId = UUID.randomUUID();
        when(simulationService.getSimulation(eq(simulationId)))
                .thenReturn(Mono.error(new RuntimeException("not found")));

        webTestClient.get()
                .uri("/api/v1/experience/lending/simulations/{id}", simulationId)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void checkEligibility_returns200WithBody() {
        var result = EligibilityResultDTO.builder()
                .evaluationId(UUID.randomUUID())
                .eligible(true)
                .maxAmount(new BigDecimal("50000"))
                .reasons(List.of())
                .build();

        when(simulationService.checkEligibility(any(CheckEligibilityCommand.class)))
                .thenReturn(Mono.just(result));

        webTestClient.post()
                .uri("/api/v1/experience/lending/eligibility")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "partyId": "550e8400-e29b-41d4-a716-446655440000",
                            "productId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                            "requestedAmount": 30000
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EligibilityResultDTO.class)
                .value(body -> {
                    assertThat(body.isEligible()).isTrue();
                    assertThat(body.getMaxAmount()).isEqualByComparingTo("50000");
                });
    }

    @Test
    void checkEligibility_returns200WithIneligibleResult() {
        var result = EligibilityResultDTO.builder()
                .evaluationId(UUID.randomUUID())
                .eligible(false)
                .reasons(List.of("Insufficient income"))
                .build();

        when(simulationService.checkEligibility(any(CheckEligibilityCommand.class)))
                .thenReturn(Mono.just(result));

        webTestClient.post()
                .uri("/api/v1/experience/lending/eligibility")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "partyId": "550e8400-e29b-41d4-a716-446655440000",
                            "productId": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
                            "requestedAmount": 999999
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EligibilityResultDTO.class)
                .value(body -> assertThat(body.isEligible()).isFalse());
    }
}
