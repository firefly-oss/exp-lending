package com.firefly.experience.lending.core.application.documents.services;

import com.firefly.experience.lending.core.application.documents.commands.UploadDocumentCommand;
import com.firefly.experience.lending.core.application.documents.queries.ApplicationDocumentDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Service for managing documents attached to a loan application,
 * including listing, uploading, downloading, and deleting documents.
 */
public interface ApplicationDocumentsService {

    Flux<ApplicationDocumentDTO> listDocuments(UUID applicationId);

    Mono<ApplicationDocumentDTO> uploadDocument(UUID applicationId, UploadDocumentCommand command);

    Mono<byte[]> downloadDocument(UUID applicationId, UUID documentId);

    Mono<Void> deleteDocument(UUID applicationId, UUID documentId);
}
