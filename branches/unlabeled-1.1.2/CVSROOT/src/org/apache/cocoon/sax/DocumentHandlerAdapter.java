/*****************************************************************************
 * Copyright (C) 1999 The Apache Software Foundation.   All rights reserved. *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1,  a copy of wich has been included  with this distribution in *
 * the LICENSE file.                                                         *
 *****************************************************************************/
package org.apache.cocoon.sax;

import org.xml.sax.AttributeList;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.DocumentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 *
 * @author <a href="mailto:fumagalli@exoffice.com">Pierpaolo Fumagalli</a>,
 *         Exoffice Technologies, INC.</a>
 * @author Copyright 1999 &copy; <a href="http://www.apache.org">The Apache
 *         Software Foundation</a>. All rights reserved.
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2000-02-12 00:33:32 $
 * @since Cocoon 2.0
 */
public class DocumentHandlerAdapter implements DocumentHandler {
    /** The SAX ContentHandler */
    private ContentHandler content=null;

    /**
     * Construct a new <code>DocumentHandlerAdapter</code> instance.
     */
    public DocumentHandlerAdapter() {
        this(null);
    }

    /**
     * Construct a new <code>DocumentHandlerAdapter</code> instance.
     */
    public DocumentHandlerAdapter(ContentHandler c) {
        super();
        this.setContentHandler(c);
    }

    /**
     * Set the current <code>ContentHandler</code>.
     */
    public void setContentHandler(ContentHandler c) {
        this.content=c;
    }
    
    /**
     * Return the current <code>ContentHandler</code>.
     */
    public ContentHandler getContentHandler() {
        return(this.content);
    }
    
    /**
     * Receive an object for locating the origin of SAX document events.
     *
     * @param locator An object that can return the location of any SAX
     *                document event.
     */
    public void setDocumentLocator(Locator locator) {
        this.content.setDocumentLocator(locator);
    }

    /**
     * Receive notification of the beginning of a document.
     */
    public void startDocument()
    throws SAXException {
        this.content.startDocument();
    }

    /**
     * Receive notification of the end of a document.
     */
    public void endDocument()
    throws SAXException {
        this.content.endDocument();
    }

    /**
     * Receive notification of the beginning of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     * @param a The attributes attached to the element. If there are no
     *          attributes, it shall be an empty Attributes object.
     */
    public void startElement(String name, AttributeList a)
    throws SAXException {
        AttributesImpl list=new AttributesImpl();
        for (int x=0; x<a.getLength(); x++) {
            list.addAttribute("",a.getName(x),a.getName(x),a.getType(x),
                                 a.getValue(x));
        }
        
        this.content.startElement("",name,name,list);
    }


    /**
     * Receive notification of the end of an element.
     *
     * @param uri The Namespace URI, or the empty string if the element has no
     *            Namespace URI or if Namespace
     *            processing is not being performed.
     * @param loc The local name (without prefix), or the empty string if
     *            Namespace processing is not being performed.
     * @param raw The raw XML 1.0 name (with prefix), or the empty string if
     *            raw names are not available.
     */
    public void endElement(String name)
    throws SAXException {
        this.content.endElement("",name,name);
    }

    /**
     * Receive notification of character data.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void characters(char ch[], int start, int len)
    throws SAXException {
        this.content.characters(ch,start,len);
    }

    /**
     * Receive notification of ignorable whitespace in element content.
     *
     * @param ch The characters from the XML document.
     * @param start The start position in the array.
     * @param len The number of characters to read from the array.
     */
    public void ignorableWhitespace(char ch[], int start, int len)
    throws SAXException {
        this.content.ignorableWhitespace(ch,start,len);
    }

    /**
     * Receive notification of a processing instruction.
     *
     * @param target The processing instruction target.
     * @param data The processing instruction data, or null if none was
     *             supplied.
     */
    public void processingInstruction(String target, String data)
    throws SAXException {
        this.content.processingInstruction(target,data);
    }
}
