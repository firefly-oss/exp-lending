/*
 * Copyright 2025 Firefly Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package io.fireflysoftware.experience.lending.core.application.disbursement.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to set the disbursement account (internal or external) on a loan application.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfigureDisbursementAccountCommand {

    /** ID of the internal or external account to use for disbursement. */
    private UUID accountId;

    /** Account type: {@code INTERNAL} or {@code EXTERNAL}. */
    private String accountType;
}
