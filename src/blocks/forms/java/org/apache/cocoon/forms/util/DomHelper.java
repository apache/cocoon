/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.forms.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.commons.lang.BooleanUtils;
import org.apache.excalibur.xml.EntityResolver;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.w3c.dom.UserDataHandler;
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

    public static final String XMLNS_URI = "http://www.w3.org/2000/xmlns/";

    /**
     * Retrieves the location of an element node in the source file from which
     * the Document was created. This will only work for Document's created
     * with the method {@link #parse(InputSource)} of this class.
     */
    public static String getLocation(Element element) {
        String location = null;
        if (element instanceof NodeImpl) {
            location = (String)((NodeImpl)element).getUserData("location");
        }

        if (location != null) {
            return location;
        }
        return "(location unknown)";
    }
    
    public static String getSystemIdLocation(Element element) {
        String loc = getLocation(element);
        if (loc.charAt(0) != '(') {
            int end = loc.lastIndexOf(':');
            if (end > 0) {
                int start = loc.lastIndexOf(':', end - 1);
                if (start >= 0) {
                    return loc.substring(0, start);
                }
            }
        }
        return null;
    }
    
    public static int getLineLocation(Element element) {
        String loc = getLocation(element);
        if (loc.charAt(0) != '(') {
            int end = loc.lastIndexOf(':');
            if (end > 0) {
                int start = loc.lastIndexOf(':', end - 1);
                if (start >= 0) {
                    return Integer.parseInt(loc.substring(start + 1, end));
                }
            }
        }
        return -1;
    }

    public static int getColumnLocation(Element element) {
        String loc = getLocation(element);
        if (loc.charAt(0) != '(') {
            int end = loc.lastIndexOf(':');
            if (end > 0) {
                return Integer.parseInt(loc.substring(end));
            }
        }
        return -1;
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
        return (Element[])elements.toArray(new Element[elements.size()]);
    }

    /**
     * Returns all Element children of an Element that belong to the given
     * namespace and have the given local name.
     */
    public static Element[] getChildElements(Element element,
            String namespace, String localName) {
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
        return (Element[])elements.toArray(new Element[elements.size()]);
    }

    /**
     * Returns the first child element with the given namespace and localName,
     * or null if there is no such element.
     */
    public static Element getChildElement(Element element, String namespace, 
            String localName) {
        Element node = null;
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
    public static Element getChildElement(Element element, String namespace, 
            String localName, boolean required) throws Exception {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element
                    && namespace.equals(node.getNamespaceURI()) 
                    && localName.equals(node.getLocalName())) {
                return (Element)node;
            }
        }
        if (required) {
            throw new Exception("Missing element \"" + localName +
                    "\" as child of element \"" + element.getTagName() + 
                    "\" at " + DomHelper.getLocation(element));
        } else {
            return null;
        }
    }

    /**
     * Returns the value of an element's attribute, but throws an exception
     * if the element has no such attribute.
     */
    public static String getAttribute(Element element, String attributeName)
            throws Exception {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.equals("")) {
            throw new Exception("Missing attribute \"" + attributeName + 
                    "\" on element \"" + element.getTagName() + 
                    "\" at " + getLocation(element));
        }
        return attrValue;
    }

    /**
     * Returns the value of an element's attribute, or a default value if the 
     * element has no such attribute.
     */
    public static String getAttribute(Element element, String attributeName, 
            String defaultValue) throws Exception {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.equals("")) {
            return defaultValue;
        }
        return attrValue;
    }

    public static int getAttributeAsInteger(Element element, 
            String attributeName) throws Exception {
        String attrValue = getAttribute(element, attributeName);
        try {
            return Integer.parseInt(attrValue);
        } catch (NumberFormatException e) {
            throw new Exception("Cannot parse the value \"" + attrValue + 
                    "\" as an integer in the attribute \"" + attributeName + 
                    "\" on the element \"" + element.getTagName() + 
                    "\" at " + getLocation(element));
        }
    }

    public static int getAttributeAsInteger(Element element, 
            String attributeName, int defaultValue) throws Exception {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.equals("")) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(attrValue);
            } catch (NumberFormatException e) {
                throw new Exception("Cannot parse the value \"" + attrValue + 
                        "\" as an integer in the attribute \"" + 
                        attributeName + "\" on the element \"" +
                        element.getTagName() + "\" at " +
                        getLocation(element));
            }
        }
    }

    public static boolean getAttributeAsBoolean(Element element, 
                String attributeName, boolean defaultValue) {
        String attrValue = element.getAttribute(attributeName);
        Boolean result;
        try {
            result = BooleanUtils.toBooleanObject(attrValue, "true", "false", null);
        } catch (IllegalArgumentException iae) {
            result = null;
        }
        if (result != null) {
            return result.booleanValue();
        }
        try {
            result = BooleanUtils.toBooleanObject(attrValue, "yes", "no", null);
        } catch (IllegalArgumentException iae) {
            result = null;
        }
        if (result != null) {
            return result.booleanValue();
        }
        return defaultValue;    
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
                throw new RuntimeException(
                        "Error in DomHelper.compileElementContent: " + 
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
        DOMParser domParser = new LocationTrackingDOMParser();
        domParser.setFeature(
                "http://apache.org/xml/features/dom/defer-node-expansion",
                false);
        domParser.setFeature(
                "http://apache.org/xml/features/dom/create-entity-ref-nodes",
                false);
        
        EntityResolver resolver = null;
        if (manager.hasService(EntityResolver.ROLE)) {
            resolver = (EntityResolver)manager.lookup(EntityResolver.ROLE);
            domParser.setEntityResolver(resolver);
        }
        
        try {
            domParser.parse(inputSource);
            return domParser.getDocument();
        } finally {
            manager.release(resolver);
        }
    }

    /**
     * An extension of the Xerces DOM parser that puts the location of each
     * node in that node's UserData.
     */
    public static class LocationTrackingDOMParser extends DOMParser {
        XMLLocator locator;

        public void startDocument(XMLLocator xmlLocator, String s,
                NamespaceContext namespaceContext,
                Augmentations augmentations) throws XNIException {
            super.startDocument(xmlLocator, s, namespaceContext,
                    augmentations);
            this.locator = xmlLocator;
            setLocation();
        }

        public void startElement(QName qName, XMLAttributes xmlAttributes,
                Augmentations augmentations) throws XNIException {
            super.startElement(qName, xmlAttributes, augmentations);
            setLocation();
        }

        private final void setLocation() {
            // Older versions of Xerces had a different signature for the
            // startDocument method. If such a version is used, the
            // startDocument method above will not be called and locator will
            // hence be null.
            // Tell the users this so that they don't get a stupid NPE.
            if (this.locator == null) {
                throw new RuntimeException(
                        "Error: locator is null. Check that you have the" +
                        " correct version of Xerces (such as the one that" +
                        " comes with Cocoon) in your endorsed library path.");
            }
            NodeImpl node = null;
            try {
                node = (NodeImpl)this.getProperty(
                        "http://apache.org/xml/properties/dom/current-element-node");
            } catch (org.xml.sax.SAXException ex) {
                System.err.println("except" + ex);
            }
            if (node != null) {
                String location = locator.getLiteralSystemId() + ":" +
                    locator.getLineNumber() + ":" + locator.getColumnNumber();
                node.setUserData("location", location, (UserDataHandler)null);
            }
        }
    }

    public static Map getLocalNSDeclarations(Element elm)
    {
        return addLocalNSDeclarations(elm, null);
    }
    
    private static Map addLocalNSDeclarations(Element elm, Map nsDeclarations)
    {
        NamedNodeMap atts = elm.getAttributes();
        int attsSize = atts.getLength();

        for (int i = 0; i < attsSize; i++)
        {
            Attr attr = (Attr)atts.item(i);
            if (XMLNS_URI.equals(attr.getNamespaceURI()))
            {
                String nsUri = attr.getValue();
                String pfx = attr.getLocalName();
                if (nsDeclarations == null)
                    nsDeclarations = new HashMap();
                nsDeclarations.put(nsUri, pfx);
            }
        }        
        return nsDeclarations;    }
    
    public static Map getInheritedNSDeclarations(Element elm)
    {
        List ancestorsAndSelf = new LinkedList();
        Element current = elm;
        while (current != null) 
        {
            ancestorsAndSelf.add(current);
            Node parent = current.getParentNode();
            if (parent.getNodeType() == Node.ELEMENT_NODE)
                current = (Element)parent;
            else 
                current = null;
        }
        
        Map nsDeclarations = null;
        ListIterator iter = ancestorsAndSelf.listIterator(ancestorsAndSelf.size());
        while (iter.hasPrevious())
        {
            Element element = (Element) iter.previous();
            nsDeclarations = addLocalNSDeclarations(element, nsDeclarations);
        }
        
        return nsDeclarations;
    }
}
