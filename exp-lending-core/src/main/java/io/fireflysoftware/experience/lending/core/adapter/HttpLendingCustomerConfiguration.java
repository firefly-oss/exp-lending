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

import io.fireflysoftware.experience.lending.core.application.commands.CreateApplicationCommand;
import io.fireflysoftware.experience.lending.core.application.queries.ApplicationDetailDTO;
import io.fireflysoftware.experience.lending.core.assetfinance.queries.FinanceType;
import io.fireflysoftware.experience.lending.core.product.ContractReadPort;
import io.fireflysoftware.experience.lending.core.product.ContractView;
import io.fireflysoftware.experience.lending.core.product.CreditProductPort;
import io.fireflysoftware.experience.lending.core.product.CreditProductRoute;
import io.fireflysoftware.experience.lending.core.product.InterestRateType;
import io.fireflysoftware.experience.lending.core.product.LoanCommandPort;
import io.fireflysoftware.experience.lending.core.product.LoanCustomerOperation;
import io.fireflysoftware.experience.lending.core.product.MortgageType;
import io.fireflysoftware.experience.lending.core.product.PersonalLoanPurpose;
import io.fireflysoftware.experience.lending.core.product.ProductRoute;
import io.fireflysoftware.experience.lending.core.product.SupplyChainFinanceType;
import org.fireflyframework.web.error.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * HTTP adapters for credit-product and loan-servicing sagas.
 * Does not import a core SDK or a domain workflow.
 */
@Configuration
public class HttpLendingCustomerConfiguration {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP = new ParameterizedTypeReference<>() {
    };

    @Bean
    public CreditProductPort creditProductPort(
            @Value("${api-configuration.domain-platform.lending-mortgage.base-path:http://localhost:8045}") String mortgage,
            @Value("${api-configuration.domain-platform.lending-personal-loans.base-path:http://localhost:8044}") String personal,
            @Value("${api-configuration.domain-platform.lending-asset-finance.base-path:http://localhost:8043}") String asset,
            @Value("${api-configuration.domain-platform.lending-supply-chain-finance.base-path:http://localhost:8046}") String supply) {
        return new HttpCreditProductClient(Map.of(
                CreditProductRoute.MORTGAGE, WebClient.builder().baseUrl(mortgage).build(),
                CreditProductRoute.PERSONAL_LOAN, WebClient.builder().baseUrl(personal).build(),
                CreditProductRoute.ASSET_FINANCE, WebClient.builder().baseUrl(asset).build(),
                CreditProductRoute.SUPPLY_CHAIN_FINANCE, WebClient.builder().baseUrl(supply).build()));
    }

    @Bean
    public LoanCommandPort loanCommandPort(
            @Value("${api-configuration.domain-platform.lending-loan-servicing.base-path:http://localhost:8084}") String base) {
        WebClient client = WebClient.builder().baseUrl(base).build();
        return (path, body) -> client.post().uri(path).bodyValue(body).retrieve().toBodilessEntity().then();
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

    public static final class HttpCreditProductClient implements CreditProductPort {

        private final Map<CreditProductRoute, WebClient> clients;

        public HttpCreditProductClient(Map<CreditProductRoute, WebClient> clients) {
            this.clients = clients;
        }

        @Override
        public Mono<ApplicationDetailDTO> open(ProductRoute route, CreateApplicationCommand command) {
            Map<String, Object> body = new LinkedHashMap<>(route.attributes());
            if (command.getPartyId() != null) {
                body.putIfAbsent("applicationId", command.getPartyId());
            } else if (!body.containsKey("applicationId") && route.route() != CreditProductRoute.ASSET_FINANCE
                    && route.route() != CreditProductRoute.SUPPLY_CHAIN_FINANCE) {
                body.put("applicationId", UUID.randomUUID());
            }
            if (command.getProductId() != null) {
                body.put("productId", command.getProductId());
            }
            if (command.getRequestedAmount() != null) {
                body.putIfAbsent("totalValue", command.getRequestedAmount());
                body.putIfAbsent("purchasePrice", command.getRequestedAmount());
            }
            validate(route.route(), body);
            return clients.get(route.route()).post().uri(route.createPath()).bodyValue(body)
                    .retrieve().bodyToMono(MAP)
                    .map(node -> ApplicationDetailDTO.builder()
                            .applicationId(uuid(node.get(route.idField()), null))
                            .productType(route.route().name())
                            .status(firstText(node, "agreementStatus", "status", "policyStatus"))
                            .requestedAmount(command.getRequestedAmount())
                            .term(command.getTerm())
                            .purpose(command.getPurpose())
                            .build());
        }

        @Override
        public Mono<ApplicationDetailDTO> command(ProductRoute route, UUID agreementId,
                                                  LoanCustomerOperation operation, Map<String, Object> body) {
            String template = route.operation(operation);
            if (template == null) {
                return Mono.error(new BusinessException(HttpStatus.CONFLICT, "JOURNEY_NOT_ENABLED",
                        operation.name() + " is not enabled for " + route.route().name()));
            }
            String path = template.replace("{id}", agreementId.toString());
            WebClient.RequestBodySpec spec = operation == LoanCustomerOperation.STATUS || operation == LoanCustomerOperation.UPDATE
                    ? clients.get(route.route()).put().uri(path)
                    : clients.get(route.route()).post().uri(path);
            return spec.bodyValue(body).retrieve().bodyToMono(MAP)
                    .map(node -> ApplicationDetailDTO.builder()
                            .applicationId(agreementId)
                            .productType(route.route().name())
                            .status(firstText(node, "agreementStatus", "status"))
                            .build());
        }

        private static void validate(CreditProductRoute route, Map<String, Object> body) {
            switch (route) {
                case MORTGAGE -> {
                    require(body, "agreementStatus", "applicationId", "mortgageType", "rateType");
                    MortgageType.valueOf(String.valueOf(body.get("mortgageType")));
                    InterestRateType.valueOf(String.valueOf(body.get("rateType")));
                    body.put("mortgageType", MortgageType.valueOf(String.valueOf(body.get("mortgageType"))).name());
                    body.put("rateType", InterestRateType.valueOf(String.valueOf(body.get("rateType"))).name());
                }
                case PERSONAL_LOAN -> {
                    require(body, "agreementStatus", "applicationId", "loanPurpose", "rateType");
                    PersonalLoanPurpose.valueOf(String.valueOf(body.get("loanPurpose")));
                    InterestRateType.valueOf(String.valueOf(body.get("rateType")));
                }
                case ASSET_FINANCE -> {
                    require(body, "agreementStatus", "endDate", "financeType", "paymentFrequency", "startDate", "totalValue");
                    body.put("financeType", FinanceType.valueOf(String.valueOf(body.get("financeType"))).name());
                }
                case SUPPLY_CHAIN_FINANCE -> {
                    require(body, "agreementNumber", "currency", "financeType", "loanServicingCaseId", "startDate", "status");
                    body.put("financeType", SupplyChainFinanceType.valueOf(String.valueOf(body.get("financeType"))).name());
                }
                case LOAN_ORIGINATION -> throw new BusinessException(HttpStatus.CONFLICT, "ROUTE_NOT_CONFIGURED",
                        "Loan origination uses its own port");
            }
        }

        private static void require(Map<String, Object> body, String... keys) {
            for (String key : keys) {
                if (body.get(key) == null || String.valueOf(body.get(key)).isBlank()) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", key + " is required");
                }
            }
        }
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

    private static String firstText(Map<String, Object> node, String... fields) {
        for (String field : fields) {
            if (node.get(field) != null) {
                return node.get(field).toString();
            }
        }
        return null;
    }
}
