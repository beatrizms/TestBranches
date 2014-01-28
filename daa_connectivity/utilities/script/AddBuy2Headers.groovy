package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AddBuy2Headers implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(AddBuy2Headers.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + AddBuy2Headers.getName());
       
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN AddBuy2Headers");
        Map myHeaders = new HashMap();
        myHeaders.put("SOAPAction", "buyRequest");
        myHeaders.put("Accept-Encoding", "gzip,deflate");
        myHeaders.put("Content-Type", "text/xml;charset=UTF-8");
        myHeaders.put("Connection", "Keep-Alive");	
        message.put("myHeader", myHeaders);
        LOGGER.debug("End Buy2 headers");
        //message.put("transid_request", generateBillingTranId());
        LOGGER_TIME.debug("OUT AddBuy2Headers");
        return null;        
    }
	
    private String generateBillingTranId(){
        String lock = "";
        Calendar today = Calendar.getInstance();
        long millis = today.getTimeInMillis();
        int randomPart = Math.random()*100;
        lock = millis + Thread.activeCount() + randomPart;
        return lock;
    }
}

