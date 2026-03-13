package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

/**
 * A pickup record for a financed asset, including status, scheduled date, and collector details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PickupDTO {

    private UUID pickupId;
    private String status;
    private LocalDate scheduledDate;
    private String collectorName;
}
