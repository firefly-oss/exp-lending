package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Current outstanding balance snapshot for an active loan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDTO {

    private UUID loanId;
    private BigDecimal outstandingPrincipal;
    private BigDecimal outstandingInterest;
    private BigDecimal totalOutstanding;
    private String currency;
    private LocalDateTime asOfDate;
}
