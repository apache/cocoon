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
package org.apache.cocoon.template.jxtg.environment;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.cocoon.template.jxtg.environment.JSIntrospector;
import org.apache.cocoon.template.jxtg.expression.JXTExpression;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.jexl.util.introspection.Info;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.lang.StringUtils;
import org.mozilla.javascript.NativeArray;
import org.w3c.dom.Node;
import org.xml.sax.Locator;

/**
 */
public class ValueHelper {

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

    public static Locale parseLocale(String locale, String variant) {
        Locale ret = null;
        String language = locale;
        String country = null;
        int index = StringUtils.indexOfAny(locale, "-_");
    
        if (index > -1) {
            language = locale.substring(0, index);
            country = locale.substring(index + 1);
        }
        if (StringUtils.isEmpty(language)) {
            throw new IllegalArgumentException("No language in locale");
        }
        if (country == null) {
            ret = variant != null ? new Locale(language, "", variant) : new Locale(language, ""); 
        } else if (country.length() > 0) {
            ret = variant != null ? new Locale(language, country, variant) : new Locale(language, country); 
        } else {
            throw new IllegalArgumentException("Empty country in locale");
        }
        return ret;
    }

    // Hack: try to prevent JXPath from converting result to a String
    public static Object getNode(JXTExpression expr, JexlContext jexlContext, JXPathContext jxpathContext, Boolean lenient)
                throws Exception {
        try {
            Object compiled = expr.getCompiledExpression();
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
            return expr.getRaw();
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Exception) {
                throw (Exception)t;
            }
            throw (Error)t;
        }
    }

    public static Object getNode(JXTExpression expr, JexlContext jexlContext, JXPathContext jxpathContext) throws Exception {
        return getNode(expr, jexlContext, jxpathContext, null);
    }

    public static Iterator getIterator(final JXTExpression items, JexlContext jexlContext, JXPathContext jxpathContext, Locator loc) throws Exception {
        Iterator iter = null;
        if (items != null) {
            if (items.getCompiledExpression() instanceof CompiledExpression) {
                CompiledExpression compiledExpression = 
                    (CompiledExpression) items.getCompiledExpression();
                Object val =
                    compiledExpression.getPointer(jxpathContext, items.getRaw()).getNode();
                // FIXME: workaround for JXPath bug
                iter =
                    val instanceof NativeArray ?
                    new JSIntrospector.NativeArrayIterator((NativeArray) val)
                        : compiledExpression.iteratePointers(jxpathContext);
            } else if (items.getCompiledExpression() instanceof Expression) {
                Expression e = (Expression) items.getCompiledExpression();
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
                        Object val = items;
                        
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

    public static Boolean getBooleanValue(JXTExpression expr, JexlContext jexlContext,
                                    JXPathContext jxpathContext) throws Exception {
        Object res = ValueHelper.getValue(expr, jexlContext, jxpathContext);
        return res instanceof Boolean ? (Boolean)res : null;
    }

    public static String getStringValue(JXTExpression expr, JexlContext jexlContext,
                                JXPathContext jxpathContext) throws Exception {
        Object res = ValueHelper.getValue(expr, jexlContext, jxpathContext);
        if (res != null) {
            return res.toString();
        }
        if (expr != null && expr.getCompiledExpression() == null) {
            return expr.getRaw();
        }
        return null;
    }

    public static Number getNumberValue(JXTExpression expr, JexlContext jexlContext, JXPathContext jxpathContext) throws Exception {
        Object res = ValueHelper.getValue(expr, jexlContext, jxpathContext);
        if (res instanceof Number) {
            return (Number)res;
        }
        if (res != null) {
            return Double.valueOf(res.toString());
        }
        return null;
    }

    public static int getIntValue(JXTExpression expr, JexlContext jexlContext, JXPathContext jxpathContext) throws Exception {
        Object res = ValueHelper.getValue(expr, jexlContext, jxpathContext);
        return res instanceof Number ? ((Number)res).intValue() : 0;
    }

    public static Object getValue(JXTExpression expr, JexlContext jexlContext, JXPathContext jxpathContext) throws Exception {
        return ValueHelper.getValue(expr, jexlContext, jxpathContext, null);
    }

    public static Object getValue(JXTExpression expr, JexlContext jexlContext,
            JXPathContext jxpathContext, Boolean lenient) throws Exception {
        if (expr != null) {
            Object compiled = expr.getCompiledExpression();
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
}
