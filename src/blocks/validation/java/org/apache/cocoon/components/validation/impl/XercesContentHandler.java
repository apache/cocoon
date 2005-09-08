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
package org.apache.cocoon.components.validation.impl;

import org.apache.xerces.util.NamespaceSupport;
import org.apache.xerces.util.SAXLocatorWrapper;
import org.apache.xerces.util.XMLAttributesImpl;
import org.apache.xerces.util.XMLSymbols;
import org.apache.xerces.xni.NamespaceContext;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * <p>An implementation of the {@link ContentHandler} interface wrapping around
 * a Xerces {@link XMLDocumentHandler} instance.</p>
 * 
 * <p>Most of this code has been derived from the Xerces JAXP Validation interface
 * available in the <code>org.xml.xerces.jaxp.validation</code> package.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesContentHandler implements ContentHandler {
    
    private final XMLDocumentHandler validationHandler;
    private final NamespaceContext namespaceContext = new NamespaceSupport();
    private final SAXLocatorWrapper locator = new SAXLocatorWrapper();

    /**
     * <p>Create a new {@link XercesContentHandler} instance.</p>
     */
    public XercesContentHandler(XMLDocumentHandler validationHandler) {
        this.validationHandler = validationHandler;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator.setLocator(locator);
    }

    public void startDocument()
    throws SAXException {
        try {
            this.validationHandler.startDocument(this.locator,
                                                 this.locator.getEncoding(),
                                                 this.namespaceContext,
                                                 null);
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        }
    }

    public void endDocument()
    throws SAXException {
        try {
            this.validationHandler.endDocument(null);
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        }
    }

    public void startPrefixMapping(String pfx, String uri)
    throws SAXException {
        try {
            this.namespaceContext.declarePrefix(pfx, uri);
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        }
    }

    public void endPrefixMapping(String arg0)
    throws SAXException {
        // Do nothing! Handled by popContext in endElement!
    }

    public void startElement(String namespace, String local, String qualified,
                             Attributes attributes)
    throws SAXException {
        try {
            QName qname = this.qname(namespace, local, qualified);
            XMLAttributes xmlatts = new XMLAttributesImpl(attributes.getLength());
            for (int x = 0; x < attributes.getLength(); x ++) {
                final String aNamespace = attributes.getURI(x);
                final String aLocalName = attributes.getLocalName(x);
                final String aQualified = attributes.getQName(x);
                final String aType = attributes.getType(x);
                final String aValue = attributes.getValue(x);
                QName aQname = this.qname(aNamespace, aLocalName, aQualified);
                xmlatts.addAttribute(aQname, aType, aValue);
            }
            this.namespaceContext.pushContext();
            this.validationHandler.startElement(qname, xmlatts, null);
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        }
    }

    public void endElement(String namespace, String local, String qualified)
    throws SAXException {
        try {
            QName qname = this.qname(namespace, local, qualified);
            this.validationHandler.endElement(qname, null);
            this.namespaceContext.popContext();
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        }
    }

    public void characters(char buffer[], int offset, int length)
    throws SAXException {
        try {
            XMLString data = new XMLString(buffer, offset, length);
            this.validationHandler.characters(data, null);
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        }
    }

    public void ignorableWhitespace(char buffer[], int offset, int length)
    throws SAXException {
        try {
            XMLString data = new XMLString(buffer, offset, length);
            this.validationHandler.ignorableWhitespace(data, null);
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        }
    }

    public void processingInstruction(String target, String extra)
    throws SAXException {
        try {
            XMLString data = new XMLString(extra.toCharArray(), 0, extra.length());
            this.validationHandler.processingInstruction(target, data, null);
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        }
    }

    public void skippedEntity(String arg0)
    throws SAXException {
        // Do nothing for skipped entities!
    }

    private QName qname(String namespace, String local, String qualified) {
        String prefix = XMLSymbols.EMPTY_STRING;
        int index = qualified.indexOf(':');
        if (index != -1) prefix = qualified.substring(0, index);
        return new  QName(prefix, local, qualified, namespace);
    }
}
