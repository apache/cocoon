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
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.cocoon.template.jxtg.environment.ErrorHolder;
import org.apache.cocoon.template.jxtg.environment.JSIntrospector;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.jexl.util.introspection.Info;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.mozilla.javascript.NativeArray;
import org.w3c.dom.Node;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class JXTExpression extends Subst {

    // Factory classes

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

    public JXTExpression(String raw, Object expr) {
        this.raw = raw;
        this.compiledExpression = expr;
    }

    String raw;
    Object compiledExpression;

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
    public Object getNode(JexlContext jexlContext, JXPathContext jxpathContext, Boolean lenient)
        throws Exception {
        try {
            Object compiled = this.getCompiledExpression();
            if (compiled instanceof CompiledExpression) {
                CompiledExpression e = (CompiledExpression)compiled;
                boolean oldLenient = jxpathContext.isLenient();
                if (lenient != null) jxpathContext.setLenient(lenient.booleanValue());
                try {
                    Iterator iter = e.iteratePointers(jxpathContext);
                    if (iter.hasNext()) {
                        Pointer first = (Pointer)iter.next();
                        if (iter.hasNext()) {
                            List result = new LinkedList();
                            result.add(first.getNode());
                            boolean dom = (first.getNode() instanceof Node);
                            while (iter.hasNext()) {
                                Object obj = ((Pointer)iter.next()).getNode();
                                dom = dom && (obj instanceof Node);
                                result.add(obj);
                            }
                            Object[] arr;
                            if (dom) {
                                arr = new Node[result.size()];
                            } else {
                                arr = new Object[result.size()];
                            }
                            result.toArray(arr);
                            return arr;
                        }
                        return first.getNode();                    
                    }
                    return null;
                } finally {
                    jxpathContext.setLenient(oldLenient);
                }
            } else if (compiled instanceof Expression) {
                Expression e = (Expression)compiled;
                return e.evaluate(jexlContext);
            }
            return this.getRaw();
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Exception) {
                throw (Exception)t;
            }
            throw (Error)t;
        }
    }

    public Object getNode(JexlContext jexlContext, JXPathContext jxpathContext)
        throws Exception {
        return getNode(jexlContext, jxpathContext, null);
    }

    public Iterator getIterator(JexlContext jexlContext, JXPathContext jxpathContext, Locator loc) throws Exception {
        Iterator iter = null;
        if (this.getCompiledExpression() != null || this.getRaw() != null) {
            if (this.getCompiledExpression() instanceof CompiledExpression) {
                CompiledExpression compiledExpression = 
                    (CompiledExpression) this.getCompiledExpression();
                Object val =
                    compiledExpression.getPointer(jxpathContext, this.getRaw()).getNode();
                // FIXME: workaround for JXPath bug
                iter =
                    val instanceof NativeArray ?
                    new JSIntrospector.NativeArrayIterator((NativeArray) val)
                        : compiledExpression.iteratePointers(jxpathContext);
            } else if (this.getCompiledExpression() instanceof Expression) {
                Expression e = (Expression) this.getCompiledExpression();
                Object result = e.evaluate(jexlContext);
                if (result != null) {
                    iter = Introspector.getUberspect().getIterator(
                                                                   result,
                                                                   new Info(
                                                                            loc.getSystemId(),
                                                                            loc.getLineNumber(),
                                                                            loc.getColumnNumber()));
                }
                if (iter == null) {
                    iter = EMPTY_ITER;
                }
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

    public Boolean getBooleanValue(JexlContext jexlContext, JXPathContext jxpathContext)
        throws Exception {
        Object res = getValue(jexlContext, jxpathContext);
        return res instanceof Boolean ? (Boolean)res : null;
    }

    public String getStringValue(JexlContext jexlContext, JXPathContext jxpathContext)
        throws Exception {
        Object res = getValue(jexlContext, jxpathContext);
        if (res != null) {
            return res.toString();
        }
        if (this.getCompiledExpression() == null) {
            return this.getRaw();
        }
        return null;
    }

    public Number getNumberValue(JexlContext jexlContext, JXPathContext jxpathContext)
        throws Exception {
        Object res = getValue(jexlContext, jxpathContext);
        if (res instanceof Number) {
            return (Number)res;
        }
        if (res != null) {
            return Double.valueOf(res.toString());
        }
        return null;
    }

    public int getIntValue(JexlContext jexlContext, JXPathContext jxpathContext)
        throws Exception {
        Object res = getValue(jexlContext, jxpathContext);
        return res instanceof Number ? ((Number)res).intValue() : 0;
    }

    public Object getValue(JexlContext jexlContext, JXPathContext jxpathContext)
        throws Exception {
        return getValue(jexlContext, jxpathContext, null);
    }

    public Object getValue(JexlContext jexlContext, JXPathContext jxpathContext,
                                  Boolean lenient) throws Exception {
        if (this.getCompiledExpression() != null) {
            Object compiled = this.getCompiledExpression();
            try {
                if (compiled instanceof CompiledExpression) {
                    CompiledExpression e = (CompiledExpression) compiled;
                    boolean oldLenient = jxpathContext.isLenient();
                    if (lenient != null) {
                        jxpathContext.setLenient(lenient.booleanValue());
                    }
                    try {
                        return e.getValue(jxpathContext);
                    } finally {
                        jxpathContext.setLenient(oldLenient);
                    }
                } else if (compiled instanceof Expression) {
                    Expression e = (Expression) compiled;
                    return e.evaluate(jexlContext);
                }
                return compiled;
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof Exception) {
                    throw (Exception) t;
                }
                throw (Error) t;
            }
        } else {
            return null;
        }
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
