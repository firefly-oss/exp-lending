package com.firefly.experience.lending.core.assetfinance.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to initiate a return process for a financed asset.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestReturnCommand {

    private UUID assetId;
    private String reason;
}
