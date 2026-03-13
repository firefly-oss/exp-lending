package com.firefly.experience.lending.core.application.disbursement.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Configured disbursement account (internal or external) for a loan application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementAccountDTO {

    private UUID accountId;
    private String iban;
    private String bankName;
    private String accountType;
    private boolean isDefault;
}
