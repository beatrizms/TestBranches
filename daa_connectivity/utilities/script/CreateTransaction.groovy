package com.ema.daa.bill;

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


public class CreateTransaction implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CreateTransaction.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + CreateTransaction.getName());
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN CreateTransaction");
        Connection conn =null;
 
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            this.createTransaction(message, conn);
            LOGGER_TIME.debug("createTransaction");
            this.updateProduct(message, conn);
        } catch (SQLException e ) {
            LOGGER.warn("CreateTransaction: SQL Exception:" + e.getMessage());
        } catch (Exception e1){
            LOGGER.warn("CreateTransaction: General Exception:" + e1.getMessage());
            LOGGER.warn("CreateTransaction: Reason:" + e1.getCause());
        }finally {
            try{
                if (conn != null){conn.close()}
            }catch(Exception ex){}
            LOGGER_TIME.debug("OUT CreateTransaction");  
        }
        return null;        
    }
    
    
    private void createTransaction(Message message,  Connection con){
        PreparedStatement ps = null;
        Statement stmt = null;
        ResultSet rs = null;
        int transId = 0;
        Float transAmount = new Float(message.get("AMOUNT_OFFER"));
        String desc = "AA amount offered";
        Float fee =0;
        Long productId = message.get("DAA_PRODUCT_ID");
        int beginTransactionIndex = -1;
        int endTransactionIndex = -1;
        String transactionId = "-1";
        message.put("PROD_TRANS_ID", "0");
		
        long notificationId = new Long(message.get("DAA_OFFER_ID")).longValue();
        String transStatus = message.get("TRANSACTION_STATUS");
        if (transStatus.equals("0")){
            beginTransactionIndex = message.get("billResult").indexOf("TRANSID_") + 8;
            endTransactionIndex = message.get("billResult").indexOf("_TRANSID");
            transactionId = message.get("billResult").substring(beginTransactionIndex, endTransactionIndex);
            LOGGER.trace("Bill TRANSACTION_ID is: " + transactionId);
            LOGGER.info("The transaction id for this operation is: " + transactionId);
            message.put("PROD_TRANS_ID", transactionId);
        }
		
        LOGGER.trace(" BILLING operation status is: " + transStatus);
        try{
            stmt = con.createStatement();
            rs = stmt.executeQuery("SELECT HIBERNATE_SEQUENCE.NEXTVAL FROM DUAL");
            if (rs.next()){
                transId = rs.getInt(1);
            }
            ps = con.prepareStatement("insert into DAA_TRANSACTIONS (DAA_TRANSACTION_ID, AMOUNT, INDEX_PER_PRODUCT, PRODUCT_ID, TRANSACTION_DESC, BILLING_TRANSACTION_NB, NOTIFICATION_TRANSACTION_NB, TRANSACTION_TIME, STATUS) " +
                    " values ( ? , ? , 0 , ? , ? , ? , ? , CURRENT_TIMESTAMP, ? )");
            ps.setInt(1, transId );
            ps.setFloat(2, transAmount);
            ps.setLong(3, productId);
            ps.setString(4, desc);
            ps.setString(5, transactionId);
            ps.setLong(6, notificationId);
            ps.setInt(7, new Integer(transStatus));
            ps.executeUpdate();
			
        }catch(SQLException e ) {
            LOGGER.warn("updateTransaction: SQL Exception:" , ex);
        }finally {
            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the result set in createTransaction method", ex)}
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement in createTransaction method", ex)}
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the Prepared statement in createTransaction method", ex)}		

        } 
    }
	
    private void updateProduct(Message message, Connection con){
        String transactionId = message.get("PROD_TRANS_ID");
        int prodId = new Integer(message.get("DAA_PRODUCT_ID")).intValue()
        PreparedStatement ps = null;
        String reimbursementStatus = ""; 
        String topUpId = message.get("topup_id");
        String query = "UPDATE DAA_PRODUCT SET BILLING_TRANSACTION_NB = ? WHERE DAA_PRODUCT_ID = " + prodId;
        LOGGER.debug(query);
        try{
            reimbursementStatus = message.get("ProcesstreatmentTreeResults2.TreatmentName");
            ps = con.prepareStatement(query);
            ps.setString(1, transactionId);
            int res = ps.executeUpdate();
        }catch(SQLException e) {
            LOGGER.warn("updateProduct: SQL Exception:" + e.getMessage());
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
