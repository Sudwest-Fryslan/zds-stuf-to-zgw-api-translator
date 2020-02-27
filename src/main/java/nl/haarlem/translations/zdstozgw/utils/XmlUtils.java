package nl.haarlem.translations.zdstozgw.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;

public class XmlUtils {
    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public static Document convertStringToDocument(String xmlStr) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder();
            return builder.parse(new InputSource(new StringReader(xmlStr)));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public static SOAPMessage stringToSoapMessage(String stringMessage) {
        SOAPMessage soapMessage = null;
        InputStream is = new ByteArrayInputStream(stringMessage.getBytes());
        try {
            soapMessage = MessageFactory.newInstance().createMessage(null, is);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return soapMessage;
    }

    public static String soapMessageToSTring(SOAPMessage soapMessage) {
        String stringMessage = "";
        try {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            soapMessage.writeTo(stream);
            stringMessage = new String(stream.toByteArray(), StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return stringMessage;
    }

    public static String xmlToString(Document xml) {
        String result = "";
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(xml), new StreamResult(writer));

            result = writer.getBuffer().toString();
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return result;
    }

    public static String getSoapMessageAsString(Document document) {
        return XmlUtils.xmlToString(document);
    }


    public static Document xmlNodesToDocument(NodeList nodes, String rootName){
        Document result = null;
        try {
            result = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Element root = result.createElement(rootName);
        result.appendChild(root);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            Node copyNode = result.importNode(node, true);
            root.appendChild(copyNode);
        }
        return result;
    }
}