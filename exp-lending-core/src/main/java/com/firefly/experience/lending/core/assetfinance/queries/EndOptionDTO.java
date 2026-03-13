package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An end-of-term option (purchase, return, or extend) for an asset finance agreement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndOptionDTO {

    private UUID optionId;
    private String type;
    private BigDecimal paidAmount;
    private boolean isExercised;
}
