package com.firefly.experience.lending.web.controllers;

import com.firefly.experience.lending.core.application.documents.commands.UploadDocumentCommand;
import com.firefly.experience.lending.core.application.documents.queries.ApplicationDocumentDTO;
import com.firefly.experience.lending.core.application.documents.services.ApplicationDocumentsService;
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

@WebFluxTest(controllers = ApplicationDocumentsController.class,
        excludeAutoConfiguration = {
                ReactiveSecurityAutoConfiguration.class,
                ReactiveUserDetailsServiceAutoConfiguration.class
        })
class ApplicationDocumentsControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private ApplicationDocumentsService applicationDocumentsService;

    // fireflyframework-web's GlobalExceptionHandler required beans
    @MockBean
    private ExceptionConverterService exceptionConverterService;
    @MockBean
    private ErrorHandlingProperties errorHandlingProperties;
    @MockBean
    private ErrorResponseNegotiator errorResponseNegotiator;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID DOCUMENT_ID = UUID.randomUUID();
    private static final String BASE_PATH = "/api/v1/experience/lending/applications/{id}/documents";

    @Test
    void listDocuments_returns200WithDocumentList() {
        var doc = ApplicationDocumentDTO.builder()
                .documentId(DOCUMENT_ID)
                .applicationId(APPLICATION_ID)
                .fileName("contract.pdf")
                .documentType("application/pdf")
                .uploadedAt(LocalDateTime.now())
                .size(2048L)
                .build();

        when(applicationDocumentsService.listDocuments(eq(APPLICATION_ID))).thenReturn(Flux.just(doc));

        webTestClient.get()
                .uri(BASE_PATH, APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApplicationDocumentDTO.class)
                .hasSize(1)
                .value(list -> {
                    assertThat(list.get(0).getDocumentId()).isEqualTo(DOCUMENT_ID);
                    assertThat(list.get(0).getFileName()).isEqualTo("contract.pdf");
                });
    }

    @Test
    void listDocuments_returns200WithEmptyList() {
        when(applicationDocumentsService.listDocuments(eq(APPLICATION_ID))).thenReturn(Flux.empty());

        webTestClient.get()
                .uri(BASE_PATH, APPLICATION_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ApplicationDocumentDTO.class)
                .hasSize(0);
    }

    @Test
    void uploadDocument_returns201WithBody() {
        var doc = ApplicationDocumentDTO.builder()
                .documentId(DOCUMENT_ID)
                .applicationId(APPLICATION_ID)
                .fileName("payslip.pdf")
                .documentType("application/pdf")
                .uploadedAt(LocalDateTime.now())
                .size(512L)
                .build();

        when(applicationDocumentsService.uploadDocument(eq(APPLICATION_ID), any(UploadDocumentCommand.class)))
                .thenReturn(Mono.just(doc));

        webTestClient.post()
                .uri(BASE_PATH, APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "fileName": "payslip.pdf",
                            "documentType": "application/pdf"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ApplicationDocumentDTO.class)
                .value(body -> {
                    assertThat(body.getDocumentId()).isEqualTo(DOCUMENT_ID);
                    assertThat(body.getFileName()).isEqualTo("payslip.pdf");
                    assertThat(body.getSize()).isEqualTo(512L);
                });
    }

    @Test
    void uploadDocument_returns500WhenServiceFails() {
        when(applicationDocumentsService.uploadDocument(eq(APPLICATION_ID), any()))
                .thenReturn(Mono.error(new RuntimeException("upload failed")));

        webTestClient.post()
                .uri(BASE_PATH, APPLICATION_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {"fileName": "fail.pdf", "documentType": "application/pdf"}
                        """)
                .exchange()
                .expectStatus().is5xxServerError();
    }

    @Test
    void downloadDocument_returns200WithBytes() {
        when(applicationDocumentsService.downloadDocument(eq(APPLICATION_ID), eq(DOCUMENT_ID)))
                .thenReturn(Mono.just(new byte[]{1, 2, 3}));

        webTestClient.get()
                .uri(BASE_PATH + "/{docId}", APPLICATION_ID, DOCUMENT_ID)
                .exchange()
                .expectStatus().isOk()
                .expectBody(byte[].class)
                .value(body -> assertThat(body).hasSize(3));
    }

    @Test
    void deleteDocument_returns204() {
        when(applicationDocumentsService.deleteDocument(eq(APPLICATION_ID), eq(DOCUMENT_ID)))
                .thenReturn(Mono.empty());

        webTestClient.delete()
                .uri(BASE_PATH + "/{docId}", APPLICATION_ID, DOCUMENT_ID)
                .exchange()
                .expectStatus().isNoContent();
    }
}
