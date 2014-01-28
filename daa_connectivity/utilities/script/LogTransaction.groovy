package com.ema.daa.topup;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import com.experian.eda.enterprise.da.processor.DAData; 
import com.experian.stratman.datasources.runtime.IData;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.util.HashMap;
import com.ema.connectivity.connectionpool.ConnectionPool;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.lang.Exception;

public class LogTransaction implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(LogTransaction.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + LogTransaction.getName()); 	
	
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN LogTransaction");
        Connection conn = null;
		
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");	
            this.updateTopUpNotification(conn, message);
            LOGGER_TIME.debug("updateTopUpNotification");      

        } catch (SQLException ex ) {
            LOGGER.warn("LogTransaction: SQL Exception ",  ex);
        } catch (Exception ex){
            LOGGER.warn("LogTransaction: General Exception ", ex);
        } finally{
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){}	
        }
        LOGGER_TIME.debug("OUT LogTransaction");   
        return null;    
    }
	
	
    private void updateTopUpNotification(Connection con,  Message message){
        PreparedStatement ps = null;
        String reimbursementStatus = ""; 
        String topUpId = message.get("topup_id");
        String queryTopUpQuery = "UPDATE TOPUP_NOTIFICATION SET PROCESS_STATUS = ? ,  FINISH_DATE = CURRENT_TIMESTAMP WHERE TOPUP_NOTIFICATION_ID = " + topUpId;
        LOGGER.debug(queryTopUpQuery);
        try{
            reimbursementStatus = message.get("TOPUP_STATUS");
            ps = con.prepareStatement(queryTopUpQuery);
            ps.setString(1, reimbursementStatus);
            int res = ps.executeUpdate();
        }catch(SQLException e) {
            LOGGER.warn("updateNotification: SQL Exception:" + e.getMessage());
        }finally {
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
