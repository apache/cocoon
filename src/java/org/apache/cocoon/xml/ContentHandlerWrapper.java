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
package org.apache.cocoon.xml;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * This class is an utility class &quot;wrapping&quot; around a SAX version 2.0
 * <code>ContentHandler</code> and forwarding it those events received throug
 * its <code>XMLConsumers</code> interface.
 * <br>
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 *         (Apache Software Foundation, Computer Associates)
 * @version CVS $Id: ContentHandlerWrapper.java,v 1.4 2004/03/08 14:04:00 cziegeler Exp $
 */
public class ContentHandlerWrapper extends AbstractXMLConsumer implements Recyclable {

    /** The current <code>ContentHandler</code>. */
    protected ContentHandler contentHandler;

    /** The optional <code>LexicalHandler</code> */
    protected LexicalHandler lexicalHandler;

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     */
    public ContentHandlerWrapper() {
        super();
     }

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     */
    public ContentHandlerWrapper(ContentHandler contentHandler) {
        this();
        this.setContentHandler(contentHandler);
    }

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     */
    public ContentHandlerWrapper(ContentHandler contentHandler,
                                 LexicalHandler lexicalHandler) {
        this();
        this.setContentHandler(contentHandler);
        this.setLexicalHandler(lexicalHandler);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     *
     * @exception IllegalStateException If the <code>ContentHandler</code>
     *                                  was already set.
     */
    public void setContentHandler(ContentHandler contentHandler)
    throws IllegalStateException {
        if (this.contentHandler!=null) throw new IllegalStateException();
        this.contentHandler=contentHandler;
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     *
     * @exception IllegalStateException If the <code>LexicalHandler</code>
     *                                  was already set.
     */
    public void setLexicalHandler(LexicalHandler lexicalHandler)
    throws IllegalStateException {
        if (this.lexicalHandler!=null) throw new IllegalStateException();
        this.lexicalHandler=lexicalHandler;
    }

    public void recycle () {
        this.contentHandler = null;
        this.lexicalHandler = null;
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator (Locator locator) {
        if (this.contentHandler==null) return;
        else this.contentHandler.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument ()
    throws SAXException {
        if (this.contentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.contentHandler.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument ()
    throws SAXException {
        this.contentHandler.endDocument();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (this.contentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.contentHandler.startPrefixMapping(prefix, uri);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        this.contentHandler.endPrefixMapping(prefix);
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        this.contentHandler.startElement(uri, loc, raw, a);
    }


    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        this.contentHandler.endElement(uri, loc, raw);
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        this.contentHandler.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        this.contentHandler.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        this.contentHandler.processingInstruction(target,data);
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name)
    throws SAXException {
        this.contentHandler.skippedEntity(name);
    }

        /**
     * Report the start of DTD declarations, if any.
     *
     * @param name The document type name.
     * @param publicId The declared public identifier for the external DTD
     *                 subset, or null if none was declared.
     * @param systemId The declared system identifier for the external DTD
     *                 subset, or null if none was declared.
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        if (this.lexicalHandler != null)
            this.lexicalHandler.startDTD(name, publicId, systemId);
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
        if (this.lexicalHandler != null)
            this.lexicalHandler.endDTD();
    }

    /**
     * Report the beginning of an entity.
     *
     * @param name The name of the entity. If it is a parameter entity, the
     *             name will begin with '%'.
     */
    public void startEntity(String name)
    throws SAXException {
        if (this.lexicalHandler != null)
            this.lexicalHandler.startEntity(name);
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     */
    public void endEntity(String name)
    throws SAXException {
        if (this.lexicalHandler != null)
            this.lexicalHandler.endEntity(name);
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        if (this.lexicalHandler != null)
            this.lexicalHandler.startCDATA();
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        if (this.lexicalHandler != null)
            this.lexicalHandler.endCDATA();
    }


    /**
     * Report an XML comment anywhere in the document.
     *
     * @param ch An array holding the characters in the comment.
     * @param start The starting position in the array.
     * @param len The number of characters to use from the array.
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        if (this.lexicalHandler != null)
            this.lexicalHandler.comment(ch, start, len);
    }

}
