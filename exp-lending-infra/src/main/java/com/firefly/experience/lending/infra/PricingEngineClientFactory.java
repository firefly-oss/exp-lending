package com.firefly.experience.lending.infra;

import com.firefly.domain.core.pricing.engine.sdk.api.PricingEngineProductsApi;
import com.firefly.domain.core.pricing.engine.sdk.api.PricingEngineSimulationApi;
import com.firefly.domain.core.pricing.engine.sdk.invoker.ApiClient;
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
