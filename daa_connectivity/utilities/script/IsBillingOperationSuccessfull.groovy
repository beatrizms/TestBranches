package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.ws.rs.core.Response;
import java.io.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.bind.annotation.XmlRootElement

class IsBillingOperationSuccessfull implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(IsBillingOperationSuccessfull.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + IsBillingOperationSuccessfull.getName()); 

    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
		String billingResult = "-1";
        LOGGER_TIME.debug("IN IsBillingOperationSuccessfull");
		LOGGER.trace("Checking the result of the billing operation. Result is: " + billingResult);
       
        try{
			LOGGER.trace("getting billing result");
			if(message.get("billResult") != null){
				int indexResult = message.get("billResult").lastIndexOf("BILL_");
				billingResult = message.get("billResult").substring(indexResult + 5 , indexResult + 6);
				LOGGER.trace("@@@@@@@@@@ IsBillingOperationSuccessfull BILLING operation result is; " + billingResult);
				message.put("TRANSACTION_STATUS", billingResult);
				if (billingResult.equals("0")){
					LOGGER_TIME.debug("OUT IsBillingOperationSuccessfull");
					return "success";
				}else{
					LOGGER_TIME.debug("OUT IsBillingOperationSuccessfull");
					return "failed";
				}
			}
			return "failed";
        }catch(Exception ex){
            LOGGER.warn("IsBillingOperationSuccessfull BILLING operation exception", ex);
        }
        LOGGER_TIME.debug("OUT IsBillingOperationSuccessfull");
        return "failed";
    }
    
}