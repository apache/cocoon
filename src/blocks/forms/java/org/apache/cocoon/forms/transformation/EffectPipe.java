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
package org.apache.cocoon.forms.transformation;

import java.util.LinkedList;

import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;

// TODO: Reduce the Element creation and deletion churn by providing startElement
// and endElement methods which do not create or use Elements on the stack.
/*
 * Base class for XMLPipe's. Allows the structure of the source code of
 * the XMLPipe to match the structure of the data being transformed.
 *
 * @author Timothy Larson
 * @version $Id: EffectPipe.java,v 1.4 2004/04/09 16:36:00 mpo Exp $
 */
public class EffectPipe extends AbstractXMLPipe {

    protected static final int EVENT_SET_DOCUMENT_LOCATOR   = 0;
    protected static final int EVENT_START_DOCUMENT         = 1;
    protected static final int EVENT_END_DOCUMENT           = 2;
    protected static final int EVENT_START_PREFIX_MAPPING   = 3;
    protected static final int EVENT_END_PREFIX_MAPPING     = 4;
    protected static final int EVENT_START_ELEMENT          = 5;
    protected static final int EVENT_END_ELEMENT            = 6;
    protected static final int EVENT_ELEMENT                = 7;
    protected static final int EVENT_CHARACTERS             = 8;
    protected static final int EVENT_IGNORABLE_WHITESPACE   = 9;
    protected static final int EVENT_PROCESSING_INSTRUCTION =10;
    protected static final int EVENT_SKIPPED_ENTITY         =11;
    protected static final int EVENT_START_DTD              =12;
    protected static final int EVENT_END_DTD                =13;
    protected static final int EVENT_START_ENTITY           =14;
    protected static final int EVENT_END_ENTITY             =15;
    protected static final int EVENT_START_CDATA            =16;
    protected static final int EVENT_END_CDATA              =17;
    protected static final int EVENT_COMMENT                =18;

    protected class Element {
        public final String prefix;
        public final String uri;
        public final String loc;
        public final String raw;
        public Attributes attrs;
        public boolean mine;

        public Element() {
            this(null, null, null, null, null);
        }

        public Element(String prefix, String uri) {
            this(prefix, uri, null, null, null);
        }

        public Element(String uri, String loc, String raw, Attributes attrs) {
            this(null, uri, loc, raw, attrs);
        }
        
        public Element(String prefix, String uri, String loc, String raw, Attributes attrs) { 
            this.prefix=prefix;
            this.uri = uri;
            this.loc = loc;
            this.raw = raw;
            this.attrs = XMLUtils.EMPTY_ATTRIBUTES;
            if (attrs == null) {
                this.attrs = XMLUtils.EMPTY_ATTRIBUTES;
                mine = true;
            } else {
                this.attrs = attrs;
                mine = false;
            }
        }

        /**
         * Adds/overwrites the attributes from the collection in the argument 
         * to the ones inside this class.
         * 
         * @param attrs collection of attributes to add/overwrite
         */
        public void addAttributes(Attributes newAttrs) {
            if (newAttrs == null || newAttrs.getLength() == 0) return; //nothing to add
            if (attrs == XMLUtils.EMPTY_ATTRIBUTES) {
                attrs = new AttributesImpl(newAttrs);
                mine = true;
            } else {
                if (!mine) {
                    attrs = new AttributesImpl(attrs);
                    mine = true;
                }

                AttributesImpl modifAttrs  = ((AttributesImpl)attrs);
                int newAttrsCount = newAttrs.getLength();
                for (int i = 0; i < newAttrsCount; i++) {
                    String uri = newAttrs.getURI(i);
                    String loc = newAttrs.getLocalName(i);
                    String raw = newAttrs.getQName(i);
                    String type = newAttrs.getType(i);
                    String value = newAttrs.getValue(i);
                    
                    int foundAttr = modifAttrs.getIndex(uri, loc);
                    if (foundAttr == -1) {
                        modifAttrs.addAttribute(uri, loc, raw, type, value);                
                    } else {
                        modifAttrs.setAttribute(foundAttr, uri, loc, raw, type, value);                                
                    }                    
                }
            }
        }

        /**
         * Adds/overwrites one single attribute to the ones already contained 
         * inside this object.
         * 
         * @param uri the uri of the attribute to add.
         * @param loc the localname of the attribute to add
         * @param raw the rawname of the attribute to add
         * @param type the type of the attribute to add
         * @param value the value of the attribute to add.
         */
        public void addAttribute(String uri, String loc, String raw, String type, String value) {
            if (!mine || attrs == XMLUtils.EMPTY_ATTRIBUTES) {
                attrs = new AttributesImpl(attrs);
                mine = true;
            }
            AttributesImpl modifAttrs  = ((AttributesImpl)attrs);
            int foundAttr = modifAttrs.getIndex(uri, loc);
            if (foundAttr == -1) {
                modifAttrs.addAttribute(uri, loc, raw, type, value);                
            } else {
                modifAttrs.setAttribute(foundAttr, uri, loc, raw, type, value);                                
            }
        }

        public void addAttribute(String prefix, String uri, String loc, String value) {
            this.addAttribute(uri, loc, prefix + ":" +loc, "CDATA", value);
        }

        public void addAttribute(String loc, String value) {
            this.addAttribute("", loc, loc, "CDATA", value);
        }

        //TODO: IMPORTANT!!! 
        // check this: when commenting out this stuff everything still compiles!
        // this means that attributes on these objects are never explicitely "claimed"
        // which means that these Element objects should never get referenced 
        // outside the scope of the SAX event that creates them.
        // Is that really the case?
//        public void claimAttributes() {
//            if (!mine) {
//                attrs = new AttributesImpl(attrs);
//                mine = true;
//            }
//        }
    }

    protected abstract class Handler {
        public abstract Handler process() throws SAXException;
    }

    protected class NullHandler extends Handler {
        public Handler process() throws SAXException {
            return this;
        }
    }

    protected class BufferHandler extends Handler {
        public Handler process() throws SAXException {
            switch (event) {
            case EVENT_ELEMENT:
                return this;
            default:
                out.buffer();
                return this;
            }
        }
    }

    protected class Output extends AbstractXMLPipe {
        private LinkedList buffers = null;
        private SaxBuffer saxBuffer = null;
        private LinkedList elements  = null;
        protected Element element  = null;

        public Output() { elements = new LinkedList(); }

        public void startPrefixMapping() throws SAXException {
            super.startPrefixMapping(input.prefix, input.uri);
        }

        public void endPrefixMapping() throws SAXException {
            super.endPrefixMapping(input.prefix);
        }

        public void element(String prefix, String uri, String loc, Attributes attrs) throws SAXException {
            element = new Element(uri, loc, prefix + ":" + loc, attrs);
        }

        public void element(String prefix, String uri, String loc) throws SAXException {
            element(prefix, uri, loc, null);
        }

        public void element(String loc, Attributes attrs) throws SAXException {
            element = new Element("", loc, loc, attrs);
        }

        public void element(String loc) throws SAXException {
            element(loc, null);
        }

        public void element() throws SAXException {
            element = new Element(input.uri, input.loc, input.raw, input.attrs);
        }

        public void attribute(String prefix, String uri, String name, String value) throws SAXException {
            element.addAttribute(prefix, uri, name, value);
        }

        public void attribute(String name, String value) throws SAXException {
            element.addAttribute(name, value);
        }

        public void copyAttribute(String prefix, String uri, String name) throws SAXException {
            String value = null;
            if (input.attrs != null && (value = input.attrs.getValue(uri, name)) != null) {
                attribute(prefix, uri, name, value);
            } else {
                throw new SAXException("Attribute \"" + name + "\" cannot be copied because it does not exist.");
            }
        }

        public void attributes(Attributes attrs) throws SAXException {
            element.addAttributes(attrs);
        }

        public void attributes() throws SAXException {
            attributes(input.attrs);
        }

        public void startElement() throws SAXException {
            if (element.attrs == null) {
                element.attrs = XMLUtils.EMPTY_ATTRIBUTES;
            }
            super.startElement(element.uri, element.loc, element.raw, element.attrs);
            elements.addFirst(element);
            element = null;
        }

        public void endElement() throws SAXException {
            element = (Element)elements.removeFirst();
            super.endElement(element.uri, element.loc, element.raw);
            element = null;
        }

        public void startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
            super.startElement(uri, loc, raw, attrs);
        }

        public void endElement(String uri, String loc, String raw) throws SAXException {
            super.endElement(uri, loc, raw);
        }

        public void copy() throws SAXException {
            switch(event) {
            case EVENT_SET_DOCUMENT_LOCATOR:   this.setDocumentLocator(locator); break;
            case EVENT_START_DOCUMENT:         this.startDocument(); break;
            case EVENT_END_DOCUMENT:           this.endDocument(); break;
            case EVENT_START_PREFIX_MAPPING:   this.startPrefixMapping(); break;
            case EVENT_END_PREFIX_MAPPING:     this.endPrefixMapping(); break;
            case EVENT_START_ELEMENT:          this.element(); attributes(); startElement(); break;
            case EVENT_END_ELEMENT:            this.endElement(); break;
            case EVENT_CHARACTERS:             this.characters(c, start, len); break;
            case EVENT_IGNORABLE_WHITESPACE:   this.ignorableWhitespace(c, start, len); break;
            case EVENT_PROCESSING_INSTRUCTION: this.processingInstruction(target, data); break;
            case EVENT_SKIPPED_ENTITY:         this.skippedEntity(name); break;
            case EVENT_START_DTD:              this.startDTD(name, publicId, systemId); break;
            case EVENT_END_DTD:                this.endDTD(); break;
            case EVENT_START_ENTITY:           this.startEntity(name); break;
            case EVENT_END_ENTITY:             this.endEntity(name); break;
            case EVENT_START_CDATA:            this.startCDATA(); break;
            case EVENT_END_CDATA:              this.endCDATA(); break;
            case EVENT_COMMENT:                this.comment(c, start, len); break;
            }
        }

        protected void bufferInit() {
            if (saxBuffer != null) {
                if (buffers == null) {
                    buffers = new LinkedList();
                }
                buffers.addFirst(saxBuffer);
            }
            saxBuffer = new SaxBuffer();
        }

        protected void bufferFini() {
            if (buffers != null && buffers.size() > 0) {
                saxBuffer = (SaxBuffer)buffers.removeFirst();
            } else {
                saxBuffer = null;
            }
        }

        protected SaxBuffer getBuffer() {
            return saxBuffer;
        }

        public void buffer() throws SAXException {
            switch(event) {
            case EVENT_SET_DOCUMENT_LOCATOR:   saxBuffer.setDocumentLocator(locator); break;
            case EVENT_START_DOCUMENT:         saxBuffer.startDocument(); break;
            case EVENT_END_DOCUMENT:           saxBuffer.endDocument(); break;
            case EVENT_START_PREFIX_MAPPING:   saxBuffer.startPrefixMapping(prefix, uri); break;
            case EVENT_END_PREFIX_MAPPING:     saxBuffer.endPrefixMapping(prefix); break;
            case EVENT_START_ELEMENT:          saxBuffer.startElement(input.uri, input.loc, input.raw, input.attrs); break;
            case EVENT_END_ELEMENT:            saxBuffer.endElement(input.uri, input.loc, input.raw); break;
            case EVENT_CHARACTERS:             saxBuffer.characters(c, start, len); break;
            case EVENT_IGNORABLE_WHITESPACE:   saxBuffer.ignorableWhitespace(c, start, len); break;
            case EVENT_PROCESSING_INSTRUCTION: saxBuffer.processingInstruction(target, data); break;
            case EVENT_SKIPPED_ENTITY:         saxBuffer.skippedEntity(name); break;
            case EVENT_START_DTD:              saxBuffer.startDTD(name, publicId, systemId); break;
            case EVENT_END_DTD:                saxBuffer.endDTD(); break;
            case EVENT_START_ENTITY:           saxBuffer.startEntity(name); break;
            case EVENT_END_ENTITY:             saxBuffer.endEntity(name); break;
            case EVENT_START_CDATA:            saxBuffer.startCDATA(); break;
            case EVENT_END_CDATA:              saxBuffer.endCDATA(); break;
            case EVENT_COMMENT:                saxBuffer.comment(c, start, len); break;
            }
        }
    }


    protected int event = 0;

    protected Handler nullHandler = new NullHandler();
    protected Handler bufferHandler = new BufferHandler();

    protected LinkedList handlers = null;
    protected Handler handler = null;

    protected LinkedList elements = null;
    protected Element input = null;

    protected Locator locator = null;
    protected String name     = null;
    protected String publicId = null;
    protected String systemId = null;
    protected String target   = null;
    protected String data     = null;
    protected String prefix   = null;
    protected String uri      = null;
    protected char   c[]      = null;
    protected int start = 0;
    protected int len = 0;

    public Output out = null;

    public void init() {
        handlers = new LinkedList();
        elements = new LinkedList();
        out = new Output();
    }

    //====================================
    // Methods overriding AbstractXMLPipe
    //====================================

    public void setConsumer(XMLConsumer consumer) {
        super.setConsumer(consumer);
        out.setConsumer(consumer);
    }

    public void setContentHandler(ContentHandler handler) {
        super.setContentHandler(handler);
        out.setContentHandler(handler);
    }

    public void setLexicalHandler(LexicalHandler handler) {
        super.setLexicalHandler(handler);
        out.setLexicalHandler(handler);
    }

    public void recycle() {
        super.recycle();
        handlers = null;
        elements = null;
        out = null;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        try {
            event = EVENT_SET_DOCUMENT_LOCATOR; handler = handler.process();
        } catch(Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void startDocument() throws SAXException { event = EVENT_START_DOCUMENT; handler = handler.process(); }

    public void endDocument() throws SAXException { event = EVENT_END_DOCUMENT; handler = handler.process(); }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        input = new Element(prefix, uri);
        elements.addFirst(input);
        //this.prefix = prefix; this.uri = uri;
        event = EVENT_START_PREFIX_MAPPING; handler = handler.process();
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        input = (Element)elements.removeFirst();
        //this.prefix = prefix;
        event = EVENT_END_PREFIX_MAPPING; handler = handler.process();
        input = null;
    }

    public void startElement(String uri, String loc, String raw, Attributes attrs) throws SAXException {
        input = new Element(uri, loc, raw, attrs);
        elements.addFirst(input);
        handlers.addFirst(handler);
        event = EVENT_ELEMENT;       handler = handler.process();
        event = EVENT_START_ELEMENT; handler = handler.process();
    }

    public void endElement(String uri, String loc, String raw) throws SAXException {
        input = (Element)elements.removeFirst();
        event = EVENT_END_ELEMENT; handler.process();
        handler = (Handler)handlers.removeFirst();
        input = null;
    }

    public void characters(char c[], int start, int len) throws SAXException {
        this.c = c; this.start = start; this.len = len;
        event = EVENT_CHARACTERS; handler = handler.process();
    }

    public void ignorableWhitespace(char c[], int start, int len) throws SAXException {
        this.c = c; this.start = start; this.len = len;
        event = EVENT_IGNORABLE_WHITESPACE; handler = handler.process();
    }

    public void processingInstruction(String target, String data) throws SAXException {
        this.target = target; this.data = data;
        event = EVENT_PROCESSING_INSTRUCTION; handler = handler.process();
    }

    public void skippedEntity(String name) throws SAXException {
        this.name = name;
        event = EVENT_SKIPPED_ENTITY; handler = handler.process();
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        this.name = name; this.publicId = publicId; this.systemId = systemId;
        event = EVENT_START_DTD; handler = handler.process();
    }

    public void endDTD() throws SAXException { event = EVENT_END_DTD; handler = handler.process(); }

    public void startEntity(String name) throws SAXException {
        this.name = name;
        event = EVENT_START_ENTITY; handler = handler.process();
    }

    public void endEntity(String name) throws SAXException {
        this.name = name;
        event = EVENT_END_ENTITY; handler = handler.process();
    }

    public void startCDATA() throws SAXException {
        event = EVENT_START_CDATA; handler = handler.process();
    }

    public void endCDATA() throws SAXException {
        event = EVENT_END_CDATA; handler = handler.process();
    }

    public void comment(char c[], int start, int len) throws SAXException {
        this.c = c; this.start = start; this.len = len;
        event = EVENT_COMMENT; handler = handler.process();
    }
}
