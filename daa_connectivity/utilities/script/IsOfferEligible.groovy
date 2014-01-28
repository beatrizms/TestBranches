package com.ema.daa.lowbal;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecisionRouting implements GroovyComponent<Message> {
    private final transient Logger LOGGER = LoggerFactory.getLogger(DecisionRouting.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + DecisionRouting.getName()); 
    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN IsOfferEligible");
		
        // FOR TEST ONLY !!!
        /*
        message.put("ExclusionsdecisionSetterTypicalResult2.DecisionCategory","ACCEPT");
        message.put("CreditOfferFinal","123");
        message.put("FeeCalculation","5"); 
        
*/
        String result = message.get("ExclusionsdecisionSetterTypicalResult2.DecisionCategory");
        LOGGER.trace("DecisionRouting: DA result for low balance is " + result);
		
        if("ACCEPT".equals(result)){
            LOGGER_TIME.debug("OUT IsOfferEligible");
            return "success"
        }
        LOGGER_TIME.debug("OUT IsOfferEligible");
        LOGGER.info("MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " is not an eligible canditate for the offer");
        return "failed"
    }
}