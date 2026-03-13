package com.firefly.experience.lending.core.servicing.queries;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * An escrow account associated with an active loan (e.g. for insurance or tax reserves).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EscrowDTO {

    private UUID escrowId;
    private String type;
    private BigDecimal balance;
}
