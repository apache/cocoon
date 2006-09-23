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
package org.apache.cocoon.template.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.cocoon.xml.XMLConsumer;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @version $Id$
 */
public class AttributeAwareXMLConsumerImpl implements AttributeAwareXMLConsumer {

    private StartElement currentElement;
    private List saxbits;
    private Locator locator;

    private XMLConsumer delegate;

    public AttributeAwareXMLConsumerImpl(XMLConsumer consumer) {
        this.delegate = consumer;
        this.saxbits = new ArrayList();
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        delegate.setDocumentLocator(locator);
    }

    public void startDocument() throws SAXException {
        delegate.startDocument();
    }

    public void endDocument() throws SAXException {
        playCache();
        delegate.endDocument();
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        playCache();
        delegate.startPrefixMapping(prefix, uri);
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        playCache();
        delegate.endPrefixMapping(prefix);
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes attrs) throws SAXException {
        playCache();
        this.currentElement = new StartElement(namespaceURI, localName, qName,
                attrs);
    }

    public void endElement(String namespaceURI, String localName, String qName)
            throws SAXException {
        playCache();
        delegate.endElement(namespaceURI, localName, qName);
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        // TODO: should we allow to emit characters before adding an attribute?
        if (this.currentElement != null)
            this.saxbits.add(new Characters(ch, start, length));
        else
            delegate.characters(ch, start, length);

    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        if (this.currentElement != null)
            this.saxbits.add(new IgnorableWhitespace(ch, start, length));
        else
            delegate.ignorableWhitespace(ch, start, length);
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        playCache();
        delegate.processingInstruction(target, data);
    }

    public void skippedEntity(String name) throws SAXException {
        playCache();
        delegate.skippedEntity(name);
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        playCache();
        delegate.startDTD(name, publicId, systemId);
    }

    public void endDTD() throws SAXException {
        playCache();
        delegate.endDTD();
    }

    public void startEntity(String name) throws SAXException {
        playCache();
        delegate.startEntity(name);
    }

    public void endEntity(String name) throws SAXException {
        playCache();
        delegate.endEntity(name);
    }

    public void startCDATA() throws SAXException {
        playCache();
        delegate.startCDATA();
    }

    public void endCDATA() throws SAXException {
        playCache();
        delegate.endCDATA();
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        playCache();
        delegate.comment(ch, start, length);
    }

    public void attribute(String uri, String localName, String qName,
            String type, String value) throws SAXException {
        if (this.currentElement == null)
            throw new SAXParseException("attribute event not allowed here",
                    this.locator);
        else {
            this.currentElement.attribute(uri, localName, qName, type, value);
            // if between currentElement and jx:attribute only whitespace
            // was recorded - skip it
            boolean whitespaceOnly = true;
            Iterator it = this.saxbits.iterator();
            while (it.hasNext()) {
                SaxBit saxBit = (SaxBit) it.next();
                if (!(saxBit instanceof IgnorableWhitespace))
                    whitespaceOnly = false;
            }
            if (whitespaceOnly)
                this.saxbits.clear();
        }
    }

    interface SaxBit {
        public void send(ContentHandler contentHandler) throws SAXException;
    }

    private class StartElement implements SaxBit {
        private String namespaceURI;
        private String localName;
        private String qName;
        private AttributesImpl attrs;

        public StartElement(String namespaceURI, String localName,
                String qName, Attributes attrs) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.qName = qName;
            this.attrs = new AttributesImpl(attrs);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startElement(this.namespaceURI, this.localName,
                    this.qName, this.attrs);
        }

        public void attribute(String uri, String localName, String qName,
                String type, String value) {
            this.attrs.addAttribute(uri, localName, qName, type, value);
        }
    }

    public final static class Characters implements SaxBit {
        public final char[] ch;

        public Characters(char[] ch, int start, int length) {
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.characters(ch, 0, ch.length);
        }
    }

    public final static class IgnorableWhitespace implements SaxBit {
        public final char[] ch;

        public IgnorableWhitespace(char[] ch, int start, int length) {
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.ignorableWhitespace(ch, 0, ch.length);
        }
    }

    public void playCache() throws SAXException {
        if (this.currentElement != null) {
            this.currentElement.send(delegate);
            this.currentElement = null;
        }

        Iterator it = this.saxbits.iterator();
        while (it.hasNext()) {
            SaxBit saxBit = (SaxBit) it.next();
            saxBit.send(delegate);
        }
        this.saxbits.clear();
    }
}
