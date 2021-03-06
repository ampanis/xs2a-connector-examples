/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.aspsp.xs2a.connector.spi.impl.authorisation;

import de.adorsys.aspsp.xs2a.connector.spi.converter.ScaLoginMapper;
import de.adorsys.aspsp.xs2a.connector.spi.converter.ScaMethodConverter;
import de.adorsys.aspsp.xs2a.connector.spi.impl.AspspConsentDataService;
import de.adorsys.aspsp.xs2a.connector.spi.impl.CmsPaymentStatusUpdateService;
import de.adorsys.aspsp.xs2a.connector.spi.impl.FeignExceptionReader;
import de.adorsys.aspsp.xs2a.connector.spi.impl.payment.internal.PaymentInternalGeneral;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.OpTypeTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCALoginResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAPaymentResponseTO;
import de.adorsys.ledgers.middleware.api.domain.sca.SCAResponseTO;
import de.adorsys.ledgers.middleware.api.domain.um.ScaUserDataTO;
import de.adorsys.ledgers.middleware.api.service.TokenStorageService;
import de.adorsys.ledgers.rest.client.AuthRequestInterceptor;
import de.adorsys.ledgers.rest.client.PaymentRestClient;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiPsuAuthorisationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PaymentAuthorisationSpi;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class PaymentAuthorisationSpiImpl extends AbstractAuthorisationSpi<SpiPayment, SCAPaymentResponseTO> implements PaymentAuthorisationSpi {
    private static final Logger logger = LoggerFactory.getLogger(PaymentAuthorisationSpiImpl.class);

    private final TokenStorageService tokenStorageService;
    private final ScaLoginMapper scaLoginMapper;
    private final AspspConsentDataService consentDataService;
    private final PaymentRestClient paymentRestClient;
    private final CmsPaymentStatusUpdateService cmsPaymentStatusUpdateService;
    private final PaymentInternalGeneral paymentInternalGeneral;


    public PaymentAuthorisationSpiImpl(GeneralAuthorisationService authorisationService,
                                       TokenStorageService tokenStorageService, ScaMethodConverter scaMethodConverter,
                                       ScaLoginMapper scaLoginMapper,
                                       AuthRequestInterceptor authRequestInterceptor, AspspConsentDataService consentDataService,
                                       PaymentRestClient paymentRestClient, CmsPaymentStatusUpdateService cmsPaymentStatusUpdateService,
                                       FeignExceptionReader feignExceptionReader, PaymentInternalGeneral paymentInternalGeneral) {
        super(authRequestInterceptor, consentDataService, authorisationService, scaMethodConverter, feignExceptionReader, tokenStorageService);
        this.tokenStorageService = tokenStorageService;
        this.scaLoginMapper = scaLoginMapper;
        this.consentDataService = consentDataService;
        this.paymentRestClient = paymentRestClient;
        this.cmsPaymentStatusUpdateService = cmsPaymentStatusUpdateService;
        this.paymentInternalGeneral = paymentInternalGeneral;
    }

    @Override
    protected OpTypeTO getOtpType() {
        return OpTypeTO.PAYMENT;
    }

    @Override
    protected TppMessage getAuthorisePsuFailureMessage(SpiPayment businessObject) {
        logger.error("Initiate single payment failed: payment ID {}", businessObject.getPaymentId());
        return new TppMessage(MessageErrorCode.PAYMENT_FAILED);
    }

    @Override
    protected SpiResponse<SpiPsuAuthorisationResponse> onSuccessfulAuthorisation(SpiPayment businessObject, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider, SpiResponse<SpiPsuAuthorisationResponse> authorisePsu, SCAPaymentResponseTO scaBusinessObjectResponse) {
        SpiResponse<SpiPsuAuthorisationResponse> response = super.onSuccessfulAuthorisation(businessObject, aspspConsentDataProvider, authorisePsu, scaBusinessObjectResponse);
        if (!response.hasError() && businessObject.getPsuDataList().size() == 1) {
            cmsPaymentStatusUpdateService.updatePaymentStatus(businessObject.getPaymentId(), aspspConsentDataProvider);
        }
        return response;
    }

    @Override
    protected String getBusinessObjectId(SpiPayment businessObject) {
        return businessObject.getPaymentId();
    }

    @Override
    protected ResponseEntity<SCAPaymentResponseTO> getSelectMethodResponse(@NotNull String authenticationMethodId, SCAPaymentResponseTO sca) {
        return paymentRestClient.selectMethod(sca.getPaymentId(), sca.getAuthorisationId(), authenticationMethodId);
    }

    @Override
    protected SCAPaymentResponseTO getSCAConsentResponse(SpiAspspConsentDataProvider aspspConsentDataProvider, boolean checkCredentials) {
        byte[] aspspConsentData = aspspConsentDataProvider.loadAspspConsentData();
        return consentDataService.response(aspspConsentData, SCAPaymentResponseTO.class, checkCredentials);
    }

    @Override
    protected SCAResponseTO initiateBusinessObject(SpiPayment businessObject, byte[] aspspConsentData) {
        return paymentInternalGeneral.initiatePaymentInternal(businessObject, aspspConsentData);
    }

    @Override
    protected SCAPaymentResponseTO mapToScaResponse(SpiPayment businessObject, byte[] aspspConsentData, SCAPaymentResponseTO originalResponse) throws IOException {
        String paymentTypeString = Optional.ofNullable(businessObject.getPaymentType()).orElseThrow(() -> new IOException("Missing payment type")).name();
        SCALoginResponseTO scaResponseTO = tokenStorageService.fromBytes(aspspConsentData, SCALoginResponseTO.class);
        SCAPaymentResponseTO paymentResponse = scaLoginMapper.toPaymentResponse(scaResponseTO);
        paymentResponse.setObjectType(SCAPaymentResponseTO.class.getSimpleName());
        paymentResponse.setPaymentId(businessObject.getPaymentId());
        paymentResponse.setPaymentType(PaymentTypeTO.valueOf(paymentTypeString));
        paymentResponse.setPaymentProduct(businessObject.getPaymentProduct());
        paymentResponse.setMultilevelScaRequired(originalResponse.isMultilevelScaRequired());
        return paymentResponse;
    }

    @Override
    protected boolean isFirstInitiationOfMultilevelSca(SpiPayment businessObject, SCAPaymentResponseTO scaPaymentResponseTO) {
        return !scaPaymentResponseTO.isMultilevelScaRequired() || businessObject.getPsuDataList().size() <= 1;
    }

    @Override
    protected Optional<List<ScaUserDataTO>> getScaMethods(SCAPaymentResponseTO sca) {
        if (sca.getScaMethods() == null) {
            return Optional.of(Collections.emptyList());
        }

        return super.getScaMethods(sca);
    }

    @Override
    public @NotNull SpiResponse<Boolean> requestTrustedBeneficiaryFlag(@NotNull SpiContextData spiContextData, @NotNull SpiPayment payment, @NotNull String authorisationId, @NotNull SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        // TODO replace with real response from ledgers https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1263
        logger.info("Retrieving mock trusted beneficiaries flag for payment: {}", payment);
        return SpiResponse.<Boolean>builder()
                       .payload(true)
                       .build();
    }
}
