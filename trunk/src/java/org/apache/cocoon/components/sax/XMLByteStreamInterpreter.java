/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.sax;

import java.util.ArrayList;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.cocoon.xml.AbstractXMLProducer;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This a simple xml compiler which takes a byte array as input.
 *
 * @author <a href="mailto:stefano@apache.org">Stefano Mazzocchi</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: XMLByteStreamInterpreter.java,v 1.7 2003/12/06 21:22:10 cziegeler Exp $
 *
 * @avalon.component
 * @avalon.service type="XMLDeserializer"
 * @x-avalon.lifestyle type="pooled"
 * @x-avalon.info name="xml-deserializer"
 */

public final class XMLByteStreamInterpreter
extends AbstractXMLProducer
implements XMLDeserializer, Recyclable {

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
    private static final int LOCATOR                = 10;
    private static final int START_DTD              = 11;
    private static final int END_DTD                = 12;
    private static final int START_CDATA            = 13;
    private static final int END_CDATA              = 14;
    private static final int SKIPPED_ENTITY         = 15;
    private static final int START_ENTITY           = 16;
    private static final int END_ENTITY             = 17;

    private ArrayList list = new ArrayList();
    private byte[] input;
    private int currentPos;

    public void recycle() {
        super.recycle();
        this.list.clear();
        this.input = null;
    }

    public void deserialize(Object saxFragment)
    throws SAXException {
        if (!(saxFragment instanceof byte[])) {
            throw new SAXException("XMLDeserializer needs byte array for deserialization.");
        }
        this.list.clear();
        this.input = (byte[])saxFragment;
        this.currentPos = 0;
        this.checkProlog();
        this.parse();
    }

    private void parse() throws SAXException {
        while ( currentPos < input.length) {
            switch (this.readEvent()) {
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
                    if (this.lexicalHandler != null) {
                        len = chars.length;
                        while (len > 0 && chars[len-1]==0) len--;
                        if (len > 0) lexicalHandler.comment(chars, 0, len);
                    }
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
                    throw new SAXException ("parsing error: event not supported.");
            }
        }
    }

    private void checkProlog() throws SAXException {
        int valid = 0;
        if (this.read() == 'C') valid++;
        if (this.read() == 'X') valid++;
        if (this.read() == 'M') valid++;
        if (this.read() == 'L') valid++;
        if (this.read() == 1) valid++;
        if (this.read() == 0) valid++;
        if (valid != 6) throw new SAXException("Unrecognized file format.");
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
        int length = this.readLength();
        int index = length & 0x00007FFF;
        if (length >= 0x00008000) {
            return (String) list.get(index);
        } else {
            char[] chars = this.readChars(index);
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
        return this.readChars(this.readLength());
    }

    private int read() throws SAXException {
        if (currentPos >= input.length)
            throw new SAXException("Reached end of input.");
        return input[currentPos++] & 0xff;
    }

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

    private void readBytes(byte[] b)
    throws SAXException {
        if (this.currentPos + b.length > this.input.length) {
            // TC:
            // >= prevents getting the last byte
            // 0 1 2 3 4   input.length = 5
            //     |_ currentPos = 2
            // b.length = 3
            // 2 + 3 > 5 ok
            // 2 + 3 >= 5 wrong
            // why has this worked before?
            throw new SAXException("End of input reached.");
        }
        System.arraycopy(this.input, this.currentPos, b, 0, b.length);
        this.currentPos += b.length;
    }

    private int readLength() throws SAXException {
        int ch1 = this.read();
        int ch2 = this.read();
        return ((ch1 << 8) + (ch2 << 0));
    }
}
