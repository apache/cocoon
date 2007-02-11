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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.jxforms.validation.Violation;
import org.apache.cocoon.components.jxforms.xmlform.Form;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.AbstractTransformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.Pointer;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

/**
 * <p><a href="http://jakarta.apache.org/commons/jxpath"><em>JX</em>Path</a> based implementation of <a href="http://www.w3.org/TR/xforms"><em>XForms</em></a></p>
 */

public class JXFormsGenerator extends AbstractGenerator {

    private static final JXPathContextFactory 
        jxpathContextFactory = JXPathContextFactory.newInstance();

    private static final char[] EMPTY_CHARS = "".toCharArray();

    private static final Attributes EMPTY_ATTRS = new AttributesImpl();

    private static final Iterator EMPTY_ITER = new Iterator() {
            public boolean hasNext() {
                return false;
            }
            public Object next() {
                return null;
            }
            public void remove() {
            }
        };

    private static final Iterator NULL_ITER = new Iterator() {
            public boolean hasNext() {
                return true;
            }
            public Object next() {
                return null;
            }
            public void remove() {
            }
        };

    final static String NS = 
        "http://cocoon.apache.org/jxforms/2002/cr";

    // Non XForms elements
    final static String FORM = "form";
    final static String VIOLATIONS = "violations";
    final static String VIOLATION = "violation";
    final static String HIDDEN = "hidden";

    /* Form Controls */
    final static String INPUT = "input";
    final static String SECRET = "secret";
    final static String TEXTAREA = "textarea";
    final static String OUTPUT = "output";
    final static String UPLOAD = "upload";
    final static String RANGE = "range";
    final static String TRIGGER = "trigger";
    final static String SUBMIT = "submit";
    final static String SELECT = "select";
    final static String SELECT1 = "select1";
    /* Selection Controls */
    final static String CHOICES = "choices";
    final static String ITEM = "item";
    final static String VALUE = "value";
    /* Additional Elements */
    final static String FILENAME = "filename";
    final static String MEDIATYPE = "mediatype";
    final static String LABEL = "label";
    final static String HELP = "help";
    final static String HINT = "hint";
    final static String ALERT = "alert";
    /* Group Module */
    final static String GROUP = "group";
    /* Switch Module */
    final static String SWITCH = "switch";
    final static String CASE = "case";
    final static String TOGGLE = "toggle";
    /* Repeat Module */
    final static String REPEAT = "repeat";
    final static String ITEMSET = "itemset";
    final static String COPY = "copy";
    final static String INSERT = "insert";
    final static String DELETE = "delete";
    final static String SETINDEX  = "setindex";

    /* Attributes */
    final static String NODESET = "nodeset";
    final static String REF = "ref";
    final static String ID = "id";
    final static String VIEW = "view";


    final static String BACK = "back";
    final static String FORWARD = "forward";
    final static String CONTINUATION = "continuation";
    final static String PHASE = "phase";


    /**
     * Compile a single XPath expression
     */

    static private CompiledExpression 
        compileExpr(String expr, Locator location) 
        throws SAXParseException {
        if (expr == null) return null;
        try {
            return JXPathContext.compile(expr);
        } catch (Exception exc) {
            throw new SAXParseException(exc.getMessage(),
                                        location, exc);
        } catch (Error err) {
            throw new SAXParseException(err.getMessage(),
                                        location, null);
        }
    }

    static class Event {
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

    static class TextEvent extends Event {
        TextEvent(Locator location, 
                  char[] chars, int start, int length) 
            throws SAXException {
            super(location);
            this.chars = new char[length];
            System.arraycopy(chars, start, this.chars, 0, length);
        }
        final char[] chars;
    }

    static class Characters extends TextEvent {
        Characters(Locator location, 
                   char[] chars, int start, int length) 
            throws SAXException {
            super(location, chars, start, length);
        }

    }

    static class StartDocument extends Event {
        StartDocument(Locator location) {
            super(location);
        }
        long compileTime;
        EndDocument endDocument; // null if document fragment
    }

    static class EndDocument extends Event {
        EndDocument(Locator location) {
            super(location);
        }
    }

    static class EndElement extends Event {
        EndElement(Locator location, 
                   StartElement startElement) {
            super(location);
            this.startElement = startElement;
        }
        final StartElement startElement;
    }

    static class EndPrefixMapping extends Event {
        EndPrefixMapping(Locator location, String prefix) {
            super(location);
            this.prefix = prefix;
        }
        final String prefix;
    }
    
    static class IgnorableWhitespace extends TextEvent {
        IgnorableWhitespace(Locator location, 
                            char[] chars, int start, int length) 
            throws SAXException {
            super(location, chars, start, length);
        }
    }

    static class ProcessingInstruction extends Event {
        ProcessingInstruction(Locator location,
                              String target, String data) {
            super(location);
            this.target = target;
            this.data = data;
        }
        final String target;
        final String data;
    }

    static class SkippedEntity extends Event {
        SkippedEntity(Locator location, String name) {
            super(location);
            this.name = name;
        }
        final String name;
    }


    static class StartElement extends Event {
        StartElement(Locator location, String namespaceURI,
                     String localName, String raw,
                     Attributes attrs) 
            throws SAXException {
            super(location);
            this.namespaceURI = namespaceURI;
            this.localName = localName;
            this.raw = raw;
            this.attributes = new AttributesImpl(attrs);
        }
        final String namespaceURI;
        final String localName;
        final String raw;
        final Attributes attributes;
        EndElement endElement;
    }


    static class StartPrefixMapping extends Event {
        StartPrefixMapping(Locator location, String prefix,
                           String uri) {
            super(location);
            this.prefix = prefix;
            this.uri = uri;
        }
        final String prefix;
        final String uri;
    }

    static class Comment extends TextEvent {
        Comment(Locator location, char[] chars,
                int start, int length)
            throws SAXException {
            super(location, chars, start, length);
        }
    }

    static class EndCDATA extends Event {
        EndCDATA(Locator location) {
            super(location);
        }
    }

    static class EndDTD extends Event {
        EndDTD(Locator location) {
            super(location);
        }
    }

    static class EndEntity extends Event {
        EndEntity(Locator location, String name) {
            super(location);
            this.name = name;
        }
        final String name;
    }

    static class StartCDATA extends Event {
        StartCDATA(Locator location) {
            super(location);
        }
    }

    static class StartDTD extends Event {
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
    
    static class StartEntity extends Event {
        public StartEntity(Locator location, String name) {
            super(location);
            this.name = name;
        }
        final String name;
    }

    /* Form Controls */

    static final String[] INPUT_CONTROLS = {
        INPUT, SECRET, TEXTAREA, SELECT,
        SELECT1
    };

    static final String[] READONLY_INPUT_CONTROLS = {
        HINT, VALUE, HELP, LABEL
    };

    private static boolean isInputControl(String name) {
        for (int i = 0; i < INPUT_CONTROLS.length; i++) {
            if (INPUT_CONTROLS[i].equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isReadonlyInputControl(String name) {
        for (int i = 0; i < READONLY_INPUT_CONTROLS.length; i++) {
            if (READONLY_INPUT_CONTROLS[i].equals(name)) {
                return true;
            }
        }
        return false;
    }

    // input, secret, textarea, select1, select

    static class StartInputControl extends Event {
        StartInputControl(Locator location, 
                          CompiledExpression ref,
                          StartElement startElement) 
            throws SAXException {
            super(location);
            this.ref = ref;
            this.startElement = startElement;
        }
        final CompiledExpression ref;
        final StartElement startElement;
        EndInputControl endInputControl;
    }

    static class EndInputControl extends Event {
        EndInputControl(Locator location, StartInputControl start) {
            super(location);
            this.startInputControl = start;
            start.endInputControl = this;
        }
        final StartInputControl startInputControl;
    }

    // hint, value, label, help

    static class StartReadonlyInputControl extends Event {
        StartReadonlyInputControl(Locator location, 
                                  CompiledExpression ref,
                                  StartElement startElement) 
            throws SAXException {
            super(location);
            this.ref = ref;
            this.startElement = startElement;
        }
        final CompiledExpression ref;
        final StartElement startElement;
        EndReadonlyInputControl endReadonlyInputControl;
    }

    static class EndReadonlyInputControl extends Event {
        EndReadonlyInputControl(Locator location, 
                                StartReadonlyInputControl start) {
            super(location); 
            this.startReadonlyInputControl = start;
            start.endReadonlyInputControl = this;
        }
        final StartReadonlyInputControl startReadonlyInputControl;
    }

    static class StartForm extends Event {
        StartForm(Locator location, StartElement start) 
            throws SAXException {
            super(location);
            this.startElement = start;
            this.formId = start.attributes.getValue("id");
        }
        final StartElement startElement;
        final String formId;
        EndForm endForm;
    }

    static class EndForm extends Event {
        EndForm(Locator location, StartForm start) {
            super(location);
            start.endForm = this;
            this.startForm = start;
        }
        final StartForm startForm;
    }

    static class StartViolations extends Event {
        StartViolations(Locator location, 
                        Event parent,
                        StartElement start) 
            throws SAXException {
            super(location);
            this.startElement = start;
            this.parent = parent;
            this.formId = start.attributes.getValue("id");
        }
        final StartElement startElement;
        final Event parent;
        final String formId;
        EndViolations endViolations;
    }

    static class EndViolations extends Event {
        EndViolations(Locator location, StartViolations start) {
            super(location);
            start.endViolations = this;
            this.startViolations = start;
        }
        final StartViolations startViolations;
    }

    static class StartRepeat extends Event {
        StartRepeat(Locator location, String namespaceURI,
                    String localName, String raw,
                    Attributes attrs, CompiledExpression nodeset) 
            throws SAXException {
            super(location);
            this.startElement = new StartElement(location,
                                                 namespaceURI,
                                                 localName,
                                                 raw,
                                                 attrs);
            this.nodeset = nodeset;
        }
        final CompiledExpression nodeset;
        final StartElement startElement;
        EndRepeat endRepeat;
    }
    
    static class EndRepeat extends Event {
        EndRepeat(Locator location) {
            super(location);
        }
    }

    static class StartItemSet extends Event {
        StartItemSet(Locator location, String namespaceURI,
                     String localName, String raw,
                     Attributes attrs, CompiledExpression nodeset) 
            throws SAXException {
            super(location);
            this.startElement = new StartElement(location,
                                                 namespaceURI,
                                                 localName,
                                                 raw,
                                                 attrs);
            this.nodeset = nodeset;
        }
        final CompiledExpression nodeset;
        final StartElement startElement;
        EndItemSet endItemSet;
    }
    
    static class EndItemSet extends Event {
        EndItemSet(Locator location) {
            super(location);
        }
    }

    static class StartSubmission extends Event {
        StartSubmission(Locator location,
                        StartElement startElement) {
            super(location);
            this.startElement = startElement;
        }
        StartElement startElement;
        EndSubmission endSubmission;
    }

    static class EndSubmission extends Event {
        EndSubmission(Locator location, StartSubmission start) {
            super(location);
            start.endSubmission = this;
            this.startSubmission = start;
        }
        final StartSubmission startSubmission;
    }

    static class StartSubmit extends Event {
        StartSubmit(Locator location, StartElement startElement) {
            super(location);
            this.startElement = startElement;
            this.submissionName = 
                startElement.attributes.getValue("submission");
        }
        final StartElement startElement;
        final String submissionName;
        StartSubmission submission;
        EndSubmit endSubmit;
    }

    static class EndSubmit extends Event {
        EndSubmit(Locator location, StartSubmit start) {
            super(location);
            start.endSubmit = this;
            this.startSubmit = start;
        }
        final StartSubmit startSubmit;
    }

    static class StartItem extends Event {
        StartItem(Locator location, StartElement startElement) {
            super(location);
            this.startElement = startElement;
        }
        final StartElement startElement;
        EndItem endItem;
    }

    static class EndItem extends Event {
        EndItem(Locator location, StartItem start) {
            super(location);
            start.endItem = this;
            this.startItem = start;
        }
        final StartItem startItem;
    }

    static class StartChoices extends Event {
        StartChoices(Locator location, StartElement startElement) {
            super(location);
            this.startElement = startElement;
        }
        final StartElement startElement;
        EndChoices endChoices;
    }

    static class EndChoices extends Event {
        EndChoices(Locator location, StartChoices start) {
            super(location);
            start.endChoices = this;
            this.startChoices = start;
        }
        final StartChoices startChoices;
    }

    static class StartValue extends Event {
        StartValue(Locator location, StartElement startElement) {
            super(location);
            this.startElement = startElement;
        }
        final StartElement startElement;
        EndValue endValue;
    }

    static class EndValue extends Event {
        EndValue(Locator location, StartValue start) {
            super(location);
            start.endValue = this;
            this.startValue = start;
        }
        final StartValue startValue;
    }


    static class StartOutput extends Event {
        StartOutput(Locator location, 
                    CompiledExpression ref,
                    CompiledExpression value,
                    StartElement startElement) {
            super(location);
            this.startElement = startElement;
            this.ref = ref;
            this.value = value;
        }
        final CompiledExpression ref;
        final CompiledExpression value;
        final StartElement startElement;
        EndOutput endOutput;
    }

    static class EndOutput extends Event {
        EndOutput(Locator location, StartOutput start) {
            super(location);
            start.endOutput = this;
            this.startOutput = start;
        }
        final StartOutput startOutput;
    }

    static class StartGroup extends Event {
        StartGroup(Locator location, 
                    CompiledExpression ref,
                    StartElement startElement) {
            super(location);
            this.ref = ref;
            this.startElement = startElement;
        }
        final CompiledExpression ref;
        final StartElement startElement;
        EndGroup endGroup;
    }

    static class EndGroup extends Event {
        EndGroup(Locator location, StartGroup start) {
            super(location);
            start.endGroup = this;
            this.startGroup = start;
        }
        final StartGroup startGroup;
    }

    static class StartHidden extends Event {
        StartHidden(Locator location, 
                    CompiledExpression ref,
                    StartElement startElement) {
            super(location);
            this.ref = ref;
            this.startElement = startElement;
        }
        final CompiledExpression ref;
        final StartElement startElement;
        EndHidden endHidden;
    }

    static class EndHidden extends Event {
        EndHidden(Locator location, StartHidden start) {
            super(location);
            start.endHidden = this;
            this.startHidden = start;
        }
        final StartHidden startHidden;
    }

    static class Parser implements ContentHandler, LexicalHandler {
        StartDocument startEvent;
        Event lastEvent;
        Stack stack = new Stack();
        Locator locator;
        Locator charLocation;
        StringBuffer charBuf;
        
        public Parser() {
        }

        StartDocument getStartEvent() {
            return startEvent;
        }
        
        private void addEvent(Event ev) throws SAXException {
            if (ev == null) {
                throw new NullPointerException("null event");
            }
            if (charBuf != null) {
                char[] chars = new char[charBuf.length()];
                charBuf.getChars(0, charBuf.length(), chars, 0);
                Characters charEvent = new Characters(charLocation,
                                                      chars, 0, chars.length);
                                                      
                lastEvent.next = charEvent;
                lastEvent = charEvent;
                charLocation = null;
                charBuf = null;
            }
            if (lastEvent == null) {
                lastEvent = startEvent = new StartDocument(locator);
            }
            lastEvent.next = ev;
            lastEvent = ev;
        }

        public void characters(char[] ch, int start, int length) 
            throws SAXException {
            if (charBuf == null) {
                charBuf = new StringBuffer();
                charLocation = new LocatorImpl(locator);
            }
            charBuf.append(ch, start, length);
        }

        public void endDocument() throws SAXException {
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
            if (NS.equals(namespaceURI)) {
                if (start instanceof StartRepeat) {
                    StartRepeat startRepeat = 
                        (StartRepeat)start;
                    newEvent = startRepeat.endRepeat = 
                        new EndRepeat(locator);
                } else if (start instanceof StartItemSet) {
                    StartItemSet startItemSet = 
                        (StartItemSet)start;
                    newEvent = startItemSet.endItemSet = 
                        new EndItemSet(locator);
                } else if (start instanceof StartInputControl) {
                    StartInputControl startInputControl = 
                        (StartInputControl)start;
                    newEvent = new EndInputControl(locator, startInputControl);
                } else if (start instanceof StartReadonlyInputControl) {
                    StartReadonlyInputControl startInputControl = 
                        (StartReadonlyInputControl)start;
                    newEvent = new EndReadonlyInputControl(locator, 
                                                           startInputControl);
                } else if (start instanceof StartSubmit) {
                    StartSubmit startSubmit = 
                        (StartSubmit)start;
                    newEvent = startSubmit.endSubmit = 
                        new EndSubmit(locator, startSubmit);
                } else if (start instanceof StartForm) {
                    StartForm startForm =
                        (StartForm)start;
                    newEvent = startForm.endForm = 
                        new EndForm(locator, 
                                    startForm);
                } else if (start instanceof StartViolations) {
                    StartViolations startViolations =
                        (StartViolations)start;
                    newEvent = startViolations.endViolations = 
                        new EndViolations(locator, 
                                          startViolations);
                } else if (start instanceof StartItem) {
                    StartItem startItem =
                        (StartItem)start;
                    newEvent = startItem.endItem = 
                        new EndItem(locator, startItem);
                } else if (start instanceof StartChoices) {
                    StartChoices startChoices =
                        (StartChoices)start;
                    newEvent = startChoices.endChoices = 
                        new EndChoices(locator, startChoices);
                } else if (start instanceof StartValue) {
                    StartValue startValue =
                        (StartValue)start;
                    newEvent = startValue.endValue = 
                        new EndValue(locator, startValue);
                } else if (start instanceof StartOutput) {
                    StartOutput startOutput =
                        (StartOutput)start;
                    newEvent = startOutput.endOutput = 
                        new EndOutput(locator, startOutput);
                } else if (start instanceof StartGroup) {
                    StartGroup startGroup =
                        (StartGroup)start;
                    newEvent = startGroup.endGroup = 
                        new EndGroup(locator, startGroup);
                } else if (start instanceof StartHidden) {
                    StartHidden startHidden =
                        (StartHidden)start;
                    newEvent = startHidden.endHidden = 
                        new EndHidden(locator, startHidden);
                } else {
                    throw new SAXParseException("unrecognized tag: " + 
                                                raw, locator, null);
                }
            } else {
                StartElement startElement = (StartElement)start;
                newEvent = startElement.endElement = 
                    new EndElement(locator, startElement);
            }
            addEvent(newEvent);
        }
        
        public void endPrefixMapping(String prefix) throws SAXException {
            EndPrefixMapping endPrefixMapping = 
                new EndPrefixMapping(locator, prefix);
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

        public void startElement(String namespaceURI,
                                 String localName,
                                 String raw,
                                 Attributes attrs) 
            throws SAXException {
            Event newEvent = null;
            if (NS.equals(namespaceURI)) {
                if (localName.equals(REPEAT)) {
                    String items = attrs.getValue(NODESET);
                    CompiledExpression expr =
                        compileExpr(items, locator);
                    StartRepeat startRepeat = 
                        new StartRepeat(locator, namespaceURI,
                                        localName, raw, attrs, expr);
                    newEvent = startRepeat;
                } else if (localName.equals(ITEMSET)) {
                    String items = attrs.getValue(NODESET);
                    CompiledExpression expr =
                        compileExpr(items, locator);
                    StartItemSet startItemSet =
                        new StartItemSet(locator, namespaceURI,
                                         localName, raw, attrs, expr);
                    newEvent = startItemSet;
                } else if (isReadonlyInputControl(localName)) {
                    String refStr = attrs.getValue("ref");
                    CompiledExpression ref = 
                        compileExpr(refStr, locator);
                    StartReadonlyInputControl startInputControl = 
                        new StartReadonlyInputControl(locator,
                                                      ref,
                                                      new StartElement(locator,
                                                                       namespaceURI, localName, raw, attrs));
                    newEvent = startInputControl;
                } else if (isInputControl(localName)) {
                    String refStr = attrs.getValue("ref");
                    CompiledExpression ref = 
                        compileExpr(refStr, locator);
                    StartInputControl startInputControl = 
                        new StartInputControl(locator,
                                              ref,
                                              new StartElement(locator, namespaceURI, 
                                                               localName, raw, attrs));
                    newEvent = startInputControl;
                } else if (SUBMIT.equals(localName)) {
                    StartSubmit startSubmit = 
                        new StartSubmit(locator,
                                        new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startSubmit;
                } else if (ITEM.equals(localName)) {
                    StartItem startItem = 
                        new StartItem(locator,
                                        new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startItem;
                } else if (CHOICES.equals(localName)) {
                    StartChoices startChoices = 
                        new StartChoices(locator,
                                        new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startChoices;
                } else if (VALUE.equals(localName)) {
                    StartValue startValue = 
                        new StartValue(locator,
                                        new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startValue;
                } else if (OUTPUT.equals(localName)) {
                    String refStr = attrs.getValue(REF);
                    String valueStr = attrs.getValue(VALUE);
                    if (refStr != null && valueStr != null) {
                        throw new SAXParseException("ref and value are mutually exclusive", locator, null);
                    }
                    CompiledExpression ref = compileExpr(refStr, 
                                                         locator);
                    CompiledExpression value = compileExpr(valueStr, 
                                                           locator);
                    StartOutput startOutput = 
                        new StartOutput(locator,
                                        ref, value,
                                        new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startOutput;
                } else if (FORM.equals(localName)) {
                    StartForm startForm = 
                        new StartForm(locator,
                                      new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startForm;
                } else if (VIOLATIONS.equals(localName)) {
                    StartViolations startViolations = 
                        new StartViolations(locator,
                                            (Event)stack.peek(),
                                            new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startViolations;
                } else if (GROUP.equals(localName)) {
                    String refStr = attrs.getValue(REF);
                    CompiledExpression ref = 
                        compileExpr(refStr, locator);
                    StartGroup startGroup = 
                        new StartGroup(locator,
                                       ref,
                                       new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startGroup;
                } else if (HIDDEN.equals(localName)) {
                    String refStr = attrs.getValue(REF);
                    CompiledExpression ref = 
                        compileExpr(refStr, locator);
                    StartHidden startHidden = 
                        new StartHidden(locator,
                                       ref,
                                       new StartElement(locator, namespaceURI, localName, raw, attrs));
                    newEvent = startHidden;
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
        
        public void startPrefixMapping(String prefix, String uri) 
            throws SAXException {
            addEvent(new StartPrefixMapping(locator, prefix, uri));
        }

        public void comment(char ch[], int start, int length) 
            throws SAXException {
            addEvent(new Comment(locator, ch, start, length));
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

    /**
     * Adapter that makes this generator usable as a transformer
     * (Note there is a performance penalty for this however: 
     * you effectively recompile the template for every instance document)
     */

    public static class TransformerAdapter extends AbstractTransformer {

        static class TemplateConsumer extends Parser implements XMLConsumer {

            JXFormsGenerator template;

            public TemplateConsumer(SourceResolver resolver, Map objectModel,
                                    String src, Parameters parameters) 
                throws ProcessingException, SAXException, IOException {
                this.template = new JXFormsGenerator();
                this.template.setup(resolver, objectModel, null, parameters);
            }

            public void endDocument() throws SAXException {
                super.endDocument();
                template.execute(template.getConsumer(),
                                 null, // form
                                 null, // view
                                 null, // contextPath
                                 jxpathContextFactory.newContext(null, null),
                                 getStartEvent(), 
                                 null);
            }

            void setConsumer(XMLConsumer consumer) {
                template.setConsumer(consumer);
            }
        }

        TemplateConsumer templateConsumer;

        public void recycle() {
            super.recycle();
            templateConsumer = null;
        }

        public void setup(SourceResolver resolver, Map objectModel,
                          String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
            templateConsumer = new TemplateConsumer(resolver, objectModel,
                                                    src,
                                                    parameters);
        }

        public void setConsumer(XMLConsumer xmlConsumer) {
            super.setConsumer(templateConsumer);
            templateConsumer.setConsumer(xmlConsumer);
        }
    }

    private static Map cache = new HashMap();
    private XMLConsumer consumer;
    private Source inputSource;
    WebContinuation kont;
    Object bean;
    Map objectModel;

    private XMLConsumer getConsumer() {
        return consumer;
    }

    public void recycle() {
        super.recycle();
        consumer = null;
        inputSource = null;
        bean = null;
        kont = null;
        objectModel = null;
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
            long lastMod = inputSource.getLastModified();
            String uri = inputSource.getURI();
            synchronized (cache) {
                StartDocument startEvent = (StartDocument)cache.get(uri);
                if (startEvent != null &&
                    lastMod > startEvent.compileTime) {
                    cache.remove(uri);
                }
            }
        }
        // FIX ME: When we decide proper way to pass "bean" and "kont"
        bean = ((Environment)resolver).getAttribute("bean-dict");
        kont = (WebContinuation)((Environment)resolver).getAttribute("kont");
        this.objectModel = objectModel;
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
        execute(consumer, 
                null, // form
                null, // view
                null, // contextPath
                jxpathContextFactory.newContext(null, null),
                startEvent, 
                null);
    }

    private void execute(final XMLConsumer consumer,
                         Form form,
                         String currentView,
                         String contextPath,
                         JXPathContext jxpathContext,
                         Event startEvent, Event endEvent) 
        throws SAXException {
        Event ev = startEvent;
        while (ev != endEvent) {
            consumer.setDocumentLocator(ev.location);
            if (ev instanceof Characters) {
                TextEvent text = (TextEvent)ev;
                consumer.characters(text.chars, 0, text.chars.length);
            } else if (ev instanceof IgnorableWhitespace) {
                TextEvent text = (TextEvent)ev;
                consumer.ignorableWhitespace(text.chars, 0, text.chars.length);
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
            } else if (ev instanceof StartPrefixMapping) {
                StartPrefixMapping startPrefixMapping = 
                    (StartPrefixMapping)ev;
                consumer.startPrefixMapping(startPrefixMapping.prefix, 
                                            startPrefixMapping.uri);
            } else if (ev instanceof Comment) {
                TextEvent text = (TextEvent)ev;
                consumer.comment(text.chars, 0, text.chars.length);
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

                ////////////////////////////////////////////////
            } else if (ev instanceof StartElement) {
                StartElement startElement = 
                    (StartElement)ev;
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);

            } else if (ev instanceof StartRepeat) {
                StartRepeat startRepeat = (StartRepeat)ev;
                final CompiledExpression nodeset = startRepeat.nodeset;
                Iterator iter = null;
                try {
                    if (nodeset == null) {
                        iter = NULL_ITER;
                    } else {
                        iter = 
                            nodeset.iteratePointers(jxpathContext);
                    }
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location,
                                                exc);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                ev.location,
                                                null);
                }
                while (iter.hasNext()) {
                    Object value;
                    Pointer ptr = (Pointer)iter.next();
                    try {
                        value = ptr.getNode();
                    } catch (Exception exc) {
                        throw new SAXParseException(exc.getMessage(),
                                                        ev.location,
                                                        exc);
                    }
                    JXPathContext localJXPathContext = 
                        jxpathContextFactory.newContext(null, value);
                    String path = "";
                    if (contextPath != null) {
                        path = contextPath + "/."; 
                    } 
                    path += ptr.asPath();
                    execute(consumer,
                            form,
                            currentView,
                            path,
                            localJXPathContext,
                            startRepeat.next,
                            startRepeat.endRepeat);
                }
                ev = startRepeat.endRepeat.next;
                continue;
            } else if (ev instanceof StartGroup) {
                StartGroup startGroup = (StartGroup)ev;
                StartElement startElement = startGroup.startElement;
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
                final CompiledExpression ref = startGroup.ref;
                if (ref != null) {
                    Object value;
                    try {
                        value = ref.getValue(jxpathContext);
                    } catch (Exception exc) {
                        throw new SAXParseException(exc.getMessage(),
                                                    ev.location,
                                                    exc);
                    }
                    JXPathContext localJXPathContext = 
                        jxpathContextFactory.newContext(null, value);
                    String path = "";
                    if (contextPath != null) {
                        path = contextPath + "/.";
                    } 
                    path += startElement.attributes.getValue(REF);
                    execute(consumer,
                            form,
                            currentView,
                            path,
                            localJXPathContext,
                            startGroup.next,
                            startGroup.endGroup);
                    ev = startGroup.endGroup;
                    continue;
                }
            } else if (ev instanceof StartItemSet) {
                StartItemSet startItemSet = (StartItemSet)ev;
                final CompiledExpression nodeset = startItemSet.nodeset;
                Iterator iter = null;
                try {
                    if (nodeset == null) {
                        iter = NULL_ITER;
                    } else {
                        iter = 
                            nodeset.iteratePointers(jxpathContext);
                    }
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location,
                                                exc);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                ev.location,
                                                null);
                }
                while (iter.hasNext()) {
                    Object value;
                    Pointer ptr = (Pointer)iter.next();
                    try {
                        value = ptr.getNode();
                    } catch (Exception exc) {
                        throw new SAXParseException(exc.getMessage(),
                                                        ev.location,
                                                        exc);
                    }
                    JXPathContext localJXPathContext = 
                        jxpathContextFactory.newContext(null, value);
                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute(NS, "ref", "ref", "CDATA",
                                       ptr.asPath());
                    consumer.startElement(NS, "item", "item",
                                          attrs);
                    String path = "";
                    if (contextPath != null) {
                        path = contextPath + "/.";
                    } 
                    path += ptr.asPath();
                    execute(consumer,
                            form,
                            currentView,
                            ptr.asPath(),
                            localJXPathContext,
                            startItemSet.next,
                            startItemSet.endItemSet);
                    consumer.endElement(NS, "item", "item");
                }
                ev = startItemSet.endItemSet.next;
                continue;
            } else if (ev instanceof StartInputControl) {
                //
                // input, textarea, secret, select1, selectMany
                //
                StartInputControl startInputControl =
                    (StartInputControl)ev;
                CompiledExpression ref = startInputControl.ref;
                StartElement startElement = startInputControl.startElement;
                String refStr = startElement.attributes.getValue("ref");
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
                if (ref != null) {
                    Iterator iter = ref.iteratePointers(jxpathContext);
                    while (iter.hasNext()) {
                        Pointer ptr = (Pointer)iter.next();
                        AttributesImpl attrs = new AttributesImpl();
                        attrs.addAttribute(NS, REF, REF, "CDATA",
                                           ptr.asPath());
                        consumer.startElement(NS, VALUE, VALUE, EMPTY_ATTRS);
                        Object val = ptr.getNode();
                        String str = String.valueOf(val);
                        consumer.characters(str.toCharArray(), 0, str.length());
                        consumer.endElement(NS, VALUE, VALUE);

                    }
                }
            } else if (ev instanceof EndInputControl) {
                StartInputControl startInputControl =
                    ((EndInputControl)ev).startInputControl;
                StartElement startElement = startInputControl.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartReadonlyInputControl) {
                //
                // label, hint, help, value
                //
                // substitute "ref" if present
                StartReadonlyInputControl startReadonlyInputControl =
                    (StartReadonlyInputControl)ev;
                StartElement startElement = startReadonlyInputControl.startElement;
                Object refValue = null;
                if (startReadonlyInputControl.ref != null) {
                    refValue = 
                        startReadonlyInputControl.ref.getValue(jxpathContext);
                }
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
                if (refValue != null) {
                    String v = String.valueOf(refValue);
                    consumer.characters(v.toCharArray(), 0, v.length());      
                }
            } else if (ev instanceof EndReadonlyInputControl) {
                StartReadonlyInputControl startReadonlyInputControl =
                    ((EndReadonlyInputControl)ev).startReadonlyInputControl;
                StartElement startElement = 
                    startReadonlyInputControl.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartForm) {
                StartForm startForm = (StartForm)ev;
                StartElement startElement = startForm.startElement;
                String view = startElement.attributes.getValue(VIEW);
                String id = startElement.attributes.getValue(ID);
                Form newForm = Form.lookup(objectModel, id);
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
                execute(consumer, newForm, view, contextPath, 
                        jxpathContextFactory.newContext(null, 
                                                        newForm.getModel()),
                        startForm.next, startForm.endForm);
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
                ev = startForm.endForm.next;
                continue;
            } else if (ev instanceof EndForm) {
                StartElement startElement = 
                    ((EndForm)ev).startForm.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartSubmit) {
                StartElement startElement = ((StartSubmit)ev).startElement;
                Attributes attrs = startElement.attributes;
                if (kont != null) {
                    String id = startElement.attributes.getValue(ID);
                    if (id == null) {
                        id = "";
                    }
                    String cont = 
                        startElement.attributes.getValue(CONTINUATION);
                    int level = 0;
                    if (BACK.equals(cont)) {
                        level = 3;
                    }
                    WebContinuation wk = kont;
                    for (int i = 0; i < level; i++) {
                        wk = wk.getParentContinuation();
                        if (wk == null) {
                            throw new SAXParseException("No such continuation",
                                                        ev.location,
                                                        null);
                        }
                    }
                    String kontId = wk.getId();
                    AttributesImpl newAttrs = 
                        new AttributesImpl(startElement.attributes);
                    int i = newAttrs.getIndex(ID);
                    String phase = attrs.getValue(PHASE);
                    if (phase == null) {
                        phase = currentView;
                    }
                    if (i >= 0) {
                        newAttrs.setValue(i, kontId + ":" + phase + ":" +id);
                    } else {
                        newAttrs.addAttribute("", ID, ID, "CDATA", 
                                              kontId + ":" + phase + ":" + id);
                    }
                    attrs = newAttrs;
                }
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      attrs);
            } else if (ev instanceof EndSubmit) {
                StartElement startElement = 
                    ((EndSubmit)ev).startSubmit.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartItem) {
                StartElement startElement = ((StartItem)ev).startElement;
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
            } else if (ev instanceof EndItem) {
                StartElement startElement = 
                    ((EndItem)ev).startItem.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartChoices) {
                StartElement startElement = ((StartChoices)ev).startElement;
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
            } else if (ev instanceof EndChoices) {
                StartElement startElement = 
                    ((EndChoices)ev).startChoices.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartValue) {
                StartElement startElement = ((StartValue)ev).startElement;
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
            } else if (ev instanceof EndValue) {
                StartElement startElement = 
                    ((EndValue)ev).startValue.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartHidden) {
                StartElement startElement = ((StartHidden)ev).startElement;
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
            } else if (ev instanceof EndHidden) {
                StartElement startElement = 
                    ((EndHidden)ev).startHidden.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartOutput) {
                StartOutput startOutput = (StartOutput)ev;
                StartElement startElement = startOutput.startElement;
                JXPathContext ctx = jxpathContext;
                String formId = startElement.attributes.getValue(FORM);
                if (formId != null) {
                    Form theForm = Form.lookup(objectModel, formId);
                    if (theForm == null) {
                        throw new SAXParseException("form not found: " + formId,
                                                    ev.location,
                                                    null);
                    }
                    ctx = jxpathContextFactory.newContext(null,
                                                          theForm.getModel());
                }
                consumer.startElement(startElement.namespaceURI,
                                      startElement.localName,
                                      startElement.raw,
                                      startElement.attributes);
                Object val = null;
                if (startOutput.ref != null) {
                    val = startOutput.ref.getValue(ctx);
                } else if (startOutput.value != null) {
                    val = startOutput.value.getValue(ctx);
                }
                if (val != null) {
                    consumer.startElement(NS, VALUE, VALUE, EMPTY_ATTRS);
                    String str = String.valueOf(val);
                    consumer.characters(str.toCharArray(), 0, str.length());
                    consumer.endElement(NS, VALUE, VALUE);
                    
                }
            } else if (ev instanceof EndOutput) {
                StartElement startElement = 
                    ((EndOutput)ev).startOutput.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof EndGroup) {
                StartElement startElement = 
                    ((EndGroup)ev).startGroup.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName,
                                    startElement.raw);
            } else if (ev instanceof StartViolations) {
                StartViolations startViolations = 
                    (StartViolations)ev;
                StartElement startElement = 
                    startViolations.startElement;
                Attributes attrs = startElement.attributes;
                String formAttr = attrs.getValue(FORM);
                Form theForm = form;
                if (formAttr != null) {
                    theForm = Form.lookup(objectModel, formAttr);
                }
                Set violations = form.getViolationsAsSortedSet();
                String mypath = null;
                if (startViolations.parent instanceof StartInputControl) {
                    StartInputControl control = 
                        (StartInputControl)startViolations.parent;
                    mypath = control.startElement.attributes.getValue(REF);
                    if (contextPath != null) {
                        if (!mypath.startsWith("/")) {
                            mypath  = contextPath + "/" + mypath;
                        }
                    }
                }
                if (violations != null) {
                    for (Iterator iter = violations.iterator(); iter.hasNext();) {
                        Violation violation = (Violation)iter.next();
                        String path = violation.getPath();
                        if (mypath == null || path.equals(mypath)) {
                            String message = violation.getMessage();
                            AttributesImpl newAttrs = 
                                new AttributesImpl(startElement.attributes);
                            newAttrs.addAttribute(null, REF, REF, "CDATA",
                                                  path);
                            consumer.startElement(NS, VIOLATION,
                                                  VIOLATION, newAttrs);
                            consumer.characters(message.toCharArray(), 0,
                                                message.length());
                            consumer.endElement(NS, VIOLATION,
                                                VIOLATION);
                        }
                    }
                }
            } else if (ev instanceof EndViolations) {
                /* No action */
            }
            ev = ev.next;
        }
    }
}
