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
package org.apache.cocoon.taglib;

import org.apache.cocoon.xml.XMLConsumer;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:volker.schmitt@basf-it-services.com">Volker Schmitt</a>
 * @version CVS $Id: TransformerTagSupport.java,v 1.3 2004/03/05 13:02:24 bdelacretaz Exp $
 */
public class TransformerTagSupport extends TagSupport implements TransformerTag {
    protected XMLConsumer xmlConsumer;

    /*
     * @see ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        xmlConsumer.setDocumentLocator(locator);
    }

    /*
     * @see ContentHandler#startDocument()
     */
    public void startDocument() throws SAXException {
    }

    /*
     * @see ContentHandler#endDocument()
     */
    public void endDocument() throws SAXException {
    }

    /*
     * @see ContentHandler#startPrefixMapping(String, String)
     */
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        xmlConsumer.startPrefixMapping(prefix, uri);
    }

    /*
     * @see ContentHandler#endPrefixMapping(String)
     */
    public void endPrefixMapping(String prefix) throws SAXException {
        xmlConsumer.endPrefixMapping(prefix);
    }

    /*
     * @see ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        xmlConsumer.startElement(namespaceURI, localName, qName, atts);
    }

    /*
     * @see ContentHandler#endElement(String, String, String)
     */
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        xmlConsumer.endElement(namespaceURI, localName, qName);
    }

    /*
     * @see ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) throws SAXException {
        xmlConsumer.characters(ch, start, length);
    }

    /*
     * @see ContentHandler#ignorableWhitespace(char[], int, int)
     */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        xmlConsumer.ignorableWhitespace(ch, start, length);
    }

    /*
     * @see ContentHandler#processingInstruction(String, String)
     */
    public void processingInstruction(String target, String data) throws SAXException {
        xmlConsumer.processingInstruction(target, data);
    }

    /*
     * @see ContentHandler#skippedEntity(String)
     */
    public void skippedEntity(String name) throws SAXException {
        xmlConsumer.skippedEntity(name);
    }

    /*
     * @see LexicalHandler#startDTD(String, String, String)
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        xmlConsumer.startDTD(name, publicId, systemId);
    }

    /*
     * @see LexicalHandler#endDTD()
     */
    public void endDTD() throws SAXException {
        xmlConsumer.endDTD();
    }

    /*
     * @see LexicalHandler#startEntity(String)
     */
    public void startEntity(String name) throws SAXException {
        xmlConsumer.startEntity(name);
    }

    /*
     * @see LexicalHandler#endEntity(String)
     */
    public void endEntity(String name) throws SAXException {
        xmlConsumer.endEntity(name);
    }

    /*
     * @see LexicalHandler#startCDATA()
     */
    public void startCDATA() throws SAXException {
        xmlConsumer.startCDATA();
    }

    /*
     * @see LexicalHandler#endCDATA()
     */
    public void endCDATA() throws SAXException {
        xmlConsumer.endCDATA();
    }

    /*
     * @see LexicalHandler#comment(char[], int, int)
     */
    public void comment(char[] ch, int start, int length) throws SAXException {
        xmlConsumer.comment(ch, start, length);
    }

    /*
     * @see XMLProducer#setConsumer(XMLConsumer)
     */
    public void setConsumer(XMLConsumer consumer) {
        this.xmlConsumer = consumer;
    }

    /*
     * @see Recyclable#recycle()
     */
    public void recycle() {
        this.xmlConsumer = null;
        super.recycle();
    }

}
