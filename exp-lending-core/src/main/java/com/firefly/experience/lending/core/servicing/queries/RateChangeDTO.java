package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A historical interest rate change record for an active loan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateChangeDTO {

    private UUID changeId;
    private BigDecimal previousRate;
    private BigDecimal newRate;
    private LocalDate effectiveDate;
}
