package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A payment record applied against a specific loan installment.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstallmentPaymentDTO {

    private UUID paymentId;
    private BigDecimal amount;
    private LocalDateTime paidAt;
}
