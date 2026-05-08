package com.firefly.experience.lending.core.simulation.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Aggregated result of a configured simulation, combining the pricing-engine
 * calculation with the persisted simulation identifier.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguredSimulationDTO {

    /** Identifier assigned by the persisting domain service. */
    private UUID simulationId;

    /** Product type, e.g. {@code PERSONAL_LOAN}. */
    private String productType;

    /** Identifier of the priced product. */
    private UUID productId;

    /** Requested principal amount. */
    private BigDecimal requestedAmount;

    /** Term in months. */
    private Integer term;

    /** Calculated monthly payment. */
    private BigDecimal monthlyPayment;

    /** Nominal annual interest rate. */
    private BigDecimal tin;

    /** APR (effective annual rate). */
    private BigDecimal tae;

    /** Sum of all payments + fees. */
    private BigDecimal totalAmount;

    /** Currency ISO 4217 code. */
    private String currency;
}
