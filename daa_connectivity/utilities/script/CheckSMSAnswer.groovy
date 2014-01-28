package com.ema.daa.accept;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CheckSMSAnswer implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CheckSMSAnswer.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + CheckSMSAnswer.class.getName());
	
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN CheckSMSAnswer");
        String smsContent = message.get("SMS_CONTENT");
        String ok = System.getProperty("smsResponse.OK");
        String stop = System.getProperty("smsResponse.STOP");
        String activate = System.getProperty("smsResponse.ACTIVATE");
        String smsProcessing = message.get("SMS_PROCESS");
		
        if (smsContent.equalsIgnoreCase(stop)){
            //return stop, even if the offer is expired
            message.put("SMS_FLAG", 1);
            LOGGER.info("SMS for customer with MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " is STOP");
            LOGGER_TIME.debug("OUT CheckSMSAnswer");	
            return "STOP"    
        } else if (smsContent.equalsIgnoreCase(activate)){
            message.put("SMS_FLAG", 0); 
            LOGGER.info("SMS for customer with MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " is REACTIVATE OFFERS");
            LOGGER_TIME.debug("OUT CheckSMSAnswer");
            return "ACTIVATE"    
        }	
		
        if (smsProcessing.equals("NO")){
            //for example when we get 2 sms for one offert
            LOGGER.trace("SMS NO PROCESS BECAUSE DUPLICATE");
            LOGGER_TIME.debug("OUT CheckSMSAnswer");
            return "NO_PROCESS"; 
        }else{
            LOGGER.trace("SMS Answer is: " + smsContent);
				
            if(message.get("OFFER_EXPIRED").equalsIgnoreCase("FALSE")){    
                if (smsContent.equalsIgnoreCase(ok)){
                    LOGGER.debug("Is offer OK");
                    message.put("IS_OFFER_OK", 1);
                    LOGGER_TIME.debug("OUT CheckSMSAnswer");
                    return "OK"
                }else{
                    //there has been a typo
                    message.put("IS_OFFER_OK", 0);
                    LOGGER.info("SMS for customer with MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " contains a wrong answer");
                    LOGGER_TIME.debug("OUT CheckSMSAnswer");
                    return "TYPO"
                }
            }else{
                message.put("IS_OFFER_OK", 0);
                LOGGER.info("SMS for customer with MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " ACCEPTED AN EXPIRED OFFER. NO MORE PROCESSNG");
                LOGGER_TIME.debug("OUT CheckSMSAnswer");
                return "EXPIRED"    
            }
            message.put("IS_OFFER_OK", 0);
            LOGGER_TIME.debug("OUT CheckSMSAnswer");
            return "NO_PROCESS"; 
        }
    }
}