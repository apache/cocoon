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

import java.io.CharArrayReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.ArrayUtils;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TextEvent extends Event {
    public TextEvent(Locator location, char[] chars, int start, int length)
            throws SAXException {
        super(location);
        StringBuffer buf = new StringBuffer();
        this.raw = new char[length];
        System.arraycopy(chars, start, this.raw, 0, length);
        CharArrayReader in = new CharArrayReader(chars, start, length);
        int ch;
        boolean inExpr = false;
        boolean xpath = false;
        try {
            top: while ((ch = in.read()) != -1) {
                // column++;
                char c = (char) ch;
                processChar: while (true) {
                    if (inExpr) {
                        if (c == '\\') {
                            ch = in.read();
                            buf.append(ch == -1 ? '\\' : (char) ch);
                        } else if (c == '}') {
                            String str = buf.toString();
                            Object compiledExpression;
                            try {
                                if (xpath) {
                                    compiledExpression = JXPathContext
                                            .compile(str);
                                } else {
                                    compiledExpression = ExpressionFactory
                                            .createExpression(str);
                                }
                            } catch (Exception exc) {
                                throw new SAXParseException(exc.getMessage(),
                                        this.getLocation(), exc);
                            } catch (Error err) {
                                throw new SAXParseException(err.getMessage(),
                                        this.getLocation(),
                                        new ErrorHolder(err));

                            }
                            substitutions.add(new JXTExpression(str,
                                    compiledExpression));
                            buf.setLength(0);
                            inExpr = false;
                        } else {
                            buf.append(c);
                        }
                    } else if (c == '$' || c == '#') {
                        ch = in.read();
                        if (ch == '{') {
                            xpath = c == '#';
                            inExpr = true;
                            if (buf.length() > 0) {
                                char[] charArray = new char[buf.length()];

                                buf.getChars(0, buf.length(), charArray, 0);
                                substitutions.add(charArray);
                                buf.setLength(0);
                            }
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
            // won't happen
            ignored.printStackTrace();
        }
        if (inExpr) {
            // unclosed #{} or ${}
            buf.insert(0, (xpath ? "#" : "$") + "{");
        }
        if (buf.length() > 0) {
            char[] charArray = new char[buf.length()];

            buf.getChars(0, buf.length(), charArray, 0);
            substitutions.add(charArray);
        } else if (substitutions.isEmpty()) {
            substitutions.add(ArrayUtils.EMPTY_CHAR_ARRAY);
        }
    }

    final List substitutions = new LinkedList();
    final char[] raw;

    public char[] getRaw() {
        return raw;
    }

    public List getSubstitutions() {
        return substitutions;
    }
}