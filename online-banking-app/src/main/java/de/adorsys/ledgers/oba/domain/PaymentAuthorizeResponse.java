package de.adorsys.ledgers.oba.domain;

import de.adorsys.ledgers.middleware.api.domain.payment.BulkPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PaymentTypeTO;
import de.adorsys.ledgers.middleware.api.domain.payment.PeriodicPaymentTO;
import de.adorsys.ledgers.middleware.api.domain.payment.SinglePaymentTO;

public class PaymentAuthorizeResponse extends AuthorizeResponse  {
	private final SinglePaymentTO singlePayment;
	private final BulkPaymentTO bulkPayment;
	private final PeriodicPaymentTO periodicPayment;
	
	private String authMessageTemplate;
	
	public PaymentAuthorizeResponse(SinglePaymentTO singlePayment) {
		this.singlePayment = singlePayment;
		this.bulkPayment = null;
		this.periodicPayment = null;
	}
	public PaymentAuthorizeResponse(BulkPaymentTO bulkPayment) {
		this.singlePayment = null;
		this.bulkPayment = bulkPayment;
		this.periodicPayment = null;
	}
	public PaymentAuthorizeResponse(PeriodicPaymentTO periodicPayment) {
		this.singlePayment = null;
		this.bulkPayment = null;
		this.periodicPayment = periodicPayment;
	}
	public PaymentAuthorizeResponse(PaymentTypeTO paymentType, Object payment) {
		switch (paymentType) {
			case SINGLE:
				this.singlePayment = (SinglePaymentTO)payment;
				this.bulkPayment = null;
				this.periodicPayment = null;
				break;
			case BULK:
				this.singlePayment = null;
				this.bulkPayment = (BulkPaymentTO)payment;
				this.periodicPayment = null;
				break;
			default:
				this.singlePayment = null;
				this.bulkPayment = null;
				this.periodicPayment = (PeriodicPaymentTO)payment;
		}	
	}
	public SinglePaymentTO getSinglePayment() {
		return singlePayment;
	}
	public BulkPaymentTO getBulkPayment() {
		return bulkPayment;
	}
	public PeriodicPaymentTO getPeriodicPayment() {
		return periodicPayment;
	}
	public String getAuthMessageTemplate() {
		return authMessageTemplate;
	}
	public void setAuthMessageTemplate(String authMessageTemplate) {
		this.authMessageTemplate = authMessageTemplate;
	}
	
}
