/*
 * Copyright 2025 Firefly Software Solutions Inc
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

package com.firefly.experience.lending.core.personalloans.services.impl;

import com.firefly.domain.lending.personalloans.sdk.api.PersonalLoansApi;
import com.firefly.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO;
import com.firefly.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO.EarlyRepaymentPenaltyTypeEnum;
import com.firefly.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO.InsuranceTypeEnum;
import com.firefly.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO.LoanPurposeEnum;
import com.firefly.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO.RateTypeEnum;
import com.firefly.experience.lending.core.personalloans.commands.CreatePersonalLoanCommand;
import com.firefly.experience.lending.core.personalloans.queries.PersonalLoanDetailDTO;
import com.firefly.experience.lending.core.personalloans.queries.PersonalLoanSummaryDTO;
import com.firefly.experience.lending.core.personalloans.services.PersonalLoansService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * Default implementation of {@link PersonalLoansService}, delegating to the Personal Loans SDK
 * for agreement creation, retrieval, listing, and update operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PersonalLoansServiceImpl implements PersonalLoansService {

    private final PersonalLoansApi personalLoansApi;

    // -------------------------------------------------------------------------
    // Agreements
    // -------------------------------------------------------------------------

    @Override
    public Mono<PersonalLoanDetailDTO> createAgreement(CreatePersonalLoanCommand command) {
        log.debug("Creating personal loan agreement applicationId={}", command.getApplicationId());
        PersonalLoanAgreementDTO dto = new PersonalLoanAgreementDTO()
                .applicationId(command.getApplicationId())
                .loanPurpose(toEnum(LoanPurposeEnum.class, command.getLoanPurpose()))
                .rateType(toEnum(RateTypeEnum.class, command.getRateType()))
                .interestRate(command.getInterestRate())
                .insuranceType(toEnum(InsuranceTypeEnum.class, command.getInsuranceType()))
                .earlyRepaymentPenaltyType(toEnum(EarlyRepaymentPenaltyTypeEnum.class, command.getEarlyRepaymentPenaltyType()));
        return personalLoansApi.createAgreement(dto, UUID.randomUUID().toString())
                .map(this::toDetail);
    }

    @Override
    public Mono<PersonalLoanDetailDTO> getAgreement(UUID agreementId) {
        log.debug("Getting personal loan agreement agreementId={}", agreementId);
        return personalLoansApi.getAgreement(agreementId, UUID.randomUUID().toString())
                .map(this::toDetail);
    }

    @Override
    public Flux<PersonalLoanSummaryDTO> listAgreements() {
        // MVP: domain-lending-personal-loans-sdk does not expose a findAll agreements
        // endpoint. Replace when upstream adds a paginated agreements resource.
        log.debug("Listing personal loan agreements (MVP stub)");
        return Flux.empty();
    }

    @Override
    public Mono<PersonalLoanDetailDTO> updateAgreement(UUID agreementId, CreatePersonalLoanCommand command) {
        log.debug("Updating personal loan agreement agreementId={}", agreementId);
        PersonalLoanAgreementDTO dto = new PersonalLoanAgreementDTO()
                .applicationId(command.getApplicationId())
                .loanPurpose(toEnum(LoanPurposeEnum.class, command.getLoanPurpose()))
                .rateType(toEnum(RateTypeEnum.class, command.getRateType()))
                .interestRate(command.getInterestRate())
                .insuranceType(toEnum(InsuranceTypeEnum.class, command.getInsuranceType()))
                .earlyRepaymentPenaltyType(toEnum(EarlyRepaymentPenaltyTypeEnum.class, command.getEarlyRepaymentPenaltyType()));
        return personalLoansApi.updateAgreement(agreementId, dto, UUID.randomUUID().toString())
                .map(this::toDetail);
    }

    // -------------------------------------------------------------------------
    // Mappers — typed SDK DTOs
    // -------------------------------------------------------------------------

    private PersonalLoanDetailDTO toDetail(PersonalLoanAgreementDTO dto) {
        return PersonalLoanDetailDTO.builder()
                .agreementId(dto.getPersonalLoanAgreementId())
                .applicationId(dto.getApplicationId())
                .servicingCaseId(dto.getServicingCaseId())
                .loanPurpose(enumValue(dto.getLoanPurpose()))
                .purposeDescription(dto.getPurposeDescription())
                .status(dto.getAgreementStatus() != null ? dto.getAgreementStatus().getValue() : null)
                .rateType(enumValue(dto.getRateType()))
                .interestRate(dto.getInterestRate())
                .insuranceType(enumValue(dto.getInsuranceType()))
                .insurancePremiumRate(dto.getInsurancePremiumRate())
                .earlyRepaymentPenaltyType(enumValue(dto.getEarlyRepaymentPenaltyType()))
                .earlyRepaymentPenaltyRate(dto.getEarlyRepaymentPenaltyRate())
                .isUnsecured(dto.getIsUnsecured())
                .guarantorRequired(dto.getGuarantorRequired())
                .coolingOffPeriodDays(dto.getCoolingOffPeriodDays())
                .originationFeeRate(dto.getOriginationFeeRate())
                .originationFeeAmount(dto.getOriginationFeeAmount())
                .agreementSignedDate(dto.getAgreementSignedDate())
                .agreementEffectiveDate(dto.getAgreementEffectiveDate())
                .build();
    }

    private PersonalLoanSummaryDTO toSummary(PersonalLoanAgreementDTO dto) {
        return PersonalLoanSummaryDTO.builder()
                .agreementId(dto.getPersonalLoanAgreementId())
                .loanPurpose(enumValue(dto.getLoanPurpose()))
                .status(dto.getAgreementStatus() != null ? dto.getAgreementStatus().getValue() : null)
                .rateType(enumValue(dto.getRateType()))
                .interestRate(dto.getInterestRate())
                .build();
    }

    // -------------------------------------------------------------------------
    // Mappers — untyped PaginationResponse items (Map extraction)
    // -------------------------------------------------------------------------

    private PersonalLoanSummaryDTO toSummaryFromMap(Object item) {
        if (item instanceof PersonalLoanAgreementDTO typed) {
            return toSummary(typed);
        }
        Map<?, ?> m = toMap(item);
        return PersonalLoanSummaryDTO.builder()
                .agreementId(extractUuid(m, "personalLoanAgreementId"))
                .loanPurpose(extractString(m, "loanPurpose"))
                .status(extractString(m, "agreementStatus"))
                .rateType(extractString(m, "rateType"))
                .interestRate(extractBigDecimal(m, "interestRate"))
                .build();
    }

    // -------------------------------------------------------------------------
    // Map extraction helpers
    // -------------------------------------------------------------------------

    private Map<?, ?> toMap(Object item) {
        return item instanceof Map<?, ?> m ? m : Map.of();
    }

    private UUID extractUuid(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof String s) {
            try { return UUID.fromString(s); } catch (IllegalArgumentException e) { return null; }
        }
        if (value instanceof UUID u) return u;
        return null;
    }

    private BigDecimal extractBigDecimal(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        if (value instanceof String s) {
            try { return new BigDecimal(s); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private String extractString(Map<?, ?> map, String key) {
        Object value = map.get(key);
        return value instanceof String s ? s : (value != null ? value.toString() : null);
    }

    private LocalDate extractLocalDate(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (value instanceof String s && !s.isBlank()) {
            try { return LocalDate.parse(s); } catch (Exception e) { return null; }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Enum conversion helpers
    // -------------------------------------------------------------------------

    private <E extends Enum<E>> E toEnum(Class<E> enumClass, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown enum value '{}' for {}", value, enumClass.getSimpleName());
            return null;
        }
    }

    private String enumValue(Enum<?> enumValue) {
        return enumValue != null ? enumValue.name() : null;
    }
}
