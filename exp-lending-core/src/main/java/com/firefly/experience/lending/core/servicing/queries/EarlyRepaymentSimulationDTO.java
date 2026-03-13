package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Simulated cost breakdown for a hypothetical early repayment on an active loan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarlyRepaymentSimulationDTO {

    private BigDecimal totalAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal savings;
}
