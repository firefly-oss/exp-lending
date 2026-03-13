package com.firefly.experience.lending.infra;

import com.firefly.domain.lending.creditscoring.sdk.api.CreditScoringApi;
import com.firefly.domain.lending.creditscoring.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Credit Scoring SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 */
@Component
public class CreditScoringClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the SDK API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Credit Scoring service
     */
    public CreditScoringClientFactory(CreditScoringProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link CreditScoringApi} bean for credit scoring operations.
     *
     * @return a ready-to-use CreditScoringApi instance
     */
    @Bean
    public CreditScoringApi creditScoringApi() {
        return new CreditScoringApi(apiClient);
    }
}
