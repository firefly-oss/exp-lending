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


package io.fireflysoftware.experience.lending.core.product;

import io.fireflysoftware.experience.lending.core.application.commands.CreateApplicationCommand;
import io.fireflysoftware.experience.lending.core.application.queries.ApplicationDetailDTO;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

/**
 * Domain port for a credit product other than generic loan origination.
 * A future BPM replaces the HTTP adapter.
 */
public interface CreditProductPort {

    Mono<ApplicationDetailDTO> open(ProductRoute route, CreateApplicationCommand command);

    Mono<ApplicationDetailDTO> command(ProductRoute route, UUID agreementId, LoanCustomerOperation operation,
                                       Map<String, Object> body);
}
