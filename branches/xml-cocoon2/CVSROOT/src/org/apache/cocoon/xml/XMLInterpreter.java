/**
 ****************************************************************************
 * Copyright (C) The Apache Software Foundation. All rights reserved.        *
 * ------------------------------------------------------------------------- *
 * This software is published under the terms of the Apache Software License *
 * version 1.1, a copy of which has been included  with this distribution in *
 * the LICENSE file.                                                         *
 ****************************************************************************
 */
package org.apache.cocoon.xml;

import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;

import org.xml.sax.XMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @version CVS $Revision: 1.1.2.2 $ $Date: 2001-03-12 05:55:27 $
 */

public final class XMLInterpreter implements XMLReader {

    public static final int START_DOCUMENT         = 0;
    public static final int END_DOCUMENT           = 1;
    public static final int START_PREFIX_MAPPING   = 2;
    public static final int END_PREFIX_MAPPING     = 3;
    public static final int START_ELEMENT          = 4;
    public static final int END_ELEMENT            = 5;
    public static final int CHARACTERS             = 6;
    public static final int IGNORABLE_WHITESPACE   = 7;
    public static final int PROCESSING_INSTRUCTION = 8;

    private ContentHandler contentHandler;
    private DTDHandler dtdHandler;
    private EntityResolver entityResolver;
    private ErrorHandler errorHandler;

    private HashMap features = new HashMap();
    private HashMap properties = new HashMap();

    public ContentHandler getContentHandler() {
        return this.contentHandler;
    }

    public DTDHandler getDTDHandler() {
        return this.dtdHandler;
    }

    public EntityResolver getEntityResolver() {
        return this.entityResolver;
    }

    public ErrorHandler getErrorHandler() {
        return this.errorHandler;
    }

    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    public void setDTDHandler(DTDHandler handler) {
        this.dtdHandler = handler;
    }

    public void setEntityResolver(EntityResolver resolver) {
        this.entityResolver = resolver;
    }

    public void setErrorHandler(ErrorHandler handler) {
        this.errorHandler = handler;
    }

    public boolean getFeature(java.lang.String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        Object o = this.features.get(name);
        return ((o != null) && ((Boolean) o).booleanValue());
    }

    public Object getProperty(java.lang.String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return this.properties.get(name);
    }

    public void setFeature(java.lang.String name, boolean value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.features.put(name, new Boolean(value));
    }

    public void setProperty(java.lang.String name, java.lang.Object value) throws SAXNotRecognizedException, SAXNotSupportedException {
        this.properties.put(name, value);
    }

    public void parse(java.lang.String systemId) throws IOException, SAXException {
        parse(new BufferedInputStream(new FileInputStream(systemId)));
    }

    public void parse(InputSource in) throws IOException, SAXException {
        parse(in.getByteStream());
    }

    public void parse(InputStream stream) throws IOException, SAXException {
        CompiledXMLInputStream input = new CompiledXMLInputStream(stream);

        while (true) {
            switch (input.readEvent()) {
                case START_DOCUMENT:
                    contentHandler.startDocument();
                    break;
                case END_DOCUMENT:
                    contentHandler.endDocument();
                    return;
                case START_PREFIX_MAPPING:
                    contentHandler.startPrefixMapping(input.readString(), input.readString());
                    break;
                case END_PREFIX_MAPPING:
                    contentHandler.endPrefixMapping(input.readString());
                    break;
                case START_ELEMENT:
                    int attributes = input.readAttributes();
                    AttributesImpl atts = new AttributesImpl();
                    for (int i = 0; i < attributes; i++) {
                        atts.addAttribute(input.readString(), input.readString(), input.readString(), input.readString(), input.readString());
                    }
                    contentHandler.startElement(input.readString(), input.readString(), input.readString(), atts);
                    break;
                case END_ELEMENT:
                    contentHandler.endElement(input.readString(), input.readString(), input.readString());
                    break;
                case CHARACTERS:
                    char[] chars = input.readChars();
                    int len = chars.length;
                    while (chars[len-1]==0) len--;
                    contentHandler.characters(chars, 0, len);
                    break;
                case IGNORABLE_WHITESPACE:
                    char[] spaces = input.readChars();
                    len = spaces.length;
                    while (spaces[len-1]==0) len--;
                    contentHandler.characters(spaces, 0, len);
                    break;
                case PROCESSING_INSTRUCTION:
                    contentHandler.processingInstruction(input.readString(), input.readString());
                    break;
                default:
                    throw new IOException ("parsing error: event not supported.");
            }
        }
    }
}
