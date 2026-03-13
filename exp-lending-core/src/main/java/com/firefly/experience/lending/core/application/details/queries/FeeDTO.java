package com.firefly.experience.lending.core.application.details.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * A fee associated with a loan application, including its amount, currency, and type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeDTO {

    private UUID feeId;
    private String feeName;
    private BigDecimal amount;
    private String currency;
    private String type;
}
