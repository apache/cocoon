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

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.cocoon.components.expression.Expression;
import org.apache.cocoon.components.expression.ExpressionContext;
import org.apache.cocoon.components.expression.jexl.JexlCompiler;
import org.apache.cocoon.components.expression.jxpath.JXPathCompiler;
import org.apache.cocoon.components.expression.jxpath.JXPathExpression;
import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class JXTExpression extends Subst {

    private String raw;
    private Object compiledExpression;

    private static JXPathCompiler jxpathCompiler = new JXPathCompiler();
    private static JexlCompiler jexlCompiler = new JexlCompiler();
    private static String JXPATH = "jxpath";
    private static String JEXL = "jexl";

    // Factory methods

    public static JXTExpression compile(final String expression, boolean xpath)
        throws Exception {
        Expression compiled;
        if (xpath) {
            compiled = jxpathCompiler.compile(JXPATH, expression);
        } else {
            compiled = jexlCompiler.compile(JEXL, expression);
        }
        return new JXTExpression(expression, compiled);
    }

    public static JXTExpression compileBoolean(String val, String msg,
                                               Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res.getCompiledExpression() == null && res.getRaw() != null) {
            res.setCompiledExpression(Boolean.valueOf(res.getRaw()));
        }
        return res;
    }

    /*
     * Compile an integer expression (returns either a Compiled Expression or an
     * Integer literal)
     */
    public static JXTExpression compileInt(String val, String msg,
            Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res.getCompiledExpression() == null && res.getRaw() != null) {
            res.setCompiledExpression(Integer.valueOf(res.getRaw()));
        }
        return res;
    }

    public static JXTExpression compileExpr(String inStr) throws Exception {
        try {
            if (inStr == null) {
                return new JXTExpression(null, null);
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


    // Members

    private JXTExpression(String raw, Object expr) {
        this.raw = raw;
        this.compiledExpression = expr;
    }

    private Object getCompiledExpression() {
        return compiledExpression;
    }

    private void setCompiledExpression(Object compiledExpression) {
        this.compiledExpression = compiledExpression;
    }

    private String getRaw() {
        return raw;
    }

    // Geting the value of the expression in various forms

    // Hack: try to prevent JXPath from converting result to a String
    public Object getNode(ExpressionContext expressionContext)
        throws Exception {
        Object compiled = this.getCompiledExpression();
        if (compiled instanceof Expression)
            return ((Expression)compiled).getNode(expressionContext);
        return this.getRaw();
    }

    public Iterator getIterator(ExpressionContext expressionContext)
        throws Exception {
        Iterator iter = null;
        if (this.getCompiledExpression() != null || this.getRaw() != null) {
            if (this.getCompiledExpression() instanceof Expression) {
                iter =
                    ((Expression)this.getCompiledExpression()).iterate(expressionContext);
            } else {
                // literal value
                iter = new Iterator() {
                        Object val = this;
                        
                        public boolean hasNext() {
                            return val != null;
                        }
                        
                        public Object next() {
                            Object res = val;
                            val = null;
                            return res;
                        }
                        
                        public void remove() {
                            // EMPTY
                        }
                    };
            }
        } else {
            iter = NULL_ITER;
        }
        return iter;
    }

    public Boolean getBooleanValue(ExpressionContext expressionContext)
        throws Exception {
        Object res = getValue(expressionContext);
        return res instanceof Boolean ? (Boolean)res : null;
    }

    public String getStringValue(ExpressionContext expressionContext)
        throws Exception {
        Object res = getValue(expressionContext);
        if (res != null) {
            return res.toString();
        }
        if (this.getCompiledExpression() == null) {
            return this.getRaw();
        }
        return null;
    }

    public Number getNumberValue(ExpressionContext expressionContext)
        throws Exception {
        Object res = getValue(expressionContext);
        if (res instanceof Number) {
            return (Number)res;
        }
        if (res != null) {
            return Double.valueOf(res.toString());
        }
        return null;
    }

    public int getIntValue(ExpressionContext expressionContext)
        throws Exception {
        Object res = getValue(expressionContext);
        return res instanceof Number ? ((Number)res).intValue() : 0;
    }

    public Object getValue(ExpressionContext expressionContext)
        throws Exception {
        if (this.getCompiledExpression() != null) {
            Object compiled = this.getCompiledExpression();
            if (compiled instanceof Expression)
                return ((Expression)compiled).evaluate(expressionContext);
            else
                return compiled;
        } else
            return null;
    }

    public void setLenient(Boolean lenient) {
        if (this.compiledExpression instanceof Expression)
            ((Expression)this.compiledExpression).setProperty(JXPathExpression.LENIENT, lenient);
    }

    private static final Iterator EMPTY_ITER = new Iterator() {
        public boolean hasNext() {
            return false;
        }

        public Object next() {
            return null;
        }

        public void remove() {
            // EMPTY
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
            // EMPTY
        }
    };
}
