package com.firefly.experience.lending.core.simulation.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to check whether a party is eligible for a given product and requested amount.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckEligibilityCommand {

    private UUID partyId;
    private UUID productId;
    private BigDecimal requestedAmount;
}
