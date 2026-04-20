package com.firefly.experience.lending.core.personalloans.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Summary view of a personal loan agreement for use in list responses.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalLoanSummaryDTO {

    private UUID agreementId;
    private String loanPurpose;
    private String status;
    private String rateType;
    private BigDecimal interestRate;
}
