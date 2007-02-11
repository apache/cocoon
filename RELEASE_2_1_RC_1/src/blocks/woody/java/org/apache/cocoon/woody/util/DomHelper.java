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

import org.apache.xerces.parsers.DOMParser;
import org.apache.xerces.xni.*;
import org.apache.xerces.dom.NodeImpl;
import org.apache.cocoon.components.sax.XMLByteStreamCompiler;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.w3c.dom.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Helper class to create and retrieve information from DOM-trees. It provides
 * some functionality comparable to what's found in Avalon's Configuration objects. These
 * lasts one could however not be used by Woody because they don't provide an
 * accurate model of an XML file (no mixed content, no namespaced attributes, no namespace declarations, ...).
 *
 * <p>This class depends specifically on the Xerces DOM implementation to be able to provide
 * information about the location of elements in their source XML file. See the {@link #getLocation} method.
 */
public class DomHelper {

    /**
     * Retrieves the location of an element node in the source file from which the Document was created.
     * This will only work for Document's created with the method {@link #parse} of this class.
     */
    public static String getLocation(Element element) {
        String location = null;
        if (element instanceof NodeImpl)
            location = (String)((NodeImpl)element).getUserData("location");

        if (location != null)
            return location;
        else
            return "(location unknown)";
    }

    /**
     * Returns all Element children of an Element that belong to the given namespace.
     */
    public static Element[] getChildElements(Element element, String namespace) {
        ArrayList elements = new ArrayList();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element && namespace.equals(node.getNamespaceURI()))
                elements.add(node);
        }
        return (Element[])elements.toArray(new Element[0]);
    }

    /**
     * Returns all Element children of an Element that belong to the given namespace and have the given local name.
     */
    public static Element[] getChildElements(Element element, String namespace, String localName) {
        ArrayList elements = new ArrayList();
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element && namespace.equals(node.getNamespaceURI()) && localName.equals(node.getLocalName()))
                elements.add(node);
        }
        return (Element[])elements.toArray(new Element[0]);
    }

    /**
     * Returns the first child element with the given namespace and localName, or null
     * if there is no such element.
     */
    public static Element getChildElement(Element element, String namespace, String localName) {
        // note: instead of calling getChildElement(element, namespace, localName, false),
        // the code is duplicated here because this method does not throw an exception
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element && namespace.equals(node.getNamespaceURI()) && localName.equals(node.getLocalName()))
                return (Element)node;
        }
        return null;
    }

    public static Element getChildElement(Element element, String namespace, String localName, boolean required) throws Exception {
        NodeList nodeList = element.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node instanceof Element && namespace.equals(node.getNamespaceURI()) && localName.equals(node.getLocalName()))
                return (Element)node;
        }
        if (required)
            throw new Exception("Missing element \"" + localName + "\" as child of element \"" + element.getTagName() + "\" at " + DomHelper.getLocation(element));
        else
            return null;
    }

    /**
     * Returns the value of an element's attribute, but throws an exception
     * if the element has no such attribute.
     */
    public static String getAttribute(Element element, String attributeName) throws Exception {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.equals(""))
            throw new Exception("Missing attribute \"" + attributeName + "\" on element \"" + element.getTagName() + "\" at " + getLocation(element));
        return attrValue;
    }

    public static int getAttributeAsInteger(Element element, String attributeName) throws Exception {
        String attrValue = getAttribute(element, attributeName);
        try {
            return Integer.parseInt(attrValue);
        } catch (NumberFormatException e) {
            throw new Exception("Cannot parse the value \"" + attrValue + "\" as an integer in the attribute \"" + attributeName + "\" on the element \"" + element.getTagName() + "\" at " + getLocation(element));
        }
    }

    public static boolean getAttributeAsBoolean(Element element, String attributeName, boolean defaultValue) throws Exception {
        String attrValue = element.getAttribute(attributeName);
        if (attrValue.equals(""))
            return defaultValue;
        else if (attrValue.equalsIgnoreCase("true") || attrValue.equalsIgnoreCase("yes"))
            return true;
        else if (attrValue.equalsIgnoreCase("false") || attrValue.equalsIgnoreCase("no"))
            return false;
        else
            throw new Exception("Cannot parse the value \"" + attrValue + "\" as a boolean in the attribute \"" + attributeName + "\" on the element \"" + element.getTagName() + "\" at " + getLocation(element));
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
     * Uses Cocoon's XMLByteStreamCompiler to convert the content of the given element to compiled
     * SAX events.
     */
    public static Object compileElementContent(Element element) {
        XMLByteStreamCompiler byteStreamCompiler = new XMLByteStreamCompiler();
        DOMStreamer domStreamer = new DOMStreamer();
        domStreamer.setContentHandler(byteStreamCompiler);

        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            try {
                domStreamer.stream(childNodes.item(i));
            } catch (SAXException e) {
                // It's unlikely that an exception will occur here, so use a runtime exception
                throw new RuntimeException("Error in DomHelper.compileElementContent: " + e.toString());
            }
        }
        return byteStreamCompiler.getSAXFragment();
    }

    /**
     * Creates a W3C Document that remembers the location of each element in the source file.
     * The location of element nodes can then be retrieved using the {@link #getLocation} method.
     */
    public static Document parse(InputSource inputSource) throws SAXException, SAXNotSupportedException, IOException {
        DOMParser domParser = new LocationTrackingDOMParser();
        domParser.setFeature("http://apache.org/xml/features/dom/defer-node-expansion", false);
        domParser.setFeature("http://apache.org/xml/features/dom/create-entity-ref-nodes", false);
        domParser.parse(inputSource);
        return domParser.getDocument();
    }

    /**
     * An extension of the Xerces DOM parser that puts the location of each node in that
     * node's UserData.
     */
    public static class LocationTrackingDOMParser extends DOMParser {
        XMLLocator locator;

        public void startDocument(XMLLocator xmlLocator, String s, NamespaceContext namespaceContext, Augmentations augmentations) throws XNIException {
            super.startDocument(xmlLocator, s, namespaceContext, augmentations);
            this.locator = xmlLocator;
            setLocation();
        }

        public void startElement(QName qName, XMLAttributes xmlAttributes, Augmentations augmentations) throws XNIException {
            super.startElement(qName, xmlAttributes, augmentations);
            setLocation();
        }

        private final void setLocation() {
            NodeImpl node = null;
            try {
                node = (NodeImpl) this.getProperty("http://apache.org/xml/properties/dom/current-element-node");
            } catch (org.xml.sax.SAXException ex) {
                System.err.println("except" + ex);
            }
            if (node != null) {
                String location = locator.getLiteralSystemId() + ": line " + locator.getLineNumber();
                node.setUserData("location", location, null);
            }
        }
    }
}
