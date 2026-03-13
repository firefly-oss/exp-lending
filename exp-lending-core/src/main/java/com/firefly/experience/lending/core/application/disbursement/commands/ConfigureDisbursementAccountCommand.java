package com.firefly.experience.lending.core.application.disbursement.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to set the disbursement account (internal or external) on a loan application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigureDisbursementAccountCommand {

    /** ID of the internal or external account to use for disbursement. */
    private UUID accountId;

    /** Account type: {@code INTERNAL} or {@code EXTERNAL}. */
    private String accountType;
}
