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
package org.apache.cocoon.template.jxtg.script;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.apache.cocoon.template.jxtg.JXTemplateGenerator;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.instruction.*;
import org.apache.cocoon.template.jxtg.script.event.*;
import org.apache.commons.lang.StringUtils;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

public class Parser implements ContentHandler, LexicalHandler {

    StartDocument startEvent;
    Event lastEvent;
    Stack stack = new Stack();
    Locator locator;
    Locator charLocation;
    StringBuffer charBuf;

    final static Map instructions = new HashMap();
    final static Class[] parametersClasses = new Class[] { StartElement.class,
            Attributes.class, Stack.class };

    public static final Locator NULL_LOCATOR = new LocatorImpl();

    static {
        try {
            registerInstruction("template", StartTemplate.class.getName());
            registerInstruction("forEach", StartForEach.class.getName());
            registerInstruction("if", StartIf.class.getName());
            registerInstruction("choose", StartChoose.class.getName());
            registerInstruction("when", StartWhen.class.getName());
            registerInstruction("otherwise", StartOtherwise.class.getName());
            registerInstruction("out", StartOut.class.getName());
            registerInstruction("import", StartImport.class.getName());
            registerInstruction("set", StartSet.class.getName());
            registerInstruction("macro", StartDefine.class.getName());
            registerInstruction("evalBody", StartEvalBody.class.getName());
            registerInstruction("eval", StartEval.class.getName());
            registerInstruction("parameter", StartParameter.class.getName());
            registerInstruction("formatNumber", StartFormatNumber.class
                    .getName());
            registerInstruction("formatDate", StartFormatDate.class.getName());
            registerInstruction("comment", StartComment.class.getName());
        } catch (Exception e) {
            // we'll do something more professional with that when the configuration moves
            // to the sitemap
            e.printStackTrace();
        }
    }

    public static void registerInstruction(String instructionName,
            String className) throws ClassNotFoundException, SecurityException,
            NoSuchMethodException {
        Class clazz = Class.forName(className);
        Constructor constructor = clazz.getConstructor(parametersClasses);
        instructions.put(instructionName, constructor);
    }

    public Parser() {
        // EMPTY
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
            Characters ev = new Characters(charLocation, chars, 0, chars.length);
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
        if (JXTemplateGenerator.NS.equals(namespaceURI)) {
            StartInstruction startInstruction = (StartInstruction) start;
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
        Event ev = new IgnorableWhitespace(locator, ch, start, length);
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
                getStartEvent().getTemplateProperties().put(
                        elementAttributes.getLocalName(i),
                        JXTExpression.compileExpr(
                                elementAttributes.getValue(i), null, locator));
                elementAttributes.removeAttribute(i--);
            }
        }
        StartElement startElement = new StartElement(locator, namespaceURI,
                localName, qname, elementAttributes);
        if (JXTemplateGenerator.NS.equals(namespaceURI)) {
            Constructor constructor = (Constructor) instructions.get(localName);
            if (constructor == null) {
                throw new SAXParseException("unrecognized tag: " + localName,
                        locator, null);
            }

            Object[] arguments = new Object[] { startElement, attrs, stack };
            try {
                newEvent = (Event) constructor.newInstance(arguments);
            } catch (Exception e) {
                throw new SAXParseException("error creating instruction: "
                        + localName, locator, e);
            }
        } else {
            newEvent = startElement;
        }
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
