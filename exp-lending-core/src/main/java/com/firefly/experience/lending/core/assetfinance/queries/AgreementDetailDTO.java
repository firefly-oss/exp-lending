package com.firefly.experience.lending.core.assetfinance.queries;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Full detail view of an asset finance agreement, including financial terms and dates.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class AgreementDetailDTO extends AgreementSummaryDTO {

    private BigDecimal residualValue;
    private BigDecimal purchaseOptionPrice;
    private LocalDate startDate;
    private LocalDate endDate;
}
