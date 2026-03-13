package com.firefly.experience.lending.infra;

import com.firefly.core.lending.origination.sdk.api.ApplicationConditionApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationDocumentApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationExternalBankAccountsApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationFeeApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationPartyApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationTaskApi;
import com.firefly.core.lending.origination.sdk.api.ApplicationVerificationApi;
import com.firefly.core.lending.origination.sdk.api.LoanApplicationsApi;
import com.firefly.core.lending.origination.sdk.api.ProposedOfferApi;
import com.firefly.core.lending.origination.sdk.api.UnderwritingDecisionApi;
import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.invoker.ApiClient;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Loan Origination SDK {@link ApiClient}
 * and exposes domain and core API beans for dependency injection.
 *
 * // ARCH-EXCEPTION: {@link ApplicationDocumentApi}, {@link ApplicationPartyApi},
 * {@link ApplicationConditionApi}, {@link ApplicationFeeApi}, {@link ApplicationTaskApi},
 * {@link ApplicationVerificationApi}, {@link LoanApplicationsApi},
 * {@link ApplicationExternalBankAccountsApi}, {@link UnderwritingDecisionApi}, and
 * {@link ProposedOfferApi} are sourced from core-lending-origination-sdk because the domain SDK
 * ({@code domain-lending-loan-origination-sdk}) does not expose these sub-resource APIs.
 */
@Component
public class LoanOriginationClientFactory {

    private final ApiClient apiClient;
    private final com.firefly.core.lending.origination.sdk.invoker.ApiClient coreApiClient;

    /**
     * Initialises API clients with base paths from configuration properties.
     *
     * @param properties     connection properties for the domain Loan Origination service
     * @param coreProperties connection properties for the core Loan Origination service
     */
    public LoanOriginationClientFactory(LoanOriginationProperties properties,
                                        CoreLoanOriginationProperties coreProperties) {
        this.apiClient = new ApiClient();
        this.apiClient.setBasePath(properties.getBasePath());

        this.coreApiClient = new com.firefly.core.lending.origination.sdk.invoker.ApiClient();
        this.coreApiClient.setBasePath(coreProperties.getBasePath());
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

    /**
     * Provides the {@link ApplicationDocumentApi} bean for document CRUD operations.
     * Uses the core service client since the domain SDK only exposes the attach operation.
     *
     * @return a ready-to-use ApplicationDocumentApi instance
     */
    @Bean
    public ApplicationDocumentApi applicationDocumentApi() {
        return new ApplicationDocumentApi(coreApiClient);
    }

    /**
     * Provides the {@link ApplicationPartyApi} bean for party CRUD operations.
     *
     * @return a ready-to-use ApplicationPartyApi instance
     */
    @Bean
    public ApplicationPartyApi applicationPartyApi() {
        return new ApplicationPartyApi(coreApiClient);
    }

    /**
     * Provides the {@link ApplicationConditionApi} bean for condition CRUD operations.
     *
     * @return a ready-to-use ApplicationConditionApi instance
     */
    @Bean
    public ApplicationConditionApi applicationConditionApi() {
        return new ApplicationConditionApi(coreApiClient);
    }

    /**
     * Provides the {@link ApplicationFeeApi} bean for fee operations.
     *
     * @return a ready-to-use ApplicationFeeApi instance
     */
    @Bean
    public ApplicationFeeApi applicationFeeApi() {
        return new ApplicationFeeApi(coreApiClient);
    }

    /**
     * Provides the {@link ApplicationTaskApi} bean for task operations.
     *
     * @return a ready-to-use ApplicationTaskApi instance
     */
    @Bean
    public ApplicationTaskApi applicationTaskApi() {
        return new ApplicationTaskApi(coreApiClient);
    }

    /**
     * Provides the {@link ApplicationVerificationApi} bean for verification operations.
     *
     * @return a ready-to-use ApplicationVerificationApi instance
     */
    @Bean
    public ApplicationVerificationApi applicationVerificationApi() {
        return new ApplicationVerificationApi(coreApiClient);
    }

    /**
     * Provides the {@link LoanApplicationsApi} bean for loan application CRUD operations.
     *
     * @return a ready-to-use LoanApplicationsApi instance
     */
    @Bean
    public LoanApplicationsApi loanApplicationsApi() {
        return new LoanApplicationsApi(coreApiClient);
    }

    /**
     * Provides the {@link ApplicationExternalBankAccountsApi} bean for external bank account operations.
     *
     * @return a ready-to-use ApplicationExternalBankAccountsApi instance
     */
    @Bean
    public ApplicationExternalBankAccountsApi applicationExternalBankAccountsApi() {
        return new ApplicationExternalBankAccountsApi(coreApiClient);
    }

    /**
     * Provides the {@link UnderwritingDecisionApi} bean for reading and writing
     * underwriting decisions attached to a loan application.
     *
     * @return a ready-to-use UnderwritingDecisionApi instance
     */
    @Bean
    public UnderwritingDecisionApi underwritingDecisionApi() {
        return new UnderwritingDecisionApi(coreApiClient);
    }

    /**
     * Provides the {@link ProposedOfferApi} bean for managing proposed offers
     * (list, accept, reject) on a loan application.
     *
     * @return a ready-to-use ProposedOfferApi instance
     */
    @Bean
    public ProposedOfferApi proposedOfferApi() {
        return new ProposedOfferApi(coreApiClient);
    }
}
