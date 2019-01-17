package de.adorsys.ledgers.oba.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import de.adorsys.ledgers.middleware.api.domain.payment.PeriodicPaymentTO;
import de.adorsys.psd2.consent.api.pis.CmsPeriodicPayment;
import de.adorsys.psd2.xs2a.core.pis.PisDayOfExecution;
import de.adorsys.psd2.xs2a.core.pis.PisExecutionRule;

@Mapper(componentModel = "spring", uses = {CurrencyMapper.class, TimeMapper.class})
public interface PeriodicPaymentMapper {
	
	@Mappings({
		@Mapping(target="executionRule", expression="java(mapPisExecutionRule(payment.getExecutionRule()))"),
		@Mapping(target="dayOfExecution", expression="java(mapPisDayOfExecution(payment.getDayOfExecution()))")
	})
    PeriodicPaymentTO toPayment(CmsPeriodicPayment payment);
    
    default String mapPisExecutionRule(PisExecutionRule rule) {
    	return rule==null
    			?null
    					:rule.getValue();
    }
    
    default int mapPisDayOfExecution(PisDayOfExecution day) {
    	return day==null
    			?1
    					: Integer.parseInt(day.getValue());
    }
}
