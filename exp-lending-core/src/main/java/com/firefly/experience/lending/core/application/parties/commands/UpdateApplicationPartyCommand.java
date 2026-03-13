package com.firefly.experience.lending.core.application.parties.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to update a party's role on a loan application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateApplicationPartyCommand {

    private String role;
}
