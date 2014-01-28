package com.ema.daa.offer;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import com.experian.eda.enterprise.da.processor.DAData; 
import com.experian.stratman.datasources.runtime.IData;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IDataToXmlConverterOffer implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(IDataToXmlConverterOffer.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + IDataToXmlConverterOffer.getName());
    
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN DataToXmlOffer");
        DAData[] results = (DAData[])message.get(System.getProperty("strategy.output.dataKey"));
        LOGGER.trace("DataToXmlOffer for offer: " + System.getProperty("strategy.output.layout.offer") +", " + System.getProperty("strategy.input.alias.offer"));
        if(results!=null){
			
            Map<String,Object> daMap = new HashMap<String,Object>(); 
            for (IData item : results) { 
                daMap.put(item.getLayout(), item); 
            } 
            IData layout = daMap.get(System.getProperty("strategy.output.layout.offer")); 
            Map<String,Object> entries = layout.getModifiedValues(); 
            for(Map.Entry<String,Object> entry :entries) { 
                message.put(entry.getKey(), entry.getValue().toString()); 
            }
        }
        LOGGER_TIME.debug("OUT DataToXmlOffer");
        return null;        
    }
	
}
