package com.firefly.experience.lending.infra;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Loan Origination SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 */
@Component
public class LoanOriginationClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the domain Loan Origination service
     */
    public LoanOriginationClientFactory(LoanOriginationProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link LoanOriginationApi} bean for loan application lifecycle operations.
     *
     * @return a ready-to-use LoanOriginationApi instance
     */
    @Bean
    public LoanOriginationApi loanOriginationApi() {
        return new LoanOriginationApi(apiClient);
    }
}
