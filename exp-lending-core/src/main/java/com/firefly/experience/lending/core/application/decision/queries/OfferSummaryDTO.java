package com.firefly.experience.lending.core.application.decision.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Summary view of a proposed loan offer for use in list responses.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class OfferSummaryDTO {

    private UUID offerId;
    private BigDecimal amount;
    private Integer term;
    private BigDecimal monthlyPayment;
    private BigDecimal annualRate;

    /** Offer lifecycle status: DRAFT, SENT, ACCEPTED, REJECTED, EXPIRED. */
    private String status;
}
