package com.ema.daa.common;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Calendar;
import java.util.Date;
// Loatz of imports// Loatz of imports
import org.jsmpp.InvalidResponseException;
import org.jsmpp.PDUException;
import org.jsmpp.bean.Alphabet;
import org.jsmpp.bean.BindType;
import org.jsmpp.bean.ESMClass;
import org.jsmpp.bean.GeneralDataCoding;
import org.jsmpp.bean.NumberingPlanIndicator;
import org.jsmpp.bean.RegisteredDelivery;
import org.jsmpp.bean.SMSCDeliveryReceipt;
import org.jsmpp.bean.TypeOfNumber;
import org.jsmpp.extra.NegativeResponseException;
import org.jsmpp.extra.ResponseTimeoutException;
import org.jsmpp.session.BindParameter;
import org.jsmpp.session.SMPPSession;
import org.jsmpp.util.AbsoluteTimeFormatter;
import org.jsmpp.util.TimeFormatter;


public class EmptyScript implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(EmptyScript.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + EmptyScript.getName());
    private static final TimeFormatter timeFormatter = new AbsoluteTimeFormatter();
	
	
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN SendSMS");

        String targetPhoneNumber = message.get("targetPhoneNumber");
        String sourcePhoneNumber = message.get("sourcePhoneNumber");
        String smsBody = message.get("smsBody");
        String smsLogin = message.get("smsLogin");
        String smsPassword = message.get("smsPassword");
        String smsHost = message.get("smsHost");
        int smsPort = Integer.parseInt(message.get("smsPort"));
        // NEW PARAMETERS
        String msmMode = message.get("smsMode");
        String systemType = "GEN";
        String smsRange = message.get("smsAddressRange");

        SMPPSession session = new SMPPSession();
        try {
            if ("advanced".equalsIgnoreCase(msmMode)) {
                // advanced mode for binding.
                LOGGER.debug("Advanced bind mode");
                LOGGER.debug("Connecting to server " + smsHost + ":" + smsPort + "with user :" + smsLogin + ", sms body is : " + smsBody + ", targetPhoneNumber is : " + targetPhoneNumber + ", sourcePhoneNumber is : " + sourcePhoneNumber + ", smsRange is : " + smsRange);

                BindParameter bindParameter = new BindParameter(BindType.BIND_TX,
                    smsLogin, smsPassword, systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    smsRange);
                session.connectAndBind(smsHost, smsPort, bindParameter);
            } else {
                // normal bind mode                
                LOGGER.debug("Connecting to server " + smsHost + ":" + smsPort + "with user :" + smsLogin + ", sms body is : " + smsBody);
                BindParameter bindParameter = new BindParameter(BindType.BIND_TX,
                    smsLogin, smsPassword, systemType,
                    TypeOfNumber.UNKNOWN,
                    NumberingPlanIndicator.UNKNOWN,
                    null);
                session.connectAndBind(smsHost, smsPort, bindParameter);
            }
        } catch (Exception ex) {
            LOGGER.warn("Failed connect and bind to host: " +  ex.getMessage());
            LOGGER_TIME.debug("OUT SendSMS");
            throw ex;
        }
        LOGGER_TIME.debug("sendSMS Bind");
        try {
            LOGGER.debug("Submiting Message ...");
            String messageId = session.submitShortMessage("CMT",
                TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, sourcePhoneNumber,
                TypeOfNumber.UNKNOWN, NumberingPlanIndicator.UNKNOWN, targetPhoneNumber,
                new ESMClass(), (byte) 0, (byte) 1, null, null,
                new RegisteredDelivery(SMSCDeliveryReceipt.DEFAULT), (byte) 0,
                new GeneralDataCoding(Alphabet.ALPHA_DEFAULT), (byte) 0,
                smsBody.getBytes());
            message.put("messageId", messageId);
            message.put("SMS_STATUS", messageId);
            LOGGER_TIME.debug("Send SMS");
        } catch (PDUException ex) {
            // Invalid PDU parameter
            LOGGER.warn("Invalid PDU parameter", ex);
        } catch (ResponseTimeoutException ex) {
            // Response timeout
            LOGGER.warn("Response timeout", ex);
        } catch (InvalidResponseException ex) {
            // Invalid response
            LOGGER.warn("Receive invalid respose",ex);
        } catch (NegativeResponseException ex) {
            // Receiving negative response (non-zero command_status)
            LOGGER.warn("Receive negative response", ex);
        } finally {
            LOGGER_TIME.debug("OUT SendSMS");
            session.unbindAndClose();
        }/**/
		
        return null;
    }
}
