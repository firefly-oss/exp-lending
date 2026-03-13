package com.firefly.experience.lending.infra;

import com.firefly.core.contract.sdk.api.ContractsApi;
import com.firefly.domain.common.contracts.sdk.api.ScaOperationsApi;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures SDK {@link com.firefly.domain.common.contracts.sdk.invoker.ApiClient}
 * instances for the Common Contracts ecosystem and exposes domain API beans.
 *
 * <p>Two SDK clients are maintained in this factory:
 * <ul>
 *   <li>{@code domainApiClient} — targets the {@code domain-common-contracts} service (SCA operations)</li>
 *   <li>{@code coreApiClient} — targets the {@code core-common-contract-mgmt} service (contract CRUD)</li>
 * </ul>
 * Both clients share the same base path ({@link ContractsProperties}) because the contracts
 * domain service and core service are co-located behind the same ingress in this environment.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link ContractsApi}; direct
 * core-common-contract-mgmt-sdk usage is temporary until the domain layer surfaces
 * this endpoint.
 */
@Component
public class ContractsClientFactory {

    private final com.firefly.domain.common.contracts.sdk.invoker.ApiClient domainApiClient;
    private final com.firefly.core.contract.sdk.invoker.ApiClient coreApiClient;

    /**
     * Initialises both API clients with the base path from configuration properties.
     *
     * @param properties connection properties for the Common Contracts service
     */
    public ContractsClientFactory(ContractsProperties properties) {
        this.domainApiClient = new com.firefly.domain.common.contracts.sdk.invoker.ApiClient();
        this.domainApiClient.setBasePath(properties.getBasePath());

        this.coreApiClient = new com.firefly.core.contract.sdk.invoker.ApiClient();
        this.coreApiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link ScaOperationsApi} bean for SCA challenge management
     * during contract signing flows.
     *
     * <p>Bean is named {@code contractsScaOperationsApi} to distinguish it from the
     * {@code scaOperationsApi} bean provided by {@link ScaClientFactory}.
     *
     * @return a ready-to-use ScaOperationsApi instance
     */
    @Bean
    public ScaOperationsApi contractsScaOperationsApi() {
        return new ScaOperationsApi(domainApiClient);
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
