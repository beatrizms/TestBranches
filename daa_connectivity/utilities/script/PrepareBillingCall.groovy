package com.ema.daa.common;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrepareBillingCall implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(PrepareBillingCall.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + PrepareBillingCall.getName()); 
    
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN PrepareBillingCall");
        String account = System.getProperty("billing.account");
        String password = System.getProperty("billing.password");
        String flag = message.get("flag");
        int amount = 0; 
        
        if("1".equals(flag)) {
            LOGGER.trace("AMOUNT_OFFER: " + (message.get("ReimbursementAmount")));
            amount = 0 + message.get("AMOUNT_OFFER");
            // CR 19012014 set to 0 for the credit offer
            message.put("transid_request", "0");
        } else {
            LOGGER.trace("ReimbursementAmount: " + (message.get("ReimbursementAmount")));
            amount = 0 + message.get("ReimbursementAmount");
			
        }
			
        message.put("amount", amount);
        message.put("account", account);
        message.put("password", password);
        LOGGER_TIME.debug("OUT PrepareBillingCall");
        return null;        
    }
}
