/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A class that can record SAX events and replay them later.
 *
 * <p>Compared to XMLByteStreamCompiler, this class is many times faster at sending out the recorded
 * SAX events since it doesn't need to convert between byte and char representations etc.
 * On the other hand, its data structure is more complex then a simple byte array,
 * making XMLByteStreamCompiler better in case the recorded SAX should be stored long-term.
 *
 * <p>Use this class if you need to frequently generate smaller amounts of SAX events,
 * or replay a set of recorded start events immediately.
 *
 * <p>Both ContentHandler and LexicalHandler are supported, the only exception is
 * that the setDocumentLocator event is not recorded.
 * 
 * @author <a href="mailto:dev@cocoon.apache.org">Apache Cocoon Team</a>
 * @version CVS $Id: SaxBuffer.java,v 1.11 2004/03/08 13:38:20 cziegeler Exp $
 */
public class SaxBuffer implements XMLConsumer, XMLizable, Recyclable, Serializable {

    /**
     * Stores list of {@link SaxBit} objects.
     */
    protected List saxbits = new ArrayList();

    /**
     * Creates empty SaxBuffer
     */
    public SaxBuffer() {
    }

    /**
     * Creates copy of another SaxBuffer
     */
    public SaxBuffer(SaxBuffer saxBuffer) {
        this.saxbits.addAll(saxBuffer.saxbits);
    }


    public void skippedEntity(String name) throws SAXException {
        saxbits.add(new SkippedEntity(name));
    }

    public void setDocumentLocator(Locator locator) {
        // don't record this event
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
    
    public boolean isEmpty() {
        return saxbits.isEmpty();
    }
    
    public void toSAX(ContentHandler contentHandler) throws SAXException {
        for (Iterator i = saxbits.iterator(); i.hasNext();) {
            SaxBit saxbit = (SaxBit)i.next();
            saxbit.send(contentHandler);
        }
    }

    /*
     * NOTE: Used in i18n XML bundle implementation
     */
    public String toString() {
        StringBuffer value = new StringBuffer();
        for (Iterator i = saxbits.iterator(); i.hasNext();) {
            SaxBit saxbit = (SaxBit)i.next();
            if (saxbit instanceof Characters) {
                ((Characters)saxbit).toString(value);
            }
        }
        
        return value.toString();
    }

    public void recycle() {
        saxbits.clear();
    }

    /**
     * SaxBit is a representation of the SAX event. Every SaxBit is immutable object.
     */
    interface SaxBit {
        public void send(ContentHandler contentHandler) throws SAXException;
    }

    final static class StartDocument implements SaxBit, Serializable {
        static public final StartDocument SINGLETON = new StartDocument();
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startDocument();
        }
    }

    final static class EndDocument implements SaxBit, Serializable {
        static public final EndDocument SINGLETON = new EndDocument();
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endDocument();
        }
    }

    final static class PI implements SaxBit, Serializable {
        private final String target;
        private final String data;

        public PI(String target, String data) {
            this.target = target;
            this.data = data;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.processingInstruction(target, data);
        }
    }

    final static class StartDTD implements SaxBit, Serializable {
        private final String name;
        private final String publicId;
        private final String systemId;

        public StartDTD(String name, String publicId, String systemId) {
            this.name = name;
            this.publicId = publicId;
            this.systemId = systemId;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startDTD(name, publicId, systemId);
        }
    }

    final static class EndDTD implements SaxBit, Serializable {
        static public final EndDTD SINGLETON = new EndDTD();
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endDTD();
        }
    }

    final static class StartEntity implements SaxBit, Serializable {
        private final String name;

        public StartEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startEntity(name);
        }
    }

    final static class EndEntity implements SaxBit, Serializable {
        private final String name;

        public EndEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endEntity(name);
        }
    }

    final static class SkippedEntity implements SaxBit, Serializable {
        private final String name;

        public SkippedEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.skippedEntity(name);
        }
    }

    final static class StartPrefixMapping implements SaxBit, Serializable {
        private final String prefix;
        private final String uri;

        public StartPrefixMapping(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    final static class EndPrefixMapping implements SaxBit, Serializable {
        private final String prefix;

        public EndPrefixMapping(String prefix) {
            this.prefix = prefix;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endPrefixMapping(prefix);
        }
    }

    final static class StartElement implements SaxBit, Serializable {
        private final String namespaceURI;
        private final String localName;
        private final String qName;
        private final Attributes attrs;

        public StartElement(String namespaceURI, String localName, String qName, Attributes attrs) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
            this.attrs = new org.xml.sax.helpers.AttributesImpl(attrs);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startElement(namespaceURI, localName, qName, attrs);
        }
    }

    final static class EndElement implements SaxBit, Serializable {
        private final String namespaceURI;
        private final String localName;
        private final String qName;

        public EndElement(String namespaceURI, String localName, String qName) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endElement(namespaceURI, localName, qName);
        }
    }

    final static class Characters implements SaxBit, Serializable {
        private final char[] ch;

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
    }

    final static class Comment implements SaxBit, Serializable {
        private final char[] ch;

        public Comment(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).comment(ch, 0, ch.length);
        }
    }

    final static class StartCDATA implements SaxBit, Serializable {
        static public final StartCDATA SINGLETON = new StartCDATA();
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startCDATA();
        }
    }

    final static class EndCDATA implements SaxBit, Serializable {
        static public final EndCDATA SINGLETON = new EndCDATA();
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endCDATA();
        }
    }

    final static class IgnorableWhitespace implements SaxBit, Serializable {
        private final char[] ch;

        public IgnorableWhitespace(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.ignorableWhitespace(ch, 0, ch.length);
        }
    }
}
