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
package org.apache.cocoon.woody.transformation;

import java.util.LinkedList;

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLConsumer;
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
 * CVS $Id: EffectPipe.java,v 1.3 2003/12/29 22:46:25 sylvain Exp $
 * @author Timothy Larson
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
        public String prefix;
        public String uri;
        public String loc;
        public String raw;
        public Attributes attrs;
        public boolean mine;

        public Element() {
            prefix = null; uri = null; loc = null; raw = null; attrs = Constants.EMPTY_ATTRS; mine = true;
        }

        public Element(String prefix, String uri) {
            this.prefix = prefix;
            this.uri = uri;
        }

        public Element(String uri, String loc, String raw, Attributes attrs) {
            this.uri = uri;
            this.loc = loc;
            this.raw = raw;
            this.attrs = Constants.EMPTY_ATTRS;
            if (attrs == null) {
                this.attrs = Constants.EMPTY_ATTRS;
                mine = true;
            } else {
                this.attrs = attrs;
                mine = false;
            }
        }

        public void addAttributes(Attributes attrs) {
            if (attrs != null) {
                if (mine == true) {
                    if (this.attrs == Constants.EMPTY_ATTRS) {
                        this.attrs = attrs;
                        mine = false;
                    } else {
                        ((AttributesImpl)this.attrs).setAttributes(attrs);
                    }
                } else {
                    this.attrs = new AttributesImpl(this.attrs);
                    ((AttributesImpl)this.attrs).setAttributes(attrs);
                    mine = true;
                }
            }
        }

        public void addAttribute(String uri, String loc, String raw, String type, String value) {
            if (!mine || attrs == Constants.EMPTY_ATTRS) {
                attrs = new AttributesImpl(attrs);
                mine = true;
            }
            ((AttributesImpl)attrs).addAttribute(uri, loc, raw, type, value);
        }

        public void addAttribute(String prefix, String uri, String loc, String value) {
            if (!mine || attrs == Constants.EMPTY_ATTRS) {
                attrs = new AttributesImpl(attrs);
                mine = true;
            }
            ((AttributesImpl)attrs).addAttribute(uri, loc, prefix + ":" +loc, "CDATA", value);
        }

        public void addAttribute(String loc, String value) {
            if (!mine || attrs == Constants.EMPTY_ATTRS) {
                attrs = new AttributesImpl(attrs);
                mine = true;
            }
            ((AttributesImpl)attrs).addAttribute("", loc, loc, "CDATA", value);
        }

        public void claimAttributes() {
            if (!mine) {
                attrs = new AttributesImpl(attrs);
                mine = true;
            }
        }
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

        public void startPrefixMapping() throws SAXException { startPrefixMapping(prefix, uri); }

        public void endPrefixMapping() throws SAXException { endPrefixMapping(prefix); }

        public void element(String prefix, String uri, String loc, Attributes attrs) throws SAXException {
            element = new Element(uri, loc, prefix + ":" + loc, attrs);
        }
 
        public void element(String prefix, String uri, String loc) throws SAXException { element(prefix, uri, loc, null); }
 
        public void element(String loc, Attributes attrs) throws SAXException { element = new Element("", loc, loc, attrs); }

        public void element(String loc) throws SAXException { element(loc, null); }

        public void element() throws SAXException { element = new Element(input.uri, input.loc, input.raw, input.attrs); }
 
        public void attribute(String prefix, String uri, String name, String value) throws SAXException {
            element.addAttribute(prefix, uri, name, value);
        }

        public void attribute(String name, String value) throws SAXException { element.addAttribute(name, value); }

        public void copyAttribute(String prefix, String uri, String name) throws SAXException {
            String value = null;
            if (input.attrs != null && (value = input.attrs.getValue(uri, name)) != null)
                attribute(prefix, uri, name, value);
            else
                throw new SAXException("Attribute \"" + name + "\" cannot be copied because it does not exist.");
        }

        public void attributes(Attributes attrs) throws SAXException { element.addAttributes(attrs); }

        public void attributes() throws SAXException { attributes(input.attrs); }

        public void startElement() throws SAXException {
            if (element.attrs == null) element.attrs = Constants.EMPTY_ATTRS;
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
                if (buffers == null)
                    buffers = new LinkedList();
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
