package com.firefly.experience.lending.core.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Full detail view of a loan application, including status, amounts, and audit timestamps.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailDTO {

    private UUID applicationId;
    private String productType;
    private String status;
    private BigDecimal requestedAmount;
    private Integer term;
    private String purpose;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
