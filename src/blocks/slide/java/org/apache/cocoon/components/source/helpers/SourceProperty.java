/*
 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: SourceProperty.java,v 1.4 2003/09/05 07:31:44 cziegeler Exp $
 */
public class SourceProperty implements XMLizable {

    private String namespace;
    private String name;
    private Element value;

    /**  */
    public static final String NS_PREFIX = "property";
    private static final String D_PREFIX = NS_PREFIX+":";

    private static final String XMLNS_NS = "http://www.w3.org/2000/xmlns/";

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
            // FIXME: There must be an easier way to create a DOM element
            DOMBuilder builder = new DOMBuilder();

            builder.startDocument();
            builder.startPrefixMapping(NS_PREFIX, namespace);
            AttributesImpl attrs = new AttributesImpl();

            attrs.addAttribute(XMLNS_NS, NS_PREFIX, "xmlns:"+NS_PREFIX,
                               "NMTOKEN", namespace);
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
        // this.value = value;

        try {
            DOMBuilder builder = new DOMBuilder();

            builder.startDocument();
            builder.startPrefixMapping(NS_PREFIX, namespace);
            AttributesImpl attrs = new AttributesImpl();

            attrs.addAttribute(XMLNS_NS, NS_PREFIX, "xmlns:"+NS_PREFIX,
                               "NMTOKEN", namespace);
            attrs.addAttribute("", "foo", "foo", "NMTOKEN", "bar");

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

        for (int i = 0; i<nodeslist.getLength(); i++)
            if ((nodeslist.item(i).getNodeType()==Node.TEXT_NODE) ||
                (nodeslist.item(i).getNodeType()==Node.CDATA_SECTION_NODE)) {
                buffer.append(nodeslist.item(i).getNodeValue());
            }

        return buffer.toString();
    }

    /**
     * Sets the value of the property
     *
     * @param value Value of the property
     */
    public void setValue(Element value) {
        if ((value.getLocalName().equals(name)) &&
            (value.getNamespaceURI().equals(namespace))) {
            this.value = value;
        }
    }

    /**
     * Sets the value of the property
     *
     *
     * @param values
     */
    public void setValue(NodeList values) {
        try {
            DOMBuilder builder = new DOMBuilder();

            builder.startDocument();
            builder.startElement(namespace, name, name, new AttributesImpl());

            DOMStreamer stream = new DOMStreamer(builder);

            for (int i = 0; i<values.getLength(); i++)
                stream.stream(values.item(i));

            builder.endElement(namespace, name, name);
            builder.endDocument();

            Document doc = builder.getDocument();

            this.value = doc.getDocumentElement();
        } catch (SAXException se) {
            // do nothing
        }
    }

    /**
     *
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
