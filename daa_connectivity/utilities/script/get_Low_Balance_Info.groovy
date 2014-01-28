package com.ema.daa.lowbal;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import com.experian.eda.enterprise.da.processor.DAData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Properties;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Collections;
import java.util.Scanner;
import java.io.File;
import java.io.FileInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ema.connectivity.connectionpool.ConnectionPool;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.lang.Exception;
import java.sql.Timestamp;
import java.util.Date;



public class DataManagement implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(DataManagement.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + DataManagement.getName());
    private final String alias;
    private final String signature;

    private static final Map mapAdaptor;
    static{
		
        Map mappingAdaptor = new HashMap();
        //load a properties field with the correspondecen between the DB columns and the fields on the CSV
	
        mappingAdaptor.put("AIRTIME_DUE", "FeeDue");
        mappingAdaptor.put("VALIDITY_DATE", "OfferEliigbilityPeriod");
        mappingAdaptor.put("TOPUP_METHOD", "OnlineAutoTopup");
	
        /*****/
        mappingAdaptor.put("AGE_OF_SUBSCRIBER", "AgeOfSubscriber");
        mappingAdaptor.put("RESTRICTION_BLOCK_STATUS", "BlockStatus");
        mappingAdaptor.put("CHANNEL_TYPE", "ChannelType");
        mappingAdaptor.put("CONTRACT_SPARE_1", "ContractInfoSpare1");
        mappingAdaptor.put("CONTRACT_SPARE_2", "ContractInfoSpare2");
        mappingAdaptor.put("CONTRACT_SPARE_3", "ContractInfoSpare3");
        mappingAdaptor.put("CONTRACT_TYPE", "ContractType");
        mappingAdaptor.put("COUNTRY", "CountryCode");
        mappingAdaptor.put("CURRENT_PLAN", "CurrentPlan");
        mappingAdaptor.put("EVENT_CODE_01", "EventCode01");
        mappingAdaptor.put("EVENT_CODE_02", "EventCode02");
        mappingAdaptor.put("EVENT_CODE_03", "EventCode03");
        mappingAdaptor.put("HANDSET_COST", "HandsetCost");
        mappingAdaptor.put("HIGHEST_AIRTIME_BALANCE_WEEK", "HighestAirtimeBalanceInWeek");
        mappingAdaptor.put("HIGHEST_TOPUP_AMOUNT", "HighestTopupAmount");
        mappingAdaptor.put("IMEI_ESN", "Imei");
        mappingAdaptor.put("LOWEST_AIRTIME_BALANCE_WEEK", "LowestAirtimeBalanceInWeek");
        mappingAdaptor.put("LOWEST_TOPUP_AMOUNT", "LowestTopupAmount");
        mappingAdaptor.put("MOBILEIDENTITYNUMBER_MIN", "Min");
        mappingAdaptor.put("MONTHS_SINCE_A_SUBSCRIBER", "MthsSinceSubscriber");
        mappingAdaptor.put("NUMDAYS_ONZERO_BALANCE_WEEK", "NbrDaysZeroBalInWeek");
        mappingAdaptor.put("NUMTIMES_ONZERO_BALANCE_WEEK", "NbrTimesZeroBalInWeek");
        mappingAdaptor.put("OUTSTANDING_BALANCE", "OutstandingBalance");
        mappingAdaptor.put("SUBSCRIBER_SPARE_1", "SubscSpare1");
        mappingAdaptor.put("SUBSCRIBER_SPARE_2", "SubscSpare2");
        mappingAdaptor.put("SUBSCRIBER_SPARE_3", "SubscSpare3");
        mappingAdaptor.put("SUBSCRIBER_TYPE", "SubscriberType");
        mappingAdaptor.put("SUBSCRIBER_NUMBER", "SubscriberUniqueId");
        mappingAdaptor.put("TOPUP_METHOD", "TopupMethod");
        mappingAdaptor.put("TOTAL_AMOUNT_AIRTIME_RECEIVED", "TotalAmountAirtimeReceived");
        mappingAdaptor.put("TOTAL_AMOUNT_AIRTIME_SENT", "TotalAmountAirtimeSent");
        mappingAdaptor.put("TOTAL_AMOUNT_SPENT", "TotalAmountSpent");
        mappingAdaptor.put("TOTAL_AMOUNT_TOPPED_UP", "TotalAmountToppedUp");
        mappingAdaptor.put("TOTAL_CALLS_COUNT", "TotalCallsCount");
        mappingAdaptor.put("TOTAL_INTERNATIONALCALLS_COUNT", "TotalInternationalCallsCount");
        mappingAdaptor.put("TOTAL_OFF_NETCALLS_COUNT", "TotalOffNetCallsCount");
        mappingAdaptor.put("TOTAL_OTHER_COUNT", "TotalOtherCount");
        mappingAdaptor.put("TOTAL_PREMIUMCALLS_COUNT", "TotalPremiumCallsCount");
        mappingAdaptor.put("TOTAL_ROAMING_CALLS_COUNT", "TotalRoamingCallsCount");
        mappingAdaptor.put("TOTAL_SMS_COUNT", "TotalSmsCount");
        mappingAdaptor.put("TOTAL_TOPUP_COUNT", "TotalTopupCount");
        mappingAdaptor.put("TOTALVALUE_DATA", "TotalValueData");
        mappingAdaptor.put("TOTALVALUE_INTERNATIONAL_CALLS", "TotalValueIntlCalls");
        mappingAdaptor.put("TOTALVALUE_OFF_NET_CALLS", "TotalValueOffNetCalls");
        mappingAdaptor.put("TOTALVALUE_OTHERS", "TotalValueOther");
        mappingAdaptor.put("TOTALVALUE_PREMIUM_CALLS", "TotalValuePremiumCalls");
        mappingAdaptor.put("TOTALVALUE_ROAMING_CALLS", "TotalValueRoamingCalls");
        mappingAdaptor.put("TOTALVALUE_SMS", "TotalValueSms");
        mappingAdaptor.put("TOTALVALUE_ON_NET_CALLS", "TotalValueOnNetCalls");
        mappingAdaptor.put("TOTAL_ON_NETCALLS_COUNT", "TotalOnNetCallsCount");
        /*****/
		
        mappingAdaptor.put("AIRTIME_RECEIVED_AVG1_4", "AirtimeReceivedW0overavgw1w4");
        mappingAdaptor.put("AIRTIME_RECEIVED_AVG2_7", "AirtimeReceivedW0overavgw2w7");
        mappingAdaptor.put("AIRTIME_RECEIVED_MAX0_5", "AirtimeReceivedmaxW0w5");
        mappingAdaptor.put("AIRTIME_RECEIVED_MIN0_5", "AirtimeReceivedminW0w5");
        mappingAdaptor.put("AIRTIME_SENT_AVG1_4", "AirtimeSentW0overavgw1w4");
        mappingAdaptor.put("AIRTIME_SENT_AVG2_7", "AirtimeSentW0overavgw2w7");
        mappingAdaptor.put("AIRTIME_SENT_MAX0_5", "AirtimeSentmaxW0w5");
        mappingAdaptor.put("AIRTIME_SENT_MIN0_5", "AirtimeSentminW0w5");
        mappingAdaptor.put("HIGHEST_BALANCE_AVG1_4", "HighestAirtimeBalW0overavgw1w4");
        mappingAdaptor.put("HIGHEST_BALANCE_AVG2_7", "HighestAirtimeBalW0overavgw2w7");
        mappingAdaptor.put("LOWEST_BALANCE_AVG1_4", "LowestAirtimeBalW0overavgw1w4");
        mappingAdaptor.put("LOWEST_BALANCE_AVG2_7", "LowestAirtimeBalW0overavgw2w7");
        mappingAdaptor.put("LOWEST_BALANCE_AVG0_5", "LowestAirtimeBallavgW0w5");
        mappingAdaptor.put("LOWEST_BALANCE_MAX0_5", "LowestAirtimeBallmaxW0w5");
        mappingAdaptor.put("LOWEST_BALANCE_MIN0_5", "LowestAirtimeBallminW0w5");
        mappingAdaptor.put("LOWEST_TOPUP_AMOUNT_AVG0_5", "LowestTopupAmtavgW0w5");
        mappingAdaptor.put("NUMDAYS_0_BALANCE_AVG1_4", "NbrDaysZerobalW0overavgw1w4");
        mappingAdaptor.put("NUMDAYS_0_BALANCE_AVG0_5", "NbrDaysZerobalavgW0w5");
        mappingAdaptor.put("NUMDAYS_0_BALANCE_MAX0_5", "NbrDaysZerobalmaxW0w5");
        mappingAdaptor.put("NUMDAYS_0_BALANCE_MIN0_5", "NbrDaysZerobalminW0w5");
        mappingAdaptor.put("NUMTIMES_0_BALANCE_AVG1_4", "NbrTimesZerobalW0overavgw1w4");
        mappingAdaptor.put("NUMTIMES_0_BALANCE_AVG2_7", "NbrTimesZerobalW0overavgw2w7");
        mappingAdaptor.put("NUMTIMES_0_BALANCE_AVG0_5", "NbrTimesZerobalavgW0w5");
        mappingAdaptor.put("NUMTIMES_0_BALANCE_MAX0_5", "NbrTimesZerobalmaxW0w5");
        mappingAdaptor.put("NUMTIMES_0_BALANCE_MIN0_5", "NbrTimesZerobalminW0w5");
        mappingAdaptor.put("TOTAL_AMOUNT_SPENT_AVG2_7", "TotalAmtSpendW0overavgw2w7");
        mappingAdaptor.put("TOTAL_SPENT_AMOUNT_AVG0_4", "TotalAmtSpendAvgW0w5");
        mappingAdaptor.put("TOTAL_SPENT_AMOUNT_MAX0_5", "TotalAmtSpendMaxW0w5");
        mappingAdaptor.put("TOTAL_SPENT_AMOUNT_MIN0_5", "TotalAmtSpendMinW0w5");
        mappingAdaptor.put("TOTAL_AMOUNT_SPENT_AVG1_4", "TotalAmtSpentW0overavgw1w4");
        mappingAdaptor.put("TOTAL_CALLS_AMOUNT_1MONTH", "TotalCallsAmountW0overavgw1w4");
        mappingAdaptor.put("TOTAL_CALLS_AMOUNT_2MONTHS", "TotalCallsAmountW0overavgw2w7");
        mappingAdaptor.put("TOTAL_CALLS_AMOUNT_AVG0_5", "TotalCallsAmountAvgW0w5");
        mappingAdaptor.put("TOTAL_CALLS_AMOUNT_MAX0_5", "TotalCallsAmountMaxW0w5");
        mappingAdaptor.put("TOTAL_CALLS_AMOUNT_MIN0_5", "TotalCallsAmountMinW0w5");
        mappingAdaptor.put("TOTAL_CALLS_COUNT_AVG1_4", "TotalCallsCountW0overavgw1w4");
        mappingAdaptor.put("TOTAL_CALLS_COUNT_AVG2_7", "TotalCallsCountW0overavgw2w7");
        mappingAdaptor.put("TOTAL_DATA_AMOUNT_AVG0_5", "TotalDataAmtAvgW0w5");
        mappingAdaptor.put("TOTAL_DATA_AMOUNT_MAX0_5", "TotalDataAmtMaxW0w5");
        mappingAdaptor.put("TOTAL_DATA_AMOUNT_MIN0_5", "TotalDataAmtMinW0w5");
        mappingAdaptor.put("TOTAL_INTERCALLS_AMOUNT_AVG0_5", "TotalIntlCallsAmtAvgW0w5");
        mappingAdaptor.put("TOTAL_INTERCALLS_AMOUNT_MAX0_5", "TotalIntlCallsAmtMaxW0w5");
        mappingAdaptor.put("TOTAL_INTERCALLS_AMOUNT_MIN0_5", "TotalIntlCallsAmtMinW0w5");
        mappingAdaptor.put("TOTAL_OTHER_AMOUNT_AVG0_5", "TotalOtherAmtAvgW0w5");
        mappingAdaptor.put("TOTAL_OTHER_AMOUNT_MAX0_5", "TotalOtherAmtMaxW0w5");
        mappingAdaptor.put("TOTAL_OTHER_AMOUNT_MIN0_5", "TotalOtherAmtMinW0w5");
        mappingAdaptor.put("TOTAL_PREMCALLS_AMOUNT_AVG0_5", "TotalPremiumCallsAmtAvgW0w5");
        mappingAdaptor.put("TOTAL_PREMCALLS_AMOUNT_MAX0_5", "TotalPremiumCallsAmtMaxW0w5");
        mappingAdaptor.put("TOTAL_PREMCALLS_AMOUNT_MIN0_5", "TotalPremiumCallsAmtMinW0w5");
        mappingAdaptor.put("TOTAL_ROAMCALLS_AMOUNT_AVG0_5", "TotalRoamingCallsAmtAvgW0w5");
        mappingAdaptor.put("TOTAL_ROAMCALLS_AMOUNT_MAX0_5", "TotalRoamingCallsAmtMaxW0w5");
        mappingAdaptor.put("TOTAL_ROAMCALLS_AMOUNT_MIN0_5", "TotalRoamingCallsAmtMinW0w5");
        mappingAdaptor.put("TOTAL_SMS_AMOUNT_AVG0_5", "TotalSmsAmtAvgW0w5");
        mappingAdaptor.put("TOTAL_SMS_AMOUNT_MAX0_5", "TotalSmsAmtMaxW0w5");
        mappingAdaptor.put("TOTAL_SMS_AMOUNT_MIN0_5", "TotalSmsAmtMinW0w5");
        mappingAdaptor.put("TOTAL_SMS_AMOUNT_AVG1_4", "TotalSmsAmountW0overavgw1w4");
        mappingAdaptor.put("TOTAL_SMS_AMOUNT_AVG2_7", "TotalSmsAmountW0overavgw2w7");
        mappingAdaptor.put("TOTAL_SMS_COUNT_AVG1_4", "TotalSmsCountW0overavgw1w4");
        mappingAdaptor.put("TOTAL_SMS_COUNT_AVG2_7", "TotalSmsCountW0overavgw2w7");
        mappingAdaptor.put("TOTAL_TOPUP_COUNT_AVG1_4", "TotalTopupCountW0overavgw1w4");
        mappingAdaptor.put("TOTAL_TOPUP_COUNT_AVG2_7", "TotalTopupCountW0overavgw2w7");
        mappingAdaptor.put("TOTAL_TOPUP_COUNT_AVG0_5", "TotalTopupCountavgW0w5");
        mappingAdaptor.put("TOTAL_TOPUP_COUNT_MIN0_5", "TotalTopupCountminW0w5");
        mappingAdaptor.put("TOTAL_TOPUP_VALUE_AVG1_4", "TotalTopupValueW0overavgw1w4");
        mappingAdaptor.put("TOTAL_TOPUP_VALUE_AVG2_7", "TotalTopupValueW0overavgw2w7");
        mappingAdaptor.put("TOTAL_TOPUP_VALUE_AVG0_5", "TotalTopupValueavgW0w5");
        mappingAdaptor.put("TOTAL_TOPUP_VALUE_MAX0_5", "TotalTopupValuemaxW0w5");
        mappingAdaptor.put("TOTAL_TOPUP_VALUE_MIN0_5", "TotalTopupValueminW0w5");
        mappingAdaptor.put("AIRTIME_RECEIVED_1MONTH", "W0w3CumAirtimeReceived");
        mappingAdaptor.put("AIRTIME_SENT_1MONTH", "W0w3CumAirtimeSent");
        mappingAdaptor.put("HIGHEST_BALANCE_1MONTH", "W0w3CumHighestAirtimeBalance");
        mappingAdaptor.put("LOWEST_BALANCE_1MONTH", "W0w3CumLowestAirtimeBalance");
        mappingAdaptor.put("NUMOFDAYS_0_BALANCE_1MONTH", "W0w3CumNbrDaysOnZeroBal");
        mappingAdaptor.put("NUMOFTIMES_0_BALANCE_1MONTH", "W0w3CumNbrTimesOnZeroBal");
        mappingAdaptor.put("TOTAL_AMOUNT_SPENT_1MONTH", "W0w3CumTotalAmountSpent");
        mappingAdaptor.put("TOTAL_CALLS_AMOUNT_AVG1_4", "W0w3CumTotalCallsAmount");
        mappingAdaptor.put("TOTAL_TOPUP_COUNT_1MONTH", "W0w3CumTotalTopupCount");
        mappingAdaptor.put("TOTAL_TOPUP_VALUE_1MONTH", "W0w3CumTotalTopupValue");
        mappingAdaptor.put("AIRTIME_RECEIVED_2MONTHS", "W0w7CumAirtimeReceived");
        mappingAdaptor.put("AIRTIME_SENT_2MONTHS", "W0w7CumAirtimeSent");
        mappingAdaptor.put("HIGHEST_BALANCE_2MONTHS", "W0w7CumHighestAirtimeBalance");
        mappingAdaptor.put("LOWEST_BALANCE_2MONTHS", "W0w7CumLowestAirtimeBalance");
        mappingAdaptor.put("NUMOFDAYS_0_BALANCE_2MONTHS", "W0w7CumNbrDaysOnZeroBal");
        mappingAdaptor.put("NUMOFTIMES_0_BALANCE_2MONTHS", "W0w7CumNbrTimesOnZeroBal");
        mappingAdaptor.put("TOTAL_AMOUNT_SPENT_2MONTHS", "W0w7CumTotalAmountSpent");
        mappingAdaptor.put("TOTAL_CALLS_AMOUNT_AVG2_7", "W0w7CumTotalCallsAmount");
        mappingAdaptor.put("TOTAL_TOPUP_COUNT_2MONTHS", "W0w7CumTotalTopupCount");
        mappingAdaptor.put("TOTAL_TOPUP_VALUE_2MONTHS", "W0w7CumTotalTopupValue");
		
        mapAdaptor = Collections.unmodifiableMap(mappingAdaptor);

    }
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        Connection conn = null;
		
        try{
            LOGGER_TIME.debug("IN get_low_Balance_Info");
            String stop = System.getProperty("smsResponse.STOP");
				
            LOGGER.trace("Start process Message");
            //Map mappingAdaptor = loadMappingAdaptor();
            message.put("reseller", System.getProperty("sourcePhoneNumber"));
			
            conn = getConnection();
            Map notifInf = getNotificationInfo(message, mapAdaptor, conn);
            LOGGER_TIME.debug("getNotificationInfo");
            getControlInfo(notifInf, conn);
            LOGGER_TIME.debug("getControlInfo");
            DAData[] data = buildData(notifInf);
            message.put("output",data);
            LOGGER_TIME.debug("OUT get_low_Balance_Info");
        }catch(Exception ex){
            LOGGER.warn("Exception: ", ex);
        }finally{
            try{ 
                if (conn != null) { conn.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the connection", ex)}	
        }		
        return null;    
	
    }
    
    private DAData[] buildData(HashMap notifInf){
		
        File csvFile = new File(System.getProperty("csv.path.offer"));
        Scanner csvInput = new Scanner(csvFile);
        HashMap<String,Integer> layoutMap = new HashMap<String,Integer>();
        ArrayList<DAData> dataList = new ArrayList<DAData>();
        DAData parameters = new DAData("OCONTROL");
        parameters.setValue("ALIAS",System.getProperty("strategy.input.alias.offer"));
        parameters.setValue("SIGNATURE",System.getProperty("strategy.input.signature"));
        LOGGER.debug("Get Low Balance infor signature= " + System.getProperty("strategy.input.alias.offer") + ", " + System.getProperty("strategy.input.signature"));
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
            }else{
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

	
    private Connection getConnection(){
        LOGGER.debug("getting connection from pool");		
        Connection connection = ConnectionPool.getConnection("DAA_CONNECTION_POOL");
        LOGGER.debug("before returning connection: " + connection);
        return connection;
    }
	
    public void getControlInfo(HashMap notifInfo, Connection conn){
        Statement stmt = null;
        ResultSet rs = null;
        float currentAmountDue = 9999999999999;
        int currentOfferNum = 9999;
        String query = "select * from DAA_CONTROL where   ( TRUNC(CURRENT_DATE, 'DDD'  ) - TRUNC(CONTROL_DATE, 'DDD')) =0 ";
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
            LOGGER.debug("getControlInfo: query executed" + query);
            if (rs.next()){
                currentAmountDue = rs.getFloat("AMOUNT_ACCEPTED");
                currentOfferNum = rs.getInt("NUM_OFFERS_SENT");
                notifInfo.put("DailycheckMaxamountDue", currentAmountDue);
                notifInfo.put("DailycheckMaxnbroffer", currentOfferNum);
            }
        } catch (SQLException ex ) {
            LOGGER.warn("getControlInfo: SQL Exception:" + ex.getMessage());
        } catch (Exception ex){
            LOGGER.warn("getControlInfo: General Exception:" + ex.getMessage());
            LOGGER.warn("getControlInfo: Reason:" + ex.getCause());
        }finally {
            try{ 
                if (rs != null) { rs.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}	
			
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	
        }
    }
    
    public Map getNotificationInfo(Message message, Map mappingAdaptor, Connection conn) {
        Statement stmt = null;
        String notificationId = message.get("notification_Id");
        HashMap notificationInfo = new HashMap();
        String min = "";
        ResultSet rs = null;
        ResultSet rs2 = null;
        ResultSet rs3 = null;
        ResultSet rs4 = null;
        String query = "select * from LOW_0_BALANCE where LOW_0_BALANCE_ID = '" + notificationId + "'";
        LOGGER.debug(query);	
        try {
            stmt = conn.createStatement();
            LOGGER.trace("getNotificationInfo: Statement created");
            /* first query */
            rs = stmt.executeQuery(query);
            if (rs.next()) {
                LOGGER.trace("getNotificationInfo: in the while ");
                min = rs.getString("MOBILEIDENTITYNUMBER_MIN");
                message.put("min", min);
                notificationInfo.put(mappingAdaptor.get("MOBILEIDENTITYNUMBER_MIN"), min);
                message.put("MOBILEIDENTITYNUMBER_MIN", min); 
                LOGGER.trace("getNotificationInfo MIN: " + min);
            }
			
			
            updateLowBalanceInfo(conn, notificationId, min); 
			
            // /* 2nd query */		
            rs2 = stmt.executeQuery("SELECT * FROM PREVIOUS_WEEK_DATA WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            LOGGER.debug("SELECT * FROM PREVIOUS_WEEK_DATA WHERE MOBILEIDENTITYNUMBER_MIN= '" + min + "'");
            if (rs2.next()){
                notificationInfo.put(mappingAdaptor.get("RESTRICTION_BLOCK_STATUS"), "");
                notificationInfo.put(mappingAdaptor.get("AGE_OF_SUBSCRIBER"),  rs2.getFloat("AGE_OF_SUBSCRIBER"));
				
                String blockStatus = (String)rs2.getString("RESTRICTION_BLOCK_STATUS");
                if (blockStatus != null){	
                    notificationInfo.put(mappingAdaptor.get("RESTRICTION_BLOCK_STATUS"), blockStatus.trim());
                    LOGGER.debug("RESTRICTION_BLOCK_STATUS: " + blockStatus.trim());
                }else{
                    notificationInfo.put(mappingAdaptor.get("RESTRICTION_BLOCK_STATUS"), "");
                }

                notificationInfo.put(mappingAdaptor.get("CHANNEL_TYPE"), rs2.getString("CHANNEL_TYPE"));
                notificationInfo.put(mappingAdaptor.get("CONTRACT_SPARE_1"), rs2.getDouble("CONTRACT_SPARE_1"));
                notificationInfo.put(mappingAdaptor.get("CONTRACT_SPARE_2"), rs2.getString("CONTRACT_SPARE_2"));
                notificationInfo.put(mappingAdaptor.get("CONTRACT_SPARE_3"), rs2.getString("CONTRACT_SPARE_3"));
                notificationInfo.put(mappingAdaptor.get("CONTRACT_TYPE"), rs2.getString("CONTRACT_TYPE"));
                notificationInfo.put(mappingAdaptor.get("COUNTRY"), rs2.getString("COUNTRY"));
                notificationInfo.put(mappingAdaptor.get("CURRENT_PLAN"), rs2.getString("CURRENT_PLAN"));
                notificationInfo.put(mappingAdaptor.get("EVENT_CODE_01"), rs2.getString("EVENT_CODE_02"));
                notificationInfo.put(mappingAdaptor.get("EVENT_CODE_02"), rs2.getString("COUNTRY"));
                notificationInfo.put(mappingAdaptor.get("EVENT_CODE_03"), rs2.getString("EVENT_CODE_03"));
                notificationInfo.put(mappingAdaptor.get("HANDSET_COST"), rs2.getDouble("HANDSET_COST"));
                notificationInfo.put(mappingAdaptor.get("HIGHEST_AIRTIME_BALANCE_WEEK"), rs2.getDouble("HIGHEST_AIRTIME_BALANCE_WEEK"));
                notificationInfo.put(mappingAdaptor.get("HIGHEST_TOPUP_AMOUNT"), rs2.getDouble("HIGHEST_TOPUP_AMOUNT"));
                notificationInfo.put(mappingAdaptor.get("IMEI_ESN"), rs2.getString("IMEI_ESN"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_AIRTIME_BALANCE_WEEK"), rs2.getDouble("LOWEST_AIRTIME_BALANCE_WEEK"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_TOPUP_AMOUNT"), rs2.getDouble("LOWEST_TOPUP_AMOUNT"));
                //notificationInfo.put(mappingAdaptor.get("MOBILEIDENTITYNUMBER_MIN"), rs2.getString("MOBILEIDENTITYNUMBER_MIN"));
                notificationInfo.put(mappingAdaptor.get("MONTHS_SINCE_A_SUBSCRIBER"), rs2.getInt("MONTHS_SINCE_A_SUBSCRIBER"));
                notificationInfo.put(mappingAdaptor.get("NUMDAYS_ONZERO_BALANCE_WEEK"), rs2.getInt("NUMDAYS_ONZERO_BALANCE_WEEK"));
                notificationInfo.put(mappingAdaptor.get("NUMTIMES_ONZERO_BALANCE_WEEK"), rs2.getInt("NUMTIMES_ONZERO_BALANCE_WEEK"));
                //notificationInfo.put(mappingAdaptor.get("OUTSTANDING_BALANCE"), rs2.getString("OUTSTANDING_BALANCE"));
                notificationInfo.put(mappingAdaptor.get("SUBSCRIBER_SPARE_1"), rs2.getDouble("SUBSCRIBER_SPARE_1"));
                notificationInfo.put(mappingAdaptor.get("SUBSCRIBER_SPARE_2"), rs2.getString("SUBSCRIBER_SPARE_2"));
                notificationInfo.put(mappingAdaptor.get("SUBSCRIBER_SPARE_3"), rs2.getString("SUBSCRIBER_SPARE_3"));
                notificationInfo.put(mappingAdaptor.get("SUBSCRIBER_TYPE"), rs2.getString("SUBSCRIBER_TYPE"));
                notificationInfo.put(mappingAdaptor.get("SUBSCRIBER_NUMBER"), rs2.getString("SUBSCRIBER_NUMBER"));
                notificationInfo.put(mappingAdaptor.get("TOPUP_METHOD"), rs2.getString("TOPUP_METHOD"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_AMOUNT_AIRTIME_RECEIVED"), rs2.getDouble("TOTAL_AMOUNT_AIRTIME_RECEIVED"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_AMOUNT_AIRTIME_SENT"), rs2.getDouble("TOTAL_AMOUNT_AIRTIME_SENT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_AMOUNT_SPENT"), rs2.getDouble("TOTAL_AMOUNT_SPENT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_AMOUNT_TOPPED_UP"), rs2.getDouble("TOTAL_AMOUNT_TOPPED_UP"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_COUNT"), rs2.getInt("TOTAL_CALLS_COUNT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_INTERNATIONALCALLS_COUNT"), rs2.getInt("TOTAL_INTERNATIONALCALLS_COUNT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_OFF_NETCALLS_COUNT"), rs2.getInt("TOTAL_OFF_NETCALLS_COUNT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_OTHER_COUNT"), rs2.getInt("TOTAL_OTHER_COUNT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_PREMIUMCALLS_COUNT"), rs2.getInt("TOTAL_PREMIUMCALLS_COUNT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_ROAMING_CALLS_COUNT"), rs2.getInt("TOTAL_ROAMING_CALLS_COUNT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SMS_COUNT"), rs2.getInt("TOTAL_SMS_COUNT"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_COUNT"), rs2.getInt("TOTAL_TOPUP_COUNT"));
                notificationInfo.put(mappingAdaptor.get("TOTALVALUE_DATA"), rs2.getDouble("TOTALVALUE_DATA"));
                notificationInfo.put(mappingAdaptor.get("TOTALVALUE_INTERNATIONAL_CALLS"), rs2.getDouble("TOTALVALUE_OFF_NET_CALLS"));
                notificationInfo.put(mappingAdaptor.get("TOTALVALUE_OFF_NET_CALLS"), rs2.getDouble("TOTALVALUE_OFF_NET_CALLS"));
            }
			
			
            rs3 = stmt.executeQuery("SELECT * FROM DERIVED_HISTORIC_DATA where MOBILE_IDENTITY_NUMBER= '" + min + "'");
            LOGGER.debug("SELECT * FROM DERIVED_HISTORIC_DATA where MOBILE_IDENTITY_NUMBER= '" + min + "'");
            if (rs3.next()){
                notificationInfo.put(mappingAdaptor.get("AIRTIME_RECEIVED_AVG1_4"),  rs3.getDouble("AIRTIME_RECEIVED_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_RECEIVED_AVG2_7"),  rs3.getDouble("AIRTIME_RECEIVED_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_RECEIVED_MAX0_5"),  rs3.getDouble("AIRTIME_RECEIVED_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_RECEIVED_MIN0_5"), rs3.getDouble("AIRTIME_RECEIVED_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_SENT_AVG1_4"),  rs3.getDouble("AIRTIME_SENT_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_SENT_AVG2_7"),  rs3.getDouble("AIRTIME_SENT_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_SENT_MAX0_5"), rs3.getDouble("AIRTIME_SENT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_SENT_MIN0_5"),  rs3.getDouble("AIRTIME_SENT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("HIGHEST_BALANCE_AVG1_4"),  rs3.getDouble("HIGHEST_BALANCE_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("HIGHEST_BALANCE_AVG2_7"), rs3.getDouble("HIGHEST_BALANCE_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_BALANCE_AVG1_4"),  rs3.getDouble("LOWEST_BALANCE_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_BALANCE_AVG2_7"),  rs3.getDouble("LOWEST_BALANCE_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_BALANCE_AVG0_5"),  rs3.getDouble("LOWEST_BALANCE_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_BALANCE_MAX0_5"),  rs3.getDouble("LOWEST_BALANCE_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_BALANCE_MIN0_5"),  rs3.getDouble("LOWEST_BALANCE_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_TOPUP_AMOUNT_AVG0_5"),  rs3.getDouble("LOWEST_TOPUP_AMOUNT_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("NUMDAYS_0_BALANCE_AVG1_4"),  rs3.getInt("NUMDAYS_0_BALANCE_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("NUMDAYS_0_BALANCE_AVG0_5"),  rs3.getInt("NUMDAYS_0_BALANCE_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("NUMDAYS_0_BALANCE_MAX0_5"),  rs3.getInt("NUMDAYS_0_BALANCE_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("NUMDAYS_0_BALANCE_MIN0_5"),  rs3.getInt("NUMDAYS_0_BALANCE_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("NUMTIMES_0_BALANCE_AVG1_4"),  rs3.getInt("NUMTIMES_0_BALANCE_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("NUMTIMES_0_BALANCE_AVG2_7"),  rs3.getInt("NUMTIMES_0_BALANCE_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("NUMTIMES_0_BALANCE_AVG0_5"),  rs3.getInt("NUMTIMES_0_BALANCE_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("NUMTIMES_0_BALANCE_MAX0_5"),  rs3.getInt("NUMTIMES_0_BALANCE_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("NUMTIMES_0_BALANCE_MIN0_5"),  rs3.getInt("NUMTIMES_0_BALANCE_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_AMOUNT_SPENT_AVG2_7"),  rs3.getDouble("TOTAL_AMOUNT_SPENT_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SPENT_AMOUNT_AVG0_4"),  rs3.getDouble("TOTAL_SPENT_AMOUNT_AVG0_4"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SPENT_AMOUNT_MAX0_5"),  rs3.getDouble("TOTAL_SPENT_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SPENT_AMOUNT_MIN0_5"),  rs3.getDouble("TOTAL_SPENT_AMOUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_AMOUNT_SPENT_AVG1_4"),  rs3.getDouble("TOTAL_AMOUNT_SPENT_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_AMOUNT_1MONTH"),  rs3.getDouble("TOTAL_CALLS_AMOUNT_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_AMOUNT_2MONTHS"),  rs3.getDouble("TOTAL_CALLS_AMOUNT_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_AMOUNT_AVG0_5"),  rs3.getDouble("TOTAL_CALLS_AMOUNT_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_AMOUNT_MAX0_5"),  rs3.getDouble("TOTAL_CALLS_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_AMOUNT_MIN0_5"),  rs3.getDouble("TOTAL_CALLS_AMOUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_COUNT_AVG1_4"),  rs3.getDouble("TOTAL_CALLS_COUNT_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_COUNT_AVG2_7"),  rs3.getDouble("TOTAL_CALLS_COUNT_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_DATA_AMOUNT_AVG0_5"),  rs3.getDouble("TOTAL_DATA_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_DATA_AMOUNT_MAX0_5"),  rs3.getDouble("TOTAL_DATA_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_DATA_AMOUNT_MIN0_5"),  rs3.getDouble("TOTAL_DATA_AMOUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_INTERCALLS_AMOUNT_AVG0_5"),  rs3.getDouble("TOTAL_INTERCALLS_AMOUNT_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_INTERCALLS_AMOUNT_MAX0_5"),  rs3.getDouble("TOTAL_INTERCALLS_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_INTERCALLS_AMOUNT_MIN0_5"),  rs3.getDouble("TOTAL_INTERCALLS_AMOUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_OTHER_AMOUNT_AVG0_5"),  rs3.getDouble("TOTAL_OTHER_AMOUNT_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_OTHER_AMOUNT_MAX0_5"),  rs3.getDouble("TOTAL_OTHER_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_OTHER_AMOUNT_MIN0_5"),  rs3.getDouble("TOTAL_OTHER_AMOUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_PREMCALLS_AMOUNT_AVG0_5"),  rs3.getDouble("TOTAL_PREMCALLS_AMOUNT_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_PREMCALLS_AMOUNT_MAX0_5"),  rs3.getDouble("TOTAL_PREMCALLS_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_PREMCALLS_AMOUNT_MIN0_5"),  rs3.getDouble("TOTAL_PREMCALLS_AMOUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_ROAMCALLS_AMOUNT_AVG0_5"),  rs3.getDouble("TOTAL_ROAMCALLS_AMOUNT_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_ROAMCALLS_AMOUNT_MAX0_5"),  rs3.getDouble("TOTAL_ROAMCALLS_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_ROAMCALLS_AMOUNT_MIN0_5"),  rs3.getDouble("TOTAL_ROAMCALLS_AMOUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SMS_AMOUNT_AVG0_5"),  rs3.getDouble("TOTAL_SMS_AMOUNT_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SMS_AMOUNT_MAX0_5"),  rs3.getDouble("TOTAL_SMS_AMOUNT_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SMS_AMOUNT_MIN0_5"),  rs3.getDouble("TOTAL_SMS_AMOUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SMS_AMOUNT_AVG1_4"),  rs3.getDouble("TOTAL_SMS_AMOUNT_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SMS_AMOUNT_AVG2_7"),  rs3.getDouble("TOTAL_SMS_AMOUNT_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SMS_COUNT_AVG1_4"),  rs3.getInt("TOTAL_SMS_COUNT_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_SMS_COUNT_AVG2_7"),  rs3.getInt("TOTAL_SMS_COUNT_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_COUNT_AVG1_4"),  rs3.getInt("TOTAL_TOPUP_COUNT_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_COUNT_AVG2_7"),  rs3.getInt("TOTAL_TOPUP_COUNT_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_COUNT_AVG0_5"),  rs3.getInt("TOTAL_TOPUP_COUNT_AVG0_5"));
                //notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_COUNT_MIN0_5"),  rs3.getInt("TOTAL_TOPUP_COUNT_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_VALUE_AVG1_4"), rs3.getDouble("TOTAL_TOPUP_VALUE_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_VALUE_AVG2_7"),  rs3.getDouble("TOTAL_TOPUP_VALUE_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_VALUE_AVG0_5"),  rs3.getDouble("TOTAL_TOPUP_VALUE_AVG0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_VALUE_MAX0_5"),  rs3.getDouble("TOTAL_TOPUP_VALUE_MAX0_5"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_VALUE_MIN0_5"),  rs3.getDouble("TOTAL_TOPUP_VALUE_MIN0_5"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_RECEIVED_1MONTH"),  rs3.getDouble("AIRTIME_RECEIVED_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_SENT_1MONTH"),  rs3.getDouble("AIRTIME_SENT_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("HIGHEST_BALANCE_1MONTH"),  rs3.getDouble("HIGHEST_BALANCE_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_BALANCE_1MONTH"),  rs3.getDouble("LOWEST_BALANCE_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("NUMOFDAYS_0_BALANCE_1MONTH"),  rs3.getInt("NUMOFDAYS_0_BALANCE_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("NUMOFTIMES_0_BALANCE_1MONTH"),  rs3.getInt("NUMOFTIMES_0_BALANCE_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_AMOUNT_SPENT_1MONTH"),  rs3.getDouble("TOTAL_AMOUNT_SPENT_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_AMOUNT_AVG1_4"),  rs3.getDouble("TOTAL_CALLS_AMOUNT_AVG1_4"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_COUNT_1MONTH"),  rs3.getDouble("TOTAL_TOPUP_COUNT_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_VALUE_1MONTH"), rs3.getDouble("TOTAL_TOPUP_VALUE_1MONTH"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_RECEIVED_2MONTHS"), rs3.getDouble("AIRTIME_RECEIVED_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("AIRTIME_SENT_2MONTHS"),  rs3.getDouble("AIRTIME_SENT_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("HIGHEST_BALANCE_2MONTHS"),  rs3.getDouble("HIGHEST_BALANCE_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("LOWEST_BALANCE_2MONTHS"),  rs3.getDouble("LOWEST_BALANCE_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("NUMOFDAYS_0_BALANCE_2MONTHS"),  rs3.getInt("NUMOFDAYS_0_BALANCE_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("NUMOFTIMES_0_BALANCE_2MONTHS"),  rs3.getInt("NUMOFTIMES_0_BALANCE_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_AMOUNT_SPENT_2MONTHS"),  rs3.getDouble("TOTAL_AMOUNT_SPENT_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_CALLS_AMOUNT_AVG2_7"),  rs3.getDouble("TOTAL_CALLS_AMOUNT_AVG2_7"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_COUNT_2MONTHS"), rs3.getInt("TOTAL_TOPUP_COUNT_2MONTHS"));
                notificationInfo.put(mappingAdaptor.get("TOTAL_TOPUP_VALUE_2MONTHS"),  rs3.getInt("TOTAL_TOPUP_VALUE_2MONTHS"));
				
				
                Float outstandingBalance = 0.0;
                String queryProduct = "SELECT SUM(BALANCE) FROM DAA_PRODUCT WHERE PRODSTATUS = 'ACTIVE' AND MOBILEIDENTITYNUMBER_MIN = '" + min + "'";
                LOGGER.debug(queryProduct);
                rs4 = stmt.executeQuery(queryProduct);
                if (rs4.next()){
                    outstandingBalance = rs4.getFloat(1);
                }
                notificationInfo.put("OutstandingBalance", outstandingBalance);
                notificationInfo.put("AirtimeBalanceDue", outstandingBalance);
				
            }

        } catch (SQLException ex ) {
            LOGGER.warn("getNotificationInfo: SQL Exception:" + ex.getMessage());
        } catch (Exception ex){
            LOGGER.warn("getNotificationInfo: General Exception:" + ex.getMessage());
            LOGGER.warn("getNotificationInfo: Reason:" + ex.getCause());
        }finally {
            try{ 
                if (rs != null) { rs.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}
            try{ 
                if (rs2 != null) { rs2.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}	
            try{ 
                if (rs3 != null) { rs3.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}	
            try{ 
                if (rs4 != null) { rs4.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the result set", ex)}		
            try{ 
                if (stmt != null) { stmt.close()}
            }catch(Exception ex){ LOGGER.warn("Error closing the statement", ex)}	

	        
        }
        return notificationInfo;
    }
	
	
    private void updateLowBalanceInfo(Connection con, String lowBalance_id, String min){
        LOGGER.debug("updateLowBalanceInfo lowBalance_id, min: " + lowBalance_id + ", " + min);
        PreparedStatement ps = null;
        int lowBal_id = 0;
        String query = "update LOW_0_BALANCE set NOTIFICATION_DATE = ? , MOBILEIDENTITYNUMBER_MIN = ? , PROCESS_STATUS = ? , SMS_STATUS = ? " +
				" where LOW_0_BALANCE_ID = " + lowBalance_id;
        LOGGER.debug("updateLowBalanceInfo: " + query);		
        try{
            ps = con.prepareStatement(query);
		
            java.sql.Timestamp date = new java.sql.Timestamp(new java.util.Date().getTime());
            LOGGER.debug("updateLowBalanceInfo date: " + date.toString());
            ps.setTimestamp(1, date);
            ps.setString(2, min);
            ps.setString(3, "");
            ps.setString(4, "NOT SENT");
            int res = ps.executeUpdate();
            LOGGER.trace("updateLowBalanceInfo lowBalance_id, min: UPDATED " + res);
        }catch(SQLException ex) {
            LOGGER.warn("updateLowBalanceInfo: SQL Exception:" + ex.getMessage());
        }finally {
            if (ps != null) { ps.close(); }
			
        } 
    }
	
	
    private void updateData(DAData layoutData, String fieldName , String fieldType, HashMap notificationInfo){
        if(notificationInfo.get(fieldName)!=null){
            LOGGER.trace("Field found: " + fieldName + " = " + notificationInfo.get(fieldName) + " Field type: " + fieldType);
            String value = notificationInfo.get(fieldName);
            if(fieldType == "BigDecimal"){
                layoutData.setValue(fieldName, Double.parseDouble(value));
            }
            else if(fieldType == "Integer"){
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
}
