package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A repayment record applied to an active loan (regular or early repayment).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepaymentDTO {

    private UUID repaymentId;
    private BigDecimal amount;
    /** REGULAR or EARLY */
    private String type;
    private LocalDateTime appliedAt;
}
