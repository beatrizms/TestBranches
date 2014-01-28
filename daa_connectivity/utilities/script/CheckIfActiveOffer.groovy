package com.ema.daa.accept;

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


public class CheckIfActiveOffer implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CheckIfActiveOffer.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + CheckIfActiveOffer.getName());

    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN CheckIfActiveOffer");
        Connection conn = null;
        try{
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            this.getOfferData(message, conn);
            LOGGER_TIME.debug("getOfferData");

            if("ACTIVE".equals(message.get("ACTIVE_PRODUCT"))){
                LOGGER.info("LOW BALANCE NOTIFICATION: MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " has already an active product. No offer will be proposed");
				this.updateCustomer(message, conn, "ACTIVE PRODUCT");
                LOGGER_TIME.debug("OUT CheckIfActiveOffer");
                return "failed";
            }
            if ("TRUE".equals(message.get("OFFER_ACTIVE"))){
                LOGGER.info("LOW BALANCE NOTIFICATION: MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " has already an active offer. No offer will be proposed");
				this.updateCustomer(message, conn, "ACTIVE OFFER");
                LOGGER_TIME.debug("OUT CheckIfActiveOffer");
                return "failed";
            }
            LOGGER_TIME.debug("OUT CheckIfActiveOffer");
            return "success";
        }catch(SQLException e) {
            LOGGER.warn("CheckIfActiveOffer: SQL Exception:" + e.getMessage());
        }finally {
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the connection", ex)}
        } 
    }
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }

    public void getOfferData(Message message, Connection conn){
        Statement stmt = null;
        ResultSet rs = null;
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");
        String query = "SELECT count(*)" + 
            " FROM DAA_OFFER" 
			" WHERE DAA_OFFER.MOBILEIDENTITYNUMBER_MIN = '" + min + " AND CURRENT_TIMESTAMP < DAA_OFFER.VALIDITY_DATE";
        LOGGER.debug("query: " + query);
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query); 
            if(rs.next()){     			
                Date currentDate = rs.getDate(1);
                Date validityDate = rs.getDate(2);
                String prodStatus = rs.getString(3);
                message.put("ACTIVE_PRODUCT", prodStatus);	
                LOGGER.debug("Last offer validy date is: "  + validityDate.toString());
                if (currentDate.compareTo(validityDate) < 0 ){
                    message.put("OFFER_ACTIVE", "TRUE");
                }else{
                    message.put("OFFER_ACTIVE", "FALSE");
                }
            }else{
                message.put("OFFER_ACTIVE", "FALSE");
            }
			
        }catch(Exception e){
            LOGGER.warn(e.getMessage());
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}

            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the result set", ex)}
        }
    }
	
	public void getProductData(Message message, Connection conn){
		int numRes = 0;
        Statement stmt = null;
        ResultSet rs = null;
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");
        String query = "SELECT (*) FROM DAA_PRODUCT where DAA_PRODUCT.MOBILEIDENTITYNUMBER_MIN = '" + min + "' AND DAA_PRODUCT.PRODSTATUS='ACTIVE'";
        LOGGER.debug("query: " + query);
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query); 
            if(rs.next()){     			
                numRes = rs.getInt(1);
                message.put("ACTIVE_PRODUCT", "ACTIVE");	
            }else{
                message.put("ACTIVE_PRODUCT", "CLOSED");
            }
			
        }catch(Exception e){
            LOGGER.warn(e.getMessage());
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}

            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the result set", ex)}
        }
    }
	
	public void updateCustomer(Message message, Connection conn, String status){
		String query = "";
		Statement stmt = null;
		int res = 0;
		try{
			query = "update low_0_balance set process_status='"+ status +"', finish_date= current_timestamp where LOW_0_BALANCE_ID = " + message.get("LOW_0_BALANCE_ID");
			stmt = conn.createStatement();
			res = stmt.executeUpdate(query);
		}catch(Exception e){
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement")}
		}finally{
		
		}
	}
}