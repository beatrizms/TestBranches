package com.ema.daa.bill;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.xml.bind.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import javax.ws.rs.core.Response;
import java.io.*;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.bind.annotation.XmlRootElement

public class LoginBuy2Business implements GroovyComponent<Message> {
    private static final transient Logger LOGGER = LoggerFactory.getLogger(LoginBuy2Business.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + LoginBuy2Business.getName());
    String transId = "";
    String result = ""; 

    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
      
        try {
            LOGGER_TIME.debug("IN Login");
            String soapAnswer = message.get("dataLogin")
            if (LOGGER.isTraceEnabled()){
                LOGGER.trace("Entering Manage Login Result, data is :" + soapAnswer);
            }
		 
            InputStream is = new ByteArrayInputStream(soapAnswer.getBytes());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.parse(is);
            Node n = d.getElementsByTagName("transid").item(0);
            if (LOGGER.isTraceEnabled()){
                LOGGER.trace("Document is : " + nodeToString(n));
            }
            JAXBContext<String> jc = JAXBContext.newInstance(String.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Object je = unmarshaller.unmarshal(new DOMSource(n), String.class);
            transId = je.getValue();	
            LOGGER.trace("Trans id is: " + je.getName() + ": " + transId);						
            LOGGER_TIME.debug("parse soap answer");
            //operation result
            n = d.getElementsByTagName("result").item(0);
            if (LOGGER.isTraceEnabled()){
                LOGGER.trace("Document is : " + nodeToString(n));
            }
            je = unmarshaller.unmarshal(new DOMSource(n), String.class);
            LOGGER_TIME.debug("parse soap answer");
            result = je.getValue(); 
            message.put("transId", transId);
            message.put("loginResult", result);
            message.put("reseller", "TEST");
            LOGGER_TIME.debug("OUT Login");
        }catch(Exception ex){
            LOGGER.warn("Login Exception parsing Soap answer is: " , ex);
        }		
         
        return null;       
    }
	
	
    private static String nodeToString(Node node) {
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException ex) {
            LOGGER.warn("nodeToString Transformer Exception", ex);
        }
        return sw.toString();
    }
}
