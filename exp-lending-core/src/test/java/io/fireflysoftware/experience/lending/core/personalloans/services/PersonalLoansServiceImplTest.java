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


package io.fireflysoftware.experience.lending.core.personalloans.services;

import io.fireflysoftware.domain.lending.personalloans.sdk.api.PersonalLoansApi;
import io.fireflysoftware.domain.lending.personalloans.sdk.model.PaginationResponsePersonalLoanAgreementDTO;
import io.fireflysoftware.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO;
import io.fireflysoftware.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO.AgreementStatusEnum;
import io.fireflysoftware.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO.LoanPurposeEnum;
import io.fireflysoftware.domain.lending.personalloans.sdk.model.PersonalLoanAgreementDTO.RateTypeEnum;
import io.fireflysoftware.experience.lending.core.personalloans.services.impl.PersonalLoansServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalLoansServiceImplTest {

    @Mock
    private PersonalLoansApi personalLoansApi;

    private PersonalLoansServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new PersonalLoansServiceImpl(personalLoansApi);
    }

    @Test
    void listAgreements_mapsSdkPage() {
        var agreementId = UUID.randomUUID();
        var agreement = new PersonalLoanAgreementDTO()
                .personalLoanAgreementId(agreementId)
                .loanPurpose(LoanPurposeEnum.GENERAL_PURPOSE)
                .agreementStatus(AgreementStatusEnum.ACTIVE)
                .rateType(RateTypeEnum.FIXED)
                .interestRate(new BigDecimal("7.50"));
        var page = new PaginationResponsePersonalLoanAgreementDTO().addContentItem(agreement);
        when(personalLoansApi.listAgreements(anyString())).thenReturn(Mono.just(page));

        StepVerifier.create(service.listAgreements())
                .assertNext(summary -> {
                    assertThat(summary.getAgreementId()).isEqualTo(agreementId);
                    assertThat(summary.getLoanPurpose()).isEqualTo(LoanPurposeEnum.GENERAL_PURPOSE.name());
                    assertThat(summary.getStatus()).isEqualTo(AgreementStatusEnum.ACTIVE.getValue());
                    assertThat(summary.getRateType()).isEqualTo(RateTypeEnum.FIXED.name());
                    assertThat(summary.getInterestRate()).isEqualByComparingTo("7.50");
                })
                .verifyComplete();
    }

    @Test
    void listAgreements_emptyWhenSdkContentIsNull() {
        when(personalLoansApi.listAgreements(anyString()))
                .thenReturn(Mono.just(new PaginationResponsePersonalLoanAgreementDTO()));

        StepVerifier.create(service.listAgreements())
                .verifyComplete();
    }
}
