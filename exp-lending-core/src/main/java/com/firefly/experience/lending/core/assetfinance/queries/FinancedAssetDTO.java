package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A financed asset (vehicle, equipment, etc.) associated with an asset finance agreement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancedAssetDTO {

    private UUID assetId;
    private String description;
    private String serialNumber;
    private BigDecimal value;
    private boolean isActive;
}
