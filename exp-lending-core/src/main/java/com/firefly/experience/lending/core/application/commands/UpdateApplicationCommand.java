package com.firefly.experience.lending.core.application.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Command to update editable fields of an existing loan application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationCommand {

    private BigDecimal requestedAmount;
    private Integer term;
    private String purpose;
}
