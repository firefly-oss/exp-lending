package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Metadata for a document attached to an active loan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanDocumentDTO {

    private UUID documentId;
    private String name;
    private String type;
    private LocalDateTime uploadedAt;
}
