package com.firefly.experience.lending.infra;

import com.firefly.domain.lending.loan.servicing.sdk.api.LoanServicingApi;
import com.firefly.domain.lending.loan.servicing.sdk.api.LoanServicingQueriesApi;
import com.firefly.domain.lending.loan.servicing.sdk.invoker.ApiClient;
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
}
