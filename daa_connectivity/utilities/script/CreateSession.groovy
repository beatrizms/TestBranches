package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import com.experian.eda.enterprise.da.processor.DAData; 
import com.experian.stratman.datasources.runtime.IData;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ema.connectivity.connectionpool.ConnectionPool;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.lang.Exception;
import java.sql.Statement;
import java.lang.Math;

public class CreateSession implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CreateSession.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + CreateSession.getName());
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
     
        LOGGER_TIME.debug("IN CreateSession");
        Connection conn = null;
		
        try {
            conn = this.getConnection();
            LOGGER.debug("getConnection");
            this.createSession(message, conn);
            LOGGER.debug("createSession");
        } catch (SQLException ex ) {
            LOGGER.warn("SetOfferAcceptFlag: SQL Exception:" + ex.getMessage());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("SetOfferAcceptFlag: SQL Exception:", ex);
            }
        } catch (Exception e1){
            LOGGER.warn("SetOfferAcceptFlag: General Exception:" + e1.getMessage());
            LOGGER.warn("SetOfferAcceptFlag: Reason:" + e1.getCause());
        }finally {
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){}
            LOGGER_TIME.debug("OUT CreateSession"); 	
        }
        return null;             
    }
    
    private void createSession(Message message,  Connection con){
        //if
        PreparedStatement ps = null;
        Statement stmt = null;
		
        String sessionId = message.get("sessionId");

        try{
            stmt = con.createStatement();
            //stmt.executeUpdate("TRUNCATE BILLING_SESSION");
            stmt.executeUpdate("DELETE  FROM BILLING_SESSION");
	
            stmt = con.createStatement();
            stmt.executeUpdate("insert into BILLING_SESSION values (1, '" + sessionId + "')");
			
        }catch(SQLException ex ) {
            LOGGER.warn("CREATE PRODUCT: SQL Exception: " + ex.getMessage());
            if (LOGGER.isTraceEnabled()) {
                LOGGER.trace("CREATE PRODUCT: SQL Exception: " , ex);
            }
        }finally {
            if (stmt != null) { stmt.close(); }
            if (ps != null) { ps.close(); }
        } 
    }
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
}
