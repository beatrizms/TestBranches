package com.ema.daa.common;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ema.connectivity.connectionpool.ConnectionPool;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Exception;
import java.lang.Math;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Calendar;

public class ProcessSms implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ProcessSms.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + ProcessSms.getName());
        
    /**
     * ======================================================
     *  Support for groovy multi thread independent behavior
     * ======================================================     * 
     * 
     * @Synchronized annotation is used when the script is intended to be run in multi-threads. Uncomment to use it. 
     *
     **/ 
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN ProcessSms");
        Connection conn = null;
        message.put("DAA_PRODUCT_ID", new Long(-1));
        message.put("reseller", System.getProperty("sourcePhoneNumber"));
        LOGGER.debug("Billing operation flag : 1");
        message.put("flag", "1");
        int res = 0;
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            getSmsNotification(message, conn);
            res = this.updateOffer(message, conn);
            LOGGER_TIME.debug("updateOffer");
            this.getOfferData(message, conn);
            LOGGER_TIME.debug("getOfferData");
            this.getLowBalanceInfo(message, conn);
            LOGGER_TIME.debug("getLowBalanceInfo");
        } catch (SQLException ex ) {
            LOGGER.warn("GetSMSData: SQL Exception:" + ex.getMessage());
        } catch (Exception ex){
            LOGGER.warn("GetSMSData: Reason:" + ex.getMessage());
            LOGGER.warn("GetSMSData: General Exception:", ex );
        }finally {
            try{ 
                if (conn != null) { conn.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the connection", ex)}	
            LOGGER_TIME.debug("OUT ProcessSms");
        }
		
        return null;        
    }
	
    private HashMap getSmsNotification(Message message, Connection conn) throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        HashMap notificationInfo = new HashMap();
        String smsId = message.get("SMS_ID");
        HashMap mappingAdaptor = message.get("MAP_REIMB_IN");
        String min = "";//message.get("MOBILEIDENTITYNUMBER_MIN");
        try {
            String query = "SELECT * FROM SMS_NOTIFICATION WHERE SMS_ID = " + smsId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            LOGGER.debug("topupinfo: query executed: " + query);
            if (rs.next()) {
                message.put("SMS_CONTENT", rs.getString("SMS_CONTENT"));
                message.put("MOBILEIDENTITYNUMBER_MIN", rs.getString("MOBILEIDENTITYNUMBER_MIN"));
            }
			
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the Result set", ex)}
        }
		
        return notificationInfo;
    }

    public int updateOffer(Message message, Connection conn){
        Statement stmt = null;
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");
        LOGGER.trace("MIN IS " + min);
        String lockKey = this.generateLockId();
        LOGGER.trace("OFFER_LOCK IS " + lockKey);
        message.put("OFFER_LOCK", lockKey);
        String query = "update DAA_OFFER set LOCK_KEY = '" + lockKey + "' WHERE LOCK_KEY IS NULL AND MOBILEIDENTITYNUMBER_MIN = '" + min + "'";
        LOGGER.debug("updateOffer query is "  + query);
        int res = 0;
        try {
            stmt = conn.createStatement();
            res = stmt.executeUpdate(query); 
            if (res == 0){
                message.put("SMS_PROCESS", "NO");
                message.put("IS_OFFER_OK", 0);
            }else{
                message.put("SMS_PROCESS", "YES");
            }
            LOGGER.debug("end updateOffer, result is " + res);
        }catch(Exception ex){
            LOGGER.warn("ProcessSMS", ex);
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	
        }
        return res;
    }
    
    public void getOfferData(Message message, Connection conn){
        Statement stmt = null;
        ResultSet rs = null;
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");
        String lockKey = message.get("OFFER_LOCK");
        message.put("OFFER_EXPIRED", "TRUE");
        String query = "SELECT CURRENT_TIMESTAMP, VALIDITY_DATE, DAA_OFFER_ID, MOBILEIDENTITYNUMBER_MIN, AMOUNT_OFFER, FEES, SMS_STATUS " + 
            "FROM DAA_OFFER where LOCK_KEY = '" + lockKey + "' AND MOBILEIDENTITYNUMBER_MIN = '" + min + "'";
        LOGGER.debug(query);
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query); 

            if(rs.next()){
                			
                Date currentDate = rs.getDate("CURRENT_TIMESTAMP");
                Date validityDate = rs.getDate("VALIDITY_DATE");
				
                message.put("min", rs.getString("MOBILEIDENTITYNUMBER_MIN"));
                message.put("MOBILEIDENTITYNUMBER_MIN", rs.getString("MOBILEIDENTITYNUMBER_MIN"));
                message.put("VALIDITY_DATE", rs.getDate("VALIDITY_DATE"));
                message.put("AMOUNT_OFFER", rs.getFloat("AMOUNT_OFFER"));
                message.put("FEES", rs.getFloat("FEES"));
                message.put("SMS_STATUS", rs.getString("SMS_STATUS"));
                message.put("DAA_OFFER_ID", rs.getInt("DAA_OFFER_ID"));
                LOGGER.trace("validy date is: "  + validityDate.toString());
                if (currentDate.compareTo(validityDate) < 0 ){
                    LOGGER.trace("offer not EXPIRED");
                    message.put("OFFER_EXPIRED", "FALSE");
                }else{
                    message.put("OFFER_EXPIRED", "TRUE");
                }
            }else{
                //if there's no offer, it could be that an SMS arrived before
                LOGGER.debug("an sms arrived before");
                message.put("SMS_PROCESS", "NO");
            }
			
            LOGGER.trace("end getOffer");
        }catch(Exception e){
            LOGGER.warn(e.getMessage());
        }finally{
            try{ 
                if (rs != null) { rs.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}	
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	
        }
    }
	
    private void getLowBalanceInfo(Message message, Connection conn) throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try {
            String min = message.get("MOBILEIDENTITYNUMBER_MIN");
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM LOW_0_BALANCE WHERE MOBILEIDENTITYNUMBER_MIN = '" + min + "' AND PROCESS_STATUS = 'ACCEPT' AND SMS_STATUS IS NOT NULL");
            LOGGER.debug("SELECT * FROM LOW_0_BALANCE WHERE MOBILEIDENTITYNUMBER_MIN = '" + min + "' AND PROCESS_STATUS = 'ACCEPT' AND SMS_STATUS IS NOT NULL");
        }finally{
            try{ 
                if (rs != null) { rs.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}	
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	
        }
		
    }
    
    private String generateLockId(){
        String lock = "";
        Calendar today = Calendar.getInstance();
        long millis = today.getTimeInMillis();
        int randomPart = Math.random()*100;
        lock = millis + Thread.activeCount() + randomPart;
        LOGGER.debug("lock_id: " + lock);
        return lock;
    } 

    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }	
    
}
