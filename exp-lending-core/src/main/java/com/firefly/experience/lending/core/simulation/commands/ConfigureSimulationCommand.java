package com.firefly.experience.lending.core.simulation.commands;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command for configuring a lending simulation. Drives a two-step flow:
 * (1) call pricing-engine to compute the simulation, (2) persist the result
 * via domain-lending-loan-origination.
 *
 * <p>Cross-field rules (validated in the service layer):
 * <ul>
 *   <li>{@code productType=PERSONAL_LOAN} requires {@code purpose}.</li>
 *   <li>{@code productType=LEASING} requires {@code sector} and {@code assetType}.</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigureSimulationCommand {

    /** Product type discriminator. Currently {@code PERSONAL_LOAN} or {@code LEASING}. */
    @NotBlank
    @Pattern(regexp = "^(PERSONAL_LOAN|LEASING)$")
    private String productType;

    /** Optional explicit product identifier. When omitted, the engine resolves the default. */
    private UUID productId;

    /** Requested principal amount. Must be strictly positive. */
    @NotNull
    @Positive
    private BigDecimal requestedAmount;

    /** Term in months. */
    @NotNull
    @Min(1)
    private Integer term;

    /** Required for {@code PERSONAL_LOAN}: e.g. {@code car}, {@code homeRenovation}. */
    private String purpose;

    /** Required for {@code LEASING}: business sector. */
    private String sector;

    /** Required for {@code LEASING}: asset type. */
    private String assetType;
}
