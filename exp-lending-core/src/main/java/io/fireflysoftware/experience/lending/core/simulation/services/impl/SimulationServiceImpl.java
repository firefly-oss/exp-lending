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


package io.fireflysoftware.experience.lending.core.simulation.services.impl;

import io.fireflysoftware.domain.product.pricing.sdk.api.EligibilityApi;
import io.fireflysoftware.domain.product.pricing.sdk.api.PricingApi;
import io.fireflysoftware.domain.product.pricing.sdk.model.EvaluateEligibilityCommand;
import io.fireflysoftware.domain.product.pricing.sdk.model.RegisterProductPricingCommand;
import io.fireflysoftware.experience.lending.core.simulation.commands.CheckEligibilityCommand;
import io.fireflysoftware.experience.lending.core.simulation.commands.CreateSimulationCommand;
import io.fireflysoftware.experience.lending.core.simulation.queries.EligibilityResultDTO;
import io.fireflysoftware.experience.lending.core.simulation.queries.SimulationResultDTO;
import io.fireflysoftware.experience.lending.core.simulation.services.SimulationService;
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

        return pricingApi.registerPricing(pricingCmd, UUID.randomUUID().toString())
                .map(response -> mapToSimulationResult(response, command.getTerm(), command.getProductType()));
    }

    @Override
    public Mono<EligibilityResultDTO> checkEligibility(CheckEligibilityCommand command) {
        log.debug("Checking eligibility for productId={}", command.getProductId());

        EvaluateEligibilityCommand body = new EvaluateEligibilityCommand()
                .partyId(command.getPartyId())
                .productId(command.getProductId())
                .requestedAmount(command.getRequestedAmount());

        return eligibilityApi.evaluateEligibility(command.getProductId(), body, UUID.randomUUID().toString())
                .map(result -> EligibilityResultDTO.builder()
                        .evaluationId(result.getEvaluationId())
                        .eligible(Boolean.TRUE.equals(result.getEligible()))
                        .maxAmount(result.getMaxAmount())
                        .reasons(result.getReasons() != null ? result.getReasons() : List.of())
                        .build());
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
