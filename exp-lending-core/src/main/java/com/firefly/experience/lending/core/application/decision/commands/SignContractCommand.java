package com.firefly.experience.lending.core.application.decision.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to initiate the SCA (Strong Customer Authentication) contract-signing flow.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SignContractCommand {

    /** Application identifier — echoed back to correlate the SCA operation. */
    private UUID applicationId;
}
