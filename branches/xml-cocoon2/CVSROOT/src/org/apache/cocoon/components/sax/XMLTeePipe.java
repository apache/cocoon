/**
 ****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 ****************************************************************************
 */

package org.apache.cocoon.components.sax;

import org.apache.excalibur.pool.Recyclable;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

import org.apache.cocoon.xml.XMLPipe;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;


/**
 * This is a simple Tee Component.
 * The incoming events are forwarded to two other components.
 *
 * @author <a href="mailto:cziegeler@sundn.de">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-20 20:50:02 $
 */

public final class XMLTeePipe
implements XMLPipe {

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     */
    public void setConsumer(XMLConsumer consumer) {
        ((XMLProducer)this.lexicalHandler).setConsumer(consumer);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     */
    public void setContentHandler(ContentHandler content) {
        ((XMLProducer)this.lexicalHandler).setContentHandler(content);
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     */
    public void setLexicalHandler(LexicalHandler lexical) {
        ((XMLProducer)this.lexicalHandler).setLexicalHandler(lexical);
    }

    private ContentHandler secondContentHandler;
    private LexicalHandler secondLexicalHandler;
    private ContentHandler contentHandler;
    private LexicalHandler lexicalHandler;

    /**
     * Create a new XMLTeePipe with two consumers
     */
    public XMLTeePipe(XMLConsumer firstPipe,
                      XMLConsumer secondConsumer) {
        this.contentHandler = firstPipe;
        this.lexicalHandler = firstPipe;
        this.secondContentHandler = secondConsumer;
        this.secondLexicalHandler = secondConsumer;
    }

    public void recycle() {
        this.secondContentHandler = null;
        this.contentHandler = null;
        this.secondLexicalHandler = null;
        this.lexicalHandler = null;
    }

    public void startDocument() throws SAXException {
        this.contentHandler.startDocument();
        this.secondContentHandler.startDocument();
    }

    public void endDocument() throws SAXException {
        this.contentHandler.endDocument();
        this.secondContentHandler.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        this.contentHandler.startPrefixMapping(prefix, uri);
        this.secondContentHandler.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        this.contentHandler.endPrefixMapping(prefix);
        this.secondContentHandler.endPrefixMapping(prefix);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
    throws SAXException {
        this.contentHandler.startElement(namespaceURI, localName, qName, atts);
        this.secondContentHandler.startElement(namespaceURI, localName, qName, atts);
    }

    public void endElement(String namespaceURI, String localName, String qName)
    throws SAXException {
        this.contentHandler.endElement(namespaceURI, localName, qName);
        this.secondContentHandler.endElement(namespaceURI, localName, qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        this.contentHandler.characters(ch, start, length);
        this.secondContentHandler.characters(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        this.contentHandler.ignorableWhitespace(ch, start, length);
        this.secondContentHandler.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        this.contentHandler.processingInstruction(target, data);
        this.secondContentHandler.processingInstruction(target, data);
    }

    public void setDocumentLocator(Locator locator) {
        this.contentHandler.setDocumentLocator(locator);
        this.secondContentHandler.setDocumentLocator(locator);
    }

    public void skippedEntity(String name) throws SAXException {
        this.contentHandler.skippedEntity(name);
        this.secondContentHandler.skippedEntity(name);
    }

    public void startDTD(String name, String public_id, String system_id)
    throws SAXException {
        this.lexicalHandler.startDTD(name, public_id, system_id);
        this.secondLexicalHandler.startDTD(name, public_id, system_id);
    }

    public void endDTD() throws SAXException {
        this.lexicalHandler.endDTD();
        this.secondLexicalHandler.endDTD();
    }

    public void startEntity(String name) throws SAXException {
        this.lexicalHandler.startEntity(name);
        this.secondLexicalHandler.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        this.lexicalHandler.endEntity(name);
        this.secondLexicalHandler.endEntity(name);
    }

    public void startCDATA() throws SAXException {
        this.lexicalHandler.startCDATA();
        this.secondLexicalHandler.startCDATA();
    }

    public void endCDATA() throws SAXException {
        this.lexicalHandler.endCDATA();
        this.secondLexicalHandler.endCDATA();
    }

    public void comment(char ary[], int start, int length)
    throws SAXException {
        this.lexicalHandler.comment(ary, start, length);
        this.secondLexicalHandler.comment(ary, start, length);
    }

}