/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.forms.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.xml.XMLConstants;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationAttributes;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;

import org.apache.commons.lang.BooleanUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Helper class to create and retrieve information from DOM-trees. It provides
 * some functionality comparable to what's found in Avalon's Configuration
 * objects. These lasts one could however not be used by Cocoon Forms because they
 * don't provide an accurate model of an XML file (no mixed content,
 * no namespaced attributes, no namespace declarations, ...).
 *
 * <p>This class depends specifically on the Xerces DOM implementation to be
 * able to provide information about the location of elements in their source
 * XML file. See the {@link #getLocation(Element)} method.
 *
 * @version $Id$
 */
public class DomHelper {

    public static final String XMLNS_URI = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;

    public static Location getLocationObject(Element element) {
        return LocationAttributes.getLocation(element);
    }

    /**
     * Retrieves the location of an element node in the source file from which
     * the Document was created. This will only work for Document's created
     * with the method {@link #parse(InputSource, ServiceManager)} of this class.
     */
    public static String getLocation(Element element) {
        return LocationAttributes.getLocationString(element);
    }

    public static String getSystemIdLocation(Element element) {
        return LocationAttributes.getURI(element);
    }

    public static int getLineLocation(Element element) {
        return LocationAttributes.getLine(element);
    }

    public static int getColumnLocation(Element element) {
        return LocationAttributes.getColumn(element);
    }

    /**
     * Returns all Element children of an Element that belong to the given
     * namespace.
     */
    public static Element[] getChildElements(Element element, String namespace) {
        ArrayList elements = new ArrayList();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element
                    && namespace.equals(node.getNamespaceURI()))
                elements.add(node);
        }
        return (Element[]) elements.toArray(new Element[elements.size()]);
    }

    /**
     * Returns all Element children of an Element that belong to the given
     * namespace and have the given local name.
     */
    public static Element[] getChildElements(Element element,
                                             String namespace,
                                             String localName) {
        ArrayList elements = new ArrayList();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element
                    && namespace.equals(node.getNamespaceURI())
                    && localName.equals(node.getLocalName())) {
                elements.add(node);
            }
        }
        return (Element[]) elements.toArray(new Element[elements.size()]);
    }

    /**
     * Returns the first child element with the given namespace and localName,
     * or null if there is no such element.
     */
    public static Element getChildElement(Element element,
                                          String namespace,
                                          String localName) {
        Element node;
        try {
            node = getChildElement(element, namespace, localName, false);
        } catch (Exception e) {
            node = null;
        }
        return node;
    }

    /**
     * Returns the first child element with the given namespace and localName,
     * or null if there is no such element and required flag is unset or
     * throws an Exception if the "required" flag is set.
     */
    public static Element getChildElement(Element element,
                                          String namespace,
                                          String localName,
                                          boolean required)
    throws FormsException {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element
                    && namespace.equals(node.getNamespaceURI())
                    && localName.equals(node.getLocalName())) {
                return (Element) node;
            }
        }

        if (required) {
            throw new FormsException("Required element '" + localName + "' is missing.",
                                     DomHelper.getLocationObject(element));
        }

        return null;
    }

    /**
     * Returns the value of an element's attribute, but throws an exception
     * if the element has no such attribute.
     */
    public static String getAttribute(Element element, String attributeName)
    throws FormsException {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.length() == 0) {
            throw new FormsException("Required attribute '" + attributeName + "' is missing.",
                                     DomHelper.getLocationObject(element));
        }

        return attrValue;
    }

    /**
     * Returns the value of an element's attribute, or a default value if the
     * element has no such attribute.
     */
    public static String getAttribute(Element element,
                                      String attributeName,
                                      String defaultValue) {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.length() == 0) {
            return defaultValue;
        }
        return attrValue;
    }

    public static int getAttributeAsInteger(Element element,
                                            String attributeName)
    throws FormsException {
        String attrValue = getAttribute(element, attributeName);
        try {
            return Integer.parseInt(attrValue);
        } catch (NumberFormatException e) {
            throw new FormsException("Cannot parse the value '" + attrValue + "' " +
                                     "as an integer in the attribute '" + attributeName + "'," +
                                     DomHelper.getLocationObject(element));
        }
    }

    public static int getAttributeAsInteger(Element element,
                                            String attributeName,
                                            int defaultValue)
    throws FormsException {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.length() == 0) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(attrValue);
        } catch (NumberFormatException e) {
            throw new FormsException("Cannot parse the value '" + attrValue + "' " +
                                     "as an integer in the attribute '" + attributeName + "'," +
                                     DomHelper.getLocationObject(element));
        }
    }

    public static boolean getAttributeAsBoolean(Element element,
                                                String attributeName,
                                                boolean defaultValue) {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.length() == 0) {
            return defaultValue;
        }

        Boolean result;
        try {
            result = BooleanUtils.toBooleanObject(attrValue, "true", "false", null);
        } catch (IllegalArgumentException e1) {
            try {
                result = BooleanUtils.toBooleanObject(attrValue, "yes", "no", null);
            } catch (IllegalArgumentException e2) {
                result = null;
            }
        }
        if (result == null) {
            return defaultValue;
        }

        return result.booleanValue();
    }

    public static String getElementText(Element element) {
        StringBuffer value = new StringBuffer();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Text || node instanceof CDATASection) {
                value.append(node.getNodeValue());
            }
        }
        return value.toString();
    }

    /**
     * Returns the content of the given Element as an object implementing the
     * XMLizable interface. Practically speaking, the implementation uses the
     * {@link SaxBuffer} class. The XMLizable object will be a standalone blurb
     * of SAX events, not producing start/endDocument calls and containing all
     * necessary namespace declarations.
     */
    public static XMLizable compileElementContent(Element element) {
        // Remove location information
        LocationAttributes.remove(element, true);

        SaxBuffer saxBuffer = new SaxBuffer();
        DOMStreamer domStreamer = new DOMStreamer();
        domStreamer.setContentHandler(saxBuffer);

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            try {
                domStreamer.stream(childNodes.item(i));
            } catch (SAXException e) {
                // It's unlikely that an exception will occur here,
                // so use a runtime exception
                throw new RuntimeException("Error in DomHelper.compileElementContent: " +
                                           e.toString());
            }
        }
        return saxBuffer;
    }

    /**
     * Creates a W3C Document that remembers the location of each element in
     * the source file. The location of element nodes can then be retrieved
     * using the {@link #getLocation(Element)} method.
     *
     * @param inputSource the inputSource to read the document from
     * @param manager the service manager where to lookup the entity resolver
     */
    public static Document parse(InputSource inputSource, ServiceManager manager)
    throws SAXException, SAXNotSupportedException, IOException, ServiceException {

        SAXParser parser = (SAXParser)manager.lookup(SAXParser.ROLE);
        DOMBuilder builder = new DOMBuilder();

        // Enhance the sax stream with location information
        ContentHandler locationHandler = new LocationAttributes.Pipe(builder);

        try {
            parser.parse(inputSource, locationHandler);
        } finally {
            manager.release(parser);
        }

        return builder.getDocument();
    }

    public static Map getLocalNSDeclarations(Element elm) {
        return addLocalNSDeclarations(elm, null);
    }

    private static Map addLocalNSDeclarations(Element elm, Map nsDeclarations) {
        NamedNodeMap atts = elm.getAttributes();
        int attsSize = atts.getLength();

        for (int i = 0; i < attsSize; i++) {
            Attr attr = (Attr) atts.item(i);
            if (XMLNS_URI.equals(attr.getNamespaceURI())) {
                String nsUri = attr.getValue();
                String pfx = attr.getLocalName();
                if (nsDeclarations == null)
                    nsDeclarations = new HashMap();
                nsDeclarations.put(nsUri, pfx);
            }
        }
        return nsDeclarations;
    }

    public static Map getInheritedNSDeclarations(Element elm) {
        List ancestorsAndSelf = new LinkedList();
        Element current = elm;
        while (current != null) {
            ancestorsAndSelf.add(current);
            Node parent = current.getParentNode();
            if (parent.getNodeType() == Node.ELEMENT_NODE)
                current = (Element) parent;
            else
                current = null;
        }

        Map nsDeclarations = null;
        ListIterator i = ancestorsAndSelf.listIterator(ancestorsAndSelf.size());
        while (i.hasPrevious()) {
            Element element = (Element) i.previous();
            nsDeclarations = addLocalNSDeclarations(element, nsDeclarations);
        }

        return nsDeclarations;
    }
}
