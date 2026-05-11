package com.firefly.experience.lending.core.application.documents.services;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationDocumentDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.PaginationResponseApplicationDocumentDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.RegisterApplicationDocumentCommand;
import com.firefly.experience.lending.core.application.documents.commands.UploadDocumentCommand;
import com.firefly.experience.lending.core.application.documents.services.impl.ApplicationDocumentsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationDocumentsServiceImplTest {

    @Mock
    private LoanOriginationApi loanOriginationApi;

    @InjectMocks
    private ApplicationDocumentsServiceImpl service;

    private static final UUID APPLICATION_ID = UUID.randomUUID();
    private static final UUID DOCUMENT_ID = UUID.randomUUID();

    @Test
    void listDocuments_returnsDocumentsMappedFromPage() {
        var sdkDto = new ApplicationDocumentDTO()
                .applicationDocumentId(DOCUMENT_ID)
                .loanApplicationId(APPLICATION_ID)
                .documentName("passport.pdf")
                .mimeType("application/pdf")
                .receivedAt(LocalDateTime.now())
                .fileSizeBytes(1024L);

        var page = new PaginationResponseApplicationDocumentDTO()
                .content(List.of(sdkDto));

        when(loanOriginationApi.getApplicationDocuments(eq(APPLICATION_ID), isNull()))
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

        when(loanOriginationApi.getApplicationDocuments(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(page));

        StepVerifier.create(service.listDocuments(APPLICATION_ID))
                .verifyComplete();
    }

    @Test
    void listDocuments_propagatesUpstreamError() {
        when(loanOriginationApi.getApplicationDocuments(eq(APPLICATION_ID), isNull()))
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
        command.setDocumentType("ID_DOCUMENT");
        command.setContent(new byte[]{1, 2, 3});

        Map<String, UUID> attachResponse = Map.of("applicationDocumentId", DOCUMENT_ID);

        var sdkResponse = new ApplicationDocumentDTO()
                .applicationDocumentId(DOCUMENT_ID)
                .loanApplicationId(APPLICATION_ID)
                .documentName("id_card.jpg")
                .mimeType("image/jpeg")
                .receivedAt(LocalDateTime.now())
                .fileSizeBytes(3L);

        when(loanOriginationApi.attachDocuments(eq(APPLICATION_ID), any(RegisterApplicationDocumentCommand.class), any(String.class)))
                .thenReturn(Mono.just(attachResponse));
        when(loanOriginationApi.getApplicationDocumentById(eq(APPLICATION_ID), eq(DOCUMENT_ID), any(String.class)))
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
    void uploadDocument_passesDocumentTypeAsCodeAndDerivesMimeTypeFromFileName() {
        var command = new UploadDocumentCommand();
        command.setApplicationId(APPLICATION_ID);
        command.setFileName("nomina_enero_2025.pdf");
        command.setDocumentType("PAYSLIP");
        command.setContent(new byte[]{1, 2, 3, 4});

        when(loanOriginationApi.attachDocuments(eq(APPLICATION_ID), any(RegisterApplicationDocumentCommand.class), any(String.class)))
                .thenReturn(Mono.just(Map.of("applicationDocumentId", DOCUMENT_ID)));
        when(loanOriginationApi.getApplicationDocumentById(eq(APPLICATION_ID), eq(DOCUMENT_ID), any(String.class)))
                .thenReturn(Mono.just(new ApplicationDocumentDTO()
                        .applicationDocumentId(DOCUMENT_ID)
                        .loanApplicationId(APPLICATION_ID)
                        .documentName("nomina_enero_2025.pdf")
                        .mimeType("application/pdf")
                        .fileSizeBytes(4L)));

        StepVerifier.create(service.uploadDocument(APPLICATION_ID, command))
                .expectNextCount(1)
                .verifyComplete();

        ArgumentCaptor<RegisterApplicationDocumentCommand> sent =
                ArgumentCaptor.forClass(RegisterApplicationDocumentCommand.class);
        verify(loanOriginationApi).attachDocuments(eq(APPLICATION_ID), sent.capture(), any(String.class));
        assertThat(sent.getValue().getDocumentTypeCode()).isEqualTo("PAYSLIP");
        assertThat(sent.getValue().getMimeType()).isEqualTo("application/pdf");
        assertThat(sent.getValue().getDocumentName()).isEqualTo("nomina_enero_2025.pdf");
        assertThat(sent.getValue().getFileSizeBytes()).isEqualTo(4L);
    }

    @Test
    void inferMimeType_returnsOctetStreamForUnknownOrMissingExtension() {
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType(null)).isEqualTo("application/octet-stream");
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType("noextension")).isEqualTo("application/octet-stream");
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType("trailing.dot.")).isEqualTo("application/octet-stream");
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType("weird.unknownext")).isEqualTo("application/octet-stream");
    }

    @Test
    void inferMimeType_recognisesCommonExtensions() {
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType("payslip.pdf")).isEqualTo("application/pdf");
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType("id.JPG")).isEqualTo("image/jpeg");
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType("scan.jpeg")).isEqualTo("image/jpeg");
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType("logo.png")).isEqualTo("image/png");
        assertThat(ApplicationDocumentsServiceImpl.inferMimeType("data.csv")).isEqualTo("text/csv");
    }

    @Test
    void uploadDocument_propagatesUpstreamError() {
        var command = new UploadDocumentCommand();
        command.setFileName("fail.pdf");
        command.setDocumentType("application/pdf");
        command.setContent(new byte[0]);

        when(loanOriginationApi.attachDocuments(eq(APPLICATION_ID), any(), any()))
                .thenReturn(Mono.error(new RuntimeException("create failed")));

        StepVerifier.create(service.uploadDocument(APPLICATION_ID, command))
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    void uploadDocument_failsWithBusinessException_whenAttachResponseMissingDocumentId() {
        var command = new UploadDocumentCommand();
        command.setFileName("anonymous.pdf");
        command.setDocumentType("application/pdf");
        command.setContent(new byte[]{9});

        when(loanOriginationApi.attachDocuments(eq(APPLICATION_ID), any(), any()))
                .thenReturn(Mono.just(Map.of()));

        StepVerifier.create(service.uploadDocument(APPLICATION_ID, command))
                .expectError(org.fireflyframework.web.error.exceptions.BusinessException.class)
                .verify();
    }

    @Test
    void downloadDocument_returnsEmptyByteArray() {
        var sdkResponse = new ApplicationDocumentDTO()
                .applicationDocumentId(DOCUMENT_ID)
                .loanApplicationId(APPLICATION_ID);

        when(loanOriginationApi.getApplicationDocuments(eq(APPLICATION_ID), isNull()))
                .thenReturn(Mono.just(new PaginationResponseApplicationDocumentDTO().content(List.of(sdkResponse))));

        StepVerifier.create(service.downloadDocument(APPLICATION_ID, DOCUMENT_ID))
                .assertNext(bytes -> assertThat(bytes).isEmpty())
                .verifyComplete();
    }

    @Test
    void deleteDocument_completesSuccessfully() {
        StepVerifier.create(service.deleteDocument(APPLICATION_ID, DOCUMENT_ID))
                .verifyComplete();
    }

    @Test
    void deleteDocument_completesAsNoOp() {
        StepVerifier.create(service.deleteDocument(APPLICATION_ID, DOCUMENT_ID))
                .verifyComplete();
    }
}
