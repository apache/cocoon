/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon;

import org.xml.sax.AttributeList;
import org.xml.sax.DocumentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * The <code>XMLConsumerImpl</code> is the default implementation of an
 * <code>XMLConsumer</code>.
 * <br>
 * NOTE: (PF) This class need to be revised in the light of the new SAX version
 * 2.0 specification.
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-09 01:53:20 $
 * @since Cocoon 2.0
 */
public class XMLConsumerImpl implements XMLConsumer {
    DocumentHandler handler=null;

    /**
     * Create a new instance of this <code>XMLConsumerImpl</code>.
     */
    public XMLConsumerImpl(DocumentHandler h) {
        super();
        this.handler=h;
    }

    /**
     * Receive notification of a processing instruction. 
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        if (this.handler!=null)
            this.handler.processingInstruction(target,data);
    }

    /**
     * Receive notification of the beginning of an element. 
     */
    public void startElement(String name, AttributeList atts)
    throws SAXException {
        if (this.handler!=null)
            this.handler.startElement(name,atts);
    }

    /**
     * Receive notification of the end of an element. 
     */
    public void endElement(String name)
    throws SAXException {
        if (this.handler!=null)
            this.handler.endElement(name);
    }

    /**
     * Receive notification of the beginning of a document. 
     */
    public void startDocument()
    throws SAXException {
        if (this.handler!=null)
            this.handler.startDocument();
    }

    /**
     * Receive notification of the end of a document. 
     */
    public void endDocument()
    throws SAXException {
        if (this.handler!=null)
            this.handler.endDocument();
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char[] ch, int start, int len)
    throws SAXException {
        if (this.handler!=null)
            this.handler.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content. 
     */
    public void ignorableWhitespace(char[] ch, int start, int len)
    throws SAXException {
        if (this.handler!=null)
            this.handler.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive an object for locating the origin of SAX document events. 
     */
    public void setDocumentLocator(Locator locator) {
        if (this.handler!=null)
            this.handler.setDocumentLocator(locator);
    }
}
