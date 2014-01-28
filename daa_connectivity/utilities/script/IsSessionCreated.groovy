package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class IsSessionCreated implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(IsSessionCreated.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + IsSessionCreated.getName());
	
    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN IsSessionCreated");
        LOGGER.trace("check if session has been created");
        String errorNumber =  message.get("returnCodeKey");
        String loginResult = message.get("result");

        if (errorNumber.equals("200")){
            LOGGER.debug("createSession sucessful");
			LOGGER.info("The billing operation CREATE SESSION, flag " + message.get("flag") + " succeed for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN"));
            LOGGER_TIME.debug("OUT IsSessionCreated");
            return "success"
        }
		LOGGER.info("The billing CREATE SESSION operation, flag " + message.get("flag") + ", failed for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + "http code: " + errorNumber + ", Session Result: " + loginResult);
        LOGGER_TIME.debug("OUT IsSessionCreated");
        LOGGER.debug("createSession failed");
        return "failed"
    }
	
    private String getResultFromString(String res){
        int beginIndex = res.indexOf("<result") + 27; 
        int endIndex = res.indexOf("</result>");
        return res.substring(beginIndex, endIndex);
    }

}