package com.ema.daa.offer;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import com.experian.eda.enterprise.da.processor.DAData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ema.connectivity.connectionpool.ConnectionPool;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Exception;
import java.sql.Timestamp;
import java.util.Date;

public class UpdateCustomer implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(UpdateCustomer.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + UpdateCustomer.getName());

    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN UpdateCustomer");
        Connection conn = null;
        Statement stmt = null;
        int lowBal_id = 0;
        Long smsId = -1;
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");		
        String stop = System.getProperty("smsResponse.STOP");
        String lastSMSContent = "";
        Integer stopFlag = message.get("SMS_FLAG");
        int res = 0;
        if (stopFlag!= null){
            try{
                conn = this.getConnection();
                LOGGER_TIME.debug("getConnection");
                stmt = conn.createStatement();
                LOGGER.trace("update CUSTOMER INFO");
                String query = "MERGE INTO DAA_CUSTOMER_DETAILS using DUAL on (CUSTOMER_MIN ='" + min + "')" +
	                "WHEN MATCHED THEN UPDATE SET CUSTOMER_STOP_FLAG= " + stopFlag +
	                "WHEN NOT MATCHED THEN INSERT(CUSTOMER_MIN, CUSTOMER_STOP_FLAG) VALUES (" + min + ", " + stopFlag + ")";
                res = stmt.executeUpdate(query);
                LOGGER_TIME.debug("updateCustomer");
                LOGGER.debug("updateCustomer:  " + query);
            }catch(SQLException ex) {
                LOGGER.warn("updateCustomer: SQL Exception:" + ex.getMessage());
            }finally {
                try{ 
                    if (stmt != null) { stmt.close()}
                }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	
                try{ 
                    if (conn != null) { conn.close()}
                }catch(Exception ex){ LOGGER.warn("Error closing the connection", ex)}	
		        
                LOGGER_TIME.debug("OUT UpdateCustomer");
            } 
        }
        return null;    
    }
    
    	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
}
