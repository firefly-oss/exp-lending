package com.firefly.experience.lending.core.application.decision.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Loan contract associated with a loan application, including its status and document reference.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContractDTO {

    private UUID contractId;
    private UUID applicationId;

    /** Contract lifecycle status (mirrors ContractStatusEnum). */
    private String status;

    /** URL or reference to the contract document in the ECM store. */
    private String documentUrl;

    /** Timestamp when all parties completed e-signature — null until signing is complete. */
    private LocalDateTime signedAt;
}
