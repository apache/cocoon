/*****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.xml;

import org.apache.avalon.logger.AbstractLoggable;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

/**
 * Extracted class from XIncludeTransformer for use in XIncludeSAXConnector.
 *
 * @author <a href="mailto:dims@yahoo.com">Davanum Srinivas</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-04-25 17:09:39 $ $Author: donaldp $
 */
public class XIncludeContentHandler extends AbstractLoggable implements ContentHandler, LexicalHandler {

    private ContentHandler content_handler;
    LexicalHandler lexical_handler;

    public XIncludeContentHandler(ContentHandler content_handler, LexicalHandler lexical_handler) {
        this.content_handler = content_handler;
        this.lexical_handler = lexical_handler;
    }

    public void setDocumentLocator(Locator locator) {
        content_handler.setDocumentLocator(locator);
    }

    public void startDocument() {
        super.getLogger().debug("Internal start document received");
        /** We don't pass start document on to the "real" handler **/
    }

    public void endDocument() {
        super.getLogger().debug("Internal end document received");
        /** We don't pass end document on to the "real" handler **/
    }

    public void startPrefixMapping(String prefix, String uri)
        throws SAXException {
        content_handler.startPrefixMapping(prefix,uri);
    }

    public void endPrefixMapping(String prefix)
        throws SAXException {
        content_handler.endPrefixMapping(prefix);
    }

    public void startElement(String namespace, String name, String raw,
        Attributes attr) throws SAXException {
        super.getLogger().debug("Internal element received: "+name);
        content_handler.startElement(namespace,name,raw,attr);
    }

    public void endElement(String namespace, String name, String raw)
        throws SAXException {
        content_handler.endElement(namespace,name,raw);
    }

    public void characters(char ary[], int start, int length)
        throws SAXException {
        content_handler.characters(ary,start,length);
    }

    public void ignorableWhitespace(char ary[], int start, int length)
        throws SAXException {
        content_handler.ignorableWhitespace(ary,start,length);
    }

    public void processingInstruction(String target, String data)
        throws SAXException {
        content_handler.processingInstruction(target,data);
    }

    public void skippedEntity(String name)
        throws SAXException {
        content_handler.skippedEntity(name);
    }

    public void startDTD(String name, String public_id, String system_id)
        throws SAXException {
        lexical_handler.startDTD(name,public_id,system_id);
    }

    public void endDTD() throws SAXException {
        lexical_handler.endDTD();
    }

    public void startEntity(String name) throws SAXException {
        lexical_handler.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        lexical_handler.endEntity(name);
    }

    public void startCDATA() throws SAXException {
        lexical_handler.startCDATA();
    }

    public void endCDATA() throws SAXException {
        lexical_handler.endCDATA();
    }

    public void comment(char ary[], int start, int length)
        throws SAXException {
        lexical_handler.comment(ary,start,length);
    }
}
