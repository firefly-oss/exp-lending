package com.firefly.experience.lending.infra;

import com.firefly.domain.lending.assetfinance.sdk.api.AssetFinanceApi;
import com.firefly.domain.lending.assetfinance.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Asset Finance SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 */
@Component
public class AssetFinanceClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Asset Finance service
     */
    public AssetFinanceClientFactory(AssetFinanceProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link AssetFinanceApi} bean for asset renting and leasing operations.
     *
     * @return a ready-to-use AssetFinanceApi instance
     */
    @Bean
    public AssetFinanceApi assetFinanceApi() {
        return new AssetFinanceApi(apiClient);
    }
}
