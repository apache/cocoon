/* ========================================================================== *
 *                                                                            *
 * Copyright 2004 The Apache Software Foundation.                             *
 *                                                                            *
 * Licensed  under the Apache License,  Version 2.0 (the "License");  you may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at                                                     *
 *                                                                            *
 *     http://www.apache.org/licenses/LICENSE-2.0                             *
 *                                                                            *
 * Unless  required  by  applicable law or  agreed  to in  writing,  software *
 * distributed under the License is distributed on an "AS IS" BASIS,  WITHOUT *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.           *
 *                                                                            *
 * See  the  License for  the  specific language  governing  permissions  and *
 * limitations under the License.                                             *
 *                                                                            *
 * ========================================================================== */
package org.apache.cocoon.kernel.configuration;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException; 
import org.xml.sax.helpers.DefaultHandler; 

/**
 * <p>The {@link ConfigurationBuilder} builds {@link Configuration} instances
 * from XML data.</p>
 *
 * <p>This class can be instantiated and used directly as a SAX
 * {@link DefaultHandler} to use with any SAX-2 compliant parser.</p>
 *
 * <p>Otherwise a simple static {@link #parse(InputSource)} method is provided
 * using the default JAXP implementation of the platform to parse XML documents
 * and producing {@link Configuration} instances.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @author <a href="http://www.vnunet.com/">VNU Business Publications</a>
 * @version 1.0 (CVS $Revision: 1.1 $)
 */
public class ConfigurationBuilder extends DefaultHandler {

    /** <p>The returnable {@link Configuration} element.</p> */
    private Configuration configuration = null;

    /** <p>The current {@link Configuration} element.</p> */
    private Configuration current = null;
    
    /** <p>The stack of {@link Configuration} elements.</p> */
    private List stack = new ArrayList();

    /** <p>The buffer holding element values.</p> */
    private StringBuffer buffer = new StringBuffer();

    /** <p>The current {@link Locator}.</p> */
    private Locator locator = null;

    /**
     * <p>Create a new {@link ConfigurationBuilder} instance.</p>
     */
    public ConfigurationBuilder() {
        super();
    }

    /**
     * <p>Return the parsed {@link Configuration} instance.</p>
     *
     * @return the parsed {@link Configuration} or <b>null</b>.
     */
    public Configuration configuration() {
        return(this.configuration);
    }

    /**
     * <p>Receive a {@link Locator} object to locate the different document
     * events.</p>
     *
     * @param locator a <b>non null</b> {@link Locator} instance.
     */
    public void setDocumentLocator (Locator locator) {
        this.locator = locator;
    }
    
    /**
     * <p>Receive notification of the beginning of the document.</p>
     *
     * <p>This method will effectively reset the {@link Configuration} item
     * being parsed.</p>
     */
    public void startDocument ()
    throws SAXException {
        this.current = null;
    }

    /**
     * <p>Receive notification of the end of the document.</p>
     *
     * <p>This method will effectively mark the {@link Configuration} item
     * being parsed as returnable by the {@link #configuration()} method.</p>
     */
    public void endDocument ()
    throws SAXException {
        this.configuration = this.current;
    }
    
    /**
     * <p>Receive notification of the start of an element.</p>
     *
     * @param uri the namespace URI of the element.
     * @param local the local name of the element.
     * @param qualified the fully qualified name of the element.
     * @param attributes all the XML attributes of the element.
     */
    public void startElement(String uri, String local, String qualified,
                             Attributes attributes)
    throws SAXException {
        /* Convert the attributes into parameters */
        Parameters parameters = new Parameters();
        for (int x = 0; x < attributes.getLength(); x++) {
            String name = attributes.getLocalName(x);
            parameters.put(name, attributes.getValue(x));
        }
        /* Create the new configuration element */
        Configuration element = new Configuration(uri, local, parameters);
        if (this.locator != null) element.locate(locator.getSystemId(),
                                                 locator.getLineNumber(),
                                                 locator.getColumnNumber());

        /* If no root element has been found yet, add it */
        if (this.current == null) this.current = element;
        this.stack.add(element);
    }

    /**
     * <p>Receive notification of the end of an element.</p>
     *
     * @param uri the namespace URI of the element.
     * @param local the local name of the element.
     * @param qualified the fully qualified name of the element.
     */
    public void endElement(String uri, String local, String qualified)
    throws SAXException {
        /* Process the value of the element */
        Configuration element = (Configuration) stack.remove(stack.size() - 1);
        if (this.buffer != null) {
            element.setValue(this.buffer.toString());
            this.buffer = null;
        }

        /* If this is not the root element, add it to the parent element */
        if (stack.size() > 0) {
            Configuration parent = (Configuration) stack.get(stack.size() - 1);
            parent.add(element);
        }
    }

    /**
     * <p>Receive notification of some character data.</p>
     *
     * @param characters an array of characters.
     * @param start the index where relevant characters start from in the array.
     * @param length the number of relevant characters in the array.
     */
    public void characters(char characters[], int start, int length)
    throws SAXException {
        /* Append valid characters to the buffer (collapse spaces) */
        String value = new String(characters, start, length);
        StringTokenizer tokenizer = new StringTokenizer(value);
        while (tokenizer.hasMoreTokens()) {
            value = tokenizer.nextToken().trim();
            if (value.length() == 0) continue;
            if (this.buffer == null) {
                this.buffer = new StringBuffer(value);
            } else {
                if (this.buffer.length() > 0) this.buffer.append(' ');
                this.buffer.append(value);
            }
        }
    }

    /**
     * <p>Parse an XML file returning a {@link Configuration} instance.</p>
     *
     * @param document the XML document to parse.
     * @throws IOException if an I/O error occurred parsing the document.
     * @throws ConfigurationException if the XML file could not be parsed or
     *                                represented as a {@link Configuration}.
     */
    public static Configuration parse(URL document)
    throws ConfigurationException, IOException {
        if (document == null) throw new NullPointerException("Null document");
        return(ConfigurationBuilder.parse(document.toString()));
    }

    /**
     * <p>Parse an XML file returning a {@link Configuration} instance.</p>
     *
     * @param document the XML document to parse.
     * @throws IOException if an I/O error occurred parsing the document.
     * @throws ConfigurationException if the XML file could not be parsed or
     *                                represented as a {@link Configuration}.
     */
    public static Configuration parse(String document)
    throws ConfigurationException, IOException {
        if (document == null) throw new NullPointerException("Null document");
        return(ConfigurationBuilder.parse(new InputSource(document)));
    }

    /**
     * <p>Parse an XML file returning a {@link Configuration} instance.</p>
     *
     * @param document the XML document to parse.
     * @throws IOException if an I/O error occurred parsing the document.
     * @throws ConfigurationException if the XML file could not be parsed or
     *                                represented as a {@link Configuration}.
     */
    public static Configuration parse(InputSource document)
    throws ConfigurationException, IOException {
        /* Check our InputSource */
        if (document == null) throw new NullPointerException("Null document");
        
        /* Instantiate a non-validating namespace aware SAXParserFactory */
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        try {
            /* Try to do some parsing */
            ConfigurationBuilder builder = new ConfigurationBuilder();
            SAXParser parser = factory.newSAXParser();
            parser.parse(document, builder);
            return(builder.configuration());
        } catch(ParserConfigurationException e) {
            throw new ConfigurationException("Unable to configure parser", e);
        } catch(SAXParseException e) {
            throw new ConfigurationException("Error parsing configurations "
                                             + "from \"" + e.getSystemId()
                                             + "\" line " + e.getLineNumber()
                                             + " column "+ e.getColumnNumber()
                                             + ": " + e.getMessage(), e);
        } catch(SAXException e) {
            Exception x = e.getException();
            if ((x != null) && (x instanceof ConfigurationException)) {
                throw((ConfigurationException)x);
            }
            throw new ConfigurationException("Can't parse configurations from "
                                             + "\"" + document.getSystemId()
                                             + "\": " + e.getMessage(), e);
        }
    }
}
