package com.ema.daa.topup;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import com.experian.eda.enterprise.da.processor.DAData;
import com.ema.connectivity.connectionpool.ConnectionPool;
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


public class GetTopUpNotificationInfo implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(GetTopUpNotificationInfo.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + GetTopUpNotificationInfo.getName());
    private static final String propertiesPath = System.getProperty("client.solution.home") + "/conf/system/system.properties";
    private final String alias;
    private final String signature;
    private HashMap mappingAdaptor;
    
    private void loadProperties(){
        LOGGER.trace("Loading properties...");
        File propertiesFile = new File(propertiesPath);        
        Properties connectivityProperties = new Properties();
        connectivityProperties.load(new FileInputStream(propertiesFile));
        Properties previousProperties = System.getProperties();
        previousProperties.putAll(connectivityProperties);
        ConnectionPool.initConnections(System.getProperties());
        LOGGER.trace("Properties OK");
		
    }
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN GetTopUpNotificationInfo");
        this.loadProperties();
        message.put("reseller", System.getProperty("sourcePhoneNumber"));
        //Flag = 0: Withdraw from subscriber, deposit to reseller.
        message.put("flag", "0");
        LOGGER.debug("Billing operation flag : 0");
        HashMap notifInf = this.getNotificationInfo(message);
        LOGGER_TIME.debug("getNotificationInfo");
        DAData[] data = buildData(notifInf);
        LOGGER_TIME.debug("buildData");
        message.put("output",data);
        message.put("TOPUP_INFO", notifInf);
        LOGGER_TIME.debug("OUT GetTopUpNotificationInfo");
        return null;        
    }
    
    private DAData[] buildData(HashMap notifInf){
        File csvFile = new File(System.getProperty("csv.path.reimb"));
        Scanner csvInput = new Scanner(csvFile);
        HashMap<String,Integer> layoutMap = new HashMap<String,Integer>();
        ArrayList<DAData> dataList = new ArrayList<DAData>();
        DAData parameters = new DAData("OCONTROL");
        parameters.setValue("ALIAS",System.getProperty("strategy.input.alias.reimb"));
        parameters.setValue("SIGNATURE",System.getProperty("strategy.input.signature"));
        LOGGER.debug("Start build alias, signature= " + System.getProperty("strategy.input.alias.reimb") + ", " + System.getProperty("strategy.input.signature"));
        dataList.add(parameters);
        int index = 0;
        // Skip the header line
        csvInput.nextLine();
        while(csvInput.hasNext()) {
            String nextLine = csvInput.nextLine();
            String[] properties = nextLine.split(",");
            String layoutName = properties[0];
            String fieldName = properties[1];
            String fieldType = properties[2];
            if(layoutMap.containsKey(layoutName)){
                int k = layoutMap.get(layoutName);
                updateData(dataList.get(k), fieldName, fieldType, notifInf);
            }
            else{
                layoutMap.put(layoutName,++index);
                dataList.add(new DAData(layoutName));
                updateData(dataList.get(index),fieldName,fieldType,notifInf);
            }
        }
        csvInput.close();
		
        for(int i=0; i<dataList.size(); i++){
            dataList.get(i).initDone();
        }
		
        return dataList.toArray(new DAData[dataList.size()]);
    }
	
    private HashMap getTopUpNotification(Message message, Connection conn) throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        HashMap notificationInfo = new HashMap();
        String topUpNotificationId = message.get("topup_id");
        HashMap mappingAdaptor = message.get("MAP_REIMB_IN");
        String min = "";//message.get("MOBILEIDENTITYNUMBER_MIN");
        try {
            String query = "SELECT * FROM TOPUP_NOTIFICATION WHERE TOPUP_NOTIFICATION_ID = " + topUpNotificationId;
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            LOGGER.debug("topupinfo: query executed: " + query);
			
            if (rs.next()) {
                notificationInfo.put(mappingAdaptor.get("TOPUP_AMOUNT"), rs.getFloat("TOPUP_AMOUNT"));
                notificationInfo.put(mappingAdaptor.get("TRANSACTION_ID"), rs.getString("TRANSACTION_ID"));
                notificationInfo.put(mappingAdaptor.get("TOPUP_TYPE"), rs.getString("TOPUP_TYPE"));
                min = rs.getString("MOBILEIDENTITYNUMBER_MIN");
                if(min != null){
                    notificationInfo.put("TopupMin", min);
                    message.put("MOBILEIDENTITYNUMBER_MIN", min);
                }
                message.put("TOPUP_AMOUNT", rs.getFloat("TOPUP_AMOUNT"));
                message.put("TOPUP_NOTIFICATION_ID", topUpNotificationId);
                LOGGER.trace("get topupinfo MIN: " + min + ", TOPUP_NOTIFICATION_ID is: " +  topUpNotificationId);
            }
	}catch(SQLException ex ) {
            LOGGER.warn("Get top up notification info: SQL Exception:" + ex.getMessage());		
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (rs != null){rs.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the Result set", ex)}
        }
		
        return notificationInfo;
    }
	
    private HashMap getLowBalanceInfo(Message message, Connection conn) throws SQLException{
        Statement stmt = null
        ResultSet rs2 = null;
        HashMap notificationInfo = new HashMap();
        HashMap mappingAdaptor = message.get("MAP_REIMB_IN");
        try {
            String min = message.get("MOBILEIDENTITYNUMBER_MIN");; 
            stmt = conn.createStatement();
            rs2 = stmt.executeQuery("SELECT * FROM LOW_0_BALANCE WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            LOGGER.debug("SELECT * FROM LOW_0_BALANCE WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            if (rs2.next()){
                notificationInfo.put("LowbalMin", min);
                notificationInfo.put("LowbalAirtimebalance",  rs2.getFloat("AIRTIME_BALANCE"));
                notificationInfo.put(mappingAdaptor.get("TRANSACTION_ID"), rs2.getString("TRANSACTION_ID"));
            }

        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (rs2 != null){rs2.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the Result set", ex)}
        }
        return notificationInfo;
    }
	
    private HashMap getProductInfo(Message message, Connection conn) throws SQLException{
        Statement stmt = null;
        HashMap notificationInfo = new HashMap();
        HashMap mappingAdaptor = message.get("MAP_REIMB_IN");
        ResultSet rs3 = null;
        try {
            String min = message.get("MOBILEIDENTITYNUMBER_MIN");
            stmt = conn.createStatement();
            rs3 = stmt.executeQuery("SELECT * FROM DAA_PRODUCT WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "' AND PRODSTATUS = 'ACTIVE'");
            LOGGER.debug("SELECT * FROM DAA_PRODUCT WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            Float airtimeDue = 0;
            if (rs3.next()){
                airtimeDue = rs3.getFloat("AIRTIME_DUE");
                notificationInfo.put(mappingAdaptor.get("AIRTIME_DUE"), rs3.getFloat("AIRTIME_DUE"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_BALANCE"), rs3.getFloat("BALANCE"));
                notificationInfo.put("DAA_OFFER_ID", rs3.getFloat("DAA_OFFER_ID"));
                notificationInfo.put("DATE_OF_ACCEPTANCE", rs3.getDate("DATE_OF_ACCEPTANCE"));
                notificationInfo.put(mappingAdaptor.get("FEE"), rs3.getFloat("FEE"));
                notificationInfo.put("INITIAL_OFFER_AMOUNT", rs3.getFloat("INITIAL_OFFER_AMOUNT"));
                notificationInfo.put(mappingAdaptor.get("MOBILEIDENTITYNUMBER_MIN"), rs3.getFloat("MOBILEIDENTITYNUMBER_MIN"));
                notificationInfo.put("ActiveProduct", rs3.getString("PRODSTATUS"));
                message.put("EXISTS_DAA", "YES");
                message.put("DAA_PRODUCT_ID", rs3.getInt("DAA_PRODUCT_ID"));
                message.put("INITIAL_OFFER_AMOUNT", rs3.getFloat("INITIAL_OFFER_AMOUNT"));
				
                LOGGER.trace("@@@@@ ResidualAmount is " + (message.get("TOPUP_AMOUNT") - rs3.getFloat("AIRTIME_DUE")));
                notificationInfo.put("ResidualAmount", message.get("TOPUP_AMOUNT") - rs3.getFloat("AIRTIME_DUE"));
                LOGGER.trace("DAA PRODUCT ID IS" + rs3.getInt("DAA_PRODUCT_ID"));
            }else{
                message.put("EXISTS_DAA", "NO");
                LOGGER.trace("the product doesn't exists");
            }
			
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (rs3 != null){rs3.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the Result set", ex)}
        }
		
        return notificationInfo;
    }
	
    private HashMap getOfferInfo (Message message, Connection conn) throws SQLException{
        Statement stmt = null;
        ResultSet rs4 = null;
        HashMap notificationInfo = new HashMap();
        HashMap mappingAdaptor = message.get("MAP_REIMB_IN");
        try{
            String min = message.get("MOBILEIDENTITYNUMBER_MIN");
            stmt = conn.createStatement();
			
            rs4 = stmt.executeQuery("SELECT * FROM DAA_OFFER WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            LOGGER.debug("SELECT * FROM DAA_OFFER WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            Float airtimeDue = 0;
            if (rs4.next()){
                LOGGER.trace("Getting offer info");
            }
			
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (rs4 != null){rs4.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the Result set", ex)}
        }
		
        return notificationInfo;
    }
	
    private HashMap getPreviousWeekInfo(Message message, Connection conn) throws SQLException{
        Statement stmt = null;
        ResultSet rs5 = null;
        HashMap notificationInfo = new HashMap();
        HashMap mappingAdaptor = message.get("MAP_REIMB_IN");
        try{
            String min = message.get("MOBILEIDENTITYNUMBER_MIN");
            stmt = conn.createStatement();
            rs5 = stmt.executeQuery("SELECT * FROM PREVIOUS_WEEK_DATA WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            LOGGER.debug("SELECT * FROM PREVIOUS_WEEK_DATA WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            Float airtimeDue = 0;
            if (rs5.next()){
                notificationInfo.put("OnlineAutoTopup", rs5.getString("TOPUP_METHOD"));
            }
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (rs5 != null) {rs5.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the Result set", ex)}
        }
		
        return notificationInfo;
    }


    
    public HashMap getNotificationInfo(Message message) {
        Connection conn = null;
        HashMap mappingAdaptor = message.get("MAP_REIMB_IN");
        HashMap notificationInfo = new HashMap();
        
        String topUpNotificationId = message.get("topup_id");
        
        try {
            conn = this.getConnection();

            notificationInfo.putAll(this.getTopUpNotification(message, conn));
			if (message.get("EXISTS_DAA", "YES")){
				notificationInfo.putAll(this.getProductInfo(message, conn));
				// CR 19012014: Get the 
				notificationInfo.putAll(this.getInitialPaymentInfo(message, conn));
				notificationInfo.putAll(this.getLowBalanceInfo(message, conn));
				
				notificationInfo.putAll(this.getOfferInfo(message, conn));
				notificationInfo.putAll(this.getPreviousWeekInfo(message, conn));
			}
        } catch (SQLException e ) {
            LOGGER.warn("getNotificationInfo: SQL Exception:" + e.getMessage());
        } catch (Exception e1){
            LOGGER.warn("getNotificationInfo: General Exception:" + e1.getMessage());
            LOGGER.warn("getNotificationInfo: Reason:" + e1.getCause());
        }finally {
            try{ 
                if (conn != null) { conn.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the connection", ex)}			
        }
        return notificationInfo;
    }
	
    private HashMap getInitialPaymentInfo(Message message, Connection conn) throws SQLException{
        Statement stmt = null
        ResultSet rs2 = null;
        HashMap initialPaymentInfo = new HashMap();
        try {
            String productId = message.get("DAA_PRODUCT_ID"); 
            stmt = conn.createStatement();
            String queryString = "SELECT * FROM DAA_TRANSACTIONS WHERE PRODUCT_ID = '" + productId + "'" +
				"AND TRANSACTION_DESC = 'AA amount offered'";
			
            LOGGER.debug(queryString);
            rs2 = stmt.executeQuery(queryString);

            if (rs2.next()){
                initialPaymentInfo.put("transid_request",  rs2.getString("BILLING_TRANSACTION_NB"));
                LOGGER.debug("transid_request is: " + rs2.getString("BILLING_TRANSACTION_NB"));
                message.put("transid_request", rs2.getString("BILLING_TRANSACTION_NB"));
            }
        }finally{
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}
            try{ 
                if (rs2 != null){rs2.close()}
            }catch(Exception ex){LOGGER.warn("Error closing the Result set", ex)}
        }
        return initialPaymentInfo;
    }
	
    private void updateData(DAData layoutData, String fieldName , String fieldType, HashMap notificationInfo){
        if(notificationInfo.get(fieldName)!=null){
            LOGGER.debug("Field found:" + fieldName + " = " + notificationInfo.get(fieldName) + " Field type: " + fieldType);
            String value = notificationInfo.get(fieldName);
            if(fieldType == "BigDecimal"){
                layoutData.setValue(fieldName, Double.parseDouble(value));
            }
            else if(fieldType == "Integer" ){

                layoutData.setValue(fieldName, Integer.parseInt(value));
            }
            else if(fieldType == "Numeric"){
                layoutData.setValue(fieldName, Float.parseFloat(value));
            }
            else{
                layoutData.setValue(fieldName, value);
            }
        }
        else{
            if(fieldType == "BigDecimal"){
                layoutData.setValue(fieldName, 1.0);
            }
            else if(fieldType == "Integer"){
                layoutData.setValue(fieldName, 1);
            }
            else if(fieldType == "Numeric"){
                layoutData.setValue(fieldName, 1.0);
            }
            else{
                layoutData.setValue(fieldName, null);
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
