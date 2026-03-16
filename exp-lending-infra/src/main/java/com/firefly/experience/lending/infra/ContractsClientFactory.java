package com.firefly.experience.lending.infra;

import com.firefly.domain.common.contracts.sdk.api.ContractsApi;
import com.firefly.domain.common.contracts.sdk.api.ContractDocumentsApi;
import com.firefly.domain.common.contracts.sdk.api.ContractPartiesApi;
import com.firefly.domain.common.contracts.sdk.api.ContractSignaturesApi;
import com.firefly.domain.common.contracts.sdk.api.ContractTermsApi;
import com.firefly.domain.common.contracts.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures SDK {@link ApiClient}
 * instances for the Common Contracts ecosystem and exposes domain API beans.
 */
@Component
public class ContractsClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Common Contracts service
     */
    public ContractsClientFactory(ContractsProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link ContractsApi} bean for contract lifecycle operations.
     *
     * @return a ready-to-use ContractsApi instance
     */
    @Bean
    public ContractsApi contractsApi() {
        return new ContractsApi(apiClient);
    }

    @Bean
    public ContractDocumentsApi contractDocumentsApi() {
        return new ContractDocumentsApi(apiClient);
    }

    @Bean
    public ContractPartiesApi contractPartiesApi() {
        return new ContractPartiesApi(apiClient);
    }

    @Bean
    public ContractSignaturesApi contractSignaturesApi() {
        return new ContractSignaturesApi(apiClient);
    }

    @Bean
    public ContractTermsApi contractTermsApi() {
        return new ContractTermsApi(apiClient);
    }
}
