package com.ema.daa.bill;

import App.generateSoapPin;
import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class AddLoginHeaders implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(AddLoginHeaders.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + AddLoginHeaders.getName());
	
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
	
        LOGGER_TIME.debug("IN AddLoginHeaders");
        new App.generateSoapPin();		    
        def tmpPinGenerator;		       
        tmpPinGenerator =new generateSoapPin();	        
      	Map myHeaders = new HashMap();
        myHeaders.put("SOAPAction", "loginRequest");
        myHeaders.put("Accept-Encoding", "gzip,deflate");
        myHeaders.put("Content-Type", "text/xml;charset=UTF-8");
        myHeaders.put("Connection", "Keep-Alive");	
        message.put("myHeader", myHeaders);
        String sessionId = message.get("sessionId");
        String tocken =  tmpPinGenerator.generateSOAPpin(sessionId, "ut_mbt", "123456");
        message.put("loginTocken", tocken);
        LOGGER_TIME.debug("OUT AddLoginHeaders");
        return null;      
    }
}
