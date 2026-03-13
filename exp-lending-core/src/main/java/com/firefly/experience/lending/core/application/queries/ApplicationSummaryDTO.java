package com.firefly.experience.lending.core.application.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Summary view of a loan application for use in list responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationSummaryDTO {

    private UUID applicationId;
    private String productType;
    private String status;
    private BigDecimal requestedAmount;
    private LocalDateTime createdAt;
}
