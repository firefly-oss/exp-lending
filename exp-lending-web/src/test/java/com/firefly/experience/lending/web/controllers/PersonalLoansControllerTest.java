package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.personalloans.commands.CreatePersonalLoanCommand;
import com.firefly.experience.lending.core.personalloans.queries.PersonalLoanDetailDTO;
import com.firefly.experience.lending.core.personalloans.queries.PersonalLoanSummaryDTO;
import com.firefly.experience.lending.core.personalloans.services.PersonalLoansService;
import org.fireflyframework.web.error.config.ErrorHandlingProperties;
import org.fireflyframework.web.error.converter.ExceptionConverterService;
import org.fireflyframework.web.error.service.ErrorResponseNegotiator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.reactive.ReactiveUserDetailsServiceAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class PersonalLoansControllerTest {

    private static final String BASE = "/api/v1/experience/lending/personal-loans";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PersonalLoansService personalLoansService;

    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
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
                .loanPurpose("VEHICLE_PURCHASE")
                .status("ACTIVE")
                .rateType("FIXED")
                .interestRate(new BigDecimal("6.00"))
                .insuranceType("BASIC")
                .earlyRepaymentPenaltyType("FLAT_FEE")
                .build();

        when(personalLoansService.updateAgreement(eq(agreementId), any(CreatePersonalLoanCommand.class)))
                .thenReturn(Mono.just(detail));

        webTestClient.put()
                .uri(BASE + "/{agreementId}", agreementId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "applicationId": "%s",
                            "loanPurpose": "VEHICLE_PURCHASE",
                            "rateType": "FIXED",
                            "interestRate": 6.00,
                            "insuranceType": "BASIC",
                            "earlyRepaymentPenaltyType": "FLAT_FEE"
                        }
                        """.formatted(UUID.randomUUID()))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PersonalLoanDetailDTO.class)
                .value(body -> {
                    assertThat(body.getAgreementId()).isEqualTo(agreementId);
                    assertThat(body.getLoanPurpose()).isEqualTo("VEHICLE_PURCHASE");
                    assertThat(body.getInterestRate()).isEqualByComparingTo("6.00");
                });
    }
}
