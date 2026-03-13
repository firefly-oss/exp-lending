package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A return record for a financed asset, including the condition report and any damage costs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReturnDTO {

    private UUID returnId;
    private String conditionReport;
    private BigDecimal damageCost;
    private boolean isFinalized;
}
