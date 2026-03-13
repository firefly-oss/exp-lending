package com.firefly.experience.lending.core.application.decision.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to accept a proposed offer on a loan application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptOfferCommand {

    private UUID offerId;
}
