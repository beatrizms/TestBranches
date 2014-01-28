package com.ema.daa.accept;

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
import java.lang.Math;


public class CreateProduct implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CreateProduct.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + CreateProduct.class.getName());

    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
		
        LOGGER_TIME.debug("IN CreateProduct");
        Connection conn = null;
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");			
            this.createProduct(message, conn);
            LOGGER_TIME.debug("createProduct");
            this.updateOfferControlAccept(message, conn);
            LOGGER_TIME.debug("updateOfferControlAccept");   
        } catch (SQLException e ) {
            LOGGER.warn("SetOfferAcceptFlag: SQL Exception:" + e.getMessage());
        } catch (Exception e1){
            LOGGER.warn("SetOfferAcceptFlag: General Exception:" + e1.getMessage());
            LOGGER.warn("SetOfferAcceptFlag: Reason:" + e1.getCause());
        }finally {
            try{ 
                if (conn != null){conn.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the connection")}
            LOGGER_TIME.debug("OUT CreateProduct");   
        }
        return null;        
    }
	
    
    
    private void createProduct(Message message,  Connection con){
        PreparedStatement ps = null;
        Statement stmt = null;
        int prod_id = 0;
        ResultSet rs = null;
        String offerId = message.get("DAA_OFFER_ID");
        Date lastRepayment = message.get("DATE_LAST_REPAYMENT");
        Date acceptanceDate = message.get("DATE_OF_ACCEPTANCE");
        String mobileIdentityNumber = message.get("min");
        LOGGER.info("Creating Product for  MIN: "  + mobileIdentityNumber + ", offer id: " + offerId + ". Current Airtime Due is " + message.get("AMOUNT_OFFER") + ", fees are " + message.get("FEES"));
        message.put("PRODUCT_STATUS", "ACTIVE");
        try{
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT HIBERNATE_SEQUENCE.NEXTVAL FROM DUAL");
            if (rs.next()){
                prod_id = rs.getInt(1);
            }
            ps = con.prepareStatement("insert into DAA_PRODUCT (DAA_PRODUCT_ID, AIRTIME_DUE, BALANCE, DAA_OFFER_ID, " +
                    " DATE_OF_ACCEPTANCE, FEE, INITIAL_OFFER_AMOUNT, MOBILEIDENTITYNUMBER_MIN, PRODSTATUS, PRODUCT_TYPE," +
                    " TOT_AMOUNT_REPAYED)" +
                    " values ( ? , ? , ? , ? , CURRENT_TIMESTAMP , ? , ? , ? , ? , ? , ?)");
					
            Float creditFee = new Float(message.get("FEES"));
            Float creditAmount = new Float(message.get("AMOUNT_OFFER"));
            Float totalDebt =  -( creditFee+ creditAmount);
            Integer totalDebtInt = Math.abs(totalDebt);
            Integer offerIdInt = Integer.parseInt(offerId);
            message.put("DebitAmount", totalDebtInt);
            LOGGER.debug("Creating Product. creditFee" + creditFee + ", creditAmount" + creditAmount +", totalDebt" + totalDebt + ", totalDebtInt" + totalDebtInt+ ", offerIdInt" + offerIdInt + 
				" prodId = " + prod_id + "'\n");
            ps.setInt(1, prod_id );
            ps.setInt(2, totalDebtInt);
            ps.setFloat(3, totalDebt);
            ps.setInt(4, offerIdInt);
            ps.setFloat(5, creditFee);
            ps.setFloat(6, creditAmount);
            ps.setString(7, mobileIdentityNumber);
            ps.setString(8, "ACTIVE");
            ps.setString(9, "DAA");
            ps.setFloat(10, 0);
			
            int res  = ps.executeUpdate();
			
            message.put("DAA_PRODUCT_ID", prod_id);
            message.put("AMOUNT_OFFER", creditAmount);
            message.put("FEES", creditFee);
        }catch(SQLException e ) {
            LOGGER.warn("CREATE PRODUCT: SQL Exception:" + e.getMessage());
        }finally {
            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the result set", ex)}
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the Prepared statement", ex) }
        } 
    }

    private void updateOfferControlAccept(Message message,  Connection con){
        Statement stmt = null;
        int total = 0;
        int lowBal_id = 0;    
        int controlId = 0;
        ResultSet rs = null;
        String query = "";
		
        Float creditAmount = new Float(message.get("AMOUNT_OFFER"));
        int res = 0;
        try{
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT HIBERNATE_SEQUENCE.NEXTVAL FROM DUAL");
            if (rs.next()){
                controlId = rs.getInt(1);
            }
            query = "SELECT count(0) FROM DAA_CONTROL where  ( TRUNC(CURRENT_DATE, 'DDD'  ) - TRUNC(CONTROL_DATE, 'DDD')) =0 "; 
            LOGGER.debug(query);
            rs = stmt.executeQuery(query);
            if (rs.next()){
                total= rs.getInt(1);
            }
            try{
                query = "INSERT INTO DAA_CONTROL (CONTROL_DATE) " +
						"VALUES ( TRUNC(CURRENT_DATE) )";  
                LOGGER.trace(query);
                res = stmt.executeUpdate(query);
            }catch(Exception e){
                LOGGER.debug("DAA CONTROL entry for today already created.");
            }
            query = "update DAA_CONTROL SET NUM_OFFERS_ACCEPTED = NUM_OFFERS_ACCEPTED + 1, AMOUNT_ACCEPTED = AMOUNT_ACCEPTED +" + creditAmount +
					" WHERE ( TRUNC(CURRENT_DATE, 'DDD'  ) - TRUNC(CONTROL_DATE, 'DDD')) =0 ";
            LOGGER.trace(query);
            res = stmt.executeUpdate(query);
			
        }catch(SQLException e) {
            LOGGER.warn("updateNotification: SQL Exception:" + e.getMessage());
        }finally {
            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the result set")}
            try{ 
                if (stmt != null){stmt.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the statement")}
			
        } 
    }   
	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");
		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
}
