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
package org.apache.cocoon.woody.util;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.xerces.dom.NodeImpl;
import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotSupportedException;

/**
 * Helper class to create and retrieve information from DOM-trees. It provides
 * some functionality comparable to what's found in Avalon's Configuration
 * objects. These lasts one could however not be used by Woody because they
 * don't provide an accurate model of an XML file (no mixed content,
 * no namespaced attributes, no namespace declarations, ...).
 *
 * <p>This class depends specifically on the Xerces DOM implementation to be
 * able to provide information about the location of elements in their source
 * XML file. See the {@link #getLocation(Element)} method.
 * 
 * @version CVS $Id: DomHelper.java,v 1.13 2004/02/29 09:21:33 antonio Exp $
 */
public class DomHelper {

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
    public static Element[] getChildElements(Element element,
            String namespace) {
        ArrayList elements = new ArrayList();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element
                    && namespace.equals(node.getNamespaceURI()))
                elements.add(node);
        }
        return (Element[])elements.toArray(new Element[0]);
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
        return (Element[])elements.toArray(new Element[0]);
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
            String attributeName, boolean defaultValue) throws Exception {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.equals("")) {
            return defaultValue;
        } else if (attrValue.equalsIgnoreCase("true")
                || attrValue.equalsIgnoreCase("yes")) {
            return true;
        } else if (attrValue.equalsIgnoreCase("false")
                || attrValue.equalsIgnoreCase("no")) {
            return false;
        } else {
            throw new Exception("Cannot parse the value \"" + attrValue + 
                    "\" as a boolean in the attribute \"" + attributeName + 
                    "\" on the element \"" + element.getTagName() + 
                    "\" at " + getLocation(element));
        }
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
     */
    public static Document parse(InputSource inputSource)
            throws SAXException, SAXNotSupportedException, IOException {
        DOMParser domParser = new LocationTrackingDOMParser();
        domParser.setFeature(
                "http://apache.org/xml/features/dom/defer-node-expansion",
                false);
        domParser.setFeature(
                "http://apache.org/xml/features/dom/create-entity-ref-nodes",
                false);
        domParser.parse(inputSource);
        return domParser.getDocument();
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
                node.setUserData("location", location, null);
            }
        }
    }
}
