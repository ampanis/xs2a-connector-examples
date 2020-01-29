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

package de.adorsys.aspsp.xs2a.connector.spi.impl.payment.type;

import de.adorsys.aspsp.xs2a.connector.spi.converter.LedgersSpiPaymentMapper;
import de.adorsys.aspsp.xs2a.connector.spi.impl.payment.GeneralPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiBulkPayment;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiBulkPaymentInitiationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.BulkPaymentSpi;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class BulkPaymentSpiImpl extends AbstractPaymentSpi<SpiBulkPayment, SpiBulkPaymentInitiationResponse> implements BulkPaymentSpi {

    @Autowired
    public BulkPaymentSpiImpl(GeneralPaymentService paymentService, LedgersSpiPaymentMapper paymentMapper) {
        super(paymentService, paymentMapper);
    }

    @Override
    public @NotNull SpiResponse<SpiBulkPayment> getPaymentById(@NotNull SpiContextData contextData,
                                                               @NotNull SpiBulkPayment payment,
                                                               @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.getPaymentById(payment, aspspConsentDataProvider, BulkPaymentTO.class,
                                             paymentMapper::mapToSpiBulkPayment, PaymentTypeTO.BULK);
    }

    @Override
    protected SpiResponse<SpiBulkPaymentInitiationResponse> processEmptyAspspConsentData(@NotNull SpiBulkPayment payment,
                                                                                         @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.firstCallInstantiatingPayment(PaymentTypeTO.BULK, payment, aspspConsentDataProvider, new SpiBulkPaymentInitiationResponse());
    }

}
