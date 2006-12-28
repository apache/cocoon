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
package org.apache.cocoon.xml;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ext.LexicalHandler;
import org.apache.excalibur.xml.sax.XMLizable;
import org.apache.avalon.excalibur.pool.Recyclable;

import java.io.Serializable;
import java.io.Writer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

/**
 * A class that can record SAX events and replay them later.
 *
 * <p>Compared to {@link org.apache.cocoon.components.sax.XMLByteStreamCompiler},
 * this class is many times faster at sending out the recorded SAX events since
 * it doesn't need to convert between byte and char representations etc.
 * On the other hand, its data structure is more complex then a simple byte array,
 * making XMLByteStreamCompiler better in case the recorded SAX should be stored long-term.
 *
 * <p>Use this class if you need to frequently generate smaller amounts of SAX events,
 * or replay a set of recorded start events immediately.</p>
 *
 * <p>Both {@link ContentHandler} and {@link LexicalHandler} are supported, the only
 * exception is that the setDocumentLocator event is not recorded.</p>
 *
 * @version $Id$
 */
public class SaxBuffer extends AbstractSAXFragment
                       implements XMLConsumer, Recyclable, Serializable {

    /**
     * Stores list of {@link SaxBit} objects.
     */
    protected List saxbits;

    /**
     * Creates empty SaxBuffer
     */
    public SaxBuffer() {
        this.saxbits = new ArrayList();
    }

    /**
     * Creates SaxBuffer based on the provided bits list.
     */
    public SaxBuffer(List bits) {
        this.saxbits = bits;
    }

    /**
     * Creates copy of another SaxBuffer
     */
    public SaxBuffer(SaxBuffer saxBuffer) {
        this.saxbits = new ArrayList(saxBuffer.saxbits);
    }

    //
    // ContentHandler Interface
    //

    public void skippedEntity(String name) throws SAXException {
        saxbits.add(new SkippedEntity(name));
    }

    public void setDocumentLocator(Locator locator) {
        // Don't record this event
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        saxbits.add(new IgnorableWhitespace(ch, start, length));
    }

    public void processingInstruction(String target, String data) throws SAXException {
        saxbits.add(new PI(target, data));
    }

    public void startDocument() throws SAXException {
        saxbits.add(StartDocument.SINGLETON);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        saxbits.add(new StartElement(namespaceURI, localName, qName, atts));
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        saxbits.add(new EndPrefixMapping(prefix));
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        saxbits.add(new Characters(ch, start, length));
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        saxbits.add(new EndElement(namespaceURI, localName, qName));
    }

    public void endDocument() throws SAXException {
        saxbits.add(EndDocument.SINGLETON);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        saxbits.add(new StartPrefixMapping(prefix, uri));
    }

    //
    // LexicalHandler Interface
    //

    public void endCDATA() throws SAXException {
        saxbits.add(EndCDATA.SINGLETON);
    }

    public void comment(char ch[], int start, int length) throws SAXException {
        saxbits.add(new Comment(ch, start, length));
    }

    public void startEntity(String name) throws SAXException {
        saxbits.add(new StartEntity(name));
    }

    public void endDTD() throws SAXException {
        saxbits.add(EndDTD.SINGLETON);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        saxbits.add(new StartDTD(name, publicId, systemId));
    }

    public void startCDATA() throws SAXException {
        saxbits.add(StartCDATA.SINGLETON);
    }

    public void endEntity(String name) throws SAXException {
        saxbits.add(new EndEntity(name));
    }

    //
    // Public Methods
    //

    /**
     * Add a bit containing XMLizable object
     */
    public void xmlizable(XMLizable xml) {
        saxbits.add(new XMLizableBit(xml));
    }

    /**
     * @return true if buffer is empty
     */
    public boolean isEmpty() {
        return saxbits.isEmpty();
    }

    /**
     * @return unmodifiable list of SAX bits
     */
    public List getBits() {
        return Collections.unmodifiableList(saxbits);
    }

    /**
     * Stream this buffer into the provided content handler.
     * If contentHandler object implements LexicalHandler, it will get lexical
     * events as well.
     */
    public void toSAX(ContentHandler contentHandler) throws SAXException {
        for (Iterator i = saxbits.iterator(); i.hasNext();) {
            SaxBit saxbit = (SaxBit)i.next();
            saxbit.send(contentHandler);
        }
    }

    /**
     * @return String value of the buffer
     */
    public String toString() {
        // NOTE: This method is used in i18n XML bundle implementation
        final StringBuffer value = new StringBuffer();
        for (Iterator i = saxbits.iterator(); i.hasNext();) {
            final SaxBit saxbit = (SaxBit) i.next();
            if (saxbit instanceof Characters) {
                ((Characters) saxbit).toString(value);
            }
        }

        return value.toString();
    }

    /**
     * Clear this buffer
     */
    public void recycle() {
        saxbits.clear();
    }

    /**
     * Dump buffer contents into the provided writer.
     */
    public void dump(Writer writer) throws IOException {
        Iterator i = saxbits.iterator();
        while (i.hasNext()) {
            final SaxBit saxbit = (SaxBit) i.next();
            saxbit.dump(writer);
        }
        writer.flush();
    }

    //
    // Implementation Methods
    //

    /**
     * Adds a SaxBit to the bits list
     */
    protected final void addBit(SaxBit bit) {
        saxbits.add(bit);
    }

    /**
     * Iterates through the bits list
     */
    protected final Iterator bits() {
        return saxbits.iterator();
    }

    /**
     * SaxBit is a representation of the SAX event. Every SaxBit is immutable object.
     */
    interface SaxBit {
        public void send(ContentHandler contentHandler) throws SAXException;
        public void dump(Writer writer) throws IOException;
    }

    public final static class StartDocument implements SaxBit, Serializable {
        public static final StartDocument SINGLETON = new StartDocument();

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startDocument();
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[StartDocument]\n");
        }
    }

    public final static class EndDocument implements SaxBit, Serializable {
        public static final EndDocument SINGLETON = new EndDocument();

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endDocument();
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[EndDocument]\n");
        }
    }

    public final static class PI implements SaxBit, Serializable {
        public final String target;
        public final String data;

        public PI(String target, String data) {
            this.target = target;
            this.data = data;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.processingInstruction(target, data);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[ProcessingInstruction] target=" + target + ",data=" + data + "\n");
        }
    }

    public final static class StartDTD implements SaxBit, Serializable {
        public final String name;
        public final String publicId;
        public final String systemId;

        public StartDTD(String name, String publicId, String systemId) {
            this.name = name;
            this.publicId = publicId;
            this.systemId = systemId;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startDTD(name, publicId, systemId);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[StartDTD] name=" + name + ",publicId=" + publicId + ",systemId=" + systemId + "\n");
        }
    }

    public final static class EndDTD implements SaxBit, Serializable {
        public static final EndDTD SINGLETON = new EndDTD();

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endDTD();
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[EndDTD]\n");
        }
    }

    public final static class StartEntity implements SaxBit, Serializable {
        public final String name;

        public StartEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startEntity(name);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[StartEntity] name=" + name + "\n");
        }
    }

    public final static class EndEntity implements SaxBit, Serializable {
        public final String name;

        public EndEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endEntity(name);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[EndEntity] name=" + name + "\n");
        }
    }

    public final static class SkippedEntity implements SaxBit, Serializable {
        public final String name;

        public SkippedEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.skippedEntity(name);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[SkippedEntity] name=" + name + "\n");
        }
    }

    public final static class StartPrefixMapping implements SaxBit, Serializable {
        public final String prefix;
        public final String uri;

        public StartPrefixMapping(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startPrefixMapping(prefix, uri);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[StartPrefixMapping] prefix=" + prefix + ",uri=" + uri + "\n");
        }
    }

    public final static class EndPrefixMapping implements SaxBit, Serializable {
        public final String prefix;

        public EndPrefixMapping(String prefix) {
            this.prefix = prefix;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endPrefixMapping(prefix);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[EndPrefixMapping] prefix=" + prefix + "\n");
        }
    }

    public final static class StartElement implements SaxBit, Serializable {
        public final String namespaceURI;
        public final String localName;
        public final String qName;
        public final Attributes attrs;

        public StartElement(String namespaceURI, String localName, String qName, Attributes attrs) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
            this.attrs = new org.xml.sax.helpers.AttributesImpl(attrs);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startElement(namespaceURI, localName, qName, attrs);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[StartElement] namespaceURI=" + namespaceURI + ",localName=" + localName + ",qName=" + qName + "\n");
            for (int i = 0; i < attrs.getLength(); i++) {
                writer.write("      [Attribute] namespaceURI=" + attrs.getURI(i) + ",localName=" + attrs.getLocalName(i) + ",qName=" + attrs.getQName(i) + ",type=" + attrs.getType(i) + ",value=" + attrs.getValue(i) + "\n");
            }
        }
    }

    public final static class EndElement implements SaxBit, Serializable {
        public final String namespaceURI;
        public final String localName;
        public final String qName;

        public EndElement(String namespaceURI, String localName, String qName) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endElement(namespaceURI, localName, qName);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[EndElement] namespaceURI=" + namespaceURI + ",localName=" + localName + ",qName=" + qName + "\n");
        }
    }

    public final static class Characters implements SaxBit, Serializable {
        public final char[] ch;

        public Characters(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.characters(ch, 0, ch.length);
        }

        public void toString(StringBuffer value) {
            value.append(ch);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[Characters] ch=" + new String(ch) + "\n");
        }
    }

    public final static class Comment implements SaxBit, Serializable {
        public final char[] ch;

        public Comment(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).comment(ch, 0, ch.length);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[Comment] ch=" + new String(ch) + "\n");
        }
    }

    public final static class StartCDATA implements SaxBit, Serializable {
        public static final StartCDATA SINGLETON = new StartCDATA();

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startCDATA();
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[StartCDATA]\n");
        }
    }

    public final static class EndCDATA implements SaxBit, Serializable {
        public static final EndCDATA SINGLETON = new EndCDATA();

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endCDATA();
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[EndCDATA]\n");
        }
    }

    public final static class IgnorableWhitespace implements SaxBit, Serializable {
        public final char[] ch;

        public IgnorableWhitespace(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.ignorableWhitespace(ch, 0, ch.length);
        }

        public void dump(Writer writer) throws IOException {
            writer.write("[IgnorableWhitespace] ch=" + new String(ch) + "\n");
        }
    }

    public final static class XMLizableBit implements SaxBit, Serializable {
        public final XMLizable xml;

        public XMLizableBit(XMLizable xml) {
            this.xml = xml;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            this.xml.toSAX(new EmbeddedXMLPipe(contentHandler));
        }

        public void dump(Writer writer) throws IOException {
            if (xml instanceof SaxBuffer) {
                writer.write("[XMLizable] Begin nested SaxBuffer\n");
                ((SaxBuffer)xml).dump(writer);
                writer.write("[XMLizable] End nested SaxBuffer\n");
            } else {
                writer.write("[XMLizable] xml=" + xml + "\n");
            }
        }
    }
}
