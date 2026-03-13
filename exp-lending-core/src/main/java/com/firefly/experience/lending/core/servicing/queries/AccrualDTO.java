package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An interest accrual record for an active loan, representing a period's accrued interest amount.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccrualDTO {

    private UUID accrualId;
    private String period;
    private BigDecimal amount;
    private LocalDateTime calculatedAt;
}
