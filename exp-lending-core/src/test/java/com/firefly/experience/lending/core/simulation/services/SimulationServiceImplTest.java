package com.firefly.experience.lending.core.simulation.services;

import com.firefly.domain.product.pricing.sdk.api.EligibilityApi;
import com.firefly.domain.product.pricing.sdk.api.PricingApi;
import com.firefly.domain.product.pricing.sdk.model.RegisterProductPricingCommand;
import com.firefly.domain.product.pricing.sdk.model.UpdateProductPricingCommand;
import com.firefly.experience.lending.core.simulation.commands.CheckEligibilityCommand;
import com.firefly.experience.lending.core.simulation.commands.CreateSimulationCommand;
import com.firefly.experience.lending.core.simulation.queries.EligibilityResultDTO;
import com.firefly.experience.lending.core.simulation.queries.SimulationResultDTO;
import com.firefly.experience.lending.core.simulation.services.impl.SimulationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SimulationServiceImplTest {

    @Mock
    private PricingApi pricingApi;

    @Mock
    private EligibilityApi eligibilityApi;

    private SimulationServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SimulationServiceImpl(pricingApi, eligibilityApi);
    }

    @Test
    void createSimulation_delegatesToPricingApi_andMapsResult() {
        var productId = UUID.randomUUID();
        var pricingId = UUID.randomUUID();
        Map<String, Object> apiResponse = Map.of(
                "productConfigurationId", pricingId.toString(),
                "configValue", "5.5"
        );

        var command = new CreateSimulationCommand();
        command.setAmount(new BigDecimal("10000"));
        command.setTerm(24);
        command.setProductType("PERSONAL_LOAN");
        command.setProductId(productId);

        when(pricingApi.registerPricing(any(RegisterProductPricingCommand.class), any()))
                .thenReturn(Mono.just(apiResponse));

        StepVerifier.create(service.createSimulation(command))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getSimulationId()).isEqualTo(pricingId);
                    assertThat(result.getAnnualRate()).isEqualByComparingTo("5.5");
                    assertThat(result.getTerm()).isEqualTo(24);
                    assertThat(result.getProductType()).isEqualTo("PERSONAL_LOAN");
                })
                .verifyComplete();
    }

    @Test
    void createSimulation_propagatesUpstreamError() {
        var command = new CreateSimulationCommand();
        command.setProductId(UUID.randomUUID());

        when(pricingApi.registerPricing(any(RegisterProductPricingCommand.class), any()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        StepVerifier.create(service.createSimulation(command))
                .expectErrorMessage("upstream error")
                .verify();
    }

    @Test
    void getSimulation_delegatesToAmendPricingApi_andMapsResult() {
        var simulationId = UUID.randomUUID();
        Map<String, Object> apiResponse = Map.of(
                "productConfigurationId", simulationId.toString(),
                "configValue", "7.25",
                "configKey", "MORTGAGE"
        );

        when(pricingApi.amendPricing(eq(simulationId), any(UpdateProductPricingCommand.class), any()))
                .thenReturn(Mono.just(apiResponse));

        StepVerifier.create(service.getSimulation(simulationId))
                .assertNext(result -> {
                    assertThat(result.getSimulationId()).isEqualTo(simulationId);
                    assertThat(result.getAnnualRate()).isEqualByComparingTo("7.25");
                    assertThat(result.getProductType()).isEqualTo("MORTGAGE");
                })
                .verifyComplete();
    }

    @Test
    void checkEligibility_delegatesToEligibilityApi_andMapsResult() {
        var productId = UUID.randomUUID();
        var partyId = UUID.randomUUID();
        var evaluationId = UUID.randomUUID();
        Map<String, Object> apiResponse = Map.of(
                "eligibilityId", evaluationId.toString(),
                "eligible", true,
                "maxAmount", 50000.0
        );

        var command = new CheckEligibilityCommand();
        command.setPartyId(partyId);
        command.setProductId(productId);
        command.setRequestedAmount(new BigDecimal("30000"));

        when(eligibilityApi.evaluateEligibility(eq(productId), any(), any()))
                .thenReturn(Mono.just(apiResponse));

        StepVerifier.create(service.checkEligibility(command))
                .assertNext(result -> {
                    assertThat(result.getEvaluationId()).isEqualTo(evaluationId);
                    assertThat(result.isEligible()).isTrue();
                    assertThat(result.getMaxAmount()).isEqualByComparingTo("50000.0");
                    assertThat(result.getReasons()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void checkEligibility_mapsIneligibleResponse() {
        var productId = UUID.randomUUID();
        Map<String, Object> apiResponse = Map.of(
                "eligibilityId", UUID.randomUUID().toString(),
                "eligible", false
        );

        var command = new CheckEligibilityCommand();
        command.setPartyId(UUID.randomUUID());
        command.setProductId(productId);
        command.setRequestedAmount(new BigDecimal("999999"));

        when(eligibilityApi.evaluateEligibility(eq(productId), any(), any()))
                .thenReturn(Mono.just(apiResponse));

        StepVerifier.create(service.checkEligibility(command))
                .assertNext(result -> assertThat(result.isEligible()).isFalse())
                .verifyComplete();
    }
}
