package com.firefly.experience.lending.core.assetfinance.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Command to record a new service event (maintenance, damage, or inspection) against a financed asset.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportServiceEventCommand {

    private String eventType;
    private String description;
    private BigDecimal cost;
}
