package com.firefly.experience.lending.core.simulation.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Result of a loan simulation, including the computed rate, term, and product context.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResultDTO {

    private UUID simulationId;
    private BigDecimal monthlyPayment;
    private BigDecimal annualRate;
    private BigDecimal totalCost;
    private Integer term;
    private String productType;
}
