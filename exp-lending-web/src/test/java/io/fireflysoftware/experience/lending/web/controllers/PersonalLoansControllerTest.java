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

package io.fireflysoftware.experience.lending.web.controllers;

import io.fireflysoftware.experience.lending.core.personalloans.commands.CreatePersonalLoanCommand;
import io.fireflysoftware.experience.lending.core.personalloans.queries.PersonalLoanDetailDTO;
import io.fireflysoftware.experience.lending.core.personalloans.queries.PersonalLoanSummaryDTO;
import io.fireflysoftware.experience.lending.core.personalloans.services.PersonalLoansService;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.web.reactive.ReactiveWebSecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PersonalLoansController.class,
        excludeAutoConfiguration = {
                ReactiveWebSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class PersonalLoansControllerTest {

    private static final String BASE = "/api/v1/experience/lending/personal-loans";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private PersonalLoansService personalLoansService;

    @MockitoBean
    private ExceptionConverterService exceptionConverterService;
    @MockitoBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockitoBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Test
    void createAgreement_returns201WithBody() {
        var agreementId = UUID.randomUUID();
        var detail = PersonalLoanDetailDTO.builder()
                .agreementId(agreementId)
                .loanPurpose("HOME_IMPROVEMENT")
                .status("DRAFT")
                .rateType("FIXED")
                .interestRate(new BigDecimal("5.50"))
                .insuranceType("COMPREHENSIVE")
                .earlyRepaymentPenaltyType("PERCENTAGE")
                .build();

        when(personalLoansService.createAgreement(any(CreatePersonalLoanCommand.class)))
                .thenReturn(Mono.just(detail));

        webTestClient.post()
                .uri(BASE)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "applicationId": "%s",
                            "loanPurpose": "HOME_IMPROVEMENT",
                            "rateType": "FIXED",
                            "interestRate": 5.50,
                            "insuranceType": "COMPREHENSIVE",
                            "earlyRepaymentPenaltyType": "PERCENTAGE"
                        }
                        """.formatted(UUID.randomUUID()))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PersonalLoanDetailDTO.class)
                .value(body -> {
                    assertThat(body.getAgreementId()).isEqualTo(agreementId);
                    assertThat(body.getLoanPurpose()).isEqualTo("HOME_IMPROVEMENT");
                    assertThat(body.getRateType()).isEqualTo("FIXED");
                });
    }

    // -------------------------------------------------------------------------
    // Get
    // -------------------------------------------------------------------------

    @Test
    void getAgreement_returns200WithDetail() {
        var agreementId = UUID.randomUUID();
        var detail = PersonalLoanDetailDTO.builder()
                .agreementId(agreementId)
                .loanPurpose("DEBT_CONSOLIDATION")
                .status("ACTIVE")
                .rateType("VARIABLE")
                .interestRate(new BigDecimal("4.25"))
                .build();

        when(personalLoansService.getAgreement(eq(agreementId))).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri(BASE + "/{agreementId}", agreementId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(PersonalLoanDetailDTO.class)
                .value(body -> {
                    assertThat(body.getAgreementId()).isEqualTo(agreementId);
                    assertThat(body.getLoanPurpose()).isEqualTo("DEBT_CONSOLIDATION");
                    assertThat(body.getInterestRate()).isEqualByComparingTo("4.25");
                });
    }

    @Test
    void getAgreement_returns404WhenNotFound() {
        var agreementId = UUID.randomUUID();
        when(personalLoansService.getAgreement(eq(agreementId))).thenReturn(Mono.empty());

        webTestClient.get()
                .uri(BASE + "/{agreementId}", agreementId)
                .exchange()
                .expectStatus().isNotFound();
    }

    // -------------------------------------------------------------------------
    // List
    // -------------------------------------------------------------------------

    @Test
    void listAgreements_returns200WithEmptyList() {
        when(personalLoansService.listAgreements()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(PersonalLoanSummaryDTO.class)
                .hasSize(0);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Test
    void updateAgreement_returns200WithBody() {
        var agreementId = UUID.randomUUID();
        var detail = PersonalLoanDetailDTO.builder()
                .agreementId(agreementId)
                .loanPurpose("MAJOR_PURCHASE")
                .status("ACTIVE")
                .rateType("FIXED")
                .interestRate(new BigDecimal("6.00"))
                .insuranceType("PAYMENT_PROTECTION")
                .earlyRepaymentPenaltyType("FIXED_FEE")
                .build();

        when(personalLoansService.updateAgreement(eq(agreementId), any(CreatePersonalLoanCommand.class)))
                .thenReturn(Mono.just(detail));

        webTestClient.put()
                .uri(BASE + "/{agreementId}", agreementId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "applicationId": "%s",
                            "loanPurpose": "MAJOR_PURCHASE",
                            "rateType": "FIXED",
                            "interestRate": 6.00,
                            "insuranceType": "PAYMENT_PROTECTION",
                            "earlyRepaymentPenaltyType": "FIXED_FEE"
                        }
                        """.formatted(UUID.randomUUID()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PersonalLoanDetailDTO.class)
                .value(body -> {
                    assertThat(body.getAgreementId()).isEqualTo(agreementId);
                    assertThat(body.getLoanPurpose()).isEqualTo("MAJOR_PURCHASE");
                    assertThat(body.getInterestRate()).isEqualByComparingTo("6.00");
                });
    }
}
