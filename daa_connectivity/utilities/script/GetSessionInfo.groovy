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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Exception;
import java.sql.Statement;
import java.lang.Math;



public class GetSessionInfo implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(GetSessionInfo.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + GetSessionInfo.getName());

    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER.debug("IN GetSessionInfo");
        Connection conn = null;
        String sessionId = "";
        try {
            conn = this.getConnection();
            LOGGER.trace("GetSessionInfo: Connected to database");
            sessionId = this.getSession(message, conn);
            message.put("sessionId", sessionId);
            LOGGER.trace("Session id is: " + sessionId);
        } catch (SQLException ex ) {
            LOGGER.warn("GetSessionInfo: SQL Exception:" + ex.getMessage());
        } catch (Exception ex){
            LOGGER.warn("GetSessionInfo: General Exception:" + ex.getMessage());
        }finally {
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){
                LOGGER.warn("Error closing the connection", ex)
            }
            LOGGER_TIME.debug("OUT GetSessionInfo"); 	
        }
        return null;           
    }
	
	    
    private String getSession(Message message,  Connection con){
        ResultSet rs = null;
        Statement stmt = null;
        String query = "select * from BILLING_SESSION";
        String sessionId = "";

        try{
            stmt = con.createStatement();
            rs = stmt.executeQuery(query);
            if (rs.next()){
                sessionId = rs.getString("SESSION_NUMBER");
            }
			
        }catch(SQLException ex ) {
            LOGGER.warn("CREATE PRODUCT: SQL Exception:" + ex.getMessage());
        }finally {
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the connection", ex)}
        } 
        LOGGER.debug("Getting session Info is: " +  sessionId);
        return sessionId;
    }
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
}
