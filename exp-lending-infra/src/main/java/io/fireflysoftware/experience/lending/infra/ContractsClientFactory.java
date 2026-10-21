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

import io.fireflysoftware.domain.common.contracts.sdk.api.ContractsApi;
import io.fireflysoftware.domain.common.contracts.sdk.api.KitDocumentsControllerApi;
import io.fireflysoftware.domain.common.contracts.sdk.api.ContractDocumentsApi;
import io.fireflysoftware.domain.common.contracts.sdk.api.ContractPartiesApi;
import io.fireflysoftware.domain.common.contracts.sdk.api.ContractSignaturesApi;
import io.fireflysoftware.domain.common.contracts.sdk.api.ContractTermsApi;
import io.fireflysoftware.domain.common.contracts.sdk.invoker.ApiClient;
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

    @Bean
    public KitDocumentsControllerApi kitDocumentsApi() {
        return new KitDocumentsControllerApi(apiClient);
    }
}
