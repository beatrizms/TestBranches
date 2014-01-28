package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AddCreateSessionHeaders implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(AddCreateSessionHeaders.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + AddCreateSessionHeaders.getName());
        
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN AddCreateSessionHeaders");
     	Map myHeaders = new HashMap();
        myHeaders.put("SOAPAction", "createSessionRequest");
        myHeaders.put("Accept-Encoding", "gzip,deflate");
        myHeaders.put("Content-Type", "text/xml;charset=UTF-8");
        myHeaders.put("Connection", "Keep-Alive");	
        message.put("myHeader", myHeaders);
        LOGGER_TIME.debug("OUT AddCreateSessionHeaders");
        return null;     
    }
}
