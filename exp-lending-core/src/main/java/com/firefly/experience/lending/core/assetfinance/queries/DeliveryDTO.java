package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A delivery record for a financed asset, including tracking and carrier details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryDTO {

    private UUID deliveryId;
    private String status;
    private String trackingNumber;
    private String carrierName;
    private LocalDateTime deliveredAt;
}
