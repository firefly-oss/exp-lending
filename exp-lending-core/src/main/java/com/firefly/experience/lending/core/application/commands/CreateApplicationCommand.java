package com.firefly.experience.lending.core.application.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Command to create a new loan application for a given product, amount, term, and purpose.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateApplicationCommand {

    private UUID productId;
    private BigDecimal requestedAmount;
    private Integer term;
    private String purpose;
}
