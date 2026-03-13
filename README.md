# exp-lending

> Backend-for-Frontend service for the lending portal — covers loan simulation, application lifecycle, underwriting decisions, contract signing, active loan servicing, asset finance, and collections

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Module Structure](#module-structure)
- [Functional Verticals](#functional-verticals)
- [API Endpoints](#api-endpoints)
- [Domain SDK Dependencies](#domain-sdk-dependencies)
- [Configuration](#configuration)
- [Running Locally](#running-locally)
- [Testing](#testing)

## Overview

`exp-lending` is the experience-layer service that powers all lending-related screens across the digital channel. It covers the full credit lifecycle — from the initial product simulation and eligibility check, through multi-step application management, underwriting decision retrieval, offer selection, SCA-protected contract signing, post-disbursement loan servicing, and asset finance agreement management, down to collections case handling.

The service uses **simple stateless composition** throughout. Every endpoint delegates to one or more downstream domain or core SDK calls, maps the result to an experience-layer DTO, and returns. There is no `@Workflow`, no Redis, and no persistent journey state within this service. The loan application lifecycle (create, update, submit, withdraw) is managed as individual independent SDK calls to the loan origination domain service, with each state transition owned by the domain layer.

Given the breadth of the lending domain, the `-core` module is organised by functional vertical (simulation, origination, servicing, asset finance, collections), each with its own package subtree containing service interfaces, implementations, command DTOs, and query DTOs. Ten controllers in the `-web` module expose these verticals over REST, split by resource type to keep each controller focused and maintainable.

## Architecture

```
Frontend / Mobile App
         |
         v
exp-lending  (port 8102)
         |
         +---> domain-lending-loan-origination-sdk  (application lifecycle)
         |
         +---> core-lending-loan-origination-sdk    (application details, documents, parties)
         |
         +---> core-lending-loan-servicing-sdk      (active loans, installments, repayments,
         |                                           disbursements, escrow, accruals, rebates,
         |                                           restructuring, early repayment)
         |
         +---> core-lending-credit-scoring-sdk      (scoring status, underwriting decision)
         |
         +---> core-lending-asset-finance-sdk       (leasing/renting agreements, assets,
         |                                           deliveries, returns, pickups, service
         |                                           events, usage, end options)
         |
         +---> core-common-contracts-sdk            (contract retrieval and signing)
         |
         +---> core-common-sca-sdk                  (SCA challenge for contract signing)
         |
         +---> core-common-notifications-sdk        (loan-related notification history)
         |
         +---> domain-product-pricing-sdk           (loan simulation, eligibility)
```

## Module Structure

| Module | Purpose |
|--------|---------|
| `exp-lending-interfaces` | Reserved for future shared contracts |
| `exp-lending-core` | Service interfaces and implementations per vertical, command and query DTOs, organised by `simulation/`, `application/`, `servicing/`, `assetfinance/`, and `collections/` sub-packages |
| `exp-lending-infra` | `ClientFactory` beans and `@ConfigurationProperties` for each downstream SDK |
| `exp-lending-web` | 10 REST controllers (one per vertical), Spring Boot application class, `application.yaml` |
| `exp-lending-sdk` | Auto-generated reactive SDK from the OpenAPI spec |

## Functional Verticals

| Vertical | Controller | Base Path | Endpoints |
|----------|-----------|-----------|-----------|
| Simulation & Eligibility | `SimulationController` | `/api/v1/experience/lending` | 3 |
| Application Lifecycle | `ApplicationController` | `/api/v1/experience/lending/applications` | 7 |
| Application Details | `ApplicationDetailsController` | `/api/v1/experience/lending/applications/{id}` | 5 |
| Application Documents | `ApplicationDocumentsController` | `/api/v1/experience/lending/applications/{id}/documents` | 4 |
| Application Parties | `ApplicationPartiesController` | `/api/v1/experience/lending/applications/{id}/parties` | 5 |
| Decision & Contract | `DecisionController` | `/api/v1/experience/lending/applications/{id}` | 8 |
| Disbursement Account | `DisbursementAccountController` | `/api/v1/experience/lending/applications/{id}` | 5 |
| Active Loans | `LoanServicingController` | `/api/v1/experience/lending/loans` | 24 |
| Asset Finance | `AssetFinanceController` | `/api/v1/experience/lending/asset-finance` | 15 |
| Collections | `CollectionsController` | `/api/v1/experience/lending/collections` | 3 |

**Total: 79 endpoints across 10 controllers.**

## API Endpoints

### Simulation & Eligibility

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `POST` | `/api/v1/experience/lending/simulations` | Compute a loan simulation for a given amount, term, and product type | `201 Created` |
| `GET` | `/api/v1/experience/lending/simulations/{id}` | Retrieve a previously created loan simulation by its identifier | `200 OK` |
| `POST` | `/api/v1/experience/lending/eligibility` | Evaluate whether a party is eligible for a given product and requested amount | `200 OK` |

### Application Lifecycle

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `POST` | `/api/v1/experience/lending/applications` | Create a new loan application | `201 Created` |
| `GET` | `/api/v1/experience/lending/applications` | List all loan applications accessible to the caller | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}` | Retrieve the full details of a loan application | `200 OK` |
| `PATCH` | `/api/v1/experience/lending/applications/{id}` | Update editable fields on a loan application | `200 OK` |
| `POST` | `/api/v1/experience/lending/applications/{id}/submission` | Submit the application for underwriting review | `202 Accepted` |
| `POST` | `/api/v1/experience/lending/applications/{id}/withdraw` | Withdraw the application at the applicant's request | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}/status-history` | Return the full status transition history for the application | `200 OK` |

### Application Details

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/v1/experience/lending/applications/{id}/conditions` | List all conditions associated with the application | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}/tasks` | List all tasks associated with the application | `200 OK` |
| `PATCH` | `/api/v1/experience/lending/applications/{id}/tasks/{taskId}` | Mark an application task as COMPLETED | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}/fees` | List all fees associated with the application | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}/verifications` | List all verifications associated with the application | `200 OK` |

### Application Documents

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/v1/experience/lending/applications/{id}/documents` | List all documents attached to the application | `200 OK` |
| `POST` | `/api/v1/experience/lending/applications/{id}/documents` | Attach a new document to the application | `201 Created` |
| `GET` | `/api/v1/experience/lending/applications/{id}/documents/{docId}` | Download the binary content of an application document | `200 OK` |
| `DELETE` | `/api/v1/experience/lending/applications/{id}/documents/{docId}` | Remove a document from the application | `204 No Content` |

### Application Parties

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/v1/experience/lending/applications/{id}/parties` | List all parties associated with the application | `200 OK` |
| `POST` | `/api/v1/experience/lending/applications/{id}/parties` | Associate a new party (co-holder, guarantor, etc.) with the application | `201 Created` |
| `GET` | `/api/v1/experience/lending/applications/{id}/parties/{partyId}` | Retrieve details of a specific application party | `200 OK` |
| `PUT` | `/api/v1/experience/lending/applications/{id}/parties/{partyId}` | Update a party's information on the application | `200 OK` |
| `DELETE` | `/api/v1/experience/lending/applications/{id}/parties/{partyId}` | Remove a party from the application | `204 No Content` |

### Decision & Contract

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/v1/experience/lending/applications/{id}/scoring-status` | Return the current credit scoring status (PENDING → IN_PROGRESS → COMPLETED) | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}/decision` | Return the underwriting decision (APPROVED, REJECTED, or CONDITIONAL) with reason codes | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}/offers` | List all proposed loan offers for the application | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}/offers/{offerId}` | Return the full detail of a specific offer including total cost, itemised fees, and attached conditions | `200 OK` |
| `POST` | `/api/v1/experience/lending/applications/{id}/offers/{offerId}/accept` | Mark the specified offer as ACCEPTED | `200 OK` |
| `POST` | `/api/v1/experience/lending/applications/{id}/offers/{offerId}/reject` | Mark the specified offer as REJECTED with an optional reason | `200 OK` |
| `GET` | `/api/v1/experience/lending/applications/{id}/contract` | Return the loan contract associated with the application, including its current status | `200 OK` |
| `POST` | `/api/v1/experience/lending/applications/{id}/contract/sign` | Initiate SCA flow for contract signing and transition the contract to ACTIVE status | `200 OK` |

### Disbursement Account

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/v1/experience/lending/applications/{id}/disbursement-account` | Return the configured disbursement account for the application | `200 OK` |
| `PUT` | `/api/v1/experience/lending/applications/{id}/disbursement-account` | Set the disbursement account (internal or external) for the application | `200 OK` |
| `POST` | `/api/v1/experience/lending/applications/{id}/external-accounts` | Register a new external bank account for disbursement | `201 Created` |
| `GET` | `/api/v1/experience/lending/applications/{id}/external-accounts` | List all external bank accounts registered for the application | `200 OK` |
| `DELETE` | `/api/v1/experience/lending/applications/{id}/external-accounts/{accId}` | Remove an external bank account from the application | `204 No Content` |

### Active Loans

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/v1/experience/lending/loans` | List all active loan servicing cases accessible to the caller | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}` | Retrieve full details of an active loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/balance` | Return the current outstanding balance for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/balance/history` | Return the full balance snapshot history for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/schedule` | Return the full amortisation schedule for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/installments` | List all installment plan entries for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/installments/{instId}` | Return a single installment plan entry | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/installments/{instId}/payments` | Return payment records linked to a specific installment | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/disbursements` | List all disbursement records for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/disbursements/{dId}` | Return a single disbursement record | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/disbursement-plan` | Return the disbursement plan for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/repayments` | List all repayment records for the loan | `200 OK` |
| `POST` | `/api/v1/experience/lending/loans/{id}/early-repayment` | Register a partial or full early repayment against the loan | `202 Accepted` |
| `POST` | `/api/v1/experience/lending/loans/{id}/early-repayment/simulation` | Return a cost breakdown for a hypothetical early repayment | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/rate-info` | Return the current interest rate and type for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/rate-changes` | Return the history of interest rate changes for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/accruals` | List all interest accrual records for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/escrow` | List all escrow accounts associated with the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/rebates` | List all rebate records applied to the loan | `200 OK` |
| `POST` | `/api/v1/experience/lending/loans/{id}/restructuring` | Submit a restructuring request for the loan | `202 Accepted` |
| `GET` | `/api/v1/experience/lending/loans/{id}/restructurings` | Return the history of restructuring requests for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/documents` | List all documents attached to the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/events` | Return the servicing event log for the loan | `200 OK` |
| `GET` | `/api/v1/experience/lending/loans/{id}/notifications` | List all notifications sent in connection with the loan | `200 OK` |

### Asset Finance

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/v1/experience/lending/asset-finance` | List all asset finance agreements (leasing and renting) | `200 OK` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}` | Return full details of a single asset finance agreement | `200 OK` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/assets` | List all financed assets associated with the agreement | `200 OK` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}` | Return details of a single financed asset | `200 OK` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/deliveries` | List all delivery records for the asset | `200 OK` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/deliveries/{dId}` | Return details of a single delivery record | `200 OK` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/returns` | List all return records for the asset | `200 OK` |
| `POST` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/returns` | Initiate a return process for the asset | `201 Created` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/pickups` | List all pickup records for the asset | `200 OK` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/service-events` | List all service events (maintenance, damage, inspection) for the asset | `200 OK` |
| `POST` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/service-events` | Record a new service event for the asset | `201 Created` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/usage` | List all usage records (mileage snapshots) for the asset | `200 OK` |
| `POST` | `/api/v1/experience/lending/asset-finance/{id}/assets/{assetId}/usage` | Record a new usage snapshot (e.g. mileage reading) for the asset | `201 Created` |
| `GET` | `/api/v1/experience/lending/asset-finance/{id}/end-options` | List all end-of-term options (purchase, return, extend) for the agreement | `200 OK` |
| `POST` | `/api/v1/experience/lending/asset-finance/{id}/end-options/{optId}/exercise` | Exercise a lease-end option (e.g., purchase the asset) | `200 OK` |

### Collections

| Method | Path | Description | Response |
|--------|------|-------------|----------|
| `GET` | `/api/v1/experience/lending/collections` | List all active collection cases accessible to the caller | `200 OK` |
| `GET` | `/api/v1/experience/lending/collections/{id}` | Retrieve full details of a collection case including its actions | `200 OK` |
| `POST` | `/api/v1/experience/lending/collections/{id}/promise-to-pay` | Record a promise-to-pay agreement against a collection case | `201 Created` |

## Domain SDK Dependencies

| SDK | ClientFactory | APIs Used | Purpose |
|-----|--------------|-----------|---------|
| `domain-lending-loan-origination-sdk` | `LoanOriginationClientFactory` | Loan application APIs | Application lifecycle — create, update, submit, withdraw, status history |
| `core-lending-loan-origination-sdk` | `CoreLoanOriginationClientFactory` | Condition, task, fee, verification, party, document APIs | Application sub-resources — conditions, tasks, fees, verifications, documents, parties, disbursement account |
| `core-lending-loan-servicing-sdk` | `CoreLoanServicingClientFactory` | Loan servicing APIs | Active loan management — balance, schedule, installments, repayments, disbursements, accruals, escrow, rebates, rate changes, restructuring, early repayment |
| `core-lending-credit-scoring-sdk` | `CreditScoringClientFactory` | Scoring and decision APIs | Credit scoring status and underwriting decision retrieval |
| `core-lending-asset-finance-sdk` | `AssetFinanceClientFactory` | Agreement, asset, delivery, return, pickup, service event, usage, end option APIs | Asset finance agreement management across the full asset lifecycle |
| `core-common-contracts-sdk` | `ContractsClientFactory` | Contract and signature APIs | Loan contract retrieval and SCA-protected signing |
| `core-common-sca-sdk` | `ScaClientFactory` | SCA APIs | Strong Customer Authentication for contract signing |
| `core-common-notifications-sdk` | `NotificationsClientFactory` | Notifications APIs | Loan-related notification history on active loans |
| `domain-product-pricing-sdk` | `ProductPricingClientFactory` | Pricing and eligibility APIs | Loan simulation computation and product eligibility checks |

## Configuration

```yaml
server:
  port: ${SERVER_PORT:8102}

api-configuration:
  domain-platform:
    lending-loan-origination:
      base-path: ${LOAN_ORIGINATION_URL:http://localhost:8082}
  core-platform:
    lending-loan-origination:
      base-path: ${CORE_LOAN_ORIGINATION_URL:http://localhost:8081}
    lending-loan-servicing:
      base-path: ${LOAN_SERVICING_URL:http://localhost:8084}
    lending-credit-scoring:
      base-path: ${CREDIT_SCORING_URL:http://localhost:8042}
    lending-asset-finance:
      base-path: ${ASSET_FINANCE_URL:http://localhost:8043}
    product-pricing:
      base-path: ${PRODUCT_PRICING_URL:http://localhost:8086}
    common-contracts:
      base-path: ${CONTRACTS_URL:http://localhost:8090}
    common-sca:
      base-path: ${SCA_URL:http://localhost:8041}
    common-notifications:
      base-path: ${NOTIFICATIONS_URL:http://localhost:8095}
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SERVER_PORT` | `8102` | HTTP server port |
| `LOAN_ORIGINATION_URL` | `http://localhost:8082` | Base URL for `domain-lending-loan-origination` |
| `CORE_LOAN_ORIGINATION_URL` | `http://localhost:8081` | Base URL for `core-lending-loan-origination` |
| `LOAN_SERVICING_URL` | `http://localhost:8084` | Base URL for `core-lending-loan-servicing` |
| `CREDIT_SCORING_URL` | `http://localhost:8042` | Base URL for `core-lending-credit-scoring` |
| `ASSET_FINANCE_URL` | `http://localhost:8043` | Base URL for `core-lending-asset-finance` |
| `PRODUCT_PRICING_URL` | `http://localhost:8086` | Base URL for `domain-product-pricing` |
| `CONTRACTS_URL` | `http://localhost:8090` | Base URL for `core-common-contracts` |
| `SCA_URL` | `http://localhost:8041` | Base URL for `core-common-sca` |
| `NOTIFICATIONS_URL` | `http://localhost:8095` | Base URL for `core-common-notifications` |

## Running Locally

```bash
# Prerequisites — ensure all downstream services are running or accessible
cd exp-lending
mvn spring-boot:run -pl exp-lending-web
```

Server starts on port `8102`. Swagger UI: [http://localhost:8102/swagger-ui.html](http://localhost:8102/swagger-ui.html)

Swagger UI and API docs are disabled in the `pro` profile.

## Testing

```bash
mvn clean verify
```

Tests cover each service implementation in `exp-lending-core` (unit tests with mocked SDK clients using Mockito and `StepVerifier`) and all 10 controllers in `exp-lending-web` (WebTestClient-based tests verifying HTTP status codes and response shapes for all 79 endpoints).
