/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-

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
package org.apache.cocoon.generation;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.xml.*;
import java.io.*;
import java.util.*;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;
import org.apache.commons.jxpath.*;

/**
 *
 * <p>Cocoon {@link Generator} that produces dynamic XML SAX events
 *  fom an XML template file.</p>
 *
 *  <p>Provides a tag library with embedded XPath expression substitution
 *  to access data sent by Cocoon flowscripts.</p>
 *  The embedded expression language allows a page author to access an 
 *  object using a simplified syntax such as
 *  <p><pre>
 *  &lt;site signOn="{accountForm/signOn}"&gt;
 *  </pre></p>
 * <p>Embedded XPath expressions are contained in curly braces.</p>
 * <p>Note that since this generator uses <a href="http://jakarta.apache.org/commons/jxpath">Apache JXPath</a>, the referenced 
 * objects may be Java Beans, DOM, JDOM, or JavaScript objects from a 
 * Flowscript. The current Web Continuation from the Flowscript 
 * is also available as an XPath variable named <code>continuation</code>. You would 
 * typically access its id:
 * <p><pre>
 *    &lt;form action="{$continuation/id}"&gt;
 * </pre></p>
 * <p>You can also reach previous continuations by using the <code>getContinuation()</code> function:</p>
 * <p><pre>
 *     &lt;form action="{getContinuation($continuation, 1)}" >
 * </pre></p>
 * <p>The <code>if</code> tag allows the conditional execution of its body 
 * according to value of a <code>test</code> attribute:</p>
 * <p><pre>
 *   &lt;if test="XPathExpression"&gt;
 *       body
 *   &lt;/if&gt;
 * </pre></p>
 * <p>The <code>choose</code> tag performs conditional block execution by the 
 * embedded <code>when</code> sub tags. It renders the body of the first 
 * <code>when</code> tag whose <code>test</code> condition evaluates to true. 
 * If none of the <code>test</code> conditions of nested <code>when</code> tags
 * evaluate to <code>true</code>, then the body of an <code>otherwise</code> 
 * tag is evaluated, if present:</p>
 * <p><pre>
 *  &lt;choose&gt;
 *    &lt;when test="XPathExpression"&gt;
 *       body
 *    &lt;/when&gt;
 *    &lt;otherwise&gt;
 *       body
 *    &lt;/otherwise&gt;
 *  &lt;/choose&gt;
 * </pre></p>
 * <p>The <code>value-of</code> tag evaluates an XPath expression and outputs 
 * the result of the evaluation:</p>
 * <p><pre>
 * &lt;value-of select="XPathExpression"/&gt;
 * </pre></p>
 * <p>The <code>for-each</code> tag allows you to iterate over a collection 
 * of objects:<p>
 * <p><pre>
 *   &lt;for-each select="XPathExpression"&gt;
 *     body
 *  &lt;/for-each&gt;
 * </pre></p>
 *
 *
 */

public class JXPathTemplate extends AbstractGenerator {

    private static final JXPathContextFactory 
        jxpathContextFactory = JXPathContextFactory.newInstance();

    static class MyVariables implements Variables {

        Map localVariables = new HashMap();

        static final String[] VARIABLES = new String[] {
            "continuation",
            "flowContext",
            "request",
            "response",
            "context",
            "session",
            "parameters"
        };

        Object bean, kont, request, response,
            session, context, parameters;

        MyVariables(Object bean, WebContinuation kont,
                    Request request, Response response,
                    org.apache.cocoon.environment.Context context,
                    Parameters parameters) {
            this.bean = bean;
            this.kont = kont;
            this.request = request;
            this.session = request.getSession(false);
            this.response = response;
            this.context = context;
            this.parameters = parameters;
        }

        public boolean isDeclaredVariable(String varName) {
            for (int i = 0; i < VARIABLES.length; i++) {
                if (varName.equals(VARIABLES[i])) {
                    return true;
                }
            }
            return localVariables.containsKey(varName);
        }
        
        public Object getVariable(String varName) {
            if (varName.equals("continuation")) {
                return kont;
            } else if (varName.equals("flowContext")) {
                return bean;
            } else if (varName.equals("request")) {
                return request;
            } else if (varName.equals("response")) {
                return response;
            } else if (varName.equals("session")) {
                return session;
            } else if (varName.equals("context")) {
                return context;
            } else if (varName.equals("parameters")) {
                return parameters;
            }
            return localVariables.get(varName);
        }
        
        public void declareVariable(String varName, Object value) {
            localVariables.put(varName, value);
        }
        
        public void undeclareVariable(String varName) {
            localVariables.remove(varName);
        }
    }


    final static String JXPATH_NS = 
        "http://cocoon.apache.org/transformation/jxpath/1.0";

    final static String TEMPLATE = "template";
    final static String FOR_EACH = "for-each";
    final static String IF = "if";
    final static String CHOOSE = "choose";
    final static String WHEN = "when";
    final static String OTHERWISE = "otherwise";
    final static String VALUE_OF = "value-of";
    final static String IMPORT = "import";
    final static String DEFINE = "define";


    // get XPath expression (optionally contained in #{})
    private String getExpr(String inStr) {
        try {
            inStr = inStr.trim();
            if (inStr.length() < 2 || (inStr.charAt(0) != '#' ||
                                       inStr.charAt(1) != '{')) {
                return inStr;
            }
            StringReader in = new StringReader(inStr);
            int ch;
            StringBuffer expr = new StringBuffer();
            in.read(); // '#'
            in.read(); // '{'
            while ((ch = in.read()) != -1) {
                char c = (char)ch;
                if (c == '}') {
                    break;
                } else if (c == '\\') {
                    ch = in.read();
                    if (ch == -1) {
                        expr.append('\\');
                    } else {
                        expr.append((char)ch);
                    }
                } else {
                    expr.append(c);
                }
            } 
            return expr.toString();
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return inStr;
    }

    class Event {
        final Locator location;
        Event next;
        Event(Locator location) {
            this.location = new LocatorImpl(location);
        }

        public String locationString() {
            String result = "";
            String systemId = location.getSystemId();
            if (systemId != null) {
                result += systemId + ", ";
            }
            result += "Line " + location.getLineNumber();
            int col = location.getColumnNumber();
            if (col > 0) {
                result += "." + col;
            }
            return result;
        }
    }

    class TextEvent extends Event {
        TextEvent(Locator location, 
                  char[] chars, int start, int length) 
            throws SAXException {
            super(location);
            StringBuffer buf = new StringBuffer();
            CharArrayReader in = new CharArrayReader(chars, start, length);
            int ch;
            boolean inExpr = false;
            try {
                while ((ch = in.read()) != -1) {
                    char c = (char)ch;
                    if (inExpr) {
                        if (c == '}') {
                            String str = buf.toString();
                            CompiledExpression compiledExpression;
                            try {
                                compiledExpression = 
                                    JXPathContext.compile(str);
                            } catch (JXPathException exc) {
                                throw new SAXParseException(exc.getMessage(),
                                                            location,
                                                            exc);
                            }
                            substitutions.add(compiledExpression);
                            buf.setLength(0);
                            inExpr = false;
                        } else if (c == '\\') {
                            ch = in.read();
                            if (ch == -1) {
                                buf.append('\\');
                            } else {
                                buf.append((char)ch);
                            } 
                        } else {
                            buf.append(c);
                        }
                    } else {
                        if (c == '\\') {
                            ch = in.read();
                            if (ch == -1) {
                                buf.append('\\');
                            } else {
                                buf.append((char)ch);
                            }
                        } else {
                            if (c == '#') {
                                ch = in.read();
                                if (ch == '{') {
                                    if (buf.length() > 0) {
                                        char[] charArray = 
                                            new char[buf.length()];
                                        
                                        buf.getChars(0, buf.length(),
                                                     charArray, 0);
                                        substitutions.add(charArray);
                                        buf.setLength(0);
                                    }
                                    inExpr = true;
                                    continue;
                                }
                                buf.append('#');
                            }
                            if (ch != -1) {
                                buf.append((char)ch);
                            }
                        }
                    }
                }
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
            if (buf.length() > 0) {
                char[] charArray = 
                    new char[buf.length()];
                buf.getChars(0, buf.length(),
                             charArray, 0);
                substitutions.add(charArray);
            } else if (substitutions.size() == 0) {
                substitutions.add(EMPTY_CHARS);
            }
        }
        final List substitutions = new LinkedList();
    }

    class Characters extends TextEvent {
        Characters(Locator location, 
                  char[] chars, int start, int length) 
            throws SAXException {
            super(location, chars, start, length);
        }

    }

    class StartDocument extends Event {
        StartDocument(Locator location) {
            super(location);
        }
        long compileTime;
        HashMap definitions = new HashMap();
        EndDocument endDocument; // null if document fragment
    }

    class EndDocument extends Event {
        EndDocument(Locator location) {
            super(location);
        }
    }

    class EndElement extends Event {
        EndElement(Locator location, 
                   StartElement startElement) {
            super(location);
            this.startElement = startElement;
        }
        final StartElement startElement;
    }

    class EndPrefixMapping extends Event {
        EndPrefixMapping(Locator location, String prefix) {
            super(location);
            this.prefix = prefix;
        }
        final String prefix;
    }
    
    class IgnorableWhitespace extends TextEvent {
        IgnorableWhitespace(Locator location, 
                            char[] chars, int start, int length) 
            throws SAXException {
            super(location, chars, start, length);
        }
    }

    class ProcessingInstruction extends Event {
        ProcessingInstruction(Locator location,
                              String target, String data) {
            super(location);
            this.target = target;
            this.data = data;
        }
        final String target;
        final String data;
    }

    class SkippedEntity extends Event {
        SkippedEntity(Locator location, String name) {
            super(location);
            this.name = name;
        }
        final String name;
    }

    abstract class AttributeEvent {
        AttributeEvent(String namespaceURI, String localName, String raw,
                       String type) {
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.raw = raw;
            this.type = type;
        }
        final String namespaceURI;
        final String localName;
        final String raw;
        final String type;
    }
    
    class CopyAttribute extends AttributeEvent {
        CopyAttribute(String namespaceURI, 
                      String localName,
                      String raw,
                      String type, String value) {
            super(namespaceURI, localName, raw, type);
            this.value = value;
        }
        final String value;
    }
    
    class Subst {
    }
    
    class Literal extends Subst {
        Literal(String val) {
            this.value = val;
        }
        final String value;
    }
    
    class Expression extends Subst {
        Expression(CompiledExpression expr) {
            this.compiledExpression = expr;
        }
        final CompiledExpression compiledExpression;
    }

    class SubstituteAttribute extends AttributeEvent {
        SubstituteAttribute(String namespaceURI,
                            String localName,
                            String raw,
                            String type, List substs) {
            super(namespaceURI, localName, raw, type);
            this.substitutions = substs;
        }
        final List substitutions;
    }

    class StartElement extends Event {
        StartElement(Locator location, String namespaceURI,
                     String localName, String raw,
                     Attributes attrs) 
            throws SAXException {
            super(location);
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.raw = raw;
            this.qname = "{"+namespaceURI+"}"+localName;
            StringBuffer buf = new StringBuffer();
            for (int i = 0, len = attrs.getLength(); i < len; i++) {
                String uri = attrs.getURI(i);
                String local = attrs.getLocalName(i);
                String qname = attrs.getQName(i);
                String type = attrs.getType(i);
                String value = attrs.getValue(i);
                StringReader in = new StringReader(value);
                int ch;
                buf.setLength(0);
                boolean inExpr = false;
                List substEvents = new LinkedList();
                try {
                    while ((ch = in.read()) != -1) {
                        char c = (char)ch;
                        if (inExpr) {
                            if (c == '}') {
                                String str = buf.toString();
                                CompiledExpression compiledExpression;
                                try {
                                    compiledExpression =
                                        JXPathContext.compile(str);
                                } catch (JXPathException exc) {
                                    throw new SAXParseException(exc.getMessage(),
                                                                location,
                                                                exc);
                                                                
                                } 
                                substEvents.add(new Expression(compiledExpression));
                                buf.setLength(0);
                                inExpr = false;
                            } else if (c == '\\') {
                                ch = in.read();
                                if (ch == -1) {
                                    buf.append('\\');
                                } else {
                                    buf.append((char)ch);
                                }
                            } else {
                                buf.append(c);
                            }
                        } else {
                            if (c == '\\') {
                                ch = in.read();
                                if (ch == -1) {
                                    buf.append('\\');
                                } else {
                                    buf.append((char)ch);
                                }
                            } else {
                                if (c == '#') {
                                    ch = in.read();
                                    if (ch == '{') {
                                        if (buf.length() > 0) {
                                            substEvents.add(new Literal(buf.toString()));
                                            buf.setLength(0);
                                        }
                                        inExpr = true;
                                        continue;
                                    }
                                    buf.append('#');
                                }
                                if (ch != -1) {
                                    buf.append((char)ch);
                                }
                            }
                        }
                    }
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
                if (buf.length() > 0) {
                    if (substEvents.size() == 0) {
                        attributeEvents.add(new CopyAttribute(uri,
                                                              local,
                                                              qname,
                                                              type,
                                                              value));
                    } else {
                        substEvents.add(new Literal(buf.toString()));
                        attributeEvents.add(new SubstituteAttribute(uri,
                                                                    local,
                                                                    qname,
                                                                    type,
                                                                    substEvents));
                    }
                } else {
                    if (substEvents.size() > 0) {
                        attributeEvents.add(new SubstituteAttribute(uri,
                                                                    local,
                                                                    qname,
                                                                    type,
                                                                    substEvents));
                    } else {
                        attributeEvents.add(new CopyAttribute(uri, local,
                                                              qname, type,
                                                               ""));
                    }
                }
            }
        }
        final String namespaceURI;
        final String localName;
        final String raw;
        final String qname;
        final List attributeEvents = new LinkedList();
        EndElement endElement;
    }

    class StartForEach extends Event {
        StartForEach(Locator location, CompiledExpression select) {
            super(location);
            this.select = select;
        }
        CompiledExpression select;
        EndForEach endForEach;
    }
    
    class EndForEach extends Event {
        EndForEach(Locator location) {
            super(location);
        }
    }

    class StartIf extends Event {
        StartIf(Locator location, CompiledExpression test) {
            super(location);
            this.test = test;
        }
        final CompiledExpression test;
        EndIf endIf;
    }

    class EndIf extends Event {
        EndIf(Locator location) {
            super(location);
        }
    }

    class StartChoose extends Event {
        StartChoose(Locator location) {
            super(location);
        }
        StartWhen firstChoice;
        StartOtherwise otherwise;
        EndChoose endChoose;
    }

    class EndChoose extends Event {
        EndChoose(Locator location) {
            super(location);
        }
    }

    class StartWhen extends Event {
        StartWhen(Locator location, CompiledExpression test) {
            super(location);
            this.test = test;
        }
        final CompiledExpression test;
        StartWhen nextChoice;
        EndWhen endWhen;
    }

    class EndWhen extends Event {
        EndWhen(Locator location) {
            super(location);
        }
    }

    class StartOtherwise extends Event {
        StartOtherwise(Locator location) {
            super(location);
        }
        EndOtherwise endOtherwise;
    }

    class EndOtherwise extends Event {
        EndOtherwise(Locator location) {
            super(location);
        }
    }

    class StartPrefixMapping extends Event {
        StartPrefixMapping(Locator location, String prefix,
                           String uri) {
            super(location);
            this.prefix = prefix;
            this.uri = uri;
        }
        final String prefix;
        final String uri;
    }

    class Comment extends TextEvent {
        Comment(Locator location, char[] chars,
                int start, int length)
            throws SAXException {
            super(location, chars, start, length);
        }
    }

    class EndCDATA extends Event {
        EndCDATA(Locator location) {
            super(location);
        }
    }

    class EndDTD extends Event {
        EndDTD(Locator location) {
            super(location);
        }
    }

    class EndEntity extends Event {
        EndEntity(Locator location, String name) {
            super(location);
            this.name = name;
        }
        final String name;
    }

    class StartCDATA extends Event {
        StartCDATA(Locator location) {
            super(location);
        }
    }

    class StartDTD extends Event {
        StartDTD(Locator location, String name, 
                 String publicId, String systemId) {
            super(location);
            this.name = name;
            this.publicId = publicId;
            this.systemId = systemId;
        }
        final String name;
        final String publicId;
        final String systemId;
    }
    
    class StartEntity extends Event {
        public StartEntity(Locator location, String name) {
            super(location);
            this.name = name;
        }
        final String name;
    }

    class StartValueOf extends Event {
        StartValueOf(Locator location, CompiledExpression expr) {
            super(location);
            this.compiledExpression = expr;
        }
        final CompiledExpression compiledExpression;
    }

    class EndValueOf extends Event {
        EndValueOf(Locator location) {
            super(location);
        }
    }

    class StartImport extends Event {
        StartImport(Locator location, AttributeEvent uri, 
                    CompiledExpression select) {
            super(location);
            this.uri = uri;
            this.select = select;
        }
        final AttributeEvent uri;
        final CompiledExpression select;
        EndImport endImport;
    }

    class EndImport extends Event {
        EndImport(Locator location) {
            super(location);
        }
    }

    class StartTemplate extends Event {
        StartTemplate(Locator location) {
            super(location);
        }
        EndTemplate endTemplate;
    }

    class EndTemplate extends Event {
        EndTemplate(Locator location) {
            super(location);
        }
    }

    class StartDefine extends Event {
        StartDefine(Locator location, String namespace, String name,
                    Map formalParameters) {
            super(location);
            this.namespace = namespace;
            this.name = name;
            this.qname = "{"+namespace+"}"+name;
            this.parameters = formalParameters;
        }
        final String namespace;
        final String name;
        final String qname;
        final Map parameters;
        EndDefine endDefine;
    }

    class EndDefine extends Event {
        EndDefine(Locator location) {
            super(location);
        }
    }

    class Parser implements LexicalHandler, ContentHandler {

        StartDocument startEvent;
        Event lastEvent;
        Stack stack = new Stack();
        Locator locator;

        StartDocument getStartEvent() {
            return startEvent;
        }
        
        private void addEvent(Event ev) {
            if (ev == null) {
                throw new NullPointerException("null event");
            }
            if (lastEvent == null) {
                lastEvent = startEvent = new StartDocument(locator);
            }
            lastEvent.next = ev;
            lastEvent = ev;
        }

        public void characters(char[] ch, int start, int length) 
            throws SAXException {
            Characters chars = new Characters(locator,
                                              ch, start, length);
            addEvent(chars);
        }

        public void endDocument() {
            StartDocument startDoc = (StartDocument)stack.pop();
            EndDocument endDoc = new EndDocument(locator);
            startDoc.endDocument = endDoc;
            addEvent(endDoc);
        }

        public void endElement(String namespaceURI,
                               String localName,
                               String raw) 
            throws SAXException {
            Event start = (Event)stack.pop();
            Event newEvent = null;
            if (JXPATH_NS.equals(namespaceURI)) {
                if (start instanceof StartForEach) {
                    StartForEach startForEach = 
                        (StartForEach)start;
                    newEvent = startForEach.endForEach = 
                        new EndForEach(locator);
                    
                } else if (start instanceof StartIf) {
                    StartIf startIf = (StartIf)start;
                    newEvent = startIf.endIf = 
                        new EndIf(locator);
                } else if (start instanceof StartWhen) {
                    StartWhen startWhen = (StartWhen)start;
                    StartChoose startChoose = (StartChoose)stack.peek();
                    if (startChoose.firstChoice != null) {
                        StartWhen w = startChoose.firstChoice;
                        while (w.nextChoice != null) {
                            w = w.nextChoice;
                        }
                        w.nextChoice = startWhen;
                    } else {
                        startChoose.firstChoice = startWhen;
                    }
                    newEvent = startWhen.endWhen = 
                        new EndWhen(locator);
                } else if (start instanceof StartOtherwise) {
                    StartOtherwise startOtherwise = 
                        (StartOtherwise)start;
                    StartChoose startChoose = (StartChoose)stack.peek();
                    newEvent = startOtherwise.endOtherwise = 
                        new EndOtherwise(locator);
                    startChoose.otherwise = startOtherwise;
                } else if (start instanceof StartValueOf) {
                    newEvent = new EndValueOf(locator);
                } else if (start instanceof StartChoose) {
                    StartChoose startChoose = (StartChoose)start;
                    newEvent = 
                        startChoose.endChoose = new EndChoose(locator);
                } else if (start instanceof StartImport) {
                    StartImport startImport = (StartImport)start;
                    newEvent = 
                        startImport.endImport = new EndImport(locator);
                } else if (start instanceof StartTemplate) {
                    StartTemplate startTemplate = (StartTemplate)start;
                    newEvent =
                        startTemplate.endTemplate = new EndTemplate(locator);
                } else if (start instanceof StartDefine) {
                    StartDefine startDefine = (StartDefine)start;
                    newEvent = 
                        startDefine.endDefine = new EndDefine(locator);
                    startEvent.definitions.put(startDefine.qname, startDefine);
                } else {
                    throw new SAXParseException("unrecognized tag: " + localName, locator, null);
                }
            } else {
                StartElement startElement = (StartElement)start;
                newEvent = startElement.endElement = 
                    new EndElement(locator, startElement);
            }
            addEvent(newEvent);
        }
        
        public void endPrefixMapping(String prefix) {
            EndPrefixMapping endPrefixMapping = 
                new EndPrefixMapping(locator, prefix);
            addEvent(endPrefixMapping);
        }

        public void ignorableWhitespace(char[] ch, int start, int length) 
            throws SAXException {
            Event ev = new IgnorableWhitespace(locator, ch, start, length);
            addEvent(ev);
        }

        public void processingInstruction(String target, String data) {
            Event pi = new ProcessingInstruction(locator, target, data);
            addEvent(pi);
        }

        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        public void skippedEntity(String name) {
            addEvent(new SkippedEntity(locator, name));
        }


        public void startDocument() {
            startEvent = new StartDocument(locator);
            lastEvent = startEvent;
            stack.push(lastEvent);
        }

        public void startElement(String namespaceURI,
                                 String localName,
                                 String raw,
                                 Attributes attrs) 
            throws SAXException {
            Event newEvent = null;
            if (JXPATH_NS.equals(namespaceURI)) {
                if (localName.equals(FOR_EACH)) {
                    String select = attrs.getValue("select");
                    if (select == null) {
                        throw 
                            new SAXParseException("for-each: \"select\" is required", 
                                                  locator, null);
                    }
                    CompiledExpression expr;
                    try {
                        expr = JXPathContext.compile(getExpr(select));
                    } catch (JXPathException exc) {
                        throw new SAXParseException(exc.getMessage(),
                                                    locator, exc);
                    }
                    StartForEach startForEach = 
                        new StartForEach(locator, expr);
                    newEvent = startForEach;
                } else if (localName.equals(CHOOSE)) {
                    StartChoose startChoose = new StartChoose(locator);
                    newEvent = startChoose;
                } else if (localName.equals(WHEN)) {
                    if (!(stack.peek() instanceof StartChoose)) {
                        throw new SAXParseException("<when> must be within <choose>", locator, null);
                    }
                    String test = attrs.getValue("test");
                    if (test == null) {
                        throw new SAXParseException("when: \"test\" is required", locator, null);
                    }
                    CompiledExpression expr;
                    try {
                        expr = JXPathContext.compile(getExpr(test));
                    } catch (JXPathException e) {
                        throw new SAXParseException("when: \"test\": " + e.getMessage(), locator, null);
                    }
                    StartWhen startWhen = new StartWhen(locator, expr);
                    newEvent = startWhen;
                } else if (localName.equals(VALUE_OF)) {
                    String select = attrs.getValue("select");
                    if (select == null) {
                        throw new SAXParseException("value-of: \"select\" is required", locator, null);
                    }
                    CompiledExpression expr;
                    try {
                        expr = JXPathContext.compile(getExpr(select));
                    } catch (Exception e) {
                        throw new SAXParseException("value-of: \"select\": " + e.getMessage(), locator, null);
                    }
                    newEvent = new StartValueOf(locator, expr);
                } else if (localName.equals(OTHERWISE)) {
                    if (!(stack.peek() instanceof StartChoose)) {
                        throw new SAXParseException("<otherwise> must be within <choose>", locator, null);
                    }
                    StartOtherwise startOtherwise = 
                        new StartOtherwise(locator);
                    newEvent = startOtherwise;
                } else if (localName.equals(IF)) {
                    String test = attrs.getValue("test");
                    if (test == null) {
                        throw new SAXParseException("if: \"test\" is required", locator, null);
                    }
                    CompiledExpression expr;
                    try {
                        expr = 
                            JXPathContext.compile(getExpr(test));
                    } catch (JXPathException e) {
                        throw new SAXParseException("if: \"test\": " + e.getMessage(), locator, null);
                    }
                    StartIf startIf = 
                        new StartIf(locator, expr);
                    newEvent = startIf;
                } else if (localName.equals(DEFINE)) {
                    // <define namespace="a" name="b">
                    // body
                    // </define>
                    String namespace = attrs.getValue(JXPATH_NS, "namespace");
                    if (namespace == null) {
                        namespace = "";
                    }
                    String name = attrs.getValue(JXPATH_NS, "name");
                    if (name == null) {
                        throw new SAXParseException("define: \"name\" is required", locator, null);
                    }
                    Map formalParams = new HashMap();
                    for (int i = 0, len = attrs.getLength(); i < len; i++) {
                        String uri = attrs.getURI(i);
                        if (uri.equals("")) {
                            String n = attrs.getLocalName(i);
                            String v = attrs.getValue(i);
                            if (v == null) v = "";
                            formalParams.put(n, v);
                        }
                    }
                    StartDefine startDefine = 
                        new StartDefine(locator, namespace, name, 
                                        formalParams);
                    newEvent = startDefine;
                } else if (localName.equals(IMPORT)) {
                    // <import uri="{root}/foo/bar.xml" select="{.}"/>
                    // Allow expression substituion in "uri" attribute
                    AttributeEvent uri = null;
                    StartElement startElement = 
                        new StartElement(locator, namespaceURI,
                                         localName, raw, attrs);
                    Iterator iter = startElement.attributeEvents.iterator();
                    while (iter.hasNext()) {
                        AttributeEvent e = (AttributeEvent)iter.next();
                        if (e.localName.equals("uri")) {
                            uri = e;
                            break;
                        }
                    }
                    if (uri == null) {
                        throw new SAXParseException("import: \"uri\" is required", locator, null);
                    }
                    // If "select" is present then its value will be used
                    // as the context object in the imported template
                    String select = attrs.getValue("select");
                    CompiledExpression expr = null;
                    if (select != null) {
                        try {
                            expr = JXPathContext.compile(getExpr(select));
                        } catch (Exception e) {
                            throw new SAXParseException("value-of: \"select\": " + e.getMessage(), locator, null);
                        }
                    }
                    StartImport startImport = 
                        new StartImport(locator, uri, expr);
                    newEvent = startImport;
                } else if (localName.equals(TEMPLATE)) {
                    StartTemplate startTemplate =
                        new StartTemplate(locator);
                    newEvent = startTemplate;
                } else {
                    throw new SAXParseException("unrecognized tag: " + localName, locator, null);
                }
            } else {
                StartElement startElem = 
                    new StartElement(locator, namespaceURI,
                                     localName, raw, attrs);
                newEvent = startElem;
            }
            stack.push(newEvent);
            addEvent(newEvent);
        }
        
        public void startPrefixMapping(String prefix, String uri) {
            addEvent(new StartPrefixMapping(locator, prefix, uri));
        }

        public void comment(char ch[], int start, int length) 
            throws SAXException {
            addEvent(new Comment(locator, ch, start, length));
        }

        public void endCDATA() {
            addEvent(new EndCDATA(locator));
        }

        public void endDTD() {
            addEvent(new EndDTD(locator));
        }

        public void endEntity(String name) {
            addEvent(new EndEntity(locator, name));
        }

        public void startCDATA() {
            addEvent(new StartCDATA(locator));
        }

        public void startDTD(String name, String publicId, String systemId) {
            addEvent(new StartDTD(locator, name, publicId, systemId));
        }
        
        public void startEntity(String name) {
            addEvent(new StartEntity(locator, name));
        }
    }

    private XMLConsumer consumer;
    private JXPathContext rootContext;
    private Variables variables;
    private static Map cache = new HashMap();
    private Source inputSource;
    private Map definitions;

    public void recycle() {
        super.recycle();
        consumer = null;
        rootContext = null;
        variables = null;
        inputSource = null;
        definitions = null;
    }

    public void setup(SourceResolver resolver, Map objectModel,
                      String src, Parameters parameters)
        throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, parameters);
        if (src != null) {
            try {
                this.inputSource = resolver.resolveURI(src);
            } catch (SourceException se) {
                throw SourceUtil.handle("Error during resolving of '" + src + "'.", se);
            }
        }
        long lastMod = inputSource.getLastModified();
        String uri = inputSource.getURI();
        synchronized (cache) {
            StartDocument startEvent = (StartDocument)cache.get(uri);
            if (startEvent != null &&
                lastMod > startEvent.compileTime) {
                cache.remove(uri);
            }
        }
        // FIX ME: When we decide proper way to pass "bean" and "kont"
        Object bean = ((Environment)resolver).getAttribute("bean-dict");
        WebContinuation kont =
            (WebContinuation)((Environment)resolver).getAttribute("kont");
        variables = new MyVariables(bean, 
                                    kont,
                                    ObjectModelHelper.getRequest(objectModel),
                                    ObjectModelHelper.getResponse(objectModel),
                                    ObjectModelHelper.getContext(objectModel),
                                    parameters);
        rootContext = jxpathContextFactory.newContext(null, bean);
        rootContext.setVariables(variables);
        definitions = new HashMap();
    }

    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    public void generate() 
        throws IOException, SAXException, ProcessingException {
        StartDocument startEvent;
        synchronized (cache) {
            startEvent = (StartDocument)cache.get(inputSource.getURI());
        }
        if (startEvent == null) {
            long compileTime = inputSource.getLastModified();
            Parser parser = new Parser();
            this.resolver.toSAX(this.inputSource, parser);
            startEvent = parser.getStartEvent();
            startEvent.compileTime = compileTime;
            synchronized (cache) {
                cache.put(inputSource.getURI(), startEvent);
            }
        }
        execute(rootContext, startEvent, null);
    }

    final static char[] EMPTY_CHARS = "".toCharArray();
    
    interface CharHandler {
        public void characters(char[] ch, int offset, int length)
            throws SAXException;
    }

    private void characters(JXPathContext context, 
                            TextEvent event,
                            CharHandler handler) throws SAXException {
        Iterator iter = event.substitutions.iterator();
        while (iter.hasNext()) {
            Object subst = iter.next();
            char[] chars;
            if (subst instanceof char[]) {
                chars = (char[])subst;
            } else {
                CompiledExpression expr = (CompiledExpression)subst;
                try {
                    Object val = expr.getValue(context);
                    if (val != null) {
                        chars = val.toString().toCharArray();
                    } else {
                        chars = EMPTY_CHARS;
                    }
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(),
                                                event.location,
                                                e);
                }
            }
            handler.characters(chars, 0, chars.length);
        }
    }

    private void execute(JXPathContext context,
                         Event startEvent, Event endEvent) 
        throws SAXException {
        Event ev = startEvent;
        while (ev != endEvent) {
            consumer.setDocumentLocator(ev.location);
            if (ev instanceof Characters) {
                TextEvent text = (TextEvent)ev;
                characters(context, text, new CharHandler() {
                        public void characters(char[] ch, int offset,
                                               int len) 
                            throws SAXException {
                            
                            consumer.characters(ch, offset, len);
                        }
                    });
            } else if (ev instanceof EndDocument) {
                consumer.endDocument();
            } else if (ev instanceof EndElement) {
                EndElement endElement = (EndElement)ev;
                StartElement startElement = 
                    (StartElement)endElement.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof EndPrefixMapping) {
                EndPrefixMapping endPrefixMapping = 
                    (EndPrefixMapping)ev;
                consumer.endPrefixMapping(endPrefixMapping.prefix);
            } else if (ev instanceof IgnorableWhitespace) {
                TextEvent text = (TextEvent)ev;
                characters(context, text, new CharHandler() {
                        public void characters(char[] ch, int offset,
                                               int len) 
                            throws SAXException {
                            consumer.ignorableWhitespace(ch, offset, len);
                        }
                    });
            } else if (ev instanceof ProcessingInstruction) {
                ProcessingInstruction pi = (ProcessingInstruction)ev;
                consumer.processingInstruction(pi.target, pi.data);
            } else if (ev instanceof SkippedEntity) {
                SkippedEntity skippedEntity = (SkippedEntity)ev;
                consumer.skippedEntity(skippedEntity.name);
            } else if (ev instanceof StartDocument) {
                StartDocument startDoc = (StartDocument)ev;
                if (startDoc.endDocument != null) {
                    // if this isn't a document fragment
                    consumer.startDocument();
                }
            } else if (ev instanceof StartIf) {
                StartIf startIf = (StartIf)ev;
                Object val;
                try {
                    val = startIf.test.getValue(context);
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(),
                                                ev.location,
                                                e);
                }
                boolean result = false;
                if (val instanceof Boolean) {
                    result = ((Boolean)val).booleanValue();
                }
                if (!result) {
                    ev = startIf.endIf.next;
                    continue;
                }
            } else if (ev instanceof StartForEach) {
                StartForEach startForEach = (StartForEach)ev;
                Iterator iter;
                try {
                    iter = 
                        startForEach.select.iteratePointers(context);
                } catch (JXPathException exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location,
                                                exc);
                }
                while (iter.hasNext()) {
                    Pointer ptr = (Pointer)iter.next();
                    Object contextObject;
                    try {
                        contextObject = ptr.getNode();
                    } catch (JXPathException exc) {
                        throw new SAXParseException(exc.getMessage(),
                                                    ev.location,
                                                    exc);
                    }
                    JXPathContext newContext = 
                        jxpathContextFactory.newContext(null, 
                                                        contextObject);
                    newContext.setVariables(variables);
                    execute(newContext,
                            startForEach.next,
                            startForEach.endForEach);
                }
                ev = startForEach.endForEach.next;
                continue;
            } else if (ev instanceof StartChoose) {
                StartChoose startChoose = (StartChoose)ev;
                StartWhen startWhen = startChoose.firstChoice; 
                for (;startWhen != null; startWhen = startWhen.nextChoice) {
                    Object val;
                    try {
                        val = startWhen.test.getValue(context);
                    } catch (JXPathException e) {
                        throw new SAXParseException(e.getMessage(),
                                                    ev.location,
                                                    e);
                    }
                    boolean result = false;
                    if (val instanceof Boolean) {
                        result = ((Boolean)val).booleanValue();
                    }
                    if (result) {
                        execute(context, startWhen.next, startWhen.endWhen);
                        break;
                    }
                }
                if (startWhen == null) {
                    if (startChoose.otherwise != null) {
                        execute(context, startChoose.otherwise.next,
                                startChoose.otherwise.endOtherwise);
                    }
                }
                ev = startChoose.endChoose.next;
                continue;
            } else if (ev instanceof StartElement) {
                StartElement startElement = (StartElement)ev;
                StartDefine def = 
                    (StartDefine)definitions.get(startElement.qname);
                if (def != null) {
                    MyVariables vars = (MyVariables)context.getVariables();
                    final Map localVariables = vars.localVariables;
                    vars.localVariables = new HashMap();
                    Iterator i = startElement.attributeEvents.iterator();
                    while (i.hasNext()) {
                        AttributeEvent attrEvent = (AttributeEvent)i.next();
                        Object defVal;
                        if (attrEvent.namespaceURI.length() == 0) {
                           defVal = def.parameters.get(attrEvent.localName);
                           if (defVal == null) continue;
                        }
                        if (attrEvent instanceof CopyAttribute) {
                            CopyAttribute copy =
                                (CopyAttribute)attrEvent;
                            vars.declareVariable(attrEvent.localName, 
                                                 copy.value);
                        } else if (attrEvent instanceof 
                                   SubstituteAttribute) {
                            SubstituteAttribute substEvent = 
                                (SubstituteAttribute)attrEvent;
                            if (substEvent.substitutions.size() == 1) {
                                Subst subst = 
                                    (Subst)substEvent.substitutions.get(0);
                                if (subst instanceof Expression) {
                                    Object val;
                                    Expression expr = (Expression)subst;
                                    try {
                                        val = 
                                            expr.compiledExpression.getValue(context);
                                    } catch (JXPathException e) {
                                        throw new SAXParseException(e.getMessage(),
                                                                    ev.location,
                                                                    e);
                                    }
                                    vars.declareVariable(attrEvent.localName,
                                                         val);
                                    continue;
                                }
                            }
                            StringBuffer buf = new StringBuffer();
                            Iterator ii = substEvent.substitutions.iterator();
                            while (ii.hasNext()) {
                                Subst subst = (Subst)ii.next();
                                if (subst instanceof Literal) {
                                    Literal lit = (Literal)subst;
                                    buf.append(lit.value);
                                } else if (subst instanceof Expression) {
                                    Expression expr = (Expression)subst;
                                    Object val;
                                    try {
                                        val = 
                                            expr.compiledExpression.getValue(context);
                                    } catch (JXPathException e) {
                                        throw new SAXParseException(e.getMessage(),
                                                                    ev.location,
                                                                    e);
                                    }
                                    if (val == null) {
                                        val = "";
                                    }
                                    buf.append(val.toString());
                                }
                            }
                            vars.declareVariable(attrEvent.localName,
                                                 buf.toString());
                        }
                    }
                    execute(context, def.next, def.endDefine);
                    vars.localVariables = localVariables;
                    ev = startElement.endElement.next;
                    continue;
                }
                Iterator i = startElement.attributeEvents.iterator();
                AttributesImpl attrs = new AttributesImpl();
                while (i.hasNext()) {
                    AttributeEvent attrEvent = (AttributeEvent)
                        i.next();
                    if (attrEvent instanceof CopyAttribute) {
                        CopyAttribute copy =
                            (CopyAttribute)attrEvent;
                        attrs.addAttribute(copy.namespaceURI,
                                           copy.localName,
                                           copy.raw,
                                           copy.type,
                                           copy.value);
                    } else if (attrEvent instanceof 
                               SubstituteAttribute) {
                        StringBuffer buf = new StringBuffer();
                        SubstituteAttribute substEvent =
                            (SubstituteAttribute)attrEvent;
                        Iterator ii = substEvent.substitutions.iterator();
                        while (ii.hasNext()) {
                            Subst subst = (Subst)ii.next();
                            if (subst instanceof Literal) {
                                Literal lit = (Literal)subst;
                                buf.append(lit.value);
                            } else if (subst instanceof Expression) {
                                Expression expr = (Expression)subst;
                                Object val;
                                try {
                                    val = 
                                        expr.compiledExpression.getValue(context);
                                } catch (JXPathException e) {
                                    throw new SAXParseException(e.getMessage(),
                                                                ev.location,
                                                                e);
                                }
                                if (val == null) {
                                    val = "";
                                }
                                buf.append(val.toString());
                            }
                        }
                        attrs.addAttribute(attrEvent.namespaceURI,
                                           attrEvent.localName,
                                           attrEvent.raw,
                                           attrEvent.type,
                                           buf.toString());
                    }
                }
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      attrs); 
            } else if (ev instanceof StartPrefixMapping) {
                StartPrefixMapping startPrefixMapping = 
                    (StartPrefixMapping)ev;
                consumer.startPrefixMapping(startPrefixMapping.prefix, 
                                            startPrefixMapping.uri);
            } else if (ev instanceof Comment) {
                TextEvent text = (TextEvent)ev;
                final StringBuffer buf = new StringBuffer();
                characters(context, text, new CharHandler() {
                        public void characters(char[] ch, int offset,
                                               int len) 
                            throws SAXException {
                            buf.append(ch, offset, len);
                        }
                    });
                char[] chars = new char[buf.length()];
                buf.getChars(0, chars.length, chars, 0);
                consumer.comment(chars, 0, chars.length);
             } else if (ev instanceof EndCDATA) {
                consumer.endCDATA();
            } else if (ev instanceof EndDTD) {
                consumer.endDTD();
            } else if (ev instanceof EndEntity) {
                consumer.endEntity(((EndEntity)ev).name);
            } else if (ev instanceof StartCDATA) {
                consumer.startCDATA();
            } else if (ev instanceof StartDTD) {
                StartDTD startDTD = (StartDTD)ev;
                consumer.startDTD(startDTD.name,
                                  startDTD.publicId,
                                  startDTD.systemId);
            } else if (ev instanceof StartEntity) {
                consumer.startEntity(((StartEntity)ev).name);
            } else if (ev instanceof StartValueOf) {
                StartValueOf startValueOf = (StartValueOf)ev;
                Object val;
                try {
                    val = startValueOf.compiledExpression.getValue(context);
                } catch (JXPathException e) {
                    throw new SAXParseException(e.getMessage(),
                                                ev.location,
                                                e);
                }
                if (val == null) {
                    val = "";
                }
                char[] ch = val.toString().toCharArray();
                consumer.characters(ch, 0, ch.length);
            } else if (ev instanceof StartTemplate) {
                // no action
            } else if (ev instanceof StartDefine) {
                StartDefine startDefine = (StartDefine)ev;
                definitions.put(startDefine.qname, startDefine);
                ev = startDefine.endDefine.next;
            } else if (ev instanceof StartImport) {
                String uri;
                StartImport startImport = (StartImport)ev;
                AttributeEvent e = startImport.uri;
                if (e instanceof CopyAttribute) {
                    CopyAttribute copy = (CopyAttribute)e;
                    uri = copy.value;
                } else {
                    StringBuffer buf = new StringBuffer();
                    SubstituteAttribute substAttr = (SubstituteAttribute)e;
                    Iterator i = substAttr.substitutions.iterator();
                    while (i.hasNext()) {
                        Subst subst = (Subst)i.next();
                        if (subst instanceof Literal) {
                            Literal lit = (Literal)subst;
                            buf.append(lit.value);
                        } else if (subst instanceof Expression) {
                            Expression expr = (Expression)subst;
                            Object val;
                            try {
                                val = 
                                    expr.compiledExpression.getValue(context);
                            } catch (JXPathException exc) {
                                throw new SAXParseException(exc.getMessage(),
                                                            ev.location,
                                                            exc);
                            }
                            if (val == null) {
                                val = "";
                            }
                            buf.append(val.toString());
                        }
                    }
                    uri = buf.toString();
                    
                }
                Source input;
                try {
                    input = resolver.resolveURI(uri);
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location,
                                                exc);
                }
                long lastMod = input.getLastModified();
                StartDocument doc;
                synchronized (cache) {
                    doc = (StartDocument)cache.get(input.getURI());
                    if (doc != null) {
                        if (doc.compileTime < lastMod) {
                            doc = null; // recompile
                        }
                    }
                }
                if (doc == null) {
                    try {
                        Parser parser = new Parser();
                        this.resolver.toSAX(input, parser);
                        doc = parser.getStartEvent();
                        doc.compileTime = lastMod;
                    } catch (Exception exc) {
                        throw new SAXParseException(exc.getMessage(),
                                                    ev.location,
                                                    exc);
                    }
                    synchronized (cache) {
                        cache.put(input.getURI(), doc);
                    }
                }
                JXPathContext select = context;
                if (startImport.select != null) {
                    try {
                        Object obj = startImport.select.getValue(context);
                        select = jxpathContextFactory.newContext(null, obj);
                        select.setVariables(variables);
                    } catch (JXPathException exc) {
                        throw new SAXParseException(exc.getMessage(),
                                                    ev.location,
                                                    exc);
                    }
                }
                execute(select, doc.next, null);
                ev = startImport.endImport.next;
                continue;
            }
            ev = ev.next;
        }
    }

}
