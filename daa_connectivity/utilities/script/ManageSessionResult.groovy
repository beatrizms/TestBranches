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


public class CreateSession implements GroovyComponent<Message> {

    private static final transient Logger LOGGER = LoggerFactory.getLogger(CreateSession.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + CreateSession.getName());
	
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
		
        LOGGER_TIME.debug("IN ManageSessionResult");
        String sessionId = ""; 
        String result = "" ; 
        try {
            String soapAnswer = message.get("data");
            if (LOGGER.isTraceEnabled()){
                LOGGER.trace(soapAnswer);
            }
            LOGGER.trace("Entering Manage Session Result");
            InputStream is = new ByteArrayInputStream(soapAnswer.getBytes());
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document d = db.parse(is);
			
            Node n = d.getElementsByTagName( "sessionid").item(0);
            if (LOGGER.isTraceEnabled()){
                LOGGER.trace("Document is : " + nodeToString(n));
            }
			
            JAXBContext<String> jc = JAXBContext.newInstance(String.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            Object je = unmarshaller.unmarshal(new DOMSource(n), String.class);
            sessionId = je.getValue();
            LOGGER.trace("Session id is: " + je.getName() + ": " + sessionId);	
            LOGGER_TIME.debug("parse soap message");
			
            n = d.getElementsByTagName( "result").item(0);
            jc = JAXBContext.newInstance(String.class);
            unmarshaller = jc.createUnmarshaller();
            je = unmarshaller.unmarshal(new DOMSource(n), String.class);
            LOGGER_TIME.debug("parse soap message");
            result = je.getValue();
            LOGGER.trace("create session result is: " + je.getName() + ": " + result);	
			 
            message.put("sessionId", sessionId);
            message.put("result", result);
            LOGGER_TIME.debug("OUT ManageSessionResult");		
        }catch(Exception ex){
            LOGGER.warn("ManageSessionResult Exception parsing Soap answer is: " + ex.getMessage() );
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
        } catch (TransformerException te) {
            LOGGER.warn("nodeToString Transformer Exception" + te.getMessage());
        }
        return sw.toString();
    }
}
