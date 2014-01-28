package com.ema.daa.offer;

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

public class updateOfferStatus implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(updateOfferStatus.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + updateOfferStatus.getName());	
	
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        Connection conn =null;
        LOGGER_TIME.debug("IN UpdateOfferStatus");
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            this.updateOfferStatusbd( conn, message);
            LOGGER_TIME.debug("updateOfferStatusbd");     
        } catch (SQLException ex ) {
            LOGGER.warn("getNotificationInfo: SQL Exception:" + ex.getMessage());
        } catch (Exception ex){
            LOGGER.warn("getNotificationInfo: General Exception:" + ex.getMessage());
            LOGGER.warn("getNotificationInfo: Reason:" + ex.getCause());
        } finally{
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){}	
            LOGGER_TIME.debug("OUT UpdateOfferStatus");
        }
		
        return null;            
    }

    private void updateOfferStatusbd(Connection con, Message message){
        int offerId = message.get("DAA_OFFER_ID");
        PreparedStatement ps = null;
        int lowBal_id =   new Integer(message.get("notification_Id")).intValue();
        try{
            ps = con.prepareStatement("update DAA_OFFER set PROCESS_STATUS = ?,  FINISH_DATE = CURRENT_TIMESTAMP, SMS_STATUS = ? WHERE daa_offer_id = ?");
            LOGGER.debug("update DAA_OFFER set PROCESS_STATUS = ?,  FINISH_DATE = CURRENT_TIMESTAMP, SMS_STATUS = ? WHERE daa_offer_id = ?");
            LOGGER.trace("updateOfferStatusbd: SeqValue: " + lowBal_id);
            ps.setString(1, "OFFER ACCEPTED");
            ps.setString(2, message.get("SMS_STATUS"));
            ps.setInt(3, offerId);
			
            int res = ps.executeUpdate();
        }catch(SQLException e) {
            LOGGER.warn("updateOfferStatusbd: SQL Exception:" + e.getMessage());
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
