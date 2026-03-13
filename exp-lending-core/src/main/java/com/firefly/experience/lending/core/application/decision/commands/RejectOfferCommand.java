package com.firefly.experience.lending.core.application.decision.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to reject a proposed offer on a loan application, with an optional reason.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RejectOfferCommand {

    /** Optional explanation provided by the applicant for rejecting the offer. */
    private String reason;
}
