package com.firefly.experience.lending.infra;

import com.firefly.domain.common.contracts.sdk.api.ScaOperationsApi;
import com.firefly.domain.common.contracts.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures an SDK {@link ApiClient} pointed at the
 * Common SCA (Strong Customer Authentication) service and exposes domain API beans.
 * <p>
 * The {@code domain-common-sca-sdk} artifact is currently being built; until its
 * OpenAPI generation is complete, {@link ScaOperationsApi} is sourced from the
 * {@code domain-common-contracts-sdk}, which mirrors the same SCA endpoints.
 * The base path is kept separate so SCA and Contracts traffic routes independently.
 */
@Component
public class ScaClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Common SCA service
     */
    public ScaClientFactory(ScaProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link ScaOperationsApi} bean for Strong Customer Authentication
     * operations: initiating challenges, verifying attempts, and querying SCA status.
     *
     * @return a ready-to-use ScaOperationsApi instance
     */
    @Bean
    public ScaOperationsApi scaOperationsApi() {
        return new ScaOperationsApi(apiClient);
    }
}
