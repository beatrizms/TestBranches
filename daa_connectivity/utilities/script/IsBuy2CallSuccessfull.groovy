package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsBuy2CallSuccessfull implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(IsBuy2CallSuccessfull.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + IsBuy2CallSuccessfull.getName());
	
    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN IsBuy2CallSuccessfull");
        String errorNumber =  message.get("returnCodeKey");
        String result = message.get("result");
		
        LOGGER.debug("Check buy2 errorNumber, result: " + errorNumber + ", " + result);
        if (errorNumber.equals("200")){
			LOGGER.info("The billing operation BUY2 " + message.get("flag") + " succeed for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN"));
            LOGGER.trace("buy2 sucessful");
            LOGGER_TIME.debug("OUT IsBuy2CallSuccessfull");
            return "success"
        }
		LOGGER.info("The billing operation BUY2 " + message.get("flag") + " failed for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + "http code: " + errorNumber + ", BUY2 Result" + result);
        LOGGER.trace("buy2 failed");
        LOGGER_TIME.debug("OUT IsBuy2CallSuccessfull");
        return "failed"
    }
	
    private String getResultFromString(String res){
        int beginIndex = res.indexOf("<result") + 27; 
        int endIndex = res.indexOf("</result>");
        return res.substring(beginIndex, endIndex);
    }
}