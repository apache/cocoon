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

import java.util.HashMap;

import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

/**
 * This a simple xml compiler which outputs a byte array.
 * If you want to reuse this instance, make sure to call {@link #recycle()}
 * inbetween two compilation tasks.
 *
 * @version $Id$
 */
public abstract class AbstractXMLByteStreamCompiler implements XMLConsumer, XMLByteStreamConstants {

    private HashMap map;
    private int mapCount;
    private boolean hasProlog = false;

    protected AbstractXMLByteStreamCompiler() {
        this.map = new HashMap();
        this.initOutput();
    }

    private void initOutput() {
        this.mapCount = 0;
        this.map.clear();
        this.hasProlog = false;
    }

    public void recycle() {
        this.initOutput();
    }

    public void startDocument() throws SAXException {
        if(!hasProlog)
            writeProlog();
        this.writeEvent(START_DOCUMENT);
    }

    public void endDocument() throws SAXException {
        this.writeEvent(END_DOCUMENT);
    }

    public void startPrefixMapping(java.lang.String prefix, java.lang.String uri) throws SAXException {
        if(!hasProlog)
            writeProlog();
        this.writeEvent(START_PREFIX_MAPPING);
        this.writeString(prefix);
        this.writeString(uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
       this.writeEvent(END_PREFIX_MAPPING);
       this.writeString(prefix);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        int length = atts.getLength();
        this.writeEvent(START_ELEMENT);
        this.writeAttributes(length);
        for (int i = 0; i < length; i++) {
            this.writeString(atts.getURI(i));
            this.writeString(atts.getLocalName(i));
            this.writeString(atts.getQName(i));
            this.writeString(atts.getType(i));
            this.writeString(atts.getValue(i));
         }
         this.writeString((namespaceURI == null ? "" : namespaceURI));
         this.writeString(localName);
         this.writeString(qName);
     }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        this.writeEvent(END_ELEMENT);
        this.writeString((namespaceURI == null ? "" : namespaceURI));
        this.writeString(localName);
        this.writeString(qName);
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        this.writeEvent(CHARACTERS);
        this.writeChars(ch, start, length);
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if(!hasProlog)
            writeProlog();
        this.writeEvent(IGNORABLE_WHITESPACE);
        this.writeChars(ch, start, length);
    }

    public void processingInstruction(String target, String data) throws SAXException {
        this.writeEvent(PROCESSING_INSTRUCTION);
        this.writeString(target);
        this.writeString(data);
    }

    public void setDocumentLocator(Locator locator) {
        try {
            if(!hasProlog)
                writeProlog();
            this.writeEvent(LOCATOR);
            String publicId = locator.getPublicId();
            String systemId = locator.getSystemId();
            this.writeString(publicId!=null?publicId:"");
            this.writeString(systemId!=null?systemId:"");
            this.write(locator.getLineNumber());
            this.write(locator.getColumnNumber());
        } catch (Exception e) {
             throw new DocumentLocatorException("Error while handling locator", e);
        }
    }

    public void skippedEntity(java.lang.String name) throws SAXException {
        this.writeEvent(SKIPPED_ENTITY);
        this.writeString(name);
    }

    /**
     * SAX Event Handling: LexicalHandler
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        this.writeEvent(START_DTD);
        this.writeString(name);
        this.writeString(publicId!=null?publicId:"");
        this.writeString(systemId!=null?systemId:"");
    }

    /**
     * SAX Event Handling: LexicalHandler
     */
    public void endDTD() throws SAXException {
        this.writeEvent(END_DTD);
    }

    /**
     * SAX Event Handling: LexicalHandler
     */
    public void startEntity(String name) throws SAXException {
        this.writeEvent(START_ENTITY);
        this.writeString(name);
    }

    /**
     * SAX Event Handling: LexicalHandler
     */
    public void endEntity(String name) throws SAXException {
        this.writeEvent(END_ENTITY);
        this.writeString(name);
    }

    /**
     * SAX Event Handling: LexicalHandler
     */
    public void startCDATA() throws SAXException {
        this.writeEvent(START_CDATA);
    }

    /**
     * SAX Event Handling: LexicalHandler
     */
    public void endCDATA() throws SAXException {
        this.writeEvent(END_CDATA);
    }

    /**
     * SAX Event Handling: LexicalHandler
     */
    public void comment(char ary[], int start, int length) throws SAXException {
        try {
            this.writeEvent(COMMENT);
            this.writeChars(ary, start, length);
        } catch (Exception e) {
            throw new SAXException(e);
        }
    }

    public final void writeEvent( final int event) throws SAXException {
        this.write(event);
    }

    public final void writeAttributes( final int attributes) throws SAXException {
        if (attributes > 0xFFFF) throw new SAXException("Too many attributes");
        this.write((attributes >>> 8) & 0xFF);
        this.write((attributes >>> 0) & 0xFF);
    }

    public final void writeString( final String str) throws SAXException {
        Integer index = (Integer) map.get(str);
        if (index == null) {
            map.put(str, new Integer(mapCount++));
            int length = str.length();
            this.writeChars(str.toCharArray(), 0, length);
        } else {
            int i = index.intValue();

            if (i <= 0x7FFF) {
                // write index value in 16-bits
                this.write(((i >>> 8) & 0xFF) | 0x80);
                this.write((i >>> 0) & 0xFF);
            } else {
                // write escape code (Short.MAX_VALUE) to write a full 32-bit value
                write((byte)0x7F);
                write((byte)0xFF);
                // write index value in 32-bit
                write((byte) ((i >>> 24) & 0xFF) | 0x80);
                write((byte) ((i >>> 16) & 0xFF));
                write((byte) ((i >>>  8) & 0xFF));
                write((byte) ((i >>>  0) & 0xFF));
            }
        }
    }

    public final void writeChars( final char[] ch, final int start, final int length) throws SAXException {
        int utflen = 0;
        int c;

        for (int i = 0; i < length; i++) {
            c = ch[i + start];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            }
            else if (c > 0x07FF) {
                utflen += 3;
            }
            else {
                utflen += 2;
            }
        }

        if (utflen >= 0x00007FFF) {
            write((byte)0x7F);
            write((byte)0xFF);
            write((byte) ((utflen >>> 24) & 0xFF));
            write((byte) ((utflen >>> 16) & 0xFF));
            write((byte) ((utflen >>>  8) & 0xFF));
            write((byte) ((utflen >>>  0) & 0xFF));
        }
        else {
            write((byte) ((utflen >>> 8) & 0xFF));
            write((byte) ((utflen >>> 0) & 0xFF));
        }

        for (int i = 0; i < length; i++) {
            c = ch[i + start];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                write((byte) c);
            }
            else if (c > 0x07FF) {
                write((byte) (0xE0 | ((c >> 12) & 0x0F)));
                write((byte) (0x80 | ((c >>  6) & 0x3F)));
                write((byte) (0x80 | ((c >>  0) & 0x3F)));
            }
            else {
                write((byte) (0xC0 | ((c >>  6) & 0x1F)));
                write((byte) (0x80 | ((c >>  0) & 0x3F)));
            }
        }
    }

    abstract protected void write( final int b ) throws SAXException;

    private void writeProlog() throws SAXException {
        write((byte)'C');
        write((byte)'X');
        write((byte)'M');
        write((byte)'L');
        write((byte)1);
        write((byte)0);
        hasProlog = true;
    }
}
