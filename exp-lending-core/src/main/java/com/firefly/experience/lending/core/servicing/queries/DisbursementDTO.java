package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A disbursement record for an active loan, including amount, status, and disbursement timestamp.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DisbursementDTO {

    private UUID disbursementId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime disbursedAt;
}
