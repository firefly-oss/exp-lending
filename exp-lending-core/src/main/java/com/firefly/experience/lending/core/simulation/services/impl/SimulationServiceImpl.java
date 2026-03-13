package com.firefly.experience.lending.core.simulation.services.impl;

import com.firefly.domain.product.pricing.sdk.api.EligibilityApi;
import com.firefly.domain.product.pricing.sdk.api.PricingApi;
import com.firefly.domain.product.pricing.sdk.model.RegisterProductPricingCommand;
import com.firefly.domain.product.pricing.sdk.model.UpdateProductPricingCommand;
import com.firefly.experience.lending.core.simulation.commands.CheckEligibilityCommand;
import com.firefly.experience.lending.core.simulation.commands.CreateSimulationCommand;
import com.firefly.experience.lending.core.simulation.queries.EligibilityResultDTO;
import com.firefly.experience.lending.core.simulation.queries.SimulationResultDTO;
import com.firefly.experience.lending.core.simulation.services.SimulationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link SimulationService}, delegating to the Product Pricing SDK
 * for loan simulation and the Product Pricing Eligibility API for eligibility evaluation.
 */
@Slf4j
@Service
public class SimulationServiceImpl implements SimulationService {

    private final PricingApi pricingApi;
    private final EligibilityApi eligibilityApi;

    public SimulationServiceImpl(
            @Qualifier("productPricingApi") PricingApi pricingApi,
            @Qualifier("productPricingEligibilityApi") EligibilityApi eligibilityApi) {
        this.pricingApi = pricingApi;
        this.eligibilityApi = eligibilityApi;
    }

    @Override
    public Mono<SimulationResultDTO> createSimulation(CreateSimulationCommand command) {
        log.debug("Creating simulation for productId={}", command.getProductId());

        var pricingCmd = new RegisterProductPricingCommand()
                .productId(command.getProductId())
                .configType(RegisterProductPricingCommand.ConfigTypeEnum.PRICING)
                .configKey(command.getProductType())
                .configValue(command.getAmount() != null ? command.getAmount().toPlainString() : null);

        return pricingApi.registerPricing(pricingCmd)
                .map(response -> mapToSimulationResult(response, command.getTerm(), command.getProductType()));
    }

    @Override
    public Mono<SimulationResultDTO> getSimulation(UUID simulationId) {
        log.debug("Getting simulation simulationId={}", simulationId);

        // MVP: domain-product-pricing has no GET by ID; amendPricing with no changes
        // retrieves the current pricing record. Replace when upstream adds a GET endpoint.
        return pricingApi.amendPricing(simulationId, new UpdateProductPricingCommand())
                .map(response -> mapToSimulationResult(response, null, null));
    }

    @Override
    public Mono<EligibilityResultDTO> checkEligibility(CheckEligibilityCommand command) {
        log.debug("Checking eligibility for productId={}", command.getProductId());

        Map<String, Object> body = Map.of(
                "partyId", command.getPartyId().toString(),
                "requestedAmount", command.getRequestedAmount()
        );

        return eligibilityApi.evaluateEligibility(command.getProductId(), body)
                .map(this::mapToEligibilityResult);
    }

    private SimulationResultDTO mapToSimulationResult(Object response, Integer term, String productType) {
        Map<?, ?> map = response instanceof Map<?, ?> m ? m : Map.of();
        return SimulationResultDTO.builder()
                .simulationId(extractUuid(map, "productConfigurationId"))
                .annualRate(extractBigDecimal(map, "configValue"))
                .term(term)
                .productType(productType != null ? productType : extractString(map, "configKey"))
                .build();
    }

    private EligibilityResultDTO mapToEligibilityResult(Object response) {
        Map<?, ?> map = response instanceof Map<?, ?> m ? m : Map.of();
        return EligibilityResultDTO.builder()
                .evaluationId(extractUuid(map, "eligibilityId"))
                .eligible(Boolean.TRUE.equals(map.get("eligible")))
                .maxAmount(extractBigDecimal(map, "maxAmount"))
                .reasons(List.of())
                .build();
    }

    private UUID extractUuid(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof String s) return UUID.fromString(s);
        if (value instanceof UUID u) return u;
        return null;
    }

    private BigDecimal extractBigDecimal(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (value instanceof String s) return new BigDecimal(s);
        return null;
    }

    private String extractString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof String s ? s : null;
    }
}
