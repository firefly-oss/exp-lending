package com.firefly.experience.lending.core.application.documents.services.impl;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationDocumentDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.RegisterApplicationDocumentCommand;
import com.firefly.experience.lending.core.application.documents.commands.UploadDocumentCommand;
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
 * domain Loan Origination SDK's {@code LoanOriginationApi} for document operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationDocumentsServiceImpl implements ApplicationDocumentsService {

    private final LoanOriginationApi loanOriginationApi;

    @Override
    public Flux<com.firefly.experience.lending.core.application.documents.queries.ApplicationDocumentDTO> listDocuments(UUID applicationId) {
        log.debug("Listing documents for applicationId={}", applicationId);
        return loanOriginationApi
                .getApplicationDocuments(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .map(this::mapToDTO);
    }

    @Override
    public Mono<com.firefly.experience.lending.core.application.documents.queries.ApplicationDocumentDTO> uploadDocument(UUID applicationId, UploadDocumentCommand command) {
        log.debug("Uploading document for applicationId={}", applicationId);

        var sdkCmd = new RegisterApplicationDocumentCommand()
                .documentName(command.getFileName())
                .mimeType(command.getDocumentType())
                .fileSizeBytes(command.getContent() != null ? (long) command.getContent().length : null);

        return loanOriginationApi
                .attachDocuments(applicationId, sdkCmd, UUID.randomUUID().toString())
                .then(Mono.defer(() -> loanOriginationApi.getApplicationDocuments(applicationId, null)))
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .filter(d -> command.getFileName().equals(d.getDocumentName()))
                .next()
                .map(this::mapToDTO);
    }

    @Override
    public Mono<byte[]> downloadDocument(UUID applicationId, UUID documentId) {
        // MVP: the domain SDK returns document metadata only; binary content is managed by an ECM layer.
        // Returns empty bytes until ECM integration is wired.
        log.debug("Downloading document documentId={} for applicationId={}", documentId, applicationId);
        return loanOriginationApi
                .getApplicationDocuments(applicationId, null)
                .flatMapIterable(page -> page.getContent() != null ? page.getContent() : List.of())
                .filter(d -> documentId.equals(d.getApplicationDocumentId()))
                .next()
                .map(d -> new byte[0]);
    }

    @Override
    public Mono<Void> deleteDocument(UUID applicationId, UUID documentId) {
        // MVP: the domain SDK does not expose a delete-document endpoint.
        // Operation completes as a no-op until the domain layer surfaces this endpoint.
        log.debug("Deleting document documentId={} for applicationId={}", documentId, applicationId);
        return Mono.empty();
    }

    private com.firefly.experience.lending.core.application.documents.queries.ApplicationDocumentDTO mapToDTO(ApplicationDocumentDTO src) {
        return com.firefly.experience.lending.core.application.documents.queries.ApplicationDocumentDTO.builder()
                .documentId(src.getApplicationDocumentId())
                .applicationId(src.getLoanApplicationId())
                .fileName(src.getDocumentName())
                .documentType(src.getMimeType())
                .uploadedAt(src.getReceivedAt() != null ? src.getReceivedAt() : src.getCreatedAt())
                .size(src.getFileSizeBytes())
                .build();
    }
}
