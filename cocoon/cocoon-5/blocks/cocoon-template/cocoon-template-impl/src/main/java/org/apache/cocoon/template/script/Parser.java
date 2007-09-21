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
package org.apache.cocoon.template.script;

import java.util.Stack;

import org.apache.cocoon.template.JXTemplateGenerator;
import org.apache.cocoon.template.environment.ParsingContext;
import org.apache.cocoon.template.instruction.Instruction;
import org.apache.cocoon.template.script.event.*;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

/**
 * @version $Id$
 */
public class Parser implements ContentHandler, LexicalHandler {
    StartDocument startEvent;
    Event lastEvent;
    Stack stack = new Stack();
    Locator locator;
    Locator charLocation;
    StringBuffer charBuf;

    public static final Locator NULL_LOCATOR = new LocatorImpl();
    protected ParsingContext parsingContext;
    
    public Parser() {
    }

    public Parser(ParsingContext parsingContext) {
        this.parsingContext = parsingContext;
    }

    public void setParsingContext(ParsingContext parsingContext) {
        this.parsingContext = parsingContext;
    }
    
    public StartDocument getStartEvent() {
        return this.startEvent;
    }

    protected void recycle() {
        startEvent = null;
        lastEvent = null;
        stack.clear();
        locator = null;
        charLocation = null;
        charBuf = null;
    }

    private void addEvent(Event ev) throws SAXException {
        if (ev != null) {
            if (lastEvent == null) {
                lastEvent = startEvent = new StartDocument(locator);
            } else {
                flushChars();
            }
            lastEvent.setNext(ev);
            lastEvent = ev;
        } else {
            throw new NullPointerException("null event");
        }
    }

    void flushChars() throws SAXException {
        if (charBuf != null) {
            char[] chars = new char[charBuf.length()];
            charBuf.getChars(0, charBuf.length(), chars, 0);
            Characters ev = new Characters(this.parsingContext, charLocation, chars, 0, chars.length);
            lastEvent.setNext(ev);
            lastEvent = ev;
            charLocation = null;
            charBuf = null;
        }
    }

    public void characters(char[] ch, int start, int length)
            throws SAXException {
        if (charBuf == null) {
            charBuf = new StringBuffer(length);
            charLocation = locator != null ? new LocatorImpl(locator)
                    : NULL_LOCATOR;
        }
        charBuf.append(ch, start, length);
    }

    public void endDocument() throws SAXException {
        StartDocument startDoc = (StartDocument) stack.pop();
        EndDocument endDoc = new EndDocument(locator);
        startDoc.setEndDocument(endDoc);
        addEvent(endDoc);
    }

    public void endElement(String namespaceURI, String localName, String raw)
            throws SAXException {
        Event start = (Event) stack.pop();
        Event newEvent = null;
        if (start instanceof Instruction) {
            Instruction startInstruction = (Instruction) start;
            EndInstruction endInstruction = new EndInstruction(locator,
                    startInstruction);
            newEvent = endInstruction;
            addEvent(newEvent);
            startInstruction.endNotify();
        } else {
            StartElement startElement = (StartElement) start;
            newEvent = new EndElement(locator, startElement);
            startElement.setEndElement((EndElement) newEvent);
            addEvent(newEvent);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        EndPrefixMapping endPrefixMapping = new EndPrefixMapping(locator,
                prefix);
        addEvent(endPrefixMapping);
    }

    public void ignorableWhitespace(char[] ch, int start, int length)
            throws SAXException {
        Event ev = new IgnorableWhitespace(this.parsingContext, locator, ch, start, length);
        addEvent(ev);
    }

    public void processingInstruction(String target, String data)
            throws SAXException {
        Event pi = new ProcessingInstruction(locator, target, data);
        addEvent(pi);
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    public void skippedEntity(String name) throws SAXException {
        addEvent(new SkippedEntity(locator, name));
    }

    public void startDocument() {
        startEvent = new StartDocument(locator);
        lastEvent = startEvent;
        stack.push(lastEvent);
    }

    public void startElement(String namespaceURI, String localName,
            String qname, Attributes attrs) throws SAXException {
        Event newEvent = null;
        AttributesImpl elementAttributes = new AttributesImpl(attrs);
        int attributeCount = elementAttributes.getLength();
        for (int i = 0; i < attributeCount; i++) {
            String attributeURI = elementAttributes.getURI(i);
            if (StringUtils.equals(attributeURI, JXTemplateGenerator.NS)) {
                // TODO: template properties should be allowed only on template
                // root
                getStartEvent().getTemplateProperties().put(
                        elementAttributes.getLocalName(i),
                        this.parsingContext.getStringTemplateParser().compileExpr(elementAttributes.getValue(i), null,
                                locator));
                elementAttributes.removeAttribute(i--);
            }
        }
        StartElement startElement = new StartElement(this.parsingContext, locator, namespaceURI, localName, qname,
                elementAttributes);
        InstructionFactory instructionFactory = this.parsingContext.getInstructionFactory();
        if (instructionFactory.isInstruction(startElement))
            newEvent = instructionFactory.createInstruction(this.parsingContext, startElement, attrs, stack);
        else
            newEvent = startElement;
        stack.push(newEvent);
        addEvent(newEvent);
    }

    public void startPrefixMapping(String prefix, String uri)
            throws SAXException {
        addEvent(new StartPrefixMapping(locator, prefix, uri));
    }

    public void comment(char ch[], int start, int length) throws SAXException {
        // DO NOTHING
    }

    public void endCDATA() throws SAXException {
        addEvent(new EndCDATA(locator));
    }

    public void endDTD() throws SAXException {
        addEvent(new EndDTD(locator));
    }

    public void endEntity(String name) throws SAXException {
        addEvent(new EndEntity(locator, name));
    }

    public void startCDATA() throws SAXException {
        addEvent(new StartCDATA(locator));
    }

    public void startDTD(String name, String publicId, String systemId)
            throws SAXException {
        addEvent(new StartDTD(locator, name, publicId, systemId));
    }

    public void startEntity(String name) throws SAXException {
        addEvent(new StartEntity(locator, name));
    }
}
