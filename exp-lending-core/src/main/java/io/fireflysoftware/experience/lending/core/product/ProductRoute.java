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

import java.util.Map;

/**
 * Route loaded from reference data. Paths and attributes are not hardcoded in the journey.
 */
public record ProductRoute(
        CreditProductRoute route,
        String createPath,
        String idField,
        Map<String, Object> attributes,
        Map<String, String> operations) {

    public ProductRoute {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
        operations = operations == null ? Map.of() : Map.copyOf(operations);
    }

    public static ProductRoute origination() {
        return new ProductRoute(CreditProductRoute.LOAN_ORIGINATION, null, null, Map.of(), Map.of());
    }

    public String operation(LoanCustomerOperation operation) {
        return operations.get(operation.name());
    }
}
