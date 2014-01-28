package com.ema.daa.common;

import com.experian.eda.enterprise.core.api.Message;
import com.experian.eda.enterprise.script.groovy.GroovyComponent;
import groovy.transform.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.lang.StringEscapeUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import javax.xml.transform.OutputKeys;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.ProcessingInstruction;
import java.lang.reflect.Method;

import java.util.Collections;

public class GenericPrepareTemplate implements GroovyComponent<Message> {
    private Document document;
    private static final transient Logger LOGGER = LoggerFactory.getLogger(GenericPrepareTemplate.class);
    private static final transient Logger LOGGER_TIME = LoggerFactory.getLogger("time.audit." + GenericPrepareTemplate.getName());
        
    /**
     * ======================================================
     *  Support for groovy multi thread independent behavior
     * ======================================================     * 
     * 
     * @Synchronized annotation is used when the script is intended to be run in multi-threads. Uncomment to use it. 
     *
     **/ 
    //@Synchronized
    public String processMessage(final Message message, final Map<String, String> dataMap) throws Exception {
        LOGGER_TIME.debug("IN GenericPrepareTemplate");
        LOGGER.trace("GenericPrepareTemplate Processing temaplate");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        message.get("ws.request.headers").put("Content-Type", "application/soap+xml;charset=UTF-8");
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.newDocument();
        }catch (ParserConfigurationException parserException) {
            parserException.getMessage();
        }

        Node myNode = createNode(document, message);
        message.put("templateRoot", myNode);
		
        LOGGER.trace("END PROCESSING TEMPLATE");
	
        LOGGER_TIME.debug("OUT GenericPrepareTemplate");
        return null;        
    }
	

    public Node createNode(Document document,final Message message) {
        // create contact element
        Element rootNode = document.createElement("message");
        document.appendChild(rootNode);
        Map messageMap = message.returnAll();

        for (key in messageMap.keySet()){
            String normalizedKey = normalizeString(key);
            Object value = messageMap.get(key);
            if (normalizedKey != null){
					
                if ( messageMap.get(key) != null && checkEligibleValue(normalizedKey)){
                    String stringValue = (String)messageMap.get(key);
                    String normalizedValue = normalizeString("" + stringValue);
                    LOGGER.trace("################### KEY, NORMALIZED AND VALUE ARE " + key + ", " + normalizedValue + ", " + value.getClass());
                    if (!checkEligibleValue(normalizedValue)) {
                        LOGGER.trace("Collection found a key : <" + key + "> Skipping");
                    } else {
                        try{
					
                            Element currentElement = document.createElement(normalizedKey);
                            currentElement.appendChild(document.createTextNode(stringValue));
                            rootNode.appendChild(currentElement);
                        }catch(Exception e){
                            LOGGER.warn("Generic Template Processing: " + key + "\n Cause " + e.getMessage());
                        }
                    }
                }
            }
        }
        Element currentElement = document.createElement("validity");
        currentElement.appendChild(document.createTextNode(System.getProperty("offer.finishDate")));
        rootNode.appendChild(currentElement);
		
        if(LOGGER.isTraceEnabled()){
            LOGGER.trace("node processing finished" + rootNode);
        }
        return rootNode;
    }
	
    String normalizeString(Object initialString){
        String tmpString = StringEscapeUtils.escapeXml("" + initialString);
        tmpString = tmpString.replace("[","");
        tmpString = tmpString.replace("]","");
        tmpString = tmpString.replace(".","");
        return tmpString;
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
  
    private boolean checkEligibleValue (Object value)
    {
  	if (value == null) return false;
	if (value instanceof com.experian.eda.enterprise.da.processor.DAData) return false;
	if (value instanceof java.util.AbstractMap) return false;
	if (value instanceof java.util.Collection) return false;
	if (value instanceof Object[]) return false;
	if (value.equals("200")) return false;
	if (value.equals("billingResult1")) return false;
	if (value.equals("billingResult")) return false;
	if (value.equals("billingReponse")) return false;
	if (value.equals("dataLogin")) return false;
	if (value.equals("dataBuy")) return false;
	if (value.equals("data")) return false;
	if (value.equals("sessionRequest")) return false;
	if (value.equals("loginRequest")) return false;
	if (value.equals("buyRequest")) return false;
	return true;
    }

}
