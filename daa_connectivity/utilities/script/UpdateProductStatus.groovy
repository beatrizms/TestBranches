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
        LOGGER_TIME.debug("IN UpdateProductStatus");
        Connection conn = null;
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            this.updateProductStatus(message.get("PRODUCT_STATUS"), message.get("DAA_OFFER_ID"), conn);
            LOGGER_TIME.debug("updateProductStatus");
        } catch (SQLException ex ) {
            LOGGER.warn("SetOfferAcceptFlag: SQL Exception:" + ex.getMessage());
        } catch (Exception ex){
            LOGGER.warn("updateProductStatus: General Exception:" + ex.getMessage());
            LOGGER.warn("SupdateProductStatus: Reason:" + ex.getCause());
        }finally {
            try{ 
                if (conn != null) { conn.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the connection", ex)}	
            LOGGER_TIME.debug("OUT UpdateProductStatus");   
        }
         
        return null;        
    }
    
    private void updateProductStatus(String productStatus, Integer prodId, Connection conn){
        PreparedStatement ps = null;
        int offer_id = 0;
        String query = "update DAA_PRODUCT set PRODSTATUS = ? WHERE DAA_PRODUCT_ID = " + prodId;
        LOGGER.debug(query);
        try{
            ps = conn.prepareStatement(query);
            ps.setString(1, productStatus);
            ps.executeUpdate();
        }catch(SQLException e) {
            LOGGER.warn("updateProductStatus: SQL Exception:" + e.getMessage());
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
