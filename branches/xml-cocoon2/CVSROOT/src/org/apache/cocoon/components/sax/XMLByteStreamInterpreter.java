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

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.IOException;

import java.util.HashMap;

import org.apache.avalon.Component;
import org.apache.avalon.Poolable;

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

import org.apache.cocoon.xml.AbstractXMLProducer;

/**
 * This a simple xml compiler which takes a byte array as input.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Revision: 1.1.2.1 $ $Date: 2001-04-11 10:52:49 $
 */

public final class XMLByteStreamInterpreter
extends AbstractXMLProducer
implements XMLDeserializer, Component, Poolable {

    private static final int START_DOCUMENT         = 0;
    private static final int END_DOCUMENT           = 1;
    private static final int START_PREFIX_MAPPING   = 2;
    private static final int END_PREFIX_MAPPING     = 3;
    private static final int START_ELEMENT          = 4;
    private static final int END_ELEMENT            = 5;
    private static final int CHARACTERS             = 6;
    private static final int IGNORABLE_WHITESPACE   = 7;
    private static final int PROCESSING_INSTRUCTION = 8;
    private static final int COMMENT                = 9;

    public void deserialize(Object saxFragment)
    throws SAXException {
        try {
            this.parse(new ByteArrayInputStream((byte[]) saxFragment));
        } catch (IOException ioe) {
            throw new SAXException("IOException: " + ioe);
        }
    }

    private void parse(InputStream stream) throws IOException, SAXException {
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
                case COMMENT:
                    chars = input.readChars();
                    if (this.lexicalHandler != null) {
                        len = chars.length;
                        while (chars[len-1]==0) len--;
                        lexicalHandler.comment(chars, 0, len);
                    }
                    break;
                default:
                    throw new IOException ("parsing error: event not supported.");
            }
        }
    }
}
