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

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Stack;

import org.apache.cocoon.template.jxtg.JXTemplateGenerator;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.script.event.*;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jxpath.JXPathContext;
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

    final static String TEMPLATE = "template";
    final static String FOR_EACH = "forEach";
    final static String IF = "if";
    final static String CHOOSE = "choose";
    final static String WHEN = "when";
    final static String OTHERWISE = "otherwise";
    final static String OUT = "out";
    final static String IMPORT = "import";
    final static String SET = "set";
    final static String MACRO = "macro";
    final static String EVALBODY = "evalBody";
    final static String EVAL = "eval";
    final static String PARAMETER = "parameter";
    final static String FORMAT_NUMBER = "formatNumber";
    final static String FORMAT_DATE = "formatDate";
    final static String COMMENT = "comment";

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
                    : JXTemplateGenerator.NULL_LOCATOR;
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
        } else {
            StartElement startElement = (StartElement) start;
            newEvent = new EndElement(locator, startElement);
            startElement.setEndElement((EndElement) newEvent);
        }
        addEvent(newEvent);
        if (start instanceof StartDefine) {
            StartDefine startDefine = (StartDefine) start;
            startDefine.finish();
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
                        compileExpr(elementAttributes.getValue(i), null,
                                locator));
                elementAttributes.removeAttribute(i--);
            }
        }
        StartElement startElement = new StartElement(locator, namespaceURI,
                localName, qname, elementAttributes);
        if (JXTemplateGenerator.NS.equals(namespaceURI)) {
            if (localName.equals(FOR_EACH)) {
                newEvent = new StartForEach(startElement, attrs, stack);
            } else if (localName.equals(FORMAT_NUMBER)) {
                newEvent = new StartFormatNumber(startElement, attrs, stack);
            } else if (localName.equals(FORMAT_DATE)) {
                newEvent = new StartFormatDate(startElement, attrs, stack);
            } else if (localName.equals(CHOOSE)) {
                newEvent = new StartChoose(startElement, attrs, stack);
            } else if (localName.equals(WHEN)) {
                newEvent = new StartWhen(startElement, attrs, stack);
            } else if (localName.equals(OUT)) {
                newEvent = new StartOut(startElement, attrs, stack);
            } else if (localName.equals(OTHERWISE)) {
                newEvent = new StartOtherwise(startElement, attrs, stack);
            } else if (localName.equals(IF)) {
                newEvent = new StartIf(startElement, attrs, stack);
            } else if (localName.equals(MACRO)) {
                newEvent = new StartDefine(startElement, attrs, stack);
            } else if (localName.equals(PARAMETER)) {
                newEvent = new StartParameter(startElement, attrs, stack);
            } else if (localName.equals(EVALBODY)) {
                newEvent = new StartEvalBody(startElement, attrs, stack);
            } else if (localName.equals(EVAL)) {
                newEvent = new StartEval(startElement, attrs, stack);
            } else if (localName.equals(SET)) {
                newEvent = new StartSet(startElement, attrs, stack);
            } else if (localName.equals(IMPORT)) {
                newEvent = new StartImport(startElement, attrs, stack);
            } else if (localName.equals(TEMPLATE)) {
                newEvent = new StartTemplate(startElement, attrs, stack);
            } else if (localName.equals(COMMENT)) {
                newEvent = new StartComment(startElement, attrs, stack);
            } else {
                throw new SAXParseException("unrecognized tag: " + localName, locator, null);
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
        //DO NOTHING
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

    public static JXTExpression compile(final String variable, boolean xpath)
            throws Exception {
        Object compiled;
        if (xpath) {
            compiled = JXPathContext.compile(variable);
        } else {
            compiled = ExpressionFactory.createExpression(variable);
        }
        return new JXTExpression(variable, compiled);
    }

    public static JXTExpression compileBoolean(String val, String msg,
            Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res != null) {
            if (res.getCompiledExpression() == null) {
                res.setCompiledExpression(Boolean.valueOf(res.getRaw()));
            }
            return res;
        }
        return null;
    }

    /*
     * Compile an integer expression (returns either a Compiled Expression or an
     * Integer literal)
     */
    public static JXTExpression compileInt(String val, String msg,
            Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res != null) {
            if (res.getCompiledExpression() == null) {
                res.setCompiledExpression(Integer.valueOf(res.getRaw()));
            }
            return res;
        }
        return null;
    }

    public static JXTExpression compileExpr(String inStr) throws Exception {
        try {
            if (inStr == null) {
                return null;
            }
            StringReader in = new StringReader(inStr.trim());
            int ch;
            boolean xpath = false;
            boolean inExpr = false;
            StringBuffer expr = new StringBuffer();
            while ((ch = in.read()) != -1) {
                char c = (char) ch;
                if (inExpr) {
                    if (c == '\\') {
                        ch = in.read();
                        expr.append((ch == -1) ? '\\' : (char) ch);
                    } else if (c == '}') {
                        return compile(expr.toString(), xpath);
                    } else {
                        expr.append(c);
                    }
                } else {
                    if (c == '$' || c == '#') {
                        ch = in.read();
                        if (ch == '{') {
                            inExpr = true;
                            xpath = c == '#';
                            continue;
                        }
                    }
                    // hack: invalid expression?
                    // just return the original and swallow exception
                    return new JXTExpression(inStr, null);
                }
            }
            if (inExpr) {
                // unclosed #{} or ${}
                throw new Exception("Unterminated " + (xpath ? "#" : "$") + "{");
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return new JXTExpression(inStr, null);
    }

    /**
     * Compile a single Jexl expr (contained in ${}) or XPath expression
     * (contained in #{})
     */

    public static JXTExpression compileExpr(String expr, String errorPrefix,
            Locator location) throws SAXParseException {
        try {
            return compileExpr(expr);
        } catch (Exception exc) {
            throw new SAXParseException(errorPrefix + exc.getMessage(),
                    location, exc);
        } catch (Error err) {
            throw new SAXParseException(errorPrefix + err.getMessage(),
                    location, new ErrorHolder(err));
        }
    }
}
