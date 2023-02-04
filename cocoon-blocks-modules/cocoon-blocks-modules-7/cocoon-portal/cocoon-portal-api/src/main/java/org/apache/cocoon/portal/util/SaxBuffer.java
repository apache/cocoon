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
package org.apache.cocoon.portal.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;

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
public class SaxBuffer implements ContentHandler, LexicalHandler {

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
    interface SaxBit extends Serializable {
        void send(ContentHandler contentHandler) throws SAXException;
    }

    public final static class StartDocument implements SaxBit {
        public static final StartDocument SINGLETON = new StartDocument();

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startDocument();
        }
    }

    public final static class EndDocument implements SaxBit {
        public static final EndDocument SINGLETON = new EndDocument();

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endDocument();
        }
    }

    public final static class PI implements SaxBit {
        public final String target;
        public final String data;

        public PI(String target, String data) {
            this.target = target;
            this.data = data;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.processingInstruction(target, data);
        }
    }

    public final static class StartDTD implements SaxBit {
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
    }

    public final static class EndDTD implements SaxBit {
        public static final EndDTD SINGLETON = new EndDTD();

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endDTD();
        }
    }

    public final static class StartEntity implements SaxBit {
        public final String name;

        public StartEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startEntity(name);
        }
    }

    public final static class EndEntity implements SaxBit {
        public final String name;

        public EndEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endEntity(name);
        }
    }

    public final static class SkippedEntity implements SaxBit {
        public final String name;

        public SkippedEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.skippedEntity(name);
        }
    }

    public final static class StartPrefixMapping implements SaxBit {
        public final String prefix;
        public final String uri;

        public StartPrefixMapping(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startPrefixMapping(prefix, uri);
        }
    }

    public final static class EndPrefixMapping implements SaxBit {
        public final String prefix;

        public EndPrefixMapping(String prefix) {
            this.prefix = prefix;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endPrefixMapping(prefix);
        }
    }

    public final static class StartElement implements SaxBit {
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
    }

    public final static class EndElement implements SaxBit {
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
    }

    public final static class Characters implements SaxBit {
        public final char[] ch;

        public Characters(char[] ch, int start, int length) {
            // make a copy so that we don't hold references to a potentially large array we don't control
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.characters(ch, 0, ch.length);
        }
    }

    public final static class Comment implements SaxBit {
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
    }

    public final static class StartCDATA implements SaxBit {
        public static final StartCDATA SINGLETON = new StartCDATA();

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startCDATA();
        }
    }

    public final static class EndCDATA implements SaxBit {
        public static final EndCDATA SINGLETON = new EndCDATA();

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endCDATA();
        }
    }

    public final static class IgnorableWhitespace implements SaxBit {
        public final char[] ch;

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
