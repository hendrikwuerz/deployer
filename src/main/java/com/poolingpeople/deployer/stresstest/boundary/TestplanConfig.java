package com.poolingpeople.deployer.stresstest.boundary;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashMap;

/**
 * Created by hendrik on 28.09.15.
 */
public class TestplanConfig {

    // the name set in the test plan for the "user defined variable" element containing the configs
    private static final String TESTPLAN_CONFIG_NAME = "config";

    // Names of the config elements in the test plan. Needed for replace with custom data
    private static final String TESTPLAN_CONFIG_IP = "IP";
    private static final String TESTPLAN_CONFIG_PORT = "Port";
    private static final String TESTPLAN_CONFIG_THREADS = "amountThreads";
    private static final String TESTPLAN_CONFIG_LOOPS = "amountLoops";

    // The new data
    private String ip, port, threads, loops;

    // setter
    public TestplanConfig setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public TestplanConfig setPort(String port) {
        this.port = port;
        return this;
    }

    public TestplanConfig setThreads(String threads) {
        this.threads = threads;
        return this;
    }

    public TestplanConfig setLoops(String loops) {
        this.loops = loops;
        return this;
    }


    /**
     * parses the test plan in the input stream and replaces ip, port, threads and loops with the values set before
     * The test plan needs a "User defined variables" element as first child under "Test Plan". It must be named as
     * "config". The variables must be defined with the names of the static vars on the top of this class.
     * If no values for ip, port, threads and loops were set before, nothing will be done
     * @param stream
     *          The original test plan
     * @return
     *          The new Test plan
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     */
    public String parseTestPlan(InputStream stream) throws ParserConfigurationException, IOException, SAXException, TransformerException {

        // insert custom data into test plan
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(stream);

        NodeList arguments = doc.getElementsByTagName("Arguments");
        for(int i = 0; i < arguments.getLength(); i++) { // Loop all "user defined variables" in the test plan
            Node userDefinedVariables = arguments.item(i);
            NamedNodeMap attributes = userDefinedVariables.getAttributes();
            Node testname = attributes.getNamedItem("testname"); // get the attribute "testname". This is the defined name for the element
            if(testname != null && testname.getTextContent().equals(TESTPLAN_CONFIG_NAME)) { // correct "user defined variables"

                NodeList elementProps = userDefinedVariables.getChildNodes().item(1).getChildNodes();
                for(int j = 0; j < elementProps.getLength(); j++) { // loop the variables
                    Node variable = elementProps.item(j);

                    if(variable != null && variable.getAttributes() != null && variable.getAttributes().getNamedItem("name") != null) {
                        String varName = variable.getAttributes().getNamedItem("name").getTextContent();

                        // update with correct value and only if this is set.
                        if(varName.equals(TESTPLAN_CONFIG_IP) && ip != null && !ip.equals("")) {
                            updateVariableValue(variable, ip);
                        } else if(varName.equals(TESTPLAN_CONFIG_PORT) && port != null && !port.equals("")) {
                            updateVariableValue(variable, port);
                        } else if(varName.equals(TESTPLAN_CONFIG_THREADS) && threads != null && !threads.equals("")) {
                            updateVariableValue(variable, threads);
                        } else if(varName.equals(TESTPLAN_CONFIG_LOOPS) && loops != null && !loops.equals("")) {
                            updateVariableValue(variable, loops);
                        }

                    }

                }
            }
        }

        /* Needed to return an input stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(doc);
        Result outputTarget = new StreamResult(outputStream);
        TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
        InputStream is = new ByteArrayInputStream(outputStream.toByteArray());*/

        StringWriter writer = new StringWriter();
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String xml = writer.toString();

        return xml;
    }


    private void updateVariableValue(Node variable, String value) {
        NodeList childNodes = variable.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++) {
            if(hasAttribute(childNodes.item(i), "name", "Argument.value")) {
                childNodes.item(i).setTextContent(value);
            }
        }
    }


    private boolean hasAttribute(Node node, String key, String value) {
        NamedNodeMap attributes = node.getAttributes();
        if(attributes == null) return false;
        for(int i = 0; i < attributes.getLength(); i++) {
            Node searchedAttribute = attributes.getNamedItem(key);
            if(searchedAttribute != null && searchedAttribute.getTextContent().equals(value)) return true;
        }
        return false;
    }
}
