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
import java.util.Calendar;
import java.util.Date;
import java.sql.Date;

public class OfferCreation implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(OfferCreation.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + OfferCreation.getName());
    
    private final String outputKey;
    private final String layoutName;
    private HashMap mappingAdaptor;


    private void initMapping(){
        mappingAdaptor.put("airtimeDue", "AIRTIME_DUE");
        mappingAdaptor.put("CreditOfferFinal", "AMOUNT_OFFER");
        mappingAdaptor.put("FeeCalculation", "FEE");
    }
	
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
	
        LOGGER_TIME.debug("IN OfferCreation");
        Connection conn =null;
        this.mappingAdaptor = new HashMap();
        Map<String,Object> entries;
        this.initMapping();
	
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            this.createOffer(conn, message);
            LOGGER_TIME.debug("createOffer");
            this.updateLowBalanceInfo( conn,   message);
            LOGGER_TIME.debug("updateLowBalanceInfo");
            this.updateOfferControl(conn, message);    
            LOGGER_TIME.debug("OUT OfferCreation");		
        } catch (SQLException ex ) {
            LOGGER.warn("OfferCreation: SQL Exception: ",  ex);
        } catch (Exception ex){
            LOGGER.warn("OfferCreation: General Exception: ", ex);
        } finally{
            try{ 
                if (conn != null) { conn.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the connection", ex)}	
        }
            
        return null;        
    }
	
    private void updateLowBalanceInfo(Connection con, Message message){
        PreparedStatement ps = null;
        String notificationId = message.get("notification_Id");
        int lowBal_id = 0;
        try{
            ps = con.prepareStatement("update LOW_0_BALANCE set FINISH_DATE = ? ,PROCESS_STATUS = ? , SMS_STATUS = ? " +
				" where LOW_0_BALANCE_ID = " + new Integer(notificationId).intValue());
            LOGGER.debug("update LOW_0_BALANCE set FINISH_DATE = ? ,PROCESS_STATUS = ? , SMS_STATUS = ? " +
				" where LOW_0_BALANCE_ID = " + new Integer(notificationId).intValue());
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            ps.setTimestamp(1, date);
            LOGGER.trace("updateNotification: SeqValue: " + new Integer(lowBal_id).toString());
            if (message.get("CreditOfferFinal") != null && message.get("FeeCalculation") !=null){
                ps.setString(2, message.get("ExclusionsdecisionSetterTypicalResult2.DecisionCategory"));
            }else{
                ps.setString(2, "SDS ERROR");
            }
            ps.setString(3, "SENT");
            int res = ps.executeUpdate();
        }catch(SQLException ex) {
            LOGGER.warn("updateNotification: SQL Exception: ", ex);
        }finally {
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the Prepared statement", ex)}		
        } 
    }
	
    private void createOffer(Connection con, Message message){
        PreparedStatement ps = null;
        Statement stmt = null;
        int offer_id = 0;
        Float creditOfferFinal = null;
        Float feeCalculation = null;
        if (message.get("CreditOfferFinal") != null && message.get("FeeCalculation") != null){
            creditOfferFinal = new Float(message.get("CreditOfferFinal")) ;
            feeCalculation = new Float(message.get("FeeCalculation"));
        }
        String mobileIdentityNumber = message.get("MOBILEIDENTITYNUMBER_MIN");
        int hours = new Integer(System.getProperty("offer.finishDate")).intValue();
        String smsId = message.get("SMS_STATUS")
        ResultSet rs = null;
        try{
            if(creditOfferFinal != null && feeCalculation != null){
                stmt = con.createStatement();
                rs = stmt.executeQuery("SELECT HIBERNATE_SEQUENCE.NEXTVAL FROM DUAL");
                if (rs.next()){
                    offer_id = rs.getInt(1);
                }
                ps = con.prepareStatement("insert into DAA_OFFER (DAA_OFFER_ID, AMOUNT_OFFER, FEES, " + 
							"MOBILEIDENTITYNUMBER_MIN, OFFER_ACCEPTANCE_FLAG, SMS_SENT_DATE, FINISH_DATE, OFFER_DATE, PROCESS_STATUS, VALIDITY_DATE, SMS_STATUS) " + 
							"values ( ? , ? , ? , ? , 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'OFFER_SENT', CURRENT_TIMESTAMP + " + hours + " /24, ?)");
                LOGGER.trace("CREATEOffer: " + mobileIdentityNumber + ", " + new Integer(offer_id).toString() + 
									", fees: " + feeCalculation + ", creditOfferFinal: " + creditOfferFinal  + " sms status: " + smsId);
				
                LOGGER.info("An offer has been created for MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + ", amount: " + creditOfferFinal + " fees: " + feeCalculation);			
                ps.setInt(1, offer_id);
                ps.setFloat(2, creditOfferFinal );
                ps.setFloat(3, feeCalculation);
                ps.setString(4, mobileIdentityNumber);
                ps.setString(5, smsId );
                int res = ps.executeUpdate();
                message.put("OFFER_ID", offer_id);
            }
        }catch(SQLException ex) {
            LOGGER.warn("updateOffer: SQL Exception:" + ex.getMessage());
        }finally {
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	
            try{ 
                if (rs != null) { rs.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing result set", ex)}		
        } 
    }

    private void updateOfferControl(Connection con, Message message){

        Statement stmt = null;
        PreparedStatement ps = null;
        String notificationId = message.get("notification_Id");
        Float creditOfferFinal = new Float(message.get("CreditOfferFinal")) ;
        int lowBal_id = 0;    
        int controlId = 0;
        ResultSet rs = null;
        String query = "";
        int res = 0;
        int total = 0;
        try{
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT HIBERNATE_SEQUENCE.NEXTVAL FROM DUAL");
            LOGGER.debug("SELECT HIBERNATE_SEQUENCE.NEXTVAL FROM DUAL");
            if (rs.next()){
                controlId = rs.getInt(1);
            }
            query = "SELECT count(0) FROM DAA_CONTROL where  ( TRUNC(CURRENT_DATE, 'DDD'  ) - TRUNC(CONTROL_DATE, 'DDD')) =0 "; 
            rs = stmt.executeQuery(query);
            if (rs.next()){
                total= rs.getInt(1);
            }
            try{
                query = "INSERT INTO DAA_CONTROL (CONTROL_DATE) " +
						"VALUES ( TRUNC(CURRENT_DATE) )";  
                LOGGER.debug(query);
                res = stmt.executeUpdate(query);
            }catch(Exception e){
                LOGGER.debug("The date already existed, no need to create another daa Control");
            }
            query = "update DAA_CONTROL SET NUM_OFFERS_SENT = NUM_OFFERS_SENT + 1, OFFER_AMOUNT_SENT = OFFER_AMOUNT_SENT +" + creditOfferFinal +
					" WHERE ( TRUNC(CURRENT_DATE, 'DDD'  ) - TRUNC(CONTROL_DATE, 'DDD')) =0 ";
            LOGGER.trace("OfferCreation.updateOfferControl: " + query);
            res = stmt.executeUpdate(query);
        }catch(SQLException ex) {
            LOGGER.warn("updateNotification: SQL Exception:", ex);
        }finally {
            try{ 
                if (rs != null) { rs.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}		
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
