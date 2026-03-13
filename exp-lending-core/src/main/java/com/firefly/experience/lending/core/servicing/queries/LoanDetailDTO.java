package com.firefly.experience.lending.core.servicing.queries;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Full detail view of an active loan servicing case, including terms and key dates.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LoanDetailDTO extends LoanSummaryDTO {

    private BigDecimal originalAmount;
    private Integer term;
    private BigDecimal interestRate;
    private LocalDate disbursementDate;
    private LocalDate maturityDate;
}
