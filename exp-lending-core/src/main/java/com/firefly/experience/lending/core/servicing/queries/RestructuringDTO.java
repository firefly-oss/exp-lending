package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A loan restructuring request record, including its status and new agreed terms.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestructuringDTO {

    private UUID restructuringId;
    private String status;
    private LocalDateTime requestedAt;
    private String newTerms;
}
