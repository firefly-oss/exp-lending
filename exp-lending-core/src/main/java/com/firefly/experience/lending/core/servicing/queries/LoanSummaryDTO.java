package com.firefly.experience.lending.core.servicing.queries;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Summary view of an active loan servicing case for use in list responses.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class LoanSummaryDTO {

    private UUID loanId;
    private String productType;
    private String status;
    private BigDecimal outstandingBalance;
    private LocalDate nextPaymentDate;
    private BigDecimal nextPaymentAmount;
}
