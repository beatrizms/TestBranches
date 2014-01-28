package com.ema.daa.topup;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import com.experian.eda.enterprise.da.processor.DAData; 
import com.experian.stratman.datasources.runtime.IData;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.util.HashMap;
import java.lang.Math;
import java.io.File;
import java.io.FileInputStream;
import com.ema.connectivity.connectionpool.ConnectionPool;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Exception;

public class UpdateDAABalance implements GroovyComponent<Message> {	
    private  String outputKey;
    private  String layoutName;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(UpdateDAABalance.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + UpdateDAABalance.getName()); 
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN UpdateDAABalance");
        Connection conn = null;		
		
        try {
            conn = this.getConnection();
            LOGGER_TIME.debug("getConnection");
            this.updateTopUpNotification(conn, message);
            LOGGER_TIME.debug("updateTopUpNotification");
            this.insertTransaction(conn, message);
            LOGGER_TIME.debug("insertTransaction");
            this.updateProduct(conn, message);
            LOGGER_TIME.debug("updateProduct");      

        } catch (SQLException ex ) {
            LOGGER.warn("getNotificationInfo: SQL Exception:" + ex.getMessage());
        } catch (Exception ex){
            LOGGER.warn("getNotificationInfo: General Exception:" + ex.getMessage());
            LOGGER.warn("getNotificationInfo: Reason:" + ex.getCause());
        } finally{
            try{ 
                if (conn != null) { conn.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the connection", ex)}	
		        
            LOGGER_TIME.debug("OUT UpdateDAABalance");   
        }
        return null;    
    }
    
    
    private void insertTransaction(Connection con,  Message message){
        String query = "";
        PreparedStatement ps = null;
        Statement stmt = null;
        int beginTransactionIndex = message.get("billResult").indexOf("TRANSID_") + 8;
        int endTransactionIndex = message.get("billResult").indexOf("_TRANSID");
        String transId = message.get("billResult").substring(beginTransactionIndex, endTransactionIndex);
        //message.put("PROD_TRANS_ID", transactionId);
        int notificationId = new Integer(message.get("TOPUP_NOTIFICATION_ID")).intValue();
        String transStatus = message.get("TRANSACTION_STATUS");
        LOGGER.debug("IsBillingOperationSuccessfull BILLING operation; " + transStatus);
        int productId = new Integer(message.get("DAA_PRODUCT_ID")).intValue();
        int indexPerProduct = 0;
        ResultSet rs = null;
		
        query = "SELECT max(index_per_product) from DAA_TRANSACTIONS where PRODUCT_ID = " +  productId;
        LOGGER.debug(query);
        stmt = con.createStatement();
        rs = stmt.executeQuery(query);
        if (rs.next()){
            indexPerProduct = rs.getInt(1) + 1;
        }
		
        int  transactionId = -1;
        rs = stmt.executeQuery("SELECT HIBERNATE_SEQUENCE.NEXTVAL FROM DUAL");
        if (rs.next()){
            transactionId = rs.getInt(1);
            message.put("DAA_TRANS_ID", transactionId);
        }
	
        query = "INSERT INTO DAA_TRANSACTIONS (DAA_TRANSACTION_ID, AMOUNT, TRANSACTION_DESC, BILLING_TRANSACTION_NB, NOTIFICATION_TRANSACTION_NB,PRODUCT_ID, TRANSACTION_TIME, STATUS, index_per_product) VALUES " +
            "( ? , ? , ? , ? , ? , ? , CURRENT_TIMESTAMP, ?, ?)";
        try{
            ps = con.prepareStatement(query);
            LOGGER.trace("Create new Transactin for Topup notification: SeqValue: " + new Integer(transactionId).toString());
            ps.setInt(1, transactionId);
            ps.setFloat(2, 0 - message.get("ReimbursementAmount"));
            ps.setString(3, "REIMBURSEMENT");
            ps.setString(4, transId);
            ps.setInt(5, notificationId);
            ps.setLong(6, productId);
            ps.setInt(7, new Integer(0));
            ps.setInt(8, indexPerProduct);
            int res = ps.executeUpdate();
        }catch(SQLException e) {
            LOGGER.warn("Create new Transaction for Topup notification: SQL Exception:" + e.getMessage());
        }finally {
            try{ 
                if (rs != null) { rs.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}	
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	        
        } 
        LOGGER.info("TOPUP OPERATION: customer with min: " + message.get("MOBILEIDENTITYNUMBER_MIN") + " has made a topup for product " + productId + ",  amount: " + message.get("ReimbursementAmount"));
    }
	
    private void updateTopUpNotification(Connection con,  Message message){
        PreparedStatement ps = null;
        String reimbursementStatus = ""; 
        String topUpId = message.get("topup_id");
        String queryTopUpQuery = "UPDATE TOPUP_NOTIFICATION SET PROCESS_STATUS = ? ,  FINISH_DATE = CURRENT_TIMESTAMP WHERE TOPUP_NOTIFICATION_ID = " + topUpId;
        LOGGER.debug(queryTopUpQuery);
        try{
            reimbursementStatus = message.get("ProcesstreatmentTreeResults2.TreatmentName");
            ps = con.prepareStatement(queryTopUpQuery);
            ps.setString(1, reimbursementStatus);
            int res = ps.executeUpdate();
        }catch(SQLException e) {
            LOGGER.warn("updateNotification: SQL Exception:" + e.getMessage());
        }finally {
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the prepared statement", ex)}	
        } 
    }



    private void updateProduct(Connection con,  Message message){
        PreparedStatement ps = null;
        Statement stmt = null;
        int numOfRepayments = 0;
        ResultSet rs = null;
        int productId = new Integer(message.get("DAA_PRODUCT_ID")).intValue();
        String queryGetRepayment = "SELECT NB_OF_REPAYMENT, TOT_AMOUNT_REPAYED from DAA_PRODUCT WHERE DAA_PRODUCT_ID = " + productId;
        String queryUpdateProd = "UPDATE DAA_PRODUCT SET BALANCE = ?, DATE_LAST_REPAYMENT = CURRENT_TIMESTAMP, NB_OF_REPAYMENT = ?, TOT_AMOUNT_REPAYED = ? , PRODSTATUS = ? WHERE DAA_PRODUCT_ID = " + productId;
        Float totalAmountRepayedBefore = 0;
        Float newTotalAmountRepayed = 0;
        Float reimbursedAmountDA = 0; 
        Float balance = 0;
        Float newBalance = 0;
		
        if (LOGGER.isTraceEnabled()){
            LOGGER.debug("update product; " + productId + ", MAP_REIMB_IN: " + message.get("MAP_REIMB_IN").toString());
            LOGGER.debug("update product; " + productId + ", message" + message.toString());
            LOGGER.debug(queryGetRepayment);
            LOGGER.debug(queryUpdateProd);
        }
        try{
            stmt = con.createStatement();
            rs = stmt.executeQuery(queryGetRepayment);
            if (rs.next()){
                numOfRepayments = rs.getInt("NB_OF_REPAYMENT");
                totalAmountRepayedBefore = rs.getFloat("TOT_AMOUNT_REPAYED");
                LOGGER.trace("NUM OF REPAY IS " + numOfRepayments);
            }
            reimbursedAmountDA = message.get("ReimbursementAmount");
            balance = new Float(message.get("TOPUP_INFO").get("AirtimeBalanceDue"));
			
            LOGGER.trace("reimbursement AMOUNT is: " + reimbursedAmountDA)
			
            newBalance = balance + reimbursedAmountDA;		

            LOGGER.trace("New Balance is : " + newBalance)		
			
            ps = con.prepareStatement(queryUpdateProd);
            ps.setFloat(1, newBalance);
            ps.setInt(2, numOfRepayments + 1);
            ps.setDouble(3, reimbursedAmountDA + totalAmountRepayedBefore);
            if (newBalance >= 0){
                ps.setString(4, "CLOSED");
                LOGGER.info("FULL REPAYMENT, new product status is CLOSED.");	
            }else{
                ps.setString(4, "ACTIVE")
            }
            message.put("NEW_BALANCE", Math.abs(newBalance));
            int res = ps.executeUpdate();
			
        }catch(SQLException e) {
            LOGGER.warn("updateNotification: SQL Exception:" + e.getMessage());
        }finally {
            try{ 
                if (rs != null) { rs.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}	
            try{ 
                if (ps != null) { ps.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	
            try{ 
                if (stmt != null) { stmt.close()}
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
