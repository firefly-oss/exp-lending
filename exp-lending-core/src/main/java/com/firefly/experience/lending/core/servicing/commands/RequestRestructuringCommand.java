package com.firefly.experience.lending.core.servicing.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Command to submit a restructuring request for an active loan.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestRestructuringCommand {

    private String reason;
    private String requestedTermChanges;
}
