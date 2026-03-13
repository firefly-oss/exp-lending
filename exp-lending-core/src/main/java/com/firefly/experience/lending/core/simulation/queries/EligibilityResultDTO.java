package com.firefly.experience.lending.core.simulation.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Result of a product eligibility evaluation for a given party and requested amount.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EligibilityResultDTO {

    private UUID evaluationId;
    private boolean eligible;
    private BigDecimal maxAmount;
    private List<String> reasons;
}
