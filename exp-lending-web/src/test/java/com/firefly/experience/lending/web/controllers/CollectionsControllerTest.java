package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.collections.commands.RegisterPaymentPromiseCommand;
import com.firefly.experience.lending.core.collections.queries.CollectionCaseDetailDTO;
import com.firefly.experience.lending.core.collections.queries.CollectionCaseSummaryDTO;
import com.firefly.experience.lending.core.collections.queries.PaymentPromiseDTO;
import com.firefly.experience.lending.core.collections.services.CollectionsService;
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
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CollectionsController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class CollectionsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CollectionsService collectionsService;

    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final String BASE = "/api/v1/experience/lending/collections";

    // --- GET /collections ---

    @Test
    void listCollectionCases_returns200WithSummaries() {
        var caseId = UUID.randomUUID();
        var summary = CollectionCaseSummaryDTO.builder()
                .caseId(caseId)
                .loanId(caseId)
                .status("ACTIVE")
                .overdueAmount(new BigDecimal("2000.00"))
                .daysPastDue(15)
                .build();

        when(collectionsService.listCollectionCases()).thenReturn(Flux.just(summary));

        webTestClient.get()
                .uri(BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CollectionCaseSummaryDTO.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0).getCaseId()).isEqualTo(caseId);
                    assertThat(list.get(0).getStatus()).isEqualTo("ACTIVE");
                });
    }

    @Test
    void listCollectionCases_returns200WithEmptyList_whenNoResults() {
        when(collectionsService.listCollectionCases()).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CollectionCaseSummaryDTO.class)
                .value(list -> assertThat(list).isEmpty());
    }

    // --- GET /collections/{id} ---

    @Test
    void getCollectionCase_returns200WithDetail() {
        var caseId = UUID.randomUUID();
        var detail = CollectionCaseDetailDTO.builder()
                .caseId(caseId)
                .loanId(caseId)
                .status("DELINQUENT")
                .overdueAmount(new BigDecimal("3500.00"))
                .daysPastDue(45)
                .actions(List.of())
                .build();

        when(collectionsService.getCollectionCase(caseId)).thenReturn(Mono.just(detail));

        webTestClient.get()
                .uri(BASE + "/{id}", caseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CollectionCaseDetailDTO.class)
                .value(body -> {
                    assertThat(body.getCaseId()).isEqualTo(caseId);
                    assertThat(body.getStatus()).isEqualTo("DELINQUENT");
                    assertThat(body.getActions()).isEmpty();
                });
    }

    // --- POST /collections/{id}/promise-to-pay ---

    @Test
    void registerPaymentPromise_returns201WithPromise() {
        var caseId = UUID.randomUUID();
        var promiseId = UUID.randomUUID();
        var promise = PaymentPromiseDTO.builder()
                .promiseId(promiseId)
                .promisedAmount(new BigDecimal("1200.00"))
                .promiseDate(LocalDate.of(2026, 4, 15))
                .status("PENDING")
                .build();

        when(collectionsService.registerPaymentPromise(eq(caseId), any(RegisterPaymentPromiseCommand.class)))
                .thenReturn(Mono.just(promise));

        var command = new RegisterPaymentPromiseCommand();
        command.setPromisedAmount(new BigDecimal("1200.00"));
        command.setPromiseDate(LocalDate.of(2026, 4, 15));

        webTestClient.post()
                .uri(BASE + "/{id}/promise-to-pay", caseId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(command)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(PaymentPromiseDTO.class)
                .value(body -> {
                    assertThat(body.getPromiseId()).isEqualTo(promiseId);
                    assertThat(body.getPromisedAmount()).isEqualByComparingTo(new BigDecimal("1200.00"));
                    assertThat(body.getStatus()).isEqualTo("PENDING");
                });
    }
}
