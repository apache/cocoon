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
package org.apache.cocoon.template.jxtg.expression;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class Substitutions {

    final private List substitutions;
    final private boolean hasSubstitutions;

    public Substitutions(Locator location, String stringTemplate) throws SAXException {
        this(location, new StringReader(stringTemplate));
    }

    public Substitutions(Locator location, char[] chars, int start, int length)
        throws SAXException {
        this(location, new CharArrayReader(chars, start, length));
    }

    private Substitutions(Locator location, Reader in) throws SAXException {
        LinkedList substitutions = new LinkedList();
        StringBuffer buf = new StringBuffer();
        buf.setLength(0);
        int ch;
        boolean inExpr = false;
        boolean xpath = false;
        try {
        top:
            while ((ch = in.read()) != -1) {
                // column++;
                char c = (char) ch;
            processChar:
                while (true) {
                    if (inExpr) {
                        if (c == '\\') {
                            ch = in.read();
                            buf.append(ch == -1 ? '\\' : (char) ch);
                        } else if (c == '}') {
                            String str = buf.toString();
                            JXTExpression compiledExpression;
                            try {
                                compiledExpression = JXTExpression.compile(str, xpath);
                            } catch (Exception exc) {
                                throw new SAXParseException(exc.getMessage(),
                                                            location, exc);
                            } catch (Error err) {
                                throw new SAXParseException(err.getMessage(),
                                                            location,
                                                            new ErrorHolder(err));
                            }
                            substitutions.add(compiledExpression);
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
                                substitutions.add(new Literal(buf.toString()));
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
            String msg = "Unterminated " + (xpath ? "#" : "$") + "{";
            throw new SAXParseException(msg, location, null);
        }
        substitutions.add(new Literal(buf.toString()));

        this.substitutions = substitutions;
        this.hasSubstitutions = !substitutions.isEmpty();
    }

    public boolean hasSubstitutions() {
        return this.hasSubstitutions;
    }

    public Iterator iterator() {
        return this.substitutions.iterator();
    }

    public int size() {
        return this.substitutions.size();
    }

    public Object get(int pos) {
        return this.substitutions.get(pos);
    }

    public String toString(Locator location, ExpressionContext expressionContext)
        throws SAXException {
        StringBuffer buf = new StringBuffer();
        Iterator iterSubst = iterator();
        while (iterSubst.hasNext()) {
            Subst subst = (Subst) iterSubst.next();
            if (subst instanceof Literal) {
                Literal lit = (Literal) subst;
                buf.append(lit.getValue());
            } else if (subst instanceof JXTExpression) {
                JXTExpression expr = (JXTExpression) subst;
                Object val;
                try {
                    val = expr.getValue(expressionContext);
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(), location, e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(), location,
                                                new ErrorHolder(err));
                }
                buf.append(val != null ? val.toString() : "");
            }
        }
        return buf.toString();
    }
}
