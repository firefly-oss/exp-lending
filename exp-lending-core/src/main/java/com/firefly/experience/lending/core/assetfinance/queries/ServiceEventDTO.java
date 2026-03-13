package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A service event (maintenance, damage, or inspection) recorded against a financed asset.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceEventDTO {

    private UUID eventId;
    private String eventType;
    private BigDecimal cost;
    private String description;
    private LocalDateTime eventDate;
}
