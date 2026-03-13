package com.firefly.experience.lending.core.application.parties.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to associate a new party with a loan application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddApplicationPartyCommand {

    private UUID partyId;
    private String role;
}
