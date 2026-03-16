package com.firefly.experience.lending.infra;

import com.firefly.core.contract.sdk.api.ContractsApi;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures SDK {@link com.firefly.core.contract.sdk.invoker.ApiClient}
 * instances for the Common Contracts ecosystem and exposes domain API beans.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link ContractsApi}; direct
 * core-common-contract-mgmt-sdk usage is temporary until the domain layer surfaces
 * this endpoint.
 */
@Component
public class ContractsClientFactory {

    private final com.firefly.core.contract.sdk.invoker.ApiClient coreApiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Common Contracts service
     */
    public ContractsClientFactory(ContractsProperties properties) {
        this.coreApiClient = new com.firefly.core.contract.sdk.invoker.ApiClient();
        this.coreApiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link ContractsApi} bean for contract lifecycle operations:
     * create, read, filter, update, and delete contracts in core-common-contract-mgmt.
     *
     * @return a ready-to-use ContractsApi instance
     */
    @Bean
    public ContractsApi contractsApi() {
        return new ContractsApi(coreApiClient);
    }
}
