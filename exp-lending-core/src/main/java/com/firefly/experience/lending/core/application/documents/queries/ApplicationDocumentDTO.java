package com.firefly.experience.lending.core.application.documents.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Metadata record for a document attached to a loan application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDocumentDTO {

    private UUID documentId;
    private UUID applicationId;
    private String fileName;
    private String documentType;
    private LocalDateTime uploadedAt;
    private Long size;
}
