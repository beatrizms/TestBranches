package com.ema.daa.offer;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import com.experian.eda.enterprise.da.processor.DAData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ema.connectivity.connectionpool.ConnectionPool;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Exception;
import java.sql.PreparedStatement;


public class CheckIfExistingMin implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CheckIfExistingMin.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + CheckIfExistingMin.getName());
	
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN CheckIfExistingMin");
        Connection conn = null;
        int existingMin = 0;
        try{
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            getNotificationInfo( message,  conn) ;
            existingMin = this.getExistingMin(message, conn);
            if (existingMin ==0){
                LOGGER.info("The MIN " + message.get("MOBILEIDENTITYNUMBER_MIN") +" is not in the system ");
                this.updateLowBalance(message, conn, "NO MATCH");
                LOGGER_TIME.debug("OUT CheckIfExistingMin");
                return "failed";
            }else{
                LOGGER.trace("Existing min " + message.get("MOBILEIDENTITYNUMBER_MIN"));
                this.updateLowBalance(message, conn, "MATCH");
                LOGGER_TIME.debug("OUT CheckIfExistingMin");
                return "success";
            }
        }catch(SQLException e) {
            LOGGER.warn("CheckIfActiveOffer: SQL Exception:" + e.getMessage());
        }finally {
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the connection", ex)}
        } 
    }
	
    private Map getNotificationInfo(Message message, Connection conn) throws SQLException {
        Statement stmt = null;
        String notificationId = message.get("notification_Id");
        HashMap notificationInfo = new HashMap();
        String min = "";
        ResultSet rs = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;
        ResultSet rs4 = null;
        String query = "select * from LOW_0_BALANCE where LOW_0_BALANCE_ID = '" + notificationId + "'";
        LOGGER.debug(query);	

        stmt = conn.createStatement();
        LOGGER.trace("getNotificationInfo: Statement created");
        /* first query */
        rs = stmt.executeQuery(query);
        if (rs.next()) {
            LOGGER.trace("getNotificationInfo: in the while ");
            min = rs.getString("MOBILEIDENTITYNUMBER_MIN");
            message.put("min", min);
            message.put("MOBILEIDENTITYNUMBER_MIN", min); 
            LOGGER.trace("getNotificationInfo MIN: " + min);
        }
    }
			
	
    private int getExistingMin(Message  message, Connection conn){
        Statement stmt = null;
        ResultSet rs = null;
        int res = 0;
        LOGGER.debug("connecction is " + conn);
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");
        String query = "SELECT * " + 
            "FROM PREVIOUS_WEEK_DATA where PREVIOUS_WEEK_DATA.MOBILEIDENTITYNUMBER_MIN = '" + min + "'";
        LOGGER.debug("query Check if Existing min: " + query);
        try {
            stmt = conn.createStatement();
            LOGGER.debug("connecction is " + conn);
            rs = stmt.executeQuery(query); 
            LOGGER.debug("connecction is " + conn);
            if(rs.next()){
                LOGGER.trace("IT exists a record on previous week data");
                return 1;              			
            }else{
                LOGGER.trace("IT DOES NO exists a record on previous week data");
                return 0;
            }
        }catch(Exception e){
            LOGGER.warn(e.getMessage());
            if (LOGGER.isDebugEnabled()){
                LOGGER.debug(e.getCause());
            }
        }finally{

            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the result set", ex)}
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}

        }
		
    }
	
    void updateLowBalance(Message message, Connection conn, String processStatus){
		
        PreparedStatement ps = null;
        //int offerIdInt = new Integer(offerId).intValue()
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");
        String lowBalanceId = message.get("notification_Id");
        LOGGER.trace("Low Balance id is:" + lowBalanceId);
        try{
            ps = conn.prepareStatement("update LOW_0_BALANCE set NOTIFICATION_DATE = CURRENT_TIMESTAMP, PROCESS_STATUS = ?, FINISH_DATE = CURRENT_TIMESTAMP where LOW_0_BALANCE_ID = ? AND MOBILEIDENTITYNUMBER_MIN= '"+ min + "'")
            LOGGER.debug("update LOW_0_BALANCE set PROCESS_STATUS = 'NO MATCH', FINISH_DATE = CURRENT_TIMESTAMP where LOW_0_BALANCE_ID = ? AND MOBILEIDENTITYNUMBER_MIN= '"+ min + "'");
            ps.setString(1, processStatus);
            ps.setInt(2, new Integer(lowBalanceId).intValue());
            int res = ps.executeUpdate();
            LOGGER.trace("lines updated: " + res);
        }catch(SQLException e) {
            LOGGER.warn("updateLowBalance No match: SQL Exception:" + e.getMessage());
        }finally {
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
        } 
    }
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }

}