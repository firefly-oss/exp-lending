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

import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Product-to-domain routing. SandboxLendingCatalogAdapter is the class an installer replaces.
 */
public interface LendingCatalogPort {

    Mono<ProductRoute> routeFor(UUID productId);

    /**
     * Path template for a loan-servicing operation, or the tokens {@code payments} and {@code contracts}.
     * Empty when the operation is not enabled.
     */
    Mono<String> loanOperation(LoanCustomerOperation operation);
}
