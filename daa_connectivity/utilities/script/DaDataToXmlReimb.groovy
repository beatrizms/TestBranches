package com.ema.daa.topup;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import com.experian.eda.enterprise.da.processor.DAData; 
import com.experian.stratman.datasources.runtime.IData;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDataToXmlConverterReimb implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(ExistsDaa.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + IDataToXmlConverterReimb.getName());

	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN DataToXmlReimb");	
        DAData[] results = (DAData[])message.get("data");
        if(results!=null){
            Map<String,Object> daMap = new HashMap<String,Object>(); 
            for (IData item : results) { 
                daMap.put(item.getLayout(), item); 
                LOGGER.trace("for on results: " + item.getLayout() + " , " + item.toString());
            } 
            IData layout = daMap.get(System.getProperty("strategy.output.layout.reimb")); 
            Map<String,Object> entries = layout.getModifiedValues(); 
            for(Map.Entry<String,Object> entry :entries) { 
                message.put(entry.getKey(), entry.getValue()); 
                LOGGER.trace("mapping da: " + entry.getKey() + " , " + entry.getValue().toString());
            }
        }
        LOGGER_TIME.debug("OUT DataToXmlReimb");
        return null;        
    }
}
