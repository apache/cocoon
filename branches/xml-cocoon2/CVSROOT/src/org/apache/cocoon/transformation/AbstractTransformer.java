/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.transformation;

import org.apache.cocoon.xml.AbstractXMLProducer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation, Exoffice Technologies)
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-07-11 23:46:56 $
 */
public abstract class AbstractTransformer extends AbstractXMLProducer
implements Transformer {

    /** Wether we are forwarding XML data or not. */
    private boolean canReset=true;

    /**
     * Receive an object for locating the origin of SAX document events.
     */
    public void setDocumentLocator(Locator locator) {
        if (super.contentHandler!=null)
            super.contentHandler.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.startDocument();
        this.canReset=false;
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.endDocument();
        this.canReset=true;
    }

    /**
     * Begin the scope of a prefix-URI Namespace mapping.
     */
    public void startPrefixMapping(String prefix, String uri)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.startPrefixMapping(prefix,uri);
    }

    /**
     * End the scope of a prefix-URI mapping.
     */
    public void endPrefixMapping(String prefix)
    throws SAXException {
    }

    /**
     * Receive notification of the beginning of an element.
     */
    public void startElement(String uri, String loc, String raw, Attributes a)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.startElement(uri,loc,raw,a);
    }


    /**
     * Receive notification of the end of an element.
     */
    public void endElement(String uri, String loc, String raw)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.endElement(uri,loc,raw);
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive notification of a processing instruction.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.processingInstruction(target,data);
    }

    /**
     * Receive notification of a skipped entity.
     */
    public void skippedEntity(String name)
    throws SAXException {
        if (super.contentHandler!=null)
            super.contentHandler.skippedEntity(name);
    }

    /**
     * Report the start of DTD declarations, if any.
     */
    public void startDTD(String name, String publicId, String systemId)
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startDTD(name,publicId,systemId);
    }

    /**
     * Report the end of DTD declarations.
     */
    public void endDTD()
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endDTD();
    }

    /**
     * Report the beginning of an entity.
     */
    public void startEntity(String name)
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startEntity(name);
    }

    /**
     * Report the end of an entity.
     */
    public void endEntity(String name)
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endEntity(name);
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA()
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.startCDATA();
    }

    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA()
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.endCDATA();
    }


    /**
     * Report an XML comment anywhere in the document.
     */
    public void comment(char ch[], int start, int len)
    throws SAXException {
        if (super.lexicalHandler!=null)
            super.lexicalHandler.comment(ch,start,len);
    }
}
