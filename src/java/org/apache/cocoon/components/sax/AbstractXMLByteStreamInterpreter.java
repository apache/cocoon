/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.sax;

import java.util.ArrayList;

import org.apache.cocoon.xml.DefaultLexicalHandler;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This a simple xml compiler which takes a byte array as input.
 * If you want to reuse this interpreter make sure to call first {@link #recycle()}
 * and then set the new consumer for the sax events.
 *
 * @version $Id: AbstractXMLByteStreamInterpreter.java 587751 2007-10-24 02:41:36Z vgritsenko $
 */
public abstract class AbstractXMLByteStreamInterpreter implements XMLProducer, XMLByteStreamConstants {

    private ArrayList list = new ArrayList();

    protected static final ContentHandler EMPTY_CONTENT_HANDLER = new DefaultHandler();

    /** The <code>ContentHandler</code> receiving SAX events. */
    protected ContentHandler contentHandler = EMPTY_CONTENT_HANDLER;

    /** The <code>LexicalHandler</code> receiving SAX events. */
    protected LexicalHandler lexicalHandler = DefaultLexicalHandler.NULL_HANDLER;

    /**
     * Set the <code>XMLConsumer</code> that will receive XML data.
     * <br>
     * This method will simply call <code>setContentHandler(consumer)</code>
     * and <code>setLexicalHandler(consumer)</code>.
     */
    public void setConsumer(XMLConsumer consumer) {
        setContentHandler(consumer);
        setLexicalHandler(consumer);
    }

    /**
     * Set the <code>ContentHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>ContentHandler</code> instance
     * accessing the protected <code>super.contentHandler</code> field.
     */
    public void setContentHandler(ContentHandler handler) {
        this.contentHandler = handler;
    }

    /**
     * Set the <code>LexicalHandler</code> that will receive XML data.
     * <br>
     * Subclasses may retrieve this <code>LexicalHandler</code> instance
     * accessing the protected <code>super.lexicalHandler</code> field.
     */
    public void setLexicalHandler(LexicalHandler handler) {
        this.lexicalHandler = handler;
    }

    public void recycle() {
        this.contentHandler = EMPTY_CONTENT_HANDLER;
        this.lexicalHandler = DefaultLexicalHandler.NULL_HANDLER;
        this.list.clear();
    }

    /**
     * This method needs to be used by sub classes to start the parsing of the byte stream
     *
     * @throws SAXException
     */
    protected void parse() throws SAXException {
        this.list.clear();
        this.checkProlog();
        int event = -1;
        int lastEvent = -1;
        while ( ( event = readEvent() ) != -1 ) {
            lastEvent = event;
            switch (event) {
                case START_DOCUMENT:
                    contentHandler.startDocument();
                    break;
                case END_DOCUMENT:
                    contentHandler.endDocument();
                    break;
                case START_PREFIX_MAPPING:
                    contentHandler.startPrefixMapping(this.readString(), this.readString());
                    break;
                case END_PREFIX_MAPPING:
                    contentHandler.endPrefixMapping(this.readString());
                    break;
                case START_ELEMENT:
                    int attributes = this.readAttributes();
                    AttributesImpl atts = new AttributesImpl();
                    for (int i = 0; i < attributes; i++) {
                        atts.addAttribute(this.readString(), this.readString(), this.readString(), this.readString(), this.readString());
                    }
                    contentHandler.startElement(this.readString(), this.readString(), this.readString(), atts);
                    break;
                case END_ELEMENT:
                    contentHandler.endElement(this.readString(), this.readString(), this.readString());
                    break;
                case CHARACTERS:
                    char[] chars = this.readChars();
                    int len = chars.length;
                    while (len > 0 && chars[len-1]==0) len--;
                    if (len > 0) contentHandler.characters(chars, 0, len);
                    break;
                case IGNORABLE_WHITESPACE:
                    char[] spaces = this.readChars();
                    len = spaces.length;
                    while (len > 0 && spaces[len-1]==0) len--;
                    if (len > 0) contentHandler.characters(spaces, 0, len);
                    break;
                case PROCESSING_INSTRUCTION:
                    contentHandler.processingInstruction(this.readString(), this.readString());
                    break;
                case COMMENT:
                    chars = this.readChars();
                    len = chars.length;
                    while (len > 0 && chars[len-1]==0) len--;
                    if (len > 0) lexicalHandler.comment(chars, 0, len);
                    break;
                case LOCATOR:
                    {
                    String publicId = this.readString();
                    String systemId = this.readString();
                    int lineNumber = this.read();
                    int columnNumber = this.read();
                    org.xml.sax.helpers.LocatorImpl locator = new org.xml.sax.helpers.LocatorImpl();
                    locator.setPublicId(publicId);
                    locator.setSystemId(systemId);
                    locator.setLineNumber(lineNumber);
                    locator.setColumnNumber(columnNumber);
                    contentHandler.setDocumentLocator(locator);
                    }
                    break;
                case START_DTD:
                    lexicalHandler.startDTD(this.readString(),
                                            this.readString(),
                                            this.readString());
                    break;
                case END_DTD:
                    lexicalHandler.endDTD();
                    break;
                case START_CDATA:
                    lexicalHandler.startCDATA();
                    break;
                case END_CDATA:
                    lexicalHandler.endCDATA();
                    break;
                case SKIPPED_ENTITY:
                    contentHandler.skippedEntity( this.readString() );
                    break;
                case START_ENTITY:
                    lexicalHandler.startEntity( this.readString() );
                    break;
                case END_ENTITY:
                    lexicalHandler.endEntity( this.readString() );
                    break;
                default:
                    throw new SAXException ("parsing error: event not supported: " + event);
            }
        }
        if( lastEvent != END_DOCUMENT )
        {
            throw new SAXException ("parsing error: premature end of stream (lastEvent was " + lastEvent + ")." );
        }
    }

    protected int readEvent() throws SAXException {
        return this.read();
    }

    private int readAttributes() throws SAXException {
        int ch1 = this.read();
        int ch2 = this.read();
        return ((ch1 << 8) + (ch2 << 0));
    }

    private String readString() throws SAXException {
        int length = this.readWord();
        int index;
        if (length >= 0x00008000) {
            // index value in 16-bits format
            index = length & 0x00007FFF;
            return (String) list.get(index);
        } else {
            if (length == 0x00007FFF) {
                length = this.readLong();
                if (length >= 0x80000000) {
                    // index value in 32-bits format
                    index = length & 0x7fffffff;
                    return (String) list.get(index);
                }
            }
            char[] chars = this.readChars(length);
            int len = chars.length;
            if (len > 0) {
                while (chars[len-1]==0) len--;
            }
            String str;
            if (len == 0) {
                str = "";
            } else {
                str = new String(chars, 0, len);
            }
            list.add(str);
            return str;
        }
    }

    /**
     * The returned char array might contain any number of zero bytes
     * at the end
     */
    private char[] readChars() throws SAXException {
        int length = this.readWord();
        if (length == 0x00007FFF) {
            length = this.readLong();
        }
        return this.readChars(length);
    }

    protected abstract int read() throws SAXException;
    protected abstract int read(byte[] b) throws SAXException;

    /**
     * The returned char array might contain any number of zero bytes
     * at the end
     */
    private char[] readChars(int len) throws SAXException {
        char[] str = new char[len];
        byte[] bytearr = new byte[len];
        int c, char2, char3;
        int count = 0;
        int i = 0;

        this.readBytes(bytearr);

        while (count < len) {
            c = bytearr[count] & 0xff;
            switch (c >> 4) {
                case 0: case 1: case 2: case 3: case 4: case 5: case 6: case 7:
                    // 0xxxxxxx
                    count++;
                    str[i++] = (char) c;
                    break;
                case 12: case 13:
                    // 110x xxxx   10xx xxxx
                    count += 2;
                    char2 = bytearr[count-1];
                    str[i++] = (char) (((c & 0x1F) << 6) | (char2 & 0x3F));
                    break;
                case 14:
                    // 1110 xxxx  10xx xxxx  10xx xxxx
                    count += 3;
                    char2 = bytearr[count-2];
                    char3 = bytearr[count-1];
                    str[i++] = ((char)(((c & 0x0F) << 12) | ((char2 & 0x3F) << 6) | ((char3 & 0x3F) << 0)));
                    break;
                default:
                    // 10xx xxxx,  1111 xxxx
                    throw new SAXException("UTFDataFormatException");
            }
        }

        return str;
    }

    private void readBytes(byte[] b) throws SAXException {
        final int bytesRead = this.read( b );
        if (bytesRead < b.length ) {
            throw new SAXException("End of is reached.");
        }
    }

    private int readWord() throws SAXException {
        int ch1 = this.read();
        int ch2 = this.read();
        return ((ch1 << 8) + (ch2 << 0));
    }

    private int readLong() throws SAXException {
        int ch1 = this.read();
        int ch2 = this.read();
        int ch3 = this.read();
        int ch4 = this.read();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    private void checkProlog() throws SAXException {
        int valid = 0;
        int ch = 0;
        if ((ch = this.read()) == 'C') valid++;
        else throw new SAXException("Unrecognized file format (." + valid + "," + ch + ")");
        if ((ch = this.read()) == 'X') valid++;
        else throw new SAXException("Unrecognized file format (." + valid + "," + ch + ")");
        if ((ch = this.read()) == 'M') valid++;
        else throw new SAXException("Unrecognized file format (." + valid + "," + ch + ")");
        if ((ch = this.read()) == 'L') valid++;
        else throw new SAXException("Unrecognized file format (." + valid + "," + ch + ")");
        if ((ch = this.read()) == 1) valid++;
        else throw new SAXException("Unrecognized file format (." + valid + "," + ch + ")");
        if ((ch = this.read()) == 0) valid++;
        else throw new SAXException("Unrecognized file format (." + valid + "," + ch + ")");
    }
}
