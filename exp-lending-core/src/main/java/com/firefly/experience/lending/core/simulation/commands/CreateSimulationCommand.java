package com.firefly.experience.lending.core.simulation.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to create a new loan simulation for a given product, amount, and term.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSimulationCommand {

    private BigDecimal amount;
    private Integer term;
    private String productType;
    private UUID productId;
}
