package com.ema.daa.topup;


import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

public class MapInputsReimb implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(MapInputsReimb.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + MapInputsReimb.getName());
    private static final Map mapAdaptor;
    static{
		
        HashMap mappingAdaptor = new HashMap();
        //load a properties field with the correspondecen between the DB columns and the fields on the CSV
        mappingAdaptor.put("AIRTIME_DUE", "AirtimeDue");
        mappingAdaptor.put("MOBILEIDENTITYNUMBER_MIN", "LowbalMin");
        mappingAdaptor.put("LOW_0_BALANCE_ID", "LowbalTransactionId");
        mappingAdaptor.put("TRANSACTION_ID", "TOPUP_NOTIFICATION_ID");
        mappingAdaptor.put("TOPUP_AMOUNT", "TopupAmount");
        mappingAdaptor.put("MOBILEIDENTITYNUMBER_MIN", "TopupMin");
        mappingAdaptor.put("TRANSACTION_ID", "TopupTransactionId");
        mappingAdaptor.put("PRODSTATUS", "ActiveProduct");
        mappingAdaptor.put("AIRTIME_BALANCE", "AirtimeBalanceDue");
        mappingAdaptor.put("FEE", "FeeDue");
        mappingAdaptor.put("TOPUP_TYPE", "TopupType");	
        mappingAdaptor.put("TOPUP_METHOD", "OnlineAutoTopup");
        mapAdaptor = Collections.unmodifiableMap(mappingAdaptor);
		

    }

    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN MapInputsReimb");
        //this.loadMappingAdaptor(message);
        message.put("MAP_REIMB_IN", mapAdaptor);
        LOGGER_TIME.debug("OUT MapInputsReimb");
        return null;        
    }
}
