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
package org.apache.cocoon.components.source.helpers;

import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.xml.sax.XMLizable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * The interface for a property of a source
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @author <a href="mailto:holz@fiz-chemie.de">Martin Holz</a>
 * @version CVS $Id: SourceProperty.java,v 1.3 2004/03/27 21:49:09 unico Exp $
 */
public class SourceProperty implements XMLizable {

    private static final String URI = "http://www.w3.org/2000/xmlns/";
    private static final String NS_PREFIX = "property";
    private static final String D_PREFIX = NS_PREFIX+":";
    
    private String namespace;
    private String name;
    private Element value;
    
    /**
     * Creates a new property for a source
     *
     * @param namespace The namespace of the property
     * @param name The name of the property
     */
    public SourceProperty(String namespace, String name) {

        this.namespace = namespace;
        this.name = name;

        try {
            DOMBuilder builder = new DOMBuilder();
            builder.startDocument();
            builder.startPrefixMapping(NS_PREFIX, namespace);
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(URI, NS_PREFIX, "xmlns:"+NS_PREFIX, "NMTOKEN", namespace);
            builder.startElement(namespace, name, D_PREFIX+name, attrs);
            builder.endElement(namespace, name, D_PREFIX+name);
            builder.endPrefixMapping(NS_PREFIX);
            Document doc = builder.getDocument();
            this.value = doc.getDocumentElement();
        } catch (SAXException se) {
            // do nothing
        }
    }

    /**
     * Creates a new property for a source
     *
     * @param namespace The namespace of the property
     * @param name The name of the property
     * @param value The value of the property
     */
    public SourceProperty(String namespace, String name, String value) {
        this.namespace = namespace;
        this.name = name;
        setValue(value);
    }

    /**
     * Creates a new property for a source
     *
     * @param property The property in DOM representation
     */
    public SourceProperty(Element property) {
        this.namespace = property.getNamespaceURI();
        this.name = property.getLocalName();
        this.value = property;
    }

    /**
     * Sets the namespace for this property
     *
     * @param namespace The namespace of the property
     * @deprecated buggy
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     * Return the namespace of the property
     *
     * @return The namespace of the property
     */
    public String getNamespace() {
        return this.namespace;
    }

    /**
     * Sets the name of the property
     *
     * @param name Name of the property
     * @deprecated buggy
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the name of the property
     *
     * @return Name of the property
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the property
     *
     * @param value Value of the property
     */
    public void setValue(String value) {
        try {
            DOMBuilder builder = new DOMBuilder();
            builder.startDocument();
            builder.startPrefixMapping(NS_PREFIX, namespace);
            AttributesImpl attrs = new AttributesImpl();
            attrs.addAttribute(URI, NS_PREFIX, "xmlns:"+NS_PREFIX, "NMTOKEN", namespace);
            builder.startElement(namespace, name, D_PREFIX+name, attrs);
            builder.characters(value.toCharArray(), 0, value.length());
            builder.endElement(namespace, name, D_PREFIX+name);
            builder.endPrefixMapping(NS_PREFIX);
            builder.endDocument();
            Document doc = builder.getDocument();
            this.value = doc.getDocumentElement();
        } catch (SAXException se) {
            // do nothing
        }
    }

    /**
     * Returns the value of the property
     *
     * @return Value of the property
     */
    public String getValueAsString() {
        NodeList nodeslist = this.value.getChildNodes();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i<nodeslist.getLength(); i++) {
            if ((nodeslist.item(i).getNodeType()==Node.TEXT_NODE) ||
                (nodeslist.item(i).getNodeType()==Node.CDATA_SECTION_NODE)) 
            {
                
                buffer.append(nodeslist.item(i).getNodeValue());
            }
        }

        return buffer.toString();
    }

    /**
     * Sets the value of the property
     *
     * @param values
     */
    public void setValue(NodeList values) {
        try {
            DOMBuilder builder = new DOMBuilder();
            builder.startDocument();
            builder.startElement(namespace, name, name, new AttributesImpl());
            DOMStreamer stream = new DOMStreamer(builder);
            for (int i = 0; i<values.getLength(); i++) {
                stream.stream(values.item(i));
            }
            builder.endElement(namespace, name, name);
            builder.endDocument();
            Document doc = builder.getDocument();
            this.value = doc.getDocumentElement();
        } catch (SAXException se) {
            // do nothing
        }
    }

    /**
     * Get the property value as DOM Element.
     */
    public Element getValue() {
        return this.value;
    }

    /**
     * Generates SAX events representing the object's state.<br/>
     * <b>NOTE</b> : if the implementation can produce lexical events, care should be taken
     * that <code>handler</code> can actually be a {@link org.apache.cocoon.xml.XMLConsumer} that accepts such
     * events.
     *
     * @param handler
     */
    public void toSAX(ContentHandler handler) throws SAXException {
        DOMStreamer stream = new DOMStreamer(handler);
        stream.stream(this.value);
    }
}
