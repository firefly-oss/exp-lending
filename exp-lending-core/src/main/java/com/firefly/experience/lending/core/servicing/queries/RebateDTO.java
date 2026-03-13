package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A rebate applied to an active loan, with the amount and reason for the credit.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebateDTO {

    private UUID rebateId;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime appliedAt;
}
