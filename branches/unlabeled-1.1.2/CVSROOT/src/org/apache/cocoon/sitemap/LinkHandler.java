/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sitemap;

import org.apache.cocoon.XMLConsumer;
import org.xml.sax.AttributeList;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributeListImpl;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>, 
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-07 15:35:42 $
 */
public abstract class LinkHandler implements XMLConsumer {
    private XMLConsumer consumer=null;
    private LinkResolver resolver=null;

    public LinkHandler(LinkResolver resolver) {
        super();
        this.resolver=null;
    }

    public void setXMLConsumer(XMLConsumer cons) {
        this.consumer=cons;
    }

    /**
     * Receive notification of a processing instruction. 
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        this.consumer.processingInstruction(target,data);
    }

    /**
     * Receive notification of the beginning of an element. 
     */
    public void startElement(String name, AttributeList atts)
    throws SAXException {
        this.consumer.startElement(name,atts);
    }

    /**
     * Receive notification of the end of an element. 
     */
    public void endElement(String name)
    throws SAXException {
        this.consumer.endElement(name);
    }

    /**
     * Receive notification of the beginning of a document. 
     */
    public void startDocument()
    throws SAXException {
        this.consumer.startDocument();
    }

    /**
     * Receive notification of the end of a document. 
     */
    public void endDocument()
    throws SAXException {
        this.consumer.endDocument();
    }

    /**
     * Receive notification of character data.
     */
    public void characters(char[] ch, int start, int len)
    throws SAXException {
        this.consumer.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content. 
     */
    public void ignorableWhitespace(char[] ch, int start, int len)
    throws SAXException {
        this.consumer.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive an object for locating the origin of SAX document events. 
     */
    public void setDocumentLocator(Locator locator) {
        this.consumer.setDocumentLocator(locator);
    }
}
