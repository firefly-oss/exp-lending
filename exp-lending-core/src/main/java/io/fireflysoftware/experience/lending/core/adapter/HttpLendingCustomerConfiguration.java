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


package io.fireflysoftware.experience.lending.core.adapter;

import io.fireflysoftware.domain.lending.assetfinance.sdk.api.AssetFinanceApi;
import io.fireflysoftware.domain.lending.mortgage.sdk.api.MortgageAgreementsApi;
import io.fireflysoftware.domain.lending.personalloans.sdk.api.PersonalLoansApi;
import io.fireflysoftware.domain.lending.supplychainfinance.sdk.api.SupplyChainFinanceAgreementsApi;
import io.fireflysoftware.experience.lending.core.product.ContractReadPort;
import io.fireflysoftware.experience.lending.core.product.ContractView;
import io.fireflysoftware.experience.lending.core.product.CreditProductPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;
import java.util.UUID;

/**
 * Credit-product commands use the mortgage, personal-loan, asset-finance, and
 * supply-chain domain SDKs. Rate, holiday, and close use the loan-servicing SDK
 * from the journey service. Contract reads stay on the contracts domain.
 */
@Configuration
public class HttpLendingCustomerConfiguration {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };

    @Bean
    public CreditProductPort creditProductPort(MortgageAgreementsApi mortgages,
                                               PersonalLoansApi personalLoans,
                                               AssetFinanceApi assetFinance,
                                               SupplyChainFinanceAgreementsApi supplyChain) {
        return new SdkCreditProductClient(mortgages, personalLoans, assetFinance, supplyChain);
    }

    @Bean
    public ContractReadPort contractReadPort(
            @Value("${api-configuration.domain-platform.common-contracts.base-path:http://localhost:8090}") String base) {
        WebClient client = WebClient.builder().baseUrl(base).build();
        return contractId -> client.get().uri("/api/v1/contracts/{id}", contractId)
                .retrieve().bodyToMono(MAP)
                .map(node -> new ContractView(
                        uuid(node.get("contractId"), contractId),
                        text(node.get("contractNumber")),
                        text(node.get("contractStatus")),
                        uuid(node.get("productId"), null)));
    }

    private static UUID uuid(Object value, UUID fallback) {
        if (value == null || value.toString().isBlank()) {
            return fallback;
        }
        return UUID.fromString(value.toString());
    }

    private static String text(Object value) {
        return value == null ? null : value.toString();
    }
}
