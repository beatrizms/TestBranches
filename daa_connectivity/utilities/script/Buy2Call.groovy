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
import javax.xml.bind.annotation.XmlRootElement;

public class Buy2Call implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(Buy2Call.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + Buy2Call.getName());

    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN Buy2Call");
        String transId = "";
        String result = "";
 		
        try{
            String soapAnswer = message.get("dataBuy")
            LOGGER.debug("@@@@@@Entering Manage Buy Result");
            if (LOGGER.isTraceEnabled()){
                LOGGER.trace("Message is : " + message);
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
            LOGGER.debug("buy 2 trans id is : " + transId);	
            LOGGER_TIME.debug("parse billing answer");
            //result
            n = d.getElementsByTagName( "result").item(0);
            if (LOGGER.isTraceEnabled()){
                LOGGER.trace("Document is : " + nodeToString(n));
            }
            je = unmarshaller.unmarshal(new DOMSource(n), String.class);
            result = je.getValue();
            LOGGER.debug("Buy 2 result 0/1 is : " + result);	
            LOGGER_TIME.debug("parseBiling answer");
            message.put("transId", transId);
            message.put("result", result);
            LOGGER_TIME.debug("OUT Buy2Call");
        }catch(Exception e){
            LOGGER.warn("Exception parsing Soap answer is: "  + e.getMessage());
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
        } catch (TransformerException e) {
            LOGGER.warn("nodeToString Transformer Exception " + e.getMessage());
        }
        return sw.toString();
    }
}
