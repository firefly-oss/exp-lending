package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A usage record (e.g. mileage snapshot) for a financed asset.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageRecordDTO {

    private UUID recordId;
    private Long mileage;
    private String usageDetail;
    private LocalDateTime reportedAt;
}
