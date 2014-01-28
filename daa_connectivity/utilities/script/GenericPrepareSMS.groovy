package com.ema.daa.common;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrepareSMS implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(PrepareSMS.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + PrepareSMS.getName());
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN GenericPrepareSMS");
        String smsBody = message.get("SMS");
	String MIN = "84" + message.get("MOBILEIDENTITYNUMBER_MIN");
        LOGGER.trace("Prepare SMS , sms body is : " + smsBody);
	message.put("targetPhoneNumber",MIN);     
        message.put("sourcePhoneNumber",System.getProperty("sourcePhoneNumber"));     
        message.put("smsBody", smsBody);     
        message.put("smsLogin",System.getProperty("smsLogin"));     
        message.put("smsPassword",System.getProperty("smsPassword"));     
        message.put("smsHost",System.getProperty("smsHost"));     
        message.put("smsPort",System.getProperty("smsPort"));   
        message.put("smsMode",System.getProperty("smsMode"));
        message.put("smsAddressRange",System.getProperty("smsAddressRange"));
        message.put("SMSbody", smsBody); 
        LOGGER_TIME.debug("OUT GenericPrepareSMS");
        return null;        
    }
}
