package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExistsSession implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ExistsSession.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + ExistsSession.getName());
	
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN ExistsSession");
        String sessionId = message.get("sessionId");
        LOGGER.debug("Checking if we have any session in the DB: "  + sessionId);
        if (sessionId != null && sessionId.length()>0){
            LOGGER.debug("Session exists: "  + sessionId);
            LOGGER_TIME.debug("OUT ExistsSession");
            return "success";
        }
        LOGGER.debug("Session doesn't exist in the DB ");
        LOGGER_TIME.debug("OUT ExistsSession");
        return "failed";
    }
}