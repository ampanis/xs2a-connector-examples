package de.adorsys.ledgers.xs2a.test.ctk.pis;

import de.adorsys.psd2.model.PaymentInitationRequestResponse201;
import de.adorsys.psd2.model.ScaStatus;
import de.adorsys.psd2.model.TransactionStatus;
import de.adorsys.psd2.model.UpdatePsuAuthenticationResponse;
import org.junit.Test;

public class FuturePaymentEmbeddedOneScaMethodIT extends AbstractPaymentEmbedded {
    @Test
    public void test_create_payment() {

        // Initiate Payment
        PaymentInitationRequestResponse201 initiatedPaymentResponse = paymentInitService.initiatePayment();

        // Login User
        UpdatePsuAuthenticationResponse loginResponse = paymentInitService.login(initiatedPaymentResponse);
        paymentInitService.validateResponseStatus(loginResponse, ScaStatus.SCAMETHODSELECTED);
        paymentInitService.checkTxStatus(loginResponse, TransactionStatus.ACCP);

        UpdatePsuAuthenticationResponse psuAuthenticationResponse = paymentInitService.authCode(loginResponse);
        paymentInitService.validateResponseStatus(psuAuthenticationResponse, ScaStatus.FINALISED);
        paymentInitService.checkTxStatus(psuAuthenticationResponse, TransactionStatus.ACSP);
    }
}
