package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A single installment plan entry for an active loan.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentDTO {

    private UUID installmentId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amount;
    /** SCHEDULED, PAID, or OVERDUE */
    private String status;
}
