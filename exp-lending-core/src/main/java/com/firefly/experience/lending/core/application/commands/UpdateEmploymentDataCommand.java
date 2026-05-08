package com.firefly.experience.lending.core.application.commands;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Command for updating an applicant's employment and economic profile attached
 * to a loan application. Maps to the SDK's
 * {@code EmploymentDataPatchRequest} after normalisation.
 *
 * <p>Date fields use the {@code MM/YYYY} format per the front-end contract
 * and are converted to {@link java.time.LocalDate} on the first day of the month
 * before being dispatched downstream.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEmploymentDataCommand {

    /**
     * Employment status from the front-end taxonomy.
     * Allowed: {@code private}, {@code public}, {@code civil}, {@code selfEmployed},
     * {@code entrepreneur}, {@code unemployed}, {@code retired}.
     */
    @Pattern(regexp = "^(private|public|civil|selfEmployed|entrepreneur|unemployed|retired)$")
    private String employmentStatus;

    /** Employment type label, e.g. {@code permanent}, {@code temporary}, {@code project}, {@code internship}. */
    private String employmentType;

    /** Employer legal name. */
    private String employer;

    /** Job position / title. */
    private String position;

    /** Employment start date as {@code MM/YYYY}. */
    @Pattern(regexp = "^(0[1-9]|1[0-2])/(19|20)\\d{2}$",
            message = "must be in MM/YYYY format")
    private String employmentStartDate;

    /** Number of paydays per year (12, 13 or 14 in Spain). */
    @Min(12)
    @Max(14)
    private Short annualPaydays;

    /** Gross monthly salary. */
    @PositiveOrZero
    private BigDecimal monthlySalary;

    /** Housing situation. Allowed: {@code rent}, {@code mortgage}, {@code owned}, {@code family}. */
    @Pattern(regexp = "^(rent|mortgage|owned|family)$")
    private String housingType;

    /** Monthly housing cost (rent or mortgage instalment). */
    @PositiveOrZero
    private BigDecimal housingCost;

    /** Housing arrangement start date as {@code MM/YYYY}. */
    @Pattern(regexp = "^(0[1-9]|1[0-2])/(19|20)\\d{2}$",
            message = "must be in MM/YYYY format")
    private String housingStartDate;

    /** Count of currently outstanding loans. */
    @Min(0)
    @Max(50)
    private Short existingLoans;

    /** Aggregated monthly debt service for other obligations. */
    @PositiveOrZero
    private BigDecimal otherDebts;
}
