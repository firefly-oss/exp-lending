package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Current interest rate information for an active loan, including type and next review date.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RateInfoDTO {

    private BigDecimal currentRate;
    /** FIXED or VARIABLE */
    private String rateType;
    private LocalDate nextReviewDate;
}
