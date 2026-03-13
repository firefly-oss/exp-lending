package com.firefly.experience.lending.core.collections.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Command to register a promise-to-pay agreement against a collection case.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPaymentPromiseCommand {

    private BigDecimal promisedAmount;
    private LocalDate promiseDate;
}
