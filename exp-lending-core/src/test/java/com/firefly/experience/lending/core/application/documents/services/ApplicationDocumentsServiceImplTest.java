package com.firefly.experience.lending.core.application.documents.services;

import com.firefly.core.lending.origination.sdk.api.ApplicationDocumentApi;
import com.firefly.core.lending.origination.sdk.model.ApplicationDocumentDTO;
import com.firefly.core.lending.origination.sdk.model.PaginationResponseApplicationDocumentDTO;
import com.firefly.experience.lending.core.application.documents.commands.UploadDocumentCommand;
import com.firefly.experience.lending.core.application.documents.services.impl.ApplicationDocumentsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentsServiceImplTest {

    @Mock
    private ApplicationDocumentApi applicationDocumentApi;

    @InjectMocks
    private ApplicationDocumentsServiceImpl service;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID DOCUMENT_ID = UUID.randomUUID();

    @Test
    void listDocuments_returnsDocumentsMappedFromPage() {
        var sdkDto = new ApplicationDocumentDTO(DOCUMENT_ID)
                .loanApplicationId(APPLICATION_ID)
                .documentName("passport.pdf")
                .mimeType("application/pdf")
                .receivedAt(LocalDateTime.now())
                .fileSizeBytes(1024L);

        var page = new PaginationResponseApplicationDocumentDTO()
                .content(List.of(sdkDto));

        when(applicationDocumentApi.findAllDocuments(eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listDocuments(APPLICATION_ID))
                .assertNext(dto -> {
                    assertThat(dto.getDocumentId()).isEqualTo(DOCUMENT_ID);
                    assertThat(dto.getApplicationId()).isEqualTo(APPLICATION_ID);
                    assertThat(dto.getFileName()).isEqualTo("passport.pdf");
                    assertThat(dto.getDocumentType()).isEqualTo("application/pdf");
                    assertThat(dto.getSize()).isEqualTo(1024L);
                })
                .verifyComplete();
    }

    @Test
    void listDocuments_returnsEmptyWhenPageContentIsNull() {
        var page = new PaginationResponseApplicationDocumentDTO().content(null);

        when(applicationDocumentApi.findAllDocuments(eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listDocuments(APPLICATION_ID))
                .verifyComplete();
    }

    @Test
    void listDocuments_propagatesUpstreamError() {
        when(applicationDocumentApi.findAllDocuments(eq(APPLICATION_ID), isNull(), isNull(), isNull(), isNull(), isNull()))
                .thenReturn(Mono.error(new RuntimeException("upstream error")));

        StepVerifier.create(service.listDocuments(APPLICATION_ID))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void uploadDocument_returnsMappedDTO() {
        var command = new UploadDocumentCommand();
        command.setApplicationId(APPLICATION_ID);
        command.setFileName("id_card.jpg");
        command.setDocumentType("image/jpeg");
        command.setContent(new byte[]{1, 2, 3});

        var sdkResponse = new ApplicationDocumentDTO(DOCUMENT_ID)
                .loanApplicationId(APPLICATION_ID)
                .documentName("id_card.jpg")
                .mimeType("image/jpeg")
                .receivedAt(LocalDateTime.now())
                .fileSizeBytes(3L);

        when(applicationDocumentApi.createDocument(eq(APPLICATION_ID), any(ApplicationDocumentDTO.class), any(String.class)))
                .thenReturn(Mono.just(sdkResponse));

        StepVerifier.create(service.uploadDocument(APPLICATION_ID, command))
                .assertNext(dto -> {
                    assertThat(dto.getDocumentId()).isEqualTo(DOCUMENT_ID);
                    assertThat(dto.getFileName()).isEqualTo("id_card.jpg");
                    assertThat(dto.getSize()).isEqualTo(3L);
                })
                .verifyComplete();
    }

    @Test
    void uploadDocument_propagatesUpstreamError() {
        var command = new UploadDocumentCommand();
        command.setFileName("fail.pdf");
        command.setDocumentType("application/pdf");
        command.setContent(new byte[0]);

        when(applicationDocumentApi.createDocument(eq(APPLICATION_ID), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("create failed")));

        StepVerifier.create(service.uploadDocument(APPLICATION_ID, command))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void downloadDocument_returnsEmptyByteArray() {
        var sdkResponse = new ApplicationDocumentDTO(DOCUMENT_ID)
                .loanApplicationId(APPLICATION_ID);

        when(applicationDocumentApi.getDocument(eq(APPLICATION_ID), eq(DOCUMENT_ID), isNull()))
                .thenReturn(Mono.just(sdkResponse));

        StepVerifier.create(service.downloadDocument(APPLICATION_ID, DOCUMENT_ID))
                .assertNext(bytes -> assertThat(bytes).isEmpty())
                .verifyComplete();
    }

    @Test
    void deleteDocument_completesSuccessfully() {
        when(applicationDocumentApi.deleteDocument(eq(APPLICATION_ID), eq(DOCUMENT_ID), any(String.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(service.deleteDocument(APPLICATION_ID, DOCUMENT_ID))
                .verifyComplete();
    }

    @Test
    void deleteDocument_propagatesUpstreamError() {
        when(applicationDocumentApi.deleteDocument(eq(APPLICATION_ID), eq(DOCUMENT_ID), any()))
                .thenReturn(Mono.error(new RuntimeException("delete failed")));

        StepVerifier.create(service.deleteDocument(APPLICATION_ID, DOCUMENT_ID))
                .expectError(RuntimeException.class)
                .verify();
    }
}
