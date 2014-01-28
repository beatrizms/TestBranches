package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsLoginSuccesfull implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(IsLoginSuccesfull.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + IsLoginSuccesfull.getName());
	
    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN IsLoginSuccesfull");
        String errorNumber =  message.get("returnCodeKey");
        String loginResult = message.get("loginResult");
		
        LOGGER.debug("Check login errorNumber, loginResult: " + errorNumber + ", " + loginResult);
		
        if (errorNumber.equals("200") && loginResult.equals("0")){
            LOGGER.debug("login sucessful: " + loginResult);
			LOGGER.info("The billing operation LOGIN, flag " + message.get("flag") + " succeed for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN"));
            LOGGER_TIME.debug("OUT IsLoginSuccesfull");
            return "success"
        }
		LOGGER.info("The billing LOGIN operation, flag " + message.get("flag") + " failed for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + "http code: " + errorNumber + ", Login Result" + loginResult);
        LOGGER.debug("login failed");
        LOGGER_TIME.debug("OUT IsLoginSuccesfull");
        return "failed"
    }
	
    private String getResultFromString(String res){
        int beginIndex = res.indexOf("<result") + 27; 
        int endIndex = res.indexOf("</result>");
        return res.substring(beginIndex, endIndex);
    }
}