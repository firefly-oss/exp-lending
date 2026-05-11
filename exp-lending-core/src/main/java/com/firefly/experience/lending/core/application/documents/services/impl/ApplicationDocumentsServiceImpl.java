package com.firefly.experience.lending.core.application.documents.services.impl;

import com.firefly.domain.lending.loan.origination.sdk.api.LoanOriginationApi;
import com.firefly.domain.lending.loan.origination.sdk.model.ApplicationDocumentDTO;
import com.firefly.domain.lending.loan.origination.sdk.model.RegisterApplicationDocumentCommand;
import com.firefly.experience.lending.core.application.documents.commands.UploadDocumentCommand;
import com.firefly.experience.lending.core.application.documents.services.ApplicationDocumentsService;
import com.firefly.experience.lending.core.util.IdempotencyKeys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.fireflyframework.web.error.exceptions.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link ApplicationDocumentsService}, delegating to the
 * domain Loan Origination SDK's {@code LoanOriginationApi} for document operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationDocumentsServiceImpl implements ApplicationDocumentsService {

    private static final String DEFAULT_MIME_TYPE = "application/octet-stream";

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
        log.debug("Uploading document for applicationId={} fileName={}", applicationId, command.getFileName());

        String resolvedMimeType = inferMimeType(command.getFileName());

        var sdkCmd = new RegisterApplicationDocumentCommand()
                .documentName(command.getFileName())
                .documentTypeCode(command.getDocumentType())
                .mimeType(resolvedMimeType)
                .fileSizeBytes(command.getContent() != null ? (long) command.getContent().length : null);

        // Same logical upload → same key. Avoids the duplicate-row hazard of UUID.randomUUID().
        String attachKey = IdempotencyKeys.of(
                "exp-lending", "upload-document",
                applicationId.toString(),
                String.valueOf(command.getFileName()),
                String.valueOf(command.getDocumentType()),
                command.getContent() != null ? String.valueOf(command.getContent().length) : "null");

        return loanOriginationApi
                .attachDocuments(applicationId, sdkCmd, attachKey)
                .flatMap(response -> {
                    UUID documentId = extractUuid(
                            response instanceof Map<?, ?> m ? m : Map.of(),
                            "applicationDocumentId");
                    if (documentId == null) {
                        return Mono.error(new BusinessException(
                                HttpStatus.BAD_GATEWAY,
                                "UPSTREAM_PROTOCOL_ERROR",
                                "domain attachDocuments did not return applicationDocumentId"));
                    }
                    String getKey = IdempotencyKeys.of(
                            "exp-lending", "upload-document", "get",
                            applicationId.toString(), documentId.toString());
                    return loanOriginationApi.getApplicationDocumentById(applicationId, documentId, getKey);
                })
                .map(this::mapToDTO);
    }

    private UUID extractUuid(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof String s) return UUID.fromString(s);
        if (value instanceof UUID u) return u;
        return null;
    }

    /**
     * Best-effort mime-type inference from a filename's extension. Falls back to
     * application/octet-stream when the extension is missing or unknown — the
     * core service stores whatever we send and does not derive its own value, so
     * sending an opaque-but-honest fallback is safer than guessing.
     */
    public static String inferMimeType(String fileName) {
        if (fileName == null) {
            return DEFAULT_MIME_TYPE;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return DEFAULT_MIME_TYPE;
        }
        String ext = fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return switch (ext) {
            case "pdf"  -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png"  -> "image/png";
            case "gif"  -> "image/gif";
            case "tif", "tiff" -> "image/tiff";
            case "webp" -> "image/webp";
            case "txt"  -> "text/plain";
            case "csv"  -> "text/csv";
            case "html", "htm" -> "text/html";
            case "json" -> "application/json";
            case "xml"  -> "application/xml";
            case "doc"  -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls"  -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            default     -> DEFAULT_MIME_TYPE;
        };
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
