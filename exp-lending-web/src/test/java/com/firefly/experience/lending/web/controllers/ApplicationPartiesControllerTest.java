package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.parties.commands.AddApplicationPartyCommand;
import com.firefly.experience.lending.core.application.parties.commands.UpdateApplicationPartyCommand;
import com.firefly.experience.lending.core.application.parties.queries.ApplicationPartyDTO;
import com.firefly.experience.lending.core.application.parties.services.ApplicationPartiesService;
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

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ApplicationPartiesController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class ApplicationPartiesControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ApplicationPartiesService applicationPartiesService;

    // fireflyframework-web's GlobalExceptionHandler required beans
    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID PARTY_ID = UUID.randomUUID();
    private static final String BASE_PATH = "/api/v1/experience/lending/applications/{id}/parties";

    private ApplicationPartyDTO sampleParty() {
        return ApplicationPartyDTO.builder()
                .partyId(PARTY_ID)
                .applicationId(APPLICATION_ID)
                .role("CO_HOLDER")
                .addedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void listParties_returns200WithPartyList() {
        when(applicationPartiesService.listParties(eq(APPLICATION_ID)))
                .thenReturn(Flux.just(sampleParty()));

        webTestClient.get()
                .uri(BASE_PATH, APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApplicationPartyDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getPartyId()).isEqualTo(PARTY_ID);
                    assertThat(list.get(0).getRole()).isEqualTo("CO_HOLDER");
                });
    }

    @Test
    void listParties_returns200WithEmptyList() {
        when(applicationPartiesService.listParties(eq(APPLICATION_ID)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH, APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApplicationPartyDTO.class)
                .hasSize(0);
    }

    @Test
    void addParty_returns201WithBody() {
        when(applicationPartiesService.addParty(eq(APPLICATION_ID), any(AddApplicationPartyCommand.class)))
                .thenReturn(Mono.just(sampleParty()));

        webTestClient.post()
                .uri(BASE_PATH, APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "partyId": "%s",
                            "role": "CO_HOLDER"
                        }
                        """.formatted(PARTY_ID))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ApplicationPartyDTO.class)
                .value(body -> {
                    assertThat(body.getPartyId()).isEqualTo(PARTY_ID);
                    assertThat(body.getApplicationId()).isEqualTo(APPLICATION_ID);
                    assertThat(body.getRole()).isEqualTo("CO_HOLDER");
                });
    }

    @Test
    void addParty_returns500WhenServiceFails() {
        when(applicationPartiesService.addParty(eq(APPLICATION_ID), any()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        webTestClient.post()
                .uri(BASE_PATH, APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"partyId": "%s", "role": "GUARANTOR"}
                        """.formatted(PARTY_ID))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void getParty_returns200WithBody() {
        when(applicationPartiesService.getParty(eq(APPLICATION_ID), eq(PARTY_ID)))
                .thenReturn(Mono.just(sampleParty()));

        webTestClient.get()
                .uri(BASE_PATH + "/{partyId}", APPLICATION_ID, PARTY_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApplicationPartyDTO.class)
                .value(body -> {
                    assertThat(body.getPartyId()).isEqualTo(PARTY_ID);
                    assertThat(body.getApplicationId()).isEqualTo(APPLICATION_ID);
                });
    }

    @Test
    void updateParty_returns200WithBody() {
        var updated = ApplicationPartyDTO.builder()
                .partyId(PARTY_ID)
                .applicationId(APPLICATION_ID)
                .role("GUARANTOR")
                .addedAt(LocalDateTime.now())
                .build();

        when(applicationPartiesService.updateParty(eq(APPLICATION_ID), eq(PARTY_ID), any(UpdateApplicationPartyCommand.class)))
                .thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri(BASE_PATH + "/{partyId}", APPLICATION_ID, PARTY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"role": "GUARANTOR"}
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ApplicationPartyDTO.class)
                .value(body -> assertThat(body.getRole()).isEqualTo("GUARANTOR"));
    }

    @Test
    void removeParty_returns204() {
        when(applicationPartiesService.removeParty(eq(APPLICATION_ID), eq(PARTY_ID)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_PATH + "/{partyId}", APPLICATION_ID, PARTY_ID)
                .exchange()
                .expectStatus().isNoContent();
    }
}
