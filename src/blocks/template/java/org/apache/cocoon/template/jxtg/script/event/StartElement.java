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
package org.apache.cocoon.template.jxtg.script.event;

import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;

import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.cocoon.template.jxtg.expression.Literal;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.AttributesImpl;

public class StartElement extends Event {
    public StartElement(Locator location, String namespaceURI,
            String localName, String raw, Attributes attrs) throws SAXException {
        super(location);
        this.namespaceURI = namespaceURI;
        this.localName = localName;
        this.raw = raw;
        this.qname = "{" + namespaceURI + "}" + localName;
        StringBuffer buf = new StringBuffer();
        int len = attrs.getLength();
        for (int i = 0; i < len; i++) {
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
            boolean xpath = false;
            try {
                top: while ((ch = in.read()) != -1) {
                    char c = (char) ch;
                    processChar: while (true) {
                        if (inExpr) {
                            if (c == '\\') {
                                ch = in.read();
                                buf.append(ch == -1 ? '\\' : (char) ch);
                            } else if (c == '}') {
                                String str = buf.toString();
                                JXTExpression compiledExpression;
                                try {
                                    compiledExpression = JXTExpression.compile(str,
                                            xpath);
                                } catch (Exception exc) {
                                    throw new SAXParseException(exc
                                            .getMessage(), location, exc);
                                } catch (Error err) {
                                    throw new SAXParseException(err
                                            .getMessage(), location,
                                            new ErrorHolder(err));
                                }
                                substEvents.add(compiledExpression);
                                buf.setLength(0);
                                inExpr = false;
                            } else {
                                buf.append(c);
                            }
                        } else if (c == '$' || c == '#') {
                            ch = in.read();
                            if (ch == '{') {
                                if (buf.length() > 0) {
                                    substEvents
                                            .add(new Literal(buf.toString()));
                                    buf.setLength(0);
                                }
                                inExpr = true;
                                xpath = c == '#';
                                continue top;
                            }
                            buf.append(c);
                            if (ch != -1) {
                                c = (char) ch;
                                continue processChar;
                            }
                        } else {
                            buf.append(c);
                        }
                        break;
                    }
                }
            } catch (IOException ignored) {
                ignored.printStackTrace();
            }
            if (inExpr) {
                // unclosed #{} or ${}
                String msg = "Unterminated " + (xpath ? "#" : "$") + "{";
                throw new SAXParseException(msg, location, null);
            }
            if (buf.length() > 0) {
                if (substEvents.size() == 0) {
                    getAttributeEvents().add(
                            new CopyAttribute(uri, local, qname, type, value));
                } else {
                    substEvents.add(new Literal(buf.toString()));
                    getAttributeEvents().add(
                            new SubstituteAttribute(uri, local, qname, type,
                                    substEvents));
                }
            } else {
                if (substEvents.size() > 0) {
                    getAttributeEvents().add(
                            new SubstituteAttribute(uri, local, qname, type,
                                    substEvents));
                } else {
                    getAttributeEvents().add(
                            new CopyAttribute(uri, local, qname, type, ""));
                }
            }
        }
        this.attributes = new AttributesImpl(attrs);
    }

    final String namespaceURI;
    final String localName;
    final String raw;
    private final String qname;
    private final List attributeEvents = new LinkedList();
    final Attributes attributes;
    EndElement endElement;

    public EndElement getEndElement() {
        return endElement;
    }

    public String getLocalName() {
        return localName;
    }

    public String getNamespaceURI() {
        return namespaceURI;
    }

    public String getRaw() {
        return raw;
    }

    public String getQname() {
        return qname;
    }

    public List getAttributeEvents() {
        return attributeEvents;
    }

    public void setEndElement(EndElement endElement) {
        this.endElement = endElement;

    }
}
