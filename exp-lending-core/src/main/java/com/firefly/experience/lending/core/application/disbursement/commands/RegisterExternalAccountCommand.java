package com.firefly.experience.lending.core.application.disbursement.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to register a new external bank account on a loan application for disbursement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterExternalAccountCommand {

    private String iban;
    private String bankName;
    private String holderName;
}
