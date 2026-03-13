package com.firefly.experience.lending.core.application.disbursement.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * An external bank account registered on a loan application for disbursement purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalAccountDTO {

    private UUID accountId;
    private String iban;
    private String bankName;
    private String holderName;
    private LocalDateTime registeredAt;
}
