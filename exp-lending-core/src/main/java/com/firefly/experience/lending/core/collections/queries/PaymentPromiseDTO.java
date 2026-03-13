package com.firefly.experience.lending.core.collections.queries;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A promise-to-pay record registered by a customer against a collection case.
 */
@Data
@SuperBuilder
@NoArgsConstructor
public class PaymentPromiseDTO {

    private UUID promiseId;
    private BigDecimal promisedAmount;
    private LocalDate promiseDate;
    private String status;
}
