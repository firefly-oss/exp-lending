package com.firefly.experience.lending.core.personalloans.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to create or update a personal loan agreement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePersonalLoanCommand {

    private UUID applicationId;
    private String loanPurpose;
    private String rateType;
    private BigDecimal interestRate;
    private String insuranceType;
    private String earlyRepaymentPenaltyType;
}
