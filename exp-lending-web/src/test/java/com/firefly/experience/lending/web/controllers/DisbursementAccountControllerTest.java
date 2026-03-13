package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.disbursement.commands.ConfigureDisbursementAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.commands.RegisterExternalAccountCommand;
import com.firefly.experience.lending.core.application.disbursement.queries.DisbursementAccountDTO;
import com.firefly.experience.lending.core.application.disbursement.queries.ExternalAccountDTO;
import com.firefly.experience.lending.core.application.disbursement.services.DisbursementAccountService;
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

@WebFluxTest(controllers = DisbursementAccountController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class DisbursementAccountControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private DisbursementAccountService disbursementAccountService;

    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID ACCOUNT_ID = UUID.randomUUID();
    private static final String BASE = "/api/v1/experience/lending/applications/{id}";

    // --- GET /disbursement-account ---

    @Test
    void getDisbursementAccount_returns200WithBody() {
        var dto = DisbursementAccountDTO.builder()
                .accountId(ACCOUNT_ID)
                .accountType("INTERNAL")
                .isDefault(true)
                .build();

        when(disbursementAccountService.getDisbursementAccount(eq(APPLICATION_ID)))
                .thenReturn(Mono.just(dto));

        webTestClient.get()
                .uri(BASE + "/disbursement-account", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DisbursementAccountDTO.class)
                .value(body -> {
                    assertThat(body.getAccountId()).isEqualTo(ACCOUNT_ID);
                    assertThat(body.getAccountType()).isEqualTo("INTERNAL");
                    assertThat(body.isDefault()).isTrue();
                });
    }

    @Test
    void getDisbursementAccount_returns404WhenNotConfigured() {
        when(disbursementAccountService.getDisbursementAccount(eq(APPLICATION_ID)))
                .thenReturn(Mono.empty());

        webTestClient.get()
                .uri(BASE + "/disbursement-account", APPLICATION_ID)
                .exchange()
                .expectStatus().isNotFound();
    }

    // --- PUT /disbursement-account ---

    @Test
    void configureDisbursementAccount_returns200WithUpdatedBody() {
        var dto = DisbursementAccountDTO.builder()
                .accountId(ACCOUNT_ID)
                .accountType("EXTERNAL")
                .iban("ES1234567890")
                .bankName("Firefly Bank")
                .isDefault(true)
                .build();

        when(disbursementAccountService.configureDisbursementAccount(eq(APPLICATION_ID),
                any(ConfigureDisbursementAccountCommand.class)))
                .thenReturn(Mono.just(dto));

        webTestClient.put()
                .uri(BASE + "/disbursement-account", APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "accountId": "%s",
                            "accountType": "EXTERNAL"
                        }
                        """.formatted(ACCOUNT_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody(DisbursementAccountDTO.class)
                .value(body -> {
                    assertThat(body.getAccountType()).isEqualTo("EXTERNAL");
                    assertThat(body.getIban()).isEqualTo("ES1234567890");
                });
    }

    @Test
    void configureDisbursementAccount_returns500WhenServiceFails() {
        when(disbursementAccountService.configureDisbursementAccount(eq(APPLICATION_ID), any()))
                .thenReturn(Mono.error(new RuntimeException("update failed")));

        webTestClient.put()
                .uri(BASE + "/disbursement-account", APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"accountId": "%s", "accountType": "INTERNAL"}
                        """.formatted(ACCOUNT_ID))
                .exchange()
                .expectStatus().is5xxServerError();
    }

    // --- POST /external-accounts ---

    @Test
    void registerExternalAccount_returns201WithBody() {
        var dto = ExternalAccountDTO.builder()
                .accountId(ACCOUNT_ID)
                .iban("ES9900112233")
                .bankName("Test Bank")
                .holderName("John Doe")
                .registeredAt(LocalDateTime.now())
                .build();

        when(disbursementAccountService.registerExternalAccount(eq(APPLICATION_ID),
                any(RegisterExternalAccountCommand.class)))
                .thenReturn(Mono.just(dto));

        webTestClient.post()
                .uri(BASE + "/external-accounts", APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "iban": "ES9900112233",
                            "bankName": "Test Bank",
                            "holderName": "John Doe"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ExternalAccountDTO.class)
                .value(body -> {
                    assertThat(body.getAccountId()).isEqualTo(ACCOUNT_ID);
                    assertThat(body.getIban()).isEqualTo("ES9900112233");
                    assertThat(body.getHolderName()).isEqualTo("John Doe");
                });
    }

    // --- GET /external-accounts ---

    @Test
    void listExternalAccounts_returns200WithList() {
        var dto = ExternalAccountDTO.builder()
                .accountId(ACCOUNT_ID)
                .iban("ES7700")
                .bankName("Bank A")
                .holderName("Alice")
                .registeredAt(LocalDateTime.now())
                .build();

        when(disbursementAccountService.listExternalAccounts(eq(APPLICATION_ID)))
                .thenReturn(Flux.just(dto));

        webTestClient.get()
                .uri(BASE + "/external-accounts", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ExternalAccountDTO.class)
                .hasSize(1)
                .value(list -> assertThat(list.get(0).getIban()).isEqualTo("ES7700"));
    }

    @Test
    void listExternalAccounts_returns200WithEmptyList() {
        when(disbursementAccountService.listExternalAccounts(eq(APPLICATION_ID)))
                .thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE + "/external-accounts", APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ExternalAccountDTO.class)
                .hasSize(0);
    }

    // --- DELETE /external-accounts/{accId} ---

    @Test
    void deleteExternalAccount_returns204() {
        when(disbursementAccountService.deleteExternalAccount(eq(APPLICATION_ID), eq(ACCOUNT_ID)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE + "/external-accounts/{accId}", APPLICATION_ID, ACCOUNT_ID)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void deleteExternalAccount_returns500WhenServiceFails() {
        when(disbursementAccountService.deleteExternalAccount(eq(APPLICATION_ID), eq(ACCOUNT_ID)))
                .thenReturn(Mono.error(new RuntimeException("delete failed")));

        webTestClient.delete()
                .uri(BASE + "/external-accounts/{accId}", APPLICATION_ID, ACCOUNT_ID)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
