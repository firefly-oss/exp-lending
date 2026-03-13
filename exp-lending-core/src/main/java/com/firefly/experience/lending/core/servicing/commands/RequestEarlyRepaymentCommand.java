package com.firefly.experience.lending.core.servicing.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Command to register a partial or full early repayment against an active loan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestEarlyRepaymentCommand {

    private BigDecimal amount;
    /** PARTIAL or FULL */
    private String type;
}
