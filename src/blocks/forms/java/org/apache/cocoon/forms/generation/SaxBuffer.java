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
package org.apache.cocoon.forms.generation;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.ext.LexicalHandler;
import org.apache.excalibur.xml.sax.XMLizable;
import java.util.List;
import java.util.Iterator;

/**
 * Hack to get the fi:styling element into the fi:widget element 
 * (Basically a copy of o.a.c.xml.SaxBuffer, but dumps the events into 
 *  a user-supplied list). TBD: merge these.
 */

public class SaxBuffer 
    implements ContentHandler, LexicalHandler, XMLizable {

    private List buffer;
    
    public SaxBuffer(List list) {
        buffer = list;
    }

    public void skippedEntity(String name) throws SAXException {
        buffer.add(new SkippedEntity(name));
    }

    public void setDocumentLocator(Locator locator) {
        // don't record this event
    }

    public void ignorableWhitespace(char ch[], int start, int length) throws SAXException {
        buffer.add(new IgnorableWhitespace(ch, start, length));
    }

    public void processingInstruction(String target, String data) throws SAXException {
        buffer.add(new PI(target, data));
    }

    public void startDocument() throws SAXException {
        buffer.add(StartDocument.SINGLETON);
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        buffer.add(new StartElement(namespaceURI, localName, qName, atts));
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        buffer.add(new EndPrefixMapping(prefix));
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        buffer.add(new Characters(ch, start, length));
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        buffer.add(new EndElement(namespaceURI, localName, qName));
    }

    public void endDocument() throws SAXException {
        buffer.add(EndDocument.SINGLETON);
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        buffer.add(new StartPrefixMapping(prefix, uri));
    }

    public void endCDATA() throws SAXException {
        buffer.add(EndCDATA.SINGLETON);
    }

    public void comment(char ch[], int start, int length) throws SAXException {
        buffer.add(new Comment(ch, start, length));
    }

    public void startEntity(String name) throws SAXException {
        buffer.add(new StartEntity(name));
    }

    public void endDTD() throws SAXException {
        buffer.add(EndDTD.SINGLETON);
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        buffer.add(new StartDTD(name, publicId, systemId));
    }

    public void startCDATA() throws SAXException {
        buffer.add(StartCDATA.SINGLETON);
    }

    public void endEntity(String name) throws SAXException {
        buffer.add(new EndEntity(name));
    }

    public void toSAX(ContentHandler contentHandler) throws SAXException {
        for (Iterator i = buffer.iterator(); i.hasNext();) {
            SaxEvent saxEvent = (SaxEvent)i.next();
            saxEvent.send(contentHandler);
        }
    }

    public interface SaxEvent {
        public void send(ContentHandler contentHandler) throws SAXException;
    }

    public final static class StartDocument implements SaxEvent {
        static public final StartDocument SINGLETON = new StartDocument();
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.startDocument();
        }
    }

    public final static class EndDocument implements SaxEvent {
        static public final EndDocument SINGLETON = new EndDocument();
        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endDocument();
        }
    }

    public final static class PI implements SaxEvent {
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

    public final static class StartDTD implements SaxEvent {
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

    public final static class EndDTD implements SaxEvent {
        static public final EndDTD SINGLETON = new EndDTD();
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endDTD();
        }
    }

    public final static class StartEntity implements SaxEvent {
        private final String name;

        public StartEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startEntity(name);
        }
    }

    public final static class EndEntity implements SaxEvent {
        private final String name;

        public EndEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endEntity(name);
        }
    }

    public final static class SkippedEntity implements SaxEvent {
        private final String name;

        public SkippedEntity(String name) {
            this.name = name;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.skippedEntity(name);
        }
    }

    public final static class StartPrefixMapping implements SaxEvent {
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

    public final static class EndPrefixMapping implements SaxEvent {
        private final String prefix;

        public EndPrefixMapping(String prefix) {
            this.prefix = prefix;
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.endPrefixMapping(prefix);
        }
    }

    public final static class StartElement implements SaxEvent {
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

    public final static class EndElement implements SaxEvent {
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

    public final static class Characters implements SaxEvent {
        private final char[] ch;

        public Characters(char[] ch, int start, int length) {
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

    public final static class Comment implements SaxEvent {
        private final char[] ch;

        public Comment(char[] ch, int start, int length) {
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).comment(ch, 0, ch.length);
        }
    }

    public final static class StartCDATA implements SaxEvent {
        static public final StartCDATA SINGLETON = new StartCDATA();
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).startCDATA();
        }
    }

    public final static class EndCDATA implements SaxEvent {
        static public final EndCDATA SINGLETON = new EndCDATA();
        public void send(ContentHandler contentHandler) throws SAXException {
            if (contentHandler instanceof LexicalHandler)
                ((LexicalHandler)contentHandler).endCDATA();
        }
    }

    public final static class IgnorableWhitespace implements SaxEvent {
        private final char[] ch;

        public IgnorableWhitespace(char[] ch, int start, int length) {
            this.ch = new char[length];
            System.arraycopy(ch, start, this.ch, 0, length);
        }

        public void send(ContentHandler contentHandler) throws SAXException {
            contentHandler.ignorableWhitespace(ch, 0, ch.length);
        }
    }
}
