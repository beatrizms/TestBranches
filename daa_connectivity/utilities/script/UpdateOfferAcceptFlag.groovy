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
import java.sql.SQLException;
import java.lang.Exception;
import java.sql.Statement;

public class UpdateOfferAcceptFlag implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(UpdateOfferAcceptFlag.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + UpdateOfferAcceptFlag.getName());
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN UpdateOfferAcceptFlag");
        Connection conn =null;
   
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            this.updateOffer(message, conn);
            LOGGER_TIME.debug("updateOffer");
        } catch (SQLException ex ) {
            LOGGER.warn("SetOfferAcceptFlag: SQL Exception:" + ex.getMessage());
        } catch (Exception ex){
            LOGGER.warn("SetOfferAcceptFlag: General Exception:" + ex.getMessage());
            LOGGER.warn("SetOfferAcceptFlag: Reason:" + ex.getCause());
        }finally {
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){}	
            LOGGER_TIME.debug("OUT UpdateOfferAcceptFlag");
        }   
        return null;        
    }
    
    private void updateOffer(Message message, Connection con){
        PreparedStatement ps = null;
        int offer_id = 0;
		
        String query = "update DAA_OFFER set OFFER_ACCEPTANCE_FLAG = ? WHERE DAA_OFFER_ID = " + message.get("DAA_OFFER_ID");
		
        LOGGER.debug("updateOffer: " + query + ", " + message.get("IS_OFFER_OK"));
        if (message.get("SMS_PROCESS").equals("YES")){
            try{
                ps = con.prepareStatement(query);
                ps.setInt(1, message.get("IS_OFFER_OK"));
                ps.executeUpdate();
            }catch(SQLException ex) {
                LOGGER.warn("updateOffer: SQL Exception:" + ex.getMessage());
            }finally {
                if (ps != null) { ps.close(); }
            } 
        }
    }
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
}
