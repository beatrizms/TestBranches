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

public class CheckIfLastSmsEqualsStop implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CheckIfLastSmsEqualsStop.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + CheckIfLastSmsEqualsStop.getName());
	
    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN CheckIfLastSmsEqualsStop");
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        int lowBal_id = 0;
        Long smsId = -1;
        String min = message.get("MOBILEIDENTITYNUMBER_MIN");		
        String stop = System.getProperty("smsResponse.STOP");
        int customerSMSFlag = 0;
        String query = "";
        try{
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            stmt = conn.createStatement();
            query= "select * from DAA_CUSTOMER_DETAILS where CUSTOMER_MIN =  " + min ;
            LOGGER.debug(query);
            rs = stmt.executeQuery(query);
            if(rs.next()){
                customerSMSFlag = rs.getInt("CUSTOMER_STOP_FLAG");
            }
            LOGGER.trace("CheckIfLastSmsEqualsStop: getCustomerInfo flag is:  " + customerSMSFlag);
        
            query = "update low_0_balance set process_status='STOP', finish_date= current_timestamp where LOW_0_BALANCE_ID = " + message.get("LOW_0_BALANCE_ID");
            LOGGER.debug(query);
            int res = stmt.executeUpdate(query);
			
            LOGGER_TIME.debug("executeUpdate");
			
        }catch(SQLException ex) {
            LOGGER.warn("getCustomerInfo: SQL Exception:" + e.xgetMessage());
        }finally {
            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the result set")}
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement")}
            try{ 
                if (conn != null){conn.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the connection")}

        } 
		
        if (customerSMSFlag == 1){
            //STOP flag is true
            LOGGER.info("LOW BALANCE NOTIFICATION: MIN: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " sent a message to STOP the offer proposals");
            LOGGER_TIME.debug("OUT CheckIfLastSmsEqualsStop");
            return "success"
        }
        LOGGER_TIME.debug("OUT CheckIfLastSmsEqualsStop");
        return "failed"
    }
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
}