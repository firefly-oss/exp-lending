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

import io.fireflysoftware.domain.lending.loan.servicing.sdk.api.LoanServicingApi;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.api.LoanServicingQueriesApi;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.api.SyndicatedFacilityApi;
import io.fireflysoftware.domain.lending.loan.servicing.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Loan Servicing SDK clients
 * and exposes domain API beans for dependency injection.
 */
@Component
public class LoanServicingClientFactory {

    private final ApiClient apiClient;

    public LoanServicingClientFactory(LoanServicingProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    @Bean
    public LoanServicingApi loanServicingApi() {
        return new LoanServicingApi(apiClient);
    }

    @Bean
    public LoanServicingQueriesApi loanServicingQueriesApi() {
        return new LoanServicingQueriesApi(apiClient);
    }

    @Bean
    public SyndicatedFacilityApi syndicatedFacilityApi() {
        return new SyndicatedFacilityApi(apiClient);
    }
}
