/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.kernel.configuration;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import org.xml.sax.ContentHandler; 
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl; 

/**
 * <p>The {@link ConfigurationSerializer} serializes {@link Configuration}
 * instances as XML data.</p>
 *
 * <p>A simple static {@link #serialize(Configuration,ContentHandler)} method
 * is provided for direct SAX-2 integration, otherwise the
 * {@link #serialize(Configuration,OutputStream)} method will use the default
 * JAXP implementation of the platform to serialize XML documents to
 * {@link OutputStream} instances.</p>
 *
 * @author <a href="mailto:pier@apache.org">Pier Fumagalli</a>
 * @version 1.0 (CVS $Revision: 1.4 $)
 */
public class ConfigurationSerializer {

    /** <p>The character array used for indenting.</p> */
    private static final char indent[] = "  ".toCharArray();
    
    /** <p>The character array used for newlines.</p> */
    private static final char newline [] = 
        System.getProperty("line.separator").toCharArray();
    
    /** <p>Deny creation of {@link ConfigurationSerializer} instances.</p> */
    public ConfigurationSerializer() {
        super();
    }

    /**
     * <p>Serialize a {@link Configuration} instance as an XML document to
     * the specified {@link OutputStream}.</p>
     *
     * @param configuration the {@link Configuration} instance to serialize.
     * @param out the {@link OutputStream} where XML will be written to.
     * @throws SAXException if there was a problem generating XML data.
     * @throws ConfigurationException if there was a problem generating XML data.
     */
    public static void serialize(Configuration configuration, OutputStream out)
    throws SAXException, ConfigurationException, IOException {
        try {
            SAXTransformerFactory fact = null;
            fact = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
            TransformerHandler hdlr = fact.newTransformerHandler();
            hdlr.setResult(new StreamResult(out));
            serialize(configuration, hdlr);
        } catch (TransformerFactoryConfigurationError e) {
            throw new SAXException("JAXP transformer factory unavailable");
        } catch (TransformerConfigurationException e) {
            throw new SAXException("JAXP transformer unavailable", e);
        } catch (SAXException e) {
            Exception x = e.getException();
            if ((x != null) && (x instanceof IOException)) {
                throw ((IOException) x);
            }
            throw (e);
        }
    }
    
    /**
     * <p>Serialize a {@link Configuration} instance to a specified
     * SAX-2 {@link ContentHandler}.</p>
     *
     * @param configuration the {@link Configuration} instance to serialize.
     * @param h the {@link ContentHandler} where events will be sent to.
     * @throws SAXException if there was a problem generating XML data.
     * @throws ConfigurationException if there was a problem generating XML data.
     */
    public static void serialize(Configuration configuration, ContentHandler h)
    throws SAXException, ConfigurationException {
        h.startDocument();
        element(configuration, h, 0, null);
        h.endDocument();
    }

    /**
     * <p>Simple method to recursively serialize a {@link Configuration}
     * element and all its children.</p>
     *
     * @param configuration the {@link Configuration} instance to serialize.
     * @param h the {@link ContentHandler} where events will be sent to.
     * @param indent the indenting level.
     * @param namespace the currently declared namespace.
     * @throws SAXException if there was a problem generating XML data.
     * @throws ConfigurationException if there was a problem converting.
     */
    private static void element(Configuration configuration, ContentHandler h,
                                int indent, String namespace)
    throws SAXException, ConfigurationException {
        /* Create and fill attributes from configuration properties */
        AttributesImpl attr = new AttributesImpl();
        Iterator iterator = configuration.attributes().keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            attr.addAttribute("", name, name, "CDATA",
                              configuration.getStringAttribute(name));
        }

        /* Check for namespace declarations */
        boolean namespacedeclared = false;
        if (namespace == null) {
            if (configuration.namespace() != null) {
                h.startPrefixMapping("", configuration.namespace());
                attr.addAttribute("", "xmlns", "xmlns", "CDATA",
                                  configuration.namespace());
                namespacedeclared = true;
            }
        } else if (!namespace.equals(configuration.namespace())) {
            String local = configuration.namespace();
            if (local == null) local = "";
            h.startPrefixMapping("", (local == null? "": local));
            attr.addAttribute("", "xmlns", "xmlns", "CDATA",
                              (local == null? "": local));
            namespacedeclared = true;
        }

        /* Notify the content handler of an element start */
        indent(h, indent);
        h.startElement("", configuration.name(), configuration.name(), attr);

        /* Dump the value if and only if we have it */
        char value[] = configuration.getStringValue("").toCharArray();
        if (value.length > 0) {
            if (configuration.size() > 0) indent(h, (indent + 1));
            h.characters(value, 0, value.length);
        }

        /* Recurse into our children processing them */
        iterator = configuration.iterator();
        while (iterator.hasNext()) {
            Configuration current = (Configuration) iterator.next();
            element(current, h, (indent + 1), configuration.namespace());
        }
        
        /* Indent (if there was output) and put the end name */
        if (configuration.size() > 0) indent(h, indent);
        h.endElement("", configuration.name(), configuration.name());

        /* If we declared the namespace, undeclare it */
        if (namespacedeclared) h.endPrefixMapping("");
    }

    /**
     * <p>Simple method to indent text in a {@link ContentHandler}.</p>
     *
     * @param h the {@link ContentHandler} where events will be sent to.
     * @param n the indenting level.
     * @throws SAXException if there was a problem generating XML data.
     */
    private static void indent(ContentHandler h, int n)
    throws SAXException {
        h.characters(newline, 0, newline.length);
        for (int x = 0; x < n; x ++) h.characters(indent, 0, indent.length);
    }
}
