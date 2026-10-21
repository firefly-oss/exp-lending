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


package io.fireflysoftware.experience.lending.infra;

import io.fireflysoftware.domain.core.pricing.engine.sdk.api.PricingEngineProductsApi;
import io.fireflysoftware.domain.core.pricing.engine.sdk.api.PricingEngineSimulationApi;
import io.fireflysoftware.domain.core.pricing.engine.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Pricing Engine SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 */
@Component
public class PricingEngineClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Pricing Engine service
     */
    public PricingEngineClientFactory(PricingEngineProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link PricingEngineProductsApi} bean for product catalog with pricing queries.
     *
     * @return a ready-to-use PricingEngineProductsApi instance
     */
    @Bean
    public PricingEngineProductsApi pricingEngineProductsApi() {
        return new PricingEngineProductsApi(apiClient);
    }

    /**
     * Provides the {@link PricingEngineSimulationApi} bean for simulation calculations.
     *
     * @return a ready-to-use PricingEngineSimulationApi instance
     */
    @Bean
    public PricingEngineSimulationApi pricingEngineSimulationApi() {
        return new PricingEngineSimulationApi(apiClient);
    }
}
