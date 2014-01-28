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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Exception;
import java.sql.Timestamp;
import java.util.Date;

public class UnlockOffer implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(UnlockOffer.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + UnlockOffer.getName());
    
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN UnlockOffer");
        Connection conn = null;

        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            this.unlockOffer(message, conn);
            LOGGER_TIME.debug("unlockOffer");
            
        } catch (SQLException ex ) {
            LOGGER.warn("UnlockOffer: SQL Exception:" , ex);
        } catch (Exception ex){
            LOGGER.warn("UnlockOffer: General Exception:", ex);
        }finally {
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the connection", ex)}	
            LOGGER_TIME.debug("OUT UnlockOffer");
        }
		
        return null;           
    }
    
    public void unlockOffer(Message message, Connection conn){
        Statement stmt = null;
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");
        String lockKey ="";
        lockKey = message.get("OFFER_LOCK");
        String query = "update DAA_OFFER set LOCK_KEY = null WHERE LOCK_KEY = '" + lockKey + "'AND MOBILEIDENTITYNUMBER_MIN = '" + min + "'";
        LOGGER.debug(query);
        try {
            stmt = conn.createStatement();
            int res = stmt.executeUpdate(query); 
            LOGGER.trace("end UnlockOffer");
        }catch(Exception ex){
            LOGGER.warn("error unlocking offer after typo message: " + ex.getMessage());
        }finally{
            if (stmt != null){stmt.close(LOGGER.warn("Error closing the statement", ex))}
        }
    }
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
}
