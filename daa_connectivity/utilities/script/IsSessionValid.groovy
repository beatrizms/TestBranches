package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsSessionValid implements GroovyComponent<Message> {


    private static final transient Logger LOGGER = LoggerFactory.getLogger(IsSessionValid.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + IsSessionValid.getName());
	
    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
		
	
        LOGGER_TIME.debug("IN IsSessionValid");
        LOGGER.debug("check if session has been created");
        String result = message.get("result");
		
        LOGGER.trace(result);
        LOGGER.debug("Check VALID SESSION ID e: " + result);
		
        if (result.equals("0")){
            LOGGER.debug("Session id is Valid");
			LOGGER.info("The billing operation BALANCE, flag " + message.get("flag") + " succeed for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN"));
            
            LOGGER_TIME.debug("OUT IsSessionValid");	
            return "success"
        }
		LOGGER.info("The billing BALANCE operation, flag " + message.get("flag") + " failed for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + ", Balace Result" + result);
        LOGGER_TIME.debug("OUT IsSessionValid");	
        LOGGER.debug("Session id is not valid ");
	
        return "failed"
    } 
	
    private String getResultFromString(String res){
        int beginIndex = res.indexOf("<result") + 27; //" xsi:type=xsd:int">");
        int endIndex = res.indexOf("</result>");
        return res.substring(beginIndex, endIndex);
    }
}