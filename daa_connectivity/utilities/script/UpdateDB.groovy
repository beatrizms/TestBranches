package com.ema.daa.lowbal;

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

public class updateDB implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(updateDB.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + updateDB.getName());
	
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
		 
        LOGGER_TIME.debug("IN updateDB");
        Connection conn = null;
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");	
            this.updateLowBalanceInfo( conn, message.get("notification_Id"), message);
            LOGGER_TIME.debug("updateLowBalanceInfo");
            this.updateOfferStatus(conn, message);
            LOGGER_TIME.debug("updateOfferStatus");

        } catch (SQLException e ) {
            LOGGER.warn("getNotificationInfo: SQL Exception:" + e.getMessage());
        } catch (Exception e1){
            LOGGER.warn("getNotificationInfo: General Exception:" + e1.getMessage());
            LOGGER.warn("getNotificationInfo: Reason:" + e1.getCause());
        } finally{
            try{ 
                if (conn != null) { conn.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the connection", ex)}	
		        
            LOGGER_TIME.debug("OUT updateDB");  
        }
        return null;            
    }

    private void updateLowBalanceInfo(Connection con,  String notificationId, Message message){

        LOGGER.trace("update Low balance info");
        PreparedStatement ps = null;
		
        int lowBal_id = 0;
        try{
            ps = con.prepareStatement("update LOW_0_BALANCE set FINISH_DATE = CURRENT_TIMESTAMP ,PROCESS_STATUS = ? , SMS_STATUS = ? " +
				" where LOW_0_BALANCE_ID = " + new Integer(notificationId).intValue());
            LOGGER.debug("update LOW_0_BALANCE set FINISH_DATE = CURRENT_TIMESTAMP ,PROCESS_STATUS = ? , SMS_STATUS = ? " +
				" where LOW_0_BALANCE_ID = " + new Integer(notificationId).intValue());
            LOGGER.trace("updateLowBalanceInfo: SeqValue: " + new Integer(lowBal_id).toString());
            ps.setString(1, message.get("ExclusionsdecisionSetterTypicalResult2.DecisionCategory"));
            ps.setString(2, message.get("SMS_STATUS"));
            int res = ps.executeUpdate();
        }catch(SQLException e) {
            LOGGER.warn("updateNotification: SQL Exception:" + e.getMessage());
        }finally {
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing prepared statment", ex)}	
        } 
    }  
	
    private void updateOfferStatus(Connection con,  Message message){

        PreparedStatement ps = null;
        String offerId = message.get("OFFER_ID");
        int lowBal_id = 0;
        try{
            ps = con.prepareStatement("update DAA_OFFER set SMS_STATUS = ? WHERE daa_offer_id = ?");
            LOGGER.debug("update DAA_OFFER set SMS_STATUS = ? WHERE daa_offer_id = ?");
            LOGGER.trace("updateOfferStatus SMS: SeqValue: " + new Integer(lowBal_id).toString());
            ps.setString(1, message.get("SMS_STATUS"));
            ps.setString(2, offerId);
            int res = ps.executeUpdate();
        }catch(SQLException e) {
            LOGGER.warn("updateOfferStatus SMS: SQL Exception:" + e.getMessage());
        }finally {
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the prepared statement", ex)}	
        } 
    }
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
}
