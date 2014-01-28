package com.ema.daa.topup;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;
import java.util.HashMap;

public class MapOutputsReimb implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(MapOutputsReimb.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + MapOutputsReimb.getName());
	
    private void loadMappingAdaptor(Message message){
        Map mappingAdaptor = new HashMap();	
        mappingAdaptor.put("AirtimeDue", "AIRTIME_DUE"); //DAA PRODUCT
        mappingAdaptor.put("CreditOfferFinal", "AIRTIME_BALANCE" );
        mappingAdaptor.put("ExclusionsdecisionSetterTypicalResult2.DecisionText", "DECISION_REIMB");
        mappingAdaptor.put("FeeCalculation", "TRANSACTION_FEE");
        mappingAdaptor.put("LoadInstructionsCredit", "TRANSACTION_AMOUNT_CREDIT");
        mappingAdaptor.put("LoadInstructionsDebit", "TRANSACTION_AMOUNT_CREDIT");
        message.put("MAP_REIMB_OUT", mappingAdaptor);
    }
    
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN MapOutputsReimb");
        this.loadMappingAdaptor(message);
        LOGGER_TIME.debug("OUT MapOutputsReimb");
        return null;        
    }
}
