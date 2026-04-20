package com.firefly.experience.lending.infra;

import com.firefly.domain.lending.personalloans.sdk.api.PersonalLoansApi;
import com.firefly.domain.lending.personalloans.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Personal Loans SDK {@link ApiClient}
 * and exposes domain API beans for dependency injection.
 */
@Component
public class PersonalLoansClientFactory {

    private final ApiClient apiClient;

    /**
     * Initialises the API client with the base path from configuration properties.
     *
     * @param properties connection properties for the Personal Loans service
     */
    public PersonalLoansClientFactory(PersonalLoansProperties properties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());
    }

    /**
     * Provides the {@link PersonalLoansApi} bean for personal loan operations.
     *
     * @return a ready-to-use PersonalLoansApi instance
     */
    @Bean
    public PersonalLoansApi personalLoansApi() {
        return new PersonalLoansApi(apiClient);
    }
}
