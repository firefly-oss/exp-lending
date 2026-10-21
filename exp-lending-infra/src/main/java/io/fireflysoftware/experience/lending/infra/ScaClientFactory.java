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

import io.fireflysoftware.domain.common.sca.sdk.api.ScaOperationsApi;
import io.fireflysoftware.domain.common.sca.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates the SCA SDK client for challenge generation and attempt verification.
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
     * Provides the {@link ScaOperationsApi} bean for SCA challenge and verification calls.
     *
     * @return a ready-to-use SCA operations client
     */
    @Bean
    public ScaOperationsApi scaOperationsApi() {
        return new ScaOperationsApi(apiClient);
    }
}
