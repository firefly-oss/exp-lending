package com.firefly.experience.lending.core.assetfinance.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to report a new usage record (e.g. mileage reading) for a financed asset.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportUsageCommand {

    private Long mileage;
    private String usageDetail;
}
