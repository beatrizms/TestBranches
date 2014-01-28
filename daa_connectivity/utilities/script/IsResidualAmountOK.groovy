package com.ema.daa.topup;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsResidualAmountOK implements GroovyComponent<Message> {
 
    private static final transient Logger LOGGER = LoggerFactory.getLogger(IsResidualAmountOK.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + IsResidualAmountOK.getName());
	
    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
		
        LOGGER_TIME.debug("IN IsResidualAmountOK");
        Float result = message.get("LoadInstructionsDebit");
        LOGGER.trace("!!!!!!DA result for topup is " + result);
        if (LOGGER.isDebugEnabled() ){
            LOGGER.debug("!!!!!!DA message " + message);
        }
        if(result > 0){
            LOGGER_TIME.debug("OUT IsResidualAmountOK");
            return "success"
        }
        message.put("TOPUP_STATUS", "No Residual Amount");
        LOGGER_TIME.debug("OUT IsResidualAmountOK");
        return "failed"
    }
}