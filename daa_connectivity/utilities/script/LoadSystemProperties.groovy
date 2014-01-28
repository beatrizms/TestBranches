package com.ema.daa.common;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import com.ema.connectivity.connectionpool.ConnectionPool;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;


public class LoadSystemProperties implements GroovyComponent<Message> {
    
    private static final transient Logger LOGGER = LoggerFactory.getLogger(LoadSystemProperties.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + LoadSystemProperties.getName());
    private static final String propertiesPath = System.getProperty("client.solution.home")+"/conf/system/system.properties";
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN LoadSystemProperties");
        LOGGER.debug("Loading properties ...");
        File propertiesFile = new File(propertiesPath);        
        Properties connectivityProperties = new Properties();
        connectivityProperties.load(new FileInputStream(propertiesFile));
        Properties previousProperties = System.getProperties();
        previousProperties.putAll(connectivityProperties);
        LOGGER.debug("Properties Loaded.");
        if (LOGGER.isTraceEnabled()){
            LOGGER.trace("Properties : " + previousProperties);
        }
        ConnectionPool.initConnections(System.getProperties());
        LOGGER_TIME.debug("OUT LoadSystemProperties");
        return null;  
    }
}
