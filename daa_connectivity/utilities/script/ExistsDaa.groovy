package com.ema.daa.topup;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExistsDaa implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(ExistsDaa.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + ExistsDaa.getName());
    //
    //  @Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN ExistsDaa");
        String exists = "failed";
        if (message.get("EXISTS_DAA").equals("YES")){
            LOGGER_TIME.debug("OUT ExistsDaa");
            exists = "success";
        }else{
            LOGGER_TIME.debug("OUT ExistsDaa");
            message.put("TOPUP_STATUS", "No active product");
        }
        LOGGER.debug("EXISTS DAA IS: " + exists);
        LOGGER_TIME.debug("OUT ExistsDaa");
        return exists
    }
}