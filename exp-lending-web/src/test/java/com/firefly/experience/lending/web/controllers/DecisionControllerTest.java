package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.decision.commands.RejectOfferCommand;
import com.firefly.experience.lending.core.application.decision.commands.SignContractCommand;
import com.firefly.experience.lending.core.application.decision.queries.ContractDTO;
import com.firefly.experience.lending.core.application.decision.queries.DecisionDTO;
import com.firefly.experience.lending.core.application.decision.queries.OfferDetailDTO;
import com.firefly.experience.lending.core.application.decision.queries.OfferSummaryDTO;
import com.firefly.experience.lending.core.application.decision.queries.ScoringStatusDTO;
import com.firefly.experience.lending.core.application.decision.services.DecisionService;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = DecisionController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class DecisionControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DecisionService decisionService;

    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID OFFER_ID = UUID.randomUUID();
    private static final UUID CONTRACT_ID = UUID.randomUUID();
    private static final String BASE = "/api/v1/experience/lending/applications/{id}";

    // -------------------------------------------------------------------------
    // GET /scoring-status
    // -------------------------------------------------------------------------

    @Test
    void getScoringStatus_returns200WithCompletedStatus() {
        var dto = ScoringStatusDTO.builder()
                .applicationId(APPLICATION_ID)
                .status("COMPLETED")
                .score(720)
                .build();

        when(decisionService.getScoringStatus(eq(APPLICATION_ID))).thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri(BASE + "/scoring-status", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ScoringStatusDTO.class)
                .value(body -> {
                    assertThat(body.getApplicationId()).isEqualTo(APPLICATION_ID);
                    assertThat(body.getStatus()).isEqualTo("COMPLETED");
                    assertThat(body.getScore()).isEqualTo(720);
                });
    }

    @Test
    void getScoringStatus_returns200WithPendingStatus() {
        var dto = ScoringStatusDTO.builder()
                .applicationId(APPLICATION_ID)
                .status("PENDING")
                .score(null)
                .build();

        when(decisionService.getScoringStatus(eq(APPLICATION_ID))).thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri(BASE + "/scoring-status", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ScoringStatusDTO.class)
                .value(body -> {
                    assertThat(body.getStatus()).isEqualTo("PENDING");
                    assertThat(body.getScore()).isNull();
                });
    }

    // -------------------------------------------------------------------------
    // GET /decision
    // -------------------------------------------------------------------------

    @Test
    void getDecision_returns200WithApprovedDecision() {
        var dto = DecisionDTO.builder()
                .applicationId(APPLICATION_ID)
                .result("APPROVED")
                .reasons(List.of())
                .decidedAt(LocalDateTime.now())
                .build();

        when(decisionService.getDecision(eq(APPLICATION_ID))).thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri(BASE + "/decision", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DecisionDTO.class)
                .value(body -> {
                    assertThat(body.getApplicationId()).isEqualTo(APPLICATION_ID);
                    assertThat(body.getResult()).isEqualTo("APPROVED");
                });
    }

    @Test
    void getDecision_returns500WhenServiceFails() {
        when(decisionService.getDecision(eq(APPLICATION_ID)))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        webTestClient.get()
                .uri(BASE + "/decision", APPLICATION_ID)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // -------------------------------------------------------------------------
    // GET /offers
    // -------------------------------------------------------------------------

    @Test
    void getOffers_returns200WithOfferList() {
        var offer = OfferSummaryDTO.builder()
                .offerId(OFFER_ID)
                .amount(new BigDecimal("10000.00"))
                .term(24)
                .monthlyPayment(new BigDecimal("450.00"))
                .annualRate(new BigDecimal("5.5"))
                .status("SENT")
                .build();

        when(decisionService.getOffers(eq(APPLICATION_ID))).thenReturn(Flux.just(offer));

        webTestClient.get()
                .uri(BASE + "/offers", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OfferSummaryDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getOfferId()).isEqualTo(OFFER_ID);
                    assertThat(list.get(0).getStatus()).isEqualTo("SENT");
                });
    }

    @Test
    void getOffers_returns200WithEmptyList() {
        when(decisionService.getOffers(eq(APPLICATION_ID))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/offers", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(OfferSummaryDTO.class)
                .hasSize(0);
    }

    // -------------------------------------------------------------------------
    // GET /offers/{offerId}
    // -------------------------------------------------------------------------

    @Test
    void getOffer_returns200WithDetailedOffer() {
        var dto = OfferDetailDTO.builder()
                .offerId(OFFER_ID)
                .amount(new BigDecimal("10000.00"))
                .term(24)
                .monthlyPayment(new BigDecimal("450.00"))
                .annualRate(new BigDecimal("5.5"))
                .status("SENT")
                .totalCost(new BigDecimal("10800.00"))
                .fees(List.of())
                .conditions(List.of())
                .build();

        when(decisionService.getOffer(eq(APPLICATION_ID), eq(OFFER_ID))).thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri(BASE + "/offers/{offerId}", APPLICATION_ID, OFFER_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(OfferDetailDTO.class)
                .value(body -> {
                    assertThat(body.getOfferId()).isEqualTo(OFFER_ID);
                    assertThat(body.getTotalCost()).isEqualByComparingTo("10800.00");
                });
    }

    // -------------------------------------------------------------------------
    // POST /offers/{offerId}/accept
    // -------------------------------------------------------------------------

    @Test
    void acceptOffer_returns200() {
        when(decisionService.acceptOffer(eq(APPLICATION_ID), eq(OFFER_ID)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(BASE + "/offers/{offerId}/accept", APPLICATION_ID, OFFER_ID)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void acceptOffer_returns500WhenServiceFails() {
        when(decisionService.acceptOffer(eq(APPLICATION_ID), eq(OFFER_ID)))
                .thenReturn(Mono.error(new RuntimeException("offer not found")));

        webTestClient.post()
                .uri(BASE + "/offers/{offerId}/accept", APPLICATION_ID, OFFER_ID)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // -------------------------------------------------------------------------
    // POST /offers/{offerId}/reject
    // -------------------------------------------------------------------------

    @Test
    void rejectOffer_returns200() {
        when(decisionService.rejectOffer(eq(APPLICATION_ID), eq(OFFER_ID),
                any(RejectOfferCommand.class)))
                .thenReturn(Mono.empty());

        webTestClient.post()
                .uri(BASE + "/offers/{offerId}/reject", APPLICATION_ID, OFFER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"reason": "Rate too high"}
                        """)
                .exchange()
                .expectStatus().isOk();
    }

    // -------------------------------------------------------------------------
    // GET /contract
    // -------------------------------------------------------------------------

    @Test
    void getContract_returns200WithContract() {
        var dto = ContractDTO.builder()
                .contractId(CONTRACT_ID)
                .applicationId(APPLICATION_ID)
                .status("PENDING_APPROVAL")
                .documentUrl("LOAN-" + APPLICATION_ID)
                .build();

        when(decisionService.getContract(eq(APPLICATION_ID))).thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri(BASE + "/contract", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ContractDTO.class)
                .value(body -> {
                    assertThat(body.getContractId()).isEqualTo(CONTRACT_ID);
                    assertThat(body.getStatus()).isEqualTo("PENDING_APPROVAL");
                    assertThat(body.getSignedAt()).isNull();
                });
    }

    // -------------------------------------------------------------------------
    // POST /contract/sign
    // -------------------------------------------------------------------------

    @Test
    void signContract_returns200WithSignedContract() {
        var dto = ContractDTO.builder()
                .contractId(CONTRACT_ID)
                .applicationId(APPLICATION_ID)
                .status("ACTIVE")
                .documentUrl("LOAN-" + APPLICATION_ID)
                .signedAt(LocalDateTime.now())
                .build();

        when(decisionService.signContract(eq(APPLICATION_ID), any(SignContractCommand.class)))
                .thenReturn(Mono.just(dto));

        webTestClient.post()
                .uri(BASE + "/contract/sign", APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"applicationId": "%s"}
                        """.formatted(APPLICATION_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody(ContractDTO.class)
                .value(body -> {
                    assertThat(body.getContractId()).isEqualTo(CONTRACT_ID);
                    assertThat(body.getStatus()).isEqualTo("ACTIVE");
                    assertThat(body.getSignedAt()).isNotNull();
                });
    }

    @Test
    void signContract_returns500WhenServiceFails() {
        when(decisionService.signContract(eq(APPLICATION_ID), any(SignContractCommand.class)))
                .thenReturn(Mono.error(new RuntimeException("SCA initiation failed")));

        webTestClient.post()
                .uri(BASE + "/contract/sign", APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"applicationId": "%s"}
                        """.formatted(APPLICATION_ID))
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
