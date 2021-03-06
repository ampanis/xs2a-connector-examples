package de.adorsys.aspsp.xs2a.connector.spi.impl.payment.type;

import de.adorsys.aspsp.xs2a.connector.spi.impl.payment.GeneralPaymentService;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiCheckConfirmationCodeRequest;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.*;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractPaymentSpi<P extends SpiPayment, R extends SpiPaymentInitiationResponse> {

    protected static final String DEBTOR_NAME = "Mocked debtor name from ASPSP";

    protected final GeneralPaymentService paymentService;

    /*
     * Initiating a payment you need a valid bearer token if not we just return ok.
     */
    public @NotNull SpiResponse<R> initiatePayment(@NotNull SpiContextData contextData, @NotNull P payment, @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return processEmptyAspspConsentData(payment, aspspConsentDataProvider, contextData.getPsuData());
    }

    public @NotNull SpiResponse<SpiGetPaymentStatusResponse> getPaymentStatusById(@NotNull SpiContextData contextData,
                                                                                  @NotNull String acceptMediaType,
                                                                                  @NotNull P payment,
                                                                                  @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.getPaymentStatusById(PaymentTypeTO.valueOf(payment.getPaymentType().name()),
                                                   acceptMediaType,
                                                   payment.getPaymentId(),
                                                   payment.getPaymentStatus(),
                                                   aspspConsentDataProvider.loadAspspConsentData());
    }

    /*
     * This attempt to execute payment without sca can only work if the core banking system decides that there is no
     * sca required. If this is the case, the payment would have been executed in the initiation phase after
     * the first login of the user.
     *
     * In sure, the core banking considers it like any other payment initiation. If the status is ScsStatus.EXEMPTED
     * then we are fine. If not the user will be required to proceed with sca.
     *
     */
    public @NotNull SpiResponse<SpiPaymentExecutionResponse> executePaymentWithoutSca(@NotNull SpiContextData contextData,
                                                                                      @NotNull P payment,
                                                                                      @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.executePaymentWithoutSca(aspspConsentDataProvider);
    }

    @Deprecated // TODO remove deprecated method in 6.7 https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1270
    public @NotNull SpiResponse<SpiPaymentExecutionResponse> verifyScaAuthorisationAndExecutePayment(@NotNull SpiContextData contextData,
                                                                                                     @NotNull SpiScaConfirmation spiScaConfirmation,
                                                                                                     @NotNull P payment,
                                                                                                     @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.verifyScaAuthorisationAndExecutePayment(spiScaConfirmation, aspspConsentDataProvider);
    }

    public @NotNull SpiResponse<SpiPaymentResponse> verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(@NotNull SpiContextData contextData,
                                                                                            @NotNull SpiScaConfirmation spiScaConfirmation,
                                                                                            @NotNull P payment,
                                                                                            @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.verifyScaAuthorisationAndExecutePaymentWithPaymentResponse(spiScaConfirmation, aspspConsentDataProvider);
    }

    public @NotNull SpiResponse<SpiPaymentConfirmationCodeValidationResponse> checkConfirmationCode(@NotNull SpiContextData contextData,
                                                                                           @NotNull SpiCheckConfirmationCodeRequest spiCheckConfirmationCodeRequest,
                                                                                           @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider) {
        return paymentService.checkConfirmationCode(spiCheckConfirmationCodeRequest, aspspConsentDataProvider);
    }

    protected abstract SpiResponse<R> processEmptyAspspConsentData(@NotNull P payment,
                                                                   @NotNull SpiAspspConsentDataProvider aspspConsentDataProvider,
                                                                   @NotNull SpiPsuData spiPsuData);

    protected  @NotNull SpiResponse<SpiPaymentConfirmationCodeValidationResponse> notifyConfirmationCodeValidation(@NotNull SpiContextData spiContextData, boolean confirmationCodeValidationResult, @NotNull P payment, boolean isCancellation, @NotNull SpiAspspConsentDataProvider spiAspspConsentDataProvider) {
        ScaStatus scaStatus  = confirmationCodeValidationResult ? ScaStatus.FINALISED : ScaStatus.FAILED;
        TransactionStatus transactionStatus = isCancellation
                                                      ? confirmationCodeValidationResult ? TransactionStatus.CANC : payment.getPaymentStatus()
                                                      : confirmationCodeValidationResult ? TransactionStatus.ACSP : TransactionStatus.RJCT;

        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(scaStatus, transactionStatus);

        return SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder()
                       .payload(response)
                       .build();
    }
}
