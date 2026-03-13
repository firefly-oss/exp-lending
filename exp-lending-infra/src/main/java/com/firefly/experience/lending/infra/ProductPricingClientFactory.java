package com.firefly.experience.lending.infra;

import com.firefly.domain.product.pricing.sdk.api.EligibilityApi;
import com.firefly.domain.product.pricing.sdk.api.FeesApi;
import com.firefly.domain.product.pricing.sdk.api.PricingApi;
import com.firefly.domain.product.pricing.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Product Pricing SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 */
@Component
public class ProductPricingClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Product Pricing service
     */
    public ProductPricingClientFactory(ProductPricingProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link EligibilityApi} bean for product eligibility queries.
     *
     * @return a ready-to-use EligibilityApi instance
     */
    @Bean
    public EligibilityApi productPricingEligibilityApi() {
        return new EligibilityApi(apiClient);
    }

    /**
     * Provides the {@link FeesApi} bean for product fee structure queries.
     *
     * @return a ready-to-use FeesApi instance
     */
    @Bean
    public FeesApi productPricingFeesApi() {
        return new FeesApi(apiClient);
    }

    /**
     * Provides the {@link PricingApi} bean for product pricing simulation.
     *
     * @return a ready-to-use PricingApi instance
     */
    @Bean
    public PricingApi productPricingApi() {
        return new PricingApi(apiClient);
    }
}
