/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/

package org.apache.cocoon.xml;

import java.util.Vector;

import org.apache.cocoon.xml.AbstractXMLConsumer;
import org.apache.excalibur.pool.Recyclable;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributeListImpl;

/**
 * This class is an utility class &quot;wrapping&quot; around a SAX version 2.0
 * <code>ContentHandler</code> and forwarding it those events received throug
 * its <code>XMLConsumers</code> interface.
 * <br>
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 *         (Apache Software Foundation, Computer Associates)
 * @version CVS $Revision: 1.1.2.8 $ $Date: 2001-04-20 20:50:19 $
 */
public class ContentHandlerWrapper extends AbstractXMLConsumer implements Recyclable {

    /** The current <code>ContentHandler</code>. */
    protected ContentHandler documentHandler;

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     */
    public ContentHandlerWrapper() {
        super();
     }

    /**
     * Create a new <code>ContentHandlerWrapper</code> instance.
     */
    public ContentHandlerWrapper(ContentHandler document) {
        this();
        this.setContentHandler(document);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     *
     * @exception IllegalStateException If the <code>ContentHandler</code>
     *                                  was already set.
     */
    public void setContentHandler(ContentHandler document)
    throws IllegalStateException {
        if (this.documentHandler!=null) throw new IllegalStateException();
        this.documentHandler=document;
    }

    public void recycle () {
        this.documentHandler = null;
    }

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator (Locator locator) {
        if (this.documentHandler==null) return;
        else this.documentHandler.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument ()
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument ()
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.endDocument();
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.startPrefixMapping(prefix, uri);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.endPrefixMapping(prefix);
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.startElement(uri, loc, raw, a);
    }


    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.endElement(uri, loc, raw);
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.processingInstruction(target,data);
    }

    /**
     * Receive notification of a skipped entity.
     *
     * @param name The name of the skipped entity.  If it is a  parameter
     *             entity, the name will begin with '%'.
     */
    public void skippedEntity(String name)
    throws SAXException {
        if (this.documentHandler==null)
            throw new SAXException("ContentHandler not set");
        this.documentHandler.skippedEntity(name);
    }
}
