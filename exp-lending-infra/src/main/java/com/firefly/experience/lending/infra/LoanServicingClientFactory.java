package com.firefly.experience.lending.infra;

import com.firefly.core.lending.servicing.sdk.api.LoanAccrualApi;
import com.firefly.core.lending.servicing.sdk.api.LoanBalanceApi;
import com.firefly.core.lending.servicing.sdk.api.LoanDisbursementApi;
import com.firefly.core.lending.servicing.sdk.api.LoanDisbursementPlanApi;
import com.firefly.core.lending.servicing.sdk.api.LoanEscrowApi;
import com.firefly.core.lending.servicing.sdk.api.LoanInstallmentPlanApi;
import com.firefly.core.lending.servicing.sdk.api.LoanInstallmentRecordApi;
import com.firefly.core.lending.servicing.sdk.api.LoanNotificationApi;
import com.firefly.core.lending.servicing.sdk.api.LoanRateChangeApi;
import com.firefly.core.lending.servicing.sdk.api.LoanRebateApi;
import com.firefly.core.lending.servicing.sdk.api.LoanRepaymentRecordApi;
import com.firefly.core.lending.servicing.sdk.api.LoanRepaymentScheduleApi;
import com.firefly.core.lending.servicing.sdk.api.LoanRestructuringApi;
import com.firefly.core.lending.servicing.sdk.api.LoanServicingCaseApi;
import com.firefly.core.lending.servicing.sdk.api.LoanServicingEventApi;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * Factory that creates and configures the Loan Servicing SDK clients
 * and exposes core API beans for dependency injection.
 *
 * // ARCH-EXCEPTION: {@link LoanBalanceApi}, {@link LoanRepaymentScheduleApi},
 * {@link LoanInstallmentPlanApi}, {@link LoanInstallmentRecordApi}, {@link LoanDisbursementApi},
 * {@link LoanDisbursementPlanApi}, {@link LoanRepaymentRecordApi}, {@link LoanRateChangeApi},
 * {@link LoanAccrualApi}, {@link LoanEscrowApi}, {@link LoanRebateApi},
 * {@link LoanRestructuringApi}, {@link LoanServicingEventApi}, and {@link LoanNotificationApi}
 * are sourced from core-lending-loan-servicing-sdk because the domain SDK
 * ({@code domain-lending-loan-servicing-sdk}) exposes only lifecycle operations via
 * {@code LoanServicingApi} and does not surface these query sub-resource APIs.
 */
@Component
public class LoanServicingClientFactory {

    private final com.firefly.core.lending.servicing.sdk.invoker.ApiClient coreApiClient;

    public LoanServicingClientFactory(CoreLoanServicingProperties coreProperties) {
        this.coreApiClient = new com.firefly.core.lending.servicing.sdk.invoker.ApiClient();
        this.coreApiClient.setBasePath(coreProperties.getBasePath());
    }

    // --- Core SDK beans ---

    @Bean
    public LoanServicingCaseApi loanServicingCaseApi() {
        return new LoanServicingCaseApi(coreApiClient);
    }

    @Bean
    public LoanBalanceApi loanBalanceApi() {
        return new LoanBalanceApi(coreApiClient);
    }

    @Bean
    public LoanRepaymentScheduleApi loanRepaymentScheduleApi() {
        return new LoanRepaymentScheduleApi(coreApiClient);
    }

    @Bean
    public LoanInstallmentPlanApi loanInstallmentPlanApi() {
        return new LoanInstallmentPlanApi(coreApiClient);
    }

    @Bean
    public LoanInstallmentRecordApi loanInstallmentRecordApi() {
        return new LoanInstallmentRecordApi(coreApiClient);
    }

    @Bean
    public LoanDisbursementApi loanDisbursementApi() {
        return new LoanDisbursementApi(coreApiClient);
    }

    @Bean
    public LoanDisbursementPlanApi loanDisbursementPlanApi() {
        return new LoanDisbursementPlanApi(coreApiClient);
    }

    @Bean
    public LoanRepaymentRecordApi loanRepaymentRecordApi() {
        return new LoanRepaymentRecordApi(coreApiClient);
    }

    @Bean
    public LoanRateChangeApi loanRateChangeApi() {
        return new LoanRateChangeApi(coreApiClient);
    }

    @Bean
    public LoanAccrualApi loanAccrualApi() {
        return new LoanAccrualApi(coreApiClient);
    }

    @Bean
    public LoanEscrowApi loanEscrowApi() {
        return new LoanEscrowApi(coreApiClient);
    }

    @Bean
    public LoanRebateApi loanRebateApi() {
        return new LoanRebateApi(coreApiClient);
    }

    @Bean
    public LoanRestructuringApi loanRestructuringApi() {
        return new LoanRestructuringApi(coreApiClient);
    }

    @Bean
    public LoanServicingEventApi loanServicingEventApi() {
        return new LoanServicingEventApi(coreApiClient);
    }

    @Bean
    public LoanNotificationApi loanNotificationApi() {
        return new LoanNotificationApi(coreApiClient);
    }
}
