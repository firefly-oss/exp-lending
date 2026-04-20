package com.firefly.experience.lending.core.personalloans.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Full detail view of a personal loan agreement, including all financial terms and dates.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PersonalLoanDetailDTO extends PersonalLoanSummaryDTO {

    private UUID applicationId;
    private UUID servicingCaseId;
    private String purposeDescription;
    private String insuranceType;
    private BigDecimal insurancePremiumRate;
    private String earlyRepaymentPenaltyType;
    private BigDecimal earlyRepaymentPenaltyRate;
    private Boolean isUnsecured;
    private Boolean guarantorRequired;
    private Integer coolingOffPeriodDays;
    private BigDecimal originationFeeRate;
    private BigDecimal originationFeeAmount;
    private LocalDate agreementSignedDate;
    private LocalDate agreementEffectiveDate;
}
