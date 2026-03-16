package com.firefly.experience.lending.core.application.documents.services.impl;

import com.firefly.core.lending.origination.sdk.api.ApplicationDocumentApi;
import com.firefly.experience.lending.core.application.documents.commands.UploadDocumentCommand;
import com.firefly.experience.lending.core.application.documents.queries.ApplicationDocumentDTO;
import com.firefly.experience.lending.core.application.documents.services.ApplicationDocumentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link ApplicationDocumentsService}, delegating to the
 * Loan Origination SDK's {@code ApplicationDocumentApi} for document operations.
 *
 * // ARCH-EXCEPTION: No domain SDK exposes {@link ApplicationDocumentApi}; direct
 * core-lending-origination-sdk usage is temporary until the domain layer surfaces this endpoint.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationDocumentsServiceImpl implements ApplicationDocumentsService {

    private final ApplicationDocumentApi applicationDocumentApi;

    @Override
    public Flux<ApplicationDocumentDTO> listDocuments(UUID applicationId) {
        log.debug("Listing documents for applicationId={}", applicationId);
        return applicationDocumentApi
                .findAllDocuments(applicationId, null, null, null, null, UUID.randomUUID().toString())
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToDTO);
    }

    @Override
    public Mono<ApplicationDocumentDTO> uploadDocument(UUID applicationId, UploadDocumentCommand command) {
        log.debug("Uploading document for applicationId={}", applicationId);

        var sdkDto = new com.firefly.core.lending.origination.sdk.model.ApplicationDocumentDTO()
                .loanApplicationId(applicationId)
                .documentName(command.getFileName())
                .mimeType(command.getDocumentType())
                .isReceived(true)
                .isMandatory(false)
                .fileSizeBytes(command.getContent() != null ? (long) command.getContent().length : null);

        return applicationDocumentApi
                .createDocument(applicationId, sdkDto, UUID.randomUUID().toString())
                .map(this::mapToDTO);
    }

    @Override
    public Mono<byte[]> downloadDocument(UUID applicationId, UUID documentId) {
        // MVP: the core SDK returns document metadata only; binary content is managed by an ECM layer.
        // Returns empty bytes until ECM integration is wired.
        log.debug("Downloading document documentId={} for applicationId={}", documentId, applicationId);
        return applicationDocumentApi
                .getDocument(applicationId, documentId, UUID.randomUUID().toString())
                .map(d -> new byte[0]);
    }

    @Override
    public Mono<Void> deleteDocument(UUID applicationId, UUID documentId) {
        log.debug("Deleting document documentId={} for applicationId={}", documentId, applicationId);
        return applicationDocumentApi.deleteDocument(applicationId, documentId, UUID.randomUUID().toString());
    }

    private ApplicationDocumentDTO mapToDTO(com.firefly.core.lending.origination.sdk.model.ApplicationDocumentDTO src) {
        return ApplicationDocumentDTO.builder()
                .documentId(src.getApplicationDocumentId())
                .applicationId(src.getLoanApplicationId())
                .fileName(src.getDocumentName())
                .documentType(src.getMimeType())
                .uploadedAt(src.getReceivedAt() != null ? src.getReceivedAt() : src.getCreatedAt())
                .size(src.getFileSizeBytes())
                .build();
    }
}
