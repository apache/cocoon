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
package org.apache.cocoon.generation;

import java.beans.PropertyDescriptor;
import java.io.CharArrayReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.TimeZone;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.javascript.fom.FOM_JavaScriptFlowHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.transformation.ServiceableTransformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.IncludeXMLConsumer;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMBuilder;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.jexl.util.introspection.Info;
import org.apache.commons.jexl.util.introspection.UberspectImpl;
import org.apache.commons.jexl.util.introspection.VelMethod;
import org.apache.commons.jexl.util.introspection.VelPropertyGet;
import org.apache.commons.jexl.util.introspection.VelPropertySet;
import org.apache.commons.jxpath.CompiledExpression;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathContextFactory;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.XMLizable;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeArray;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.LocatorImpl;

/**
 * @cocoon.sitemap.component.documentation
 * Provides a generic page template with embedded JSTL and XPath
 * expression substitution to access data sent by Cocoon Flowscripts.
 *  
 * @cocoon.sitemap.component.name   jx
 * @cocoon.sitemap.component.label  content
 * @cocoon.sitemap.component.logger sitemap.generator.jx
 * 
 * @cocoon.sitemap.component.pooling.min   2
 * @cocoon.sitemap.component.pooling.max  16
 * @cocoon.sitemap.component.pooling.grow  2
 * 
 *
 * @version CVS $Id: JXTemplateGenerator.java,v 1.48 2004/06/27 17:40:10 antonio Exp $
 */
public class JXTemplateGenerator extends ServiceableGenerator implements CacheableProcessingComponent {

    private static final JXPathContextFactory
        jxpathContextFactory = JXPathContextFactory.newInstance();

    private static final Attributes EMPTY_ATTRS = new AttributesImpl();

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

    private static final Locator NULL_LOCATOR = new LocatorImpl();

    private XMLConsumer getConsumer() {
        return this.xmlConsumer;
    }

    public static class ErrorHolder extends Exception {

        private Error err;

        public ErrorHolder(Error err) {
            super(err.getMessage());
            this.err = err;
        }

        public void printStackTrace(PrintStream ps) {
            err.printStackTrace(ps);
        }

        public void printStackTrace(PrintWriter pw) {
            err.printStackTrace(pw);
        }

        public void printStackTrace() {
            err.printStackTrace();
        }

        public Error getError() {
            return err;
        }

    }

    /**
     * Facade to the Locator to be set on the consumer prior to
     * sending other events, location member changeable
     */
    public class LocatorFacade implements Locator {
        private Locator locator;

        public LocatorFacade(Locator initialLocator) {
            this.locator = initialLocator;
        }

        public void setDocumentLocator(Locator newLocator) {
            this.locator = newLocator;
        }

        public int getColumnNumber() {
            return this.locator.getColumnNumber();
        }

        public int getLineNumber() {
            return this.locator.getLineNumber();
        }

        public String getPublicId() {
            return this.locator.getPublicId();
        }

        public String getSystemId() {
            return this.locator.getSystemId();
        }
    }

    /**
     * Jexl Introspector that supports Rhino JavaScript objects
     * as well as Java Objects
     */
    static class JSIntrospector extends UberspectImpl {

        static class JSMethod implements VelMethod {

            Scriptable scope;
            String name;

            public JSMethod(Scriptable scope, String name) {
                this.scope = scope;
                this.name = name;
            }

            public Object invoke(Object thisArg, Object[] args)
                throws Exception {
                Context cx = Context.enter();
                try {
                    Object result;
                    Scriptable thisObj;
                    if (!(thisArg instanceof Scriptable)) {
                        thisObj = Context.toObject(thisArg, scope);
                    } else {
                        thisObj = (Scriptable)thisArg;
                    }
                    result = ScriptableObject.getProperty(thisObj, name);
                    Object[] newArgs = null;
                    if (args != null) {
                        newArgs = new Object[args.length];
                        int len = args.length;
                        for (int i = 0; i < len; i++) {
                            newArgs[i] = args[i];
                            if (args[i] != null &&
                                !(args[i] instanceof Number) &&
                                !(args[i] instanceof Boolean) &&
                                !(args[i] instanceof String) &&
                                !(args[i] instanceof Scriptable)) {
                                newArgs[i] = Context.toObject(args[i], scope);
                            }
                        }
                    }
                    result = ScriptRuntime.call(cx, result, thisObj,
                                                newArgs, scope);
                    if (result == Undefined.instance ||
                        result == Scriptable.NOT_FOUND) {
                        result = null;

                    } else {
                        if (!(result instanceof NativeJavaClass)) {
                            while (result instanceof Wrapper) {
                                result = ((Wrapper)result).unwrap();
                            }
                        }
                    }
                    return result;
                } catch (JavaScriptException e) {
                    throw new java.lang.reflect.InvocationTargetException(e);
                } finally {
                    Context.exit();
                }
            }

            public boolean isCacheable() {
                return false;
            }

            public String getMethodName() {
                return name;
            }

            public Class getReturnType() {
                return Object.class;
            }

        }

        static class JSPropertyGet implements VelPropertyGet {

            Scriptable scope;
            String name;

            public JSPropertyGet(Scriptable scope, String name) {
                this.scope = scope;
                this.name = name;
            }

            public Object invoke(Object thisArg) throws Exception {
                org.mozilla.javascript.Context cx = Context.enter();
                try {
                    Scriptable thisObj;
                    if (!(thisArg instanceof Scriptable)) {
                        thisObj = Context.toObject(thisArg, scope);
                    } else {
                        thisObj = (Scriptable)thisArg;
                    }
                    Object result = ScriptableObject.getProperty(thisObj, name);
                    if (result == Scriptable.NOT_FOUND) {
                        result = ScriptableObject.getProperty(thisObj, "get" + StringUtils.capitalize(name));
                        if (result != Scriptable.NOT_FOUND &&
                            result instanceof Function) {
                            try {
                                result = ((Function)result).call(cx,
                                                                 ScriptableObject.getTopLevelScope(thisObj), thisObj, new Object[] {});
                            } catch (JavaScriptException exc) {
                                exc.printStackTrace();
                                result = null;
                            }
                        }
                    }
                    if (result == Scriptable.NOT_FOUND ||
                        result == Undefined.instance) {
                        result = null;
                    } else if (result instanceof Wrapper) {
                        if (!(result instanceof NativeJavaClass)) {
                            result = ((Wrapper)result).unwrap();
                        }
                    }
                    return result;
                } finally {
                    Context.exit();
                }
            }

            public boolean isCacheable() {
                return false;
            }

            public String getMethodName() {
                return name;
            }

        }

        static class JSPropertySet implements VelPropertySet {

            Scriptable scope;
            String name;

            public JSPropertySet(Scriptable scope, String name) {
                this.scope = scope;
                this.name = name;
            }

            public Object invoke(Object thisArg, Object rhs) throws Exception {
                Context.enter();
                try {
                    Scriptable thisObj;
                    Object arg = rhs;
                    if (!(thisArg instanceof Scriptable)) {
                        thisObj = Context.toObject(thisArg, scope);
                    } else {
                        thisObj = (Scriptable)thisArg;
                    }
                    if (arg != null &&
                        !(arg instanceof Number) &&
                        !(arg instanceof Boolean) &&
                        !(arg instanceof String) &&
                        !(arg instanceof Scriptable)) {
                        arg = Context.toObject(arg, scope);
                    }
                    ScriptableObject.putProperty(thisObj, name, arg);
                    return rhs;
                } finally {
                    Context.exit();
                }
            }

            public boolean isCacheable() {
                return false;
            }

            public String getMethodName() {
                return name;
            }
        }

        static class NativeArrayIterator implements Iterator {

            NativeArray arr;
            int index;

            public NativeArrayIterator(NativeArray arr) {
                this.arr = arr;
                this.index = 0;
            }

            public boolean hasNext() {
                return index < (int)arr.jsGet_length();
            }

            public Object next() {
                Context.enter();
                try {
                    Object result = arr.get(index++, arr);
                    if (result == Undefined.instance ||
                        result == Scriptable.NOT_FOUND) {
                        result = null;
                    } else {
                        if (!(result instanceof NativeJavaClass)) {
                            while (result instanceof Wrapper) {
                                result = ((Wrapper)result).unwrap();
                            }
                        }
                    }
                    return result;
                } finally {
                    Context.exit();
                }
            }

            public void remove() {
                arr.delete(index);
            }
        }

        static class ScriptableIterator implements Iterator {

            Scriptable scope;
            Object[] ids;
            int index;

            public ScriptableIterator(Scriptable scope) {
                this.scope = scope;
                this.ids = scope.getIds();
                this.index = 0;
            }

            public boolean hasNext() {
                return index < ids.length;
            }

            public Object next() {
                Context.enter();
                try {
                    Object result =
                        ScriptableObject.getProperty(scope,
                                                     ids[index++].toString());
                    if (result == Undefined.instance ||
                        result == Scriptable.NOT_FOUND) {
                        result = null;
                    } else {
                        if (!(result instanceof NativeJavaClass)) {
                            while (result instanceof Wrapper) {
                                result = ((Wrapper)result).unwrap();
                            }
                        }
                    }
                    return result;
                } finally {
                    Context.exit();
                }
            }

            public void remove() {
                Context.enter();
                try {
                    scope.delete(ids[index].toString());
                } finally {
                    Context.exit();
                }
            }
        }

        public Iterator getIterator(Object obj, Info i)
            throws Exception {
            if (!(obj instanceof Scriptable)) {
                // support Enumeration
                if (obj instanceof Enumeration) {
                    final Enumeration e = (Enumeration)obj;
                    return new Iterator() {

                            public boolean hasNext() {
                                return e.hasMoreElements();
                            }

                            public Object next() {
                                return e.nextElement();
                            }

                            public void remove() {
                                // no action
                            }

                        };
                } else if (obj instanceof Iterator) {
                    // support Iterator
                    return (Iterator)obj;
                }
                return super.getIterator(obj, i);
            }
            if (obj instanceof NativeArray) {
                return new NativeArrayIterator((NativeArray)obj);
            }
            return new ScriptableIterator((Scriptable)obj);
        }

        public VelMethod getMethod(Object obj, String methodName,
                                   Object[] args, Info i)
            throws Exception {
            if (!(obj instanceof Scriptable)) {
                return super.getMethod(obj, methodName, args, i);
            }
            return new JSMethod((Scriptable)obj, methodName);
        }

        public VelPropertyGet getPropertyGet(Object obj, String identifier,
                                             Info i)
            throws Exception {
            if (!(obj instanceof Scriptable)) {
                return super.getPropertyGet(obj, identifier, i);
            }
            return new JSPropertyGet((Scriptable)obj, identifier);
        }

        public VelPropertySet getPropertySet(Object obj, String identifier,
                                             Object arg, Info i)
            throws Exception {
            if (!(obj instanceof Scriptable)) {
                return super.getPropertySet(obj, identifier, arg, i);
            }
            return new JSPropertySet((Scriptable)obj, identifier);
        }
    }

    static class MyJexlContext extends HashMap implements JexlContext {

        private MyJexlContext closure;

        MyJexlContext() {
            this(null);
        }

        MyJexlContext(MyJexlContext closure) {
            this.closure = closure;
        }

        public Map getVars() {
            return this;
        }

        public void setVars(Map map) {
            putAll(map);
        }

        public boolean containsKey(Object key) {
            return this.get(key) !=null;
        }

        public Object get(Object key) {
            if (key.equals("this")) {
                return this;
            }
            Object result = super.get(key);
            if (result == null && closure != null) {
                result = closure.get(key);
            }
            return result;
        }
    }

    static class MyVariables implements Variables {

        MyVariables closure;

        Map localVariables = new HashMap();

        static final String[] VARIABLES = new String[] {
            "cocoon",
            "continuation",
            "flowContext",
            "request",
            "response",
            "context",
            "session",
            "parameters"
        };

        Object cocoon;

        // backward compatibility
        Object bean, kont, request, response,
            session, context, parameters;

        MyVariables(Object cocoon,
                    Object bean,
                    WebContinuation kont,
                    Object request,
                    Object session,
                    Object context,
                    Object parameters) {
            this.cocoon = cocoon;
            this.bean = bean;
            this.kont = kont;
            this.request = request;
            this.session = session;
            this.context = context;
            this.parameters = parameters;
        }

        public MyVariables(MyVariables parent) {
            this.closure = parent;
        }

        public boolean isDeclaredVariable(String varName) {
            int len = VARIABLES.length;
            for (int i = 0; i < len; i++) {
                if (varName.equals(VARIABLES[i])) {
                    return true;
                }
            }
            if (localVariables.containsKey(varName)) {
                return true;
            }
            if (closure != null) {
                return closure.isDeclaredVariable(varName);
            }
            return false;
        }

        public Object getVariable(String varName) {
            Object result = localVariables.get(varName);
            if (result != null) {
                return result;
            }
            if (closure != null) {
                return closure.getVariable(varName);
            }
            if (varName.equals("cocoon")) {
                return cocoon;
            }
            // backward compatibility
            if (varName.equals("continuation")) {
                return kont;
            } else if (varName.equals("flowContext")) {
                return bean;
            } else if (varName.equals("request")) {
                return request;
            } else if (varName.equals("session")) {
                return session;
            } else if (varName.equals("context")) {
                return context;
            } else if (varName.equals("parameters")) {
                return parameters;
            }
            return null;
        }

        public void declareVariable(String varName, Object value) {
            localVariables.put(varName, value);
        }

        public void undeclareVariable(String varName) {
            localVariables.remove(varName);
        }
    }

    static {
        // Hack: there's no _nice_ way to add my introspector to Jexl right now
        try {
            Field field =
                org.apache.commons.jexl.util.Introspector.class.getDeclaredField("uberSpect");
            field.setAccessible(true);
            field.set(null, new JSIntrospector());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** The namespace used by this generator */
    public final static String NS = "http://apache.org/cocoon/templates/jx/1.0";

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
    final static String CACHE_KEY = "cache-key";
    final static String VALIDITY = "cache-validity";

    /**
     * Compile a single Jexl expr (contained in ${}) or XPath expression
     * (contained in #{})
     */

    private static JXTExpression compileExpr(String expr, String errorPrefix,
                                   Locator location) throws SAXParseException {
        try {
            return compileExpr(expr);
        } catch (Exception exc) {
            throw new SAXParseException(errorPrefix + exc.getMessage(),
                                                            location, exc);
        } catch (Error err) {
            throw new SAXParseException(errorPrefix + err.getMessage(),
                                                            location,
                                        new ErrorHolder(err));
        }
    }

    private static JXTExpression compileExpr(String inStr) throws Exception {
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
                char c = (char)ch;
                if (inExpr) {
                    if (c == '\\') {
                        ch = in.read();
                        if (ch == -1) {
                            expr.append('\\');
                        } else {
                            expr.append((char)ch);
                        }
                    } else if (c == '}') {
                        String str = expr.toString();
                        return compile(str, xpath);
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
                String msg;
                if (xpath) {
                    msg = "Unterminated #{";
                } else {
                    msg = "Unterminated ${";
                }
                throw new Exception(msg);
            }
        } catch (IOException ignored) {
            ignored.printStackTrace();
        }
        return new JXTExpression(inStr, null);
    }

    /*
     * Compile an integer expression (returns either a Compiled Expression
     * or an Integer literal)
     */
    private static JXTExpression compileInt(String val, String msg,
                                        Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res != null) {
            if (res.compiledExpression == null) {
                res.compiledExpression = Integer.valueOf(res.raw);
            }
            return res;
        }
        return null;
    }

    private static JXTExpression compileBoolean(String val, String msg,
                                        Locator location) throws SAXException {
        JXTExpression res = compileExpr(val, msg, location);
        if (res != null) {
            if (res.compiledExpression == null) {
                res.compiledExpression = Boolean.valueOf(res.raw);
            }
            return res;
        }
        return null;
    }

    private static JXTExpression compile(final String variable, boolean xpath)
                throws Exception {
        Object compiled;
        if (xpath) {
            compiled = JXPathContext.compile(variable);
        } else {
            compiled = ExpressionFactory.createExpression(variable);
        }
        return new JXTExpression(variable, compiled);
    }

    static private Object getValue(JXTExpression expr, JexlContext jexlContext,
                            JXPathContext jxpathContext, Boolean lenient)
        throws Exception {
        if (expr != null) {
            Object compiled = expr.compiledExpression;
            try {
                if (compiled instanceof CompiledExpression) {
                    CompiledExpression e = (CompiledExpression)compiled;
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
                    Expression e = (Expression)compiled;
                    return e.evaluate(jexlContext);
                }
                return compiled;
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof Exception) {
                    throw (Exception)t;
                }
                throw (Error)t;
            }
        } else {
            return null;
        }
    }

    static private Object getValue(JXTExpression expr, JexlContext jexlContext, JXPathContext jxpathContext) throws Exception {
        return getValue(expr, jexlContext, jxpathContext, null);
    }

    static private int getIntValue(JXTExpression expr, JexlContext jexlContext,
                                JXPathContext jxpathContext) throws Exception {
        Object res = getValue(expr, jexlContext, jxpathContext);
        if (res instanceof Number) {
            return ((Number)res).intValue();
        }
        return 0;
    }

    static private Number getNumberValue(JXTExpression expr, JexlContext jexlContext,
                               JXPathContext jxpathContext) throws Exception {
        Object res = getValue(expr, jexlContext, jxpathContext);
        if (res instanceof Number) {
            return (Number)res;
        }
        if (res != null) {
            return Double.valueOf(res.toString());
        }
        return null;
    }

    static private String getStringValue(JXTExpression expr, JexlContext jexlContext,
                                JXPathContext jxpathContext) throws Exception {
        Object res = getValue(expr, jexlContext, jxpathContext);
        if (res != null) {
            return res.toString();
        }
        if (expr != null && expr.compiledExpression == null) {
            return expr.raw;
        }
        return null;
    }

    static private Boolean getBooleanValue(JXTExpression expr, JexlContext jexlContext,
                                    JXPathContext jxpathContext)
        throws Exception {
        Object res = getValue(expr, jexlContext, jxpathContext);
        if (res instanceof Boolean) {
            return (Boolean)res;
        }
        return null;
    }

    // Hack: try to prevent JXPath from converting result to a String
    private Object getNode(JXTExpression expr, JexlContext jexlContext,
                           JXPathContext jxpathContext, Boolean lenient)
        throws Exception {
        try {
            Object compiled = expr.compiledExpression;
            if (compiled instanceof CompiledExpression) {
                CompiledExpression e = (CompiledExpression)compiled;
                boolean oldLenient = jxpathContext.isLenient();
                if (lenient != null) jxpathContext.setLenient(lenient.booleanValue());
                try {
                    Iterator iter = 
                        e.iteratePointers(jxpathContext);
                    if (!iter.hasNext()) {
                        return null;
                    }
                    Pointer first = (Pointer)iter.next();
                    if (!iter.hasNext()) {
                        return first.getNode();
                    }
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
                } finally {
                    jxpathContext.setLenient(oldLenient);
                }
            } else if (compiled instanceof org.apache.commons.jexl.Expression) {
                org.apache.commons.jexl.Expression e =
                    (org.apache.commons.jexl.Expression)compiled;
                return e.evaluate(jexlContext);
            }
            return expr.raw;
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof Exception) {
                throw (Exception)t;
            }
            throw (Error)t;
        }
    }

    private Object getNode(JXTExpression expr, JexlContext jexlContext, JXPathContext jxpathContext) throws Exception {
        return getNode(expr, jexlContext, jxpathContext, null);
    }

    static class Event {
        final Locator location;
        Event next; // in document order
        Event(Locator locator) {
            if (locator != null) {
                this.location = new LocatorImpl(locator);
            } else {
                this.location = NULL_LOCATOR;
            }
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
        TextEvent(Locator location, char[] chars, int start, int length)
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
                    char c = (char)ch;
                    processChar: while (true) {
                        if (inExpr) {
                            if (c == '\\') {
                                ch = in.read();
                                if (ch == -1) {
                                    buf.append('\\');
                                } else {
                                    buf.append((char)ch);
                                }
                            } else if (c == '}') {
                                String str = buf.toString();
                                Object compiledExpression;
                                try {
                                    if (xpath) {
                                        compiledExpression =
                                            JXPathContext.compile(str);
                                    } else {
                                        compiledExpression =
                                            ExpressionFactory.createExpression(str);
                                    }
                                } catch (Exception exc) {
                                    throw new SAXParseException(exc.getMessage(),
                                                                this.location,
                                                                exc);
                                } catch (Error err) {
                                    throw new SAXParseException(err.getMessage(),
                                                                this.location,
                                                                new ErrorHolder(err));

                                }
                                substitutions.add(new JXTExpression(str,
                                                                 compiledExpression));
                                buf.setLength(0);
                                inExpr = false;
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
                            } else if (c == '$' || c == '#') {
                                ch = in.read();
                                if (ch == '{') {
                                    xpath = c == '#';
                                    inExpr = true;
                                    if (buf.length() > 0) {
                                        char[] charArray =
                                            new char[buf.length()];

                                        buf.getChars(0, buf.length(),
                                                     charArray, 0);
                                        substitutions.add(charArray);
                                        buf.setLength(0);
                                    }
                                    continue top;
                                } else {
                                    buf.append(c);
                                    if (ch != -1) {
                                        c = (char)ch;
                                        continue processChar;
                                    }
                                }
                            } else {
                                buf.append(c);
                            }
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
                String str;
                if (xpath) {
                    str = "#{";
                } else {
                    str = "${";
                }
                buf.insert(0, str);
            }
            if (buf.length() > 0) {
                char[] charArray =
                    new char[buf.length()];
                buf.getChars(0, buf.length(), charArray, 0);
                substitutions.add(charArray);
            } else if (substitutions.size() == 0) {
                substitutions.add(ArrayUtils.EMPTY_CHAR_ARRAY);
            }
        }
        final List substitutions = new LinkedList();
        final char[] raw;
    }

    static class Characters extends TextEvent {
        Characters(Locator location, char[] chars, int start, int length)
                                                          throws SAXException {
            super(location, chars, start, length);
        }
    }

    static class StartDocument extends Event {
        StartDocument(Locator location) {
            super(location);
            templateProperties = new HashMap();
        }
        SourceValidity compileTime;
        EndDocument endDocument; // null if document fragment
        Map templateProperties;
    }

    static class EndDocument extends Event {
        EndDocument(Locator location) {
            super(location);
        }
    }

    static class EndElement extends Event {
        EndElement(Locator location, StartElement startElement) {
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
        IgnorableWhitespace(Locator location, char[] chars,
                                   int start, int length) throws SAXException {
            super(location, chars, start, length);
        }
    }

    static class ProcessingInstruction extends Event {
        ProcessingInstruction(Locator location, String target, String data) {
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

    abstract static class AttributeEvent {
        AttributeEvent(String namespaceURI, String localName,
                                                     String raw, String type) {
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

    static class CopyAttribute extends AttributeEvent {
        CopyAttribute(String namespaceURI, String localName, String raw,
                                                   String type, String value) {
            super(namespaceURI, localName, raw, type);
            this.value = value;
        }
        final String value;
    }

    static class Subst {
    }

    static class Literal extends Subst {
        Literal(String val) {
            this.value = val;
        }
        final String value;
    }

    static class JXTExpression extends Subst {
        JXTExpression(String raw, Object expr) {
            this.raw = raw;
            this.compiledExpression = expr;
        }
        String raw;
        Object compiledExpression;
    }

    static class SubstituteAttribute extends AttributeEvent {
        SubstituteAttribute(String namespaceURI, String localName, String raw,
                                                    String type, List substs) {
            super(namespaceURI, localName, raw, type);
            this.substitutions = substs;
        }
        final List substitutions;
    }

    static class StartElement extends Event {
        StartElement(Locator location, String namespaceURI, String localName,
                            String raw, Attributes attrs) throws SAXException {
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
                        char c = (char)ch;
                        processChar: while (true) {
                            if (inExpr) {
                                if (c == '\\') {
                                    ch = in.read();
                                    if (ch == -1) {
                                        buf.append('\\');
                                    } else {
                                        buf.append((char)ch);
                                    }
                                } else if (c == '}') {
                                    String str = buf.toString();
                                    JXTExpression compiledExpression;
                                    try {
                                        compiledExpression =
                                            compile(str, xpath);
                                    } catch (Exception exc) {
                                        throw new SAXParseException(
                                                exc.getMessage(), location,
                                                exc);
                                    } catch (Error err) {
                                        throw new SAXParseException(
                                                err.getMessage(), location,
                                                new ErrorHolder(err));
                                    }
                                    substEvents.add(compiledExpression);
                                    buf.setLength(0);
                                    inExpr = false;
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
                                } if (c == '$' || c == '#') {
                                    ch = in.read();
                                    if (ch == '{') {
                                        if (buf.length() > 0) {
                                            substEvents.add(new Literal(buf.toString()));
                                            buf.setLength(0);
                                        }
                                        inExpr = true;
                                        xpath = c == '#';
                                        continue top;
                                    } else {
                                        buf.append(c);
                                        if (ch != -1) {
                                            c = (char)ch;
                                            continue processChar;
                                        }
                                    }
                                } else {
                                    buf.append(c);
                                }
                            }
                            break;
                        }
                    }
                } catch (IOException ignored) {
                    ignored.printStackTrace();
                }
                if (inExpr) {
                    // unclosed #{} or ${}
                    String msg;
                    if (xpath) {
                        msg = "Unterminated #{";
                    } else {
                        msg = "Unterminated ${";
                    }
                    throw new SAXParseException(msg, location, null);
                }
                if (buf.length() > 0) {
                    if (substEvents.size() == 0) {
                        attributeEvents.add(new CopyAttribute(uri, local,
                                                          qname, type, value));
                    } else {
                        substEvents.add(new Literal(buf.toString()));
                        attributeEvents.add(new SubstituteAttribute(uri, local,
                                                    qname, type, substEvents));
                    }
                } else {
                    if (substEvents.size() > 0) {
                        attributeEvents.add(new SubstituteAttribute(uri, local,
                                                     qname, type, substEvents));
                    } else {
                        attributeEvents.add(new CopyAttribute(uri, local,
                                                     qname, type, ""));
                    }
                }
            }
            this.attributes = new AttributesImpl(attrs);
        }
        final String namespaceURI;
        final String localName;
        final String raw;
        final String qname;
        final List attributeEvents = new LinkedList();
        final Attributes attributes;
        EndElement endElement;
    }

    static class StartPrefixMapping extends Event {
        StartPrefixMapping(Locator location, String prefix, String uri) {
            super(location);
            this.prefix = prefix;
            this.uri = uri;
        }
        final String prefix;
        final String uri;
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

    static class StartInstruction extends Event {
        StartInstruction(StartElement startElement) {
            super(startElement.location);
            this.startElement = startElement;
        }
        final StartElement startElement;
        EndInstruction endInstruction;
    }

    static class EndInstruction extends Event {
        EndInstruction(Locator locator, StartInstruction startInstruction) {
            super(locator);
            this.startInstruction = startInstruction;
            startInstruction.endInstruction = this;
        }
        final StartInstruction startInstruction;
    }

    static class StartForEach extends StartInstruction {
        StartForEach(StartElement raw, JXTExpression items, JXTExpression var,
                JXTExpression varStatus, JXTExpression begin, JXTExpression end,
                JXTExpression step, Boolean lenient) {
            super(raw);
            this.items = items;
            this.var = var;
            this.varStatus = varStatus;
            this.begin = begin;
            this.end = end;
            this.step = step;
            this.lenient = lenient;
        }
        final JXTExpression items;
        final JXTExpression var;
        final JXTExpression varStatus;
        final JXTExpression begin;
        final JXTExpression end;
        final JXTExpression step;
        final Boolean lenient;
    }

    static class StartIf extends StartInstruction {
        StartIf(StartElement raw, JXTExpression test) {
            super(raw);
            this.test = test;
        }
        final JXTExpression test;
    }

    static class StartChoose extends StartInstruction {
        StartChoose(StartElement raw) {
            super(raw);
        }
        StartWhen firstChoice;
        StartOtherwise otherwise;
    }

    static class StartWhen extends StartInstruction {
        StartWhen(StartElement raw, JXTExpression test) {
            super(raw);
            this.test = test;
        }
        final JXTExpression test;
        StartWhen nextChoice;
    }

    static class StartOtherwise extends StartInstruction {
        StartOtherwise(StartElement raw) {
            super(raw);
        }
    }

    static class StartOut extends StartInstruction {
        StartOut(StartElement raw, JXTExpression expr, Boolean lenient) {
            super(raw);
            this.compiledExpression = expr;
            this.lenient = lenient;
        }
        final JXTExpression compiledExpression;
        final Boolean lenient;
    }

    static class StartImport extends StartInstruction {
        StartImport(StartElement raw, AttributeEvent uri, JXTExpression select) {
            super(raw);
            this.uri = uri;
            this.select = select;
        }
        final AttributeEvent uri;
        final JXTExpression select;
    }

    static class StartTemplate extends StartInstruction {
        StartTemplate(StartElement raw) {
            super(raw);
        }
    }

    static class StartEvalBody extends StartInstruction {
        StartEvalBody(StartElement raw) {
            super(raw);
        }
    }

    static class StartEval extends StartInstruction {
        StartEval(StartElement raw, JXTExpression value) {
            super(raw);
            this.value = value;
        }
        final JXTExpression value;
    }

    static class StartDefine extends StartInstruction {
        StartDefine(StartElement raw, String namespace, String name) {
            super(raw);
            this.namespace = namespace;
            this.name = name;
            this.qname = "{"+namespace+"}"+name;
            this.parameters = new HashMap();
        }
        final String name;
        final String namespace;
        final String qname;
        final Map parameters;
        Event body;
        void finish() throws SAXException {
            Event e = next;
            boolean params = true;
            while (e != this.endInstruction) {
                if (e instanceof StartParameter) {
                    StartParameter startParam = (StartParameter)e;
                    if (!params) {
                        throw new SAXParseException("<parameter> not allowed here: \""+startParam.name +"\"", startParam.location, null);
                    }
                    Object prev =
                        parameters.put(startParam.name, startParam);
                    if (prev != null) {
                        throw new SAXParseException("duplicate parameter: \""+startParam.name +"\"", location, null);
                    }
                    e = startParam.endInstruction;
                } else if (e instanceof IgnorableWhitespace) {
                } else if (e instanceof Characters) {
                    // check for whitespace
                    char[] ch = ((TextEvent)e).raw;
                    int len = ch.length;
                    for (int i = 0; i < len; i++) {
                        if (!Character.isWhitespace(ch[i])) {
                            if (params) {
                                params = false;
                                body = e;
                            }
                            break;
                        }
                    }
                } else {
                    if (params) {
                        params = false;
                        body = e;
                    }
                }
                e = e.next;
            }
            if (this.body == null) {
                this.body = this.endInstruction;
            }
        }
    }

    static class StartParameter extends StartInstruction {
        StartParameter(StartElement raw, String name, String optional,
                       String default_) {
            super(raw);
            this.name = name;
            this.optional = optional;
            this.default_ = default_;
        }
        final String name;
        final String optional;
        final String default_;
    }

    static class StartSet extends StartInstruction {
        StartSet(StartElement raw, JXTExpression var, JXTExpression value) {
            super(raw);
            this.var = var;
            this.value = value;
        }
        final JXTExpression var;
        final JXTExpression value;
    }

    
     static class StartComment extends StartInstruction {
        StartComment(StartElement raw) {
            super(raw);
        }
    }

    // formatNumber tag (borrows from Jakarta taglibs JSTL)

    private static Locale parseLocale(String locale, String variant) {
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
            if (variant != null) {
                ret = new Locale(language, "", variant);
            } else {
                ret = new Locale(language, "");
            }
        } else if (country.length() > 0) {
            if (variant != null) {
                ret = new Locale(language, country, variant);
            } else {
                ret = new Locale(language, country);
            }
        } else {
            throw new IllegalArgumentException("Empty country in locale");
        }
        return ret;
    }

    private static final String NUMBER = "number";
    private static final String CURRENCY = "currency";
    private static final String PERCENT = "percent";

    static class StartFormatNumber extends StartInstruction {

        JXTExpression value;
        JXTExpression type;
        JXTExpression pattern;
        JXTExpression currencyCode;
        JXTExpression currencySymbol;
        JXTExpression isGroupingUsed;
        JXTExpression maxIntegerDigits;
        JXTExpression minIntegerDigits;
        JXTExpression maxFractionDigits;
        JXTExpression minFractionDigits;
        JXTExpression locale;

        JXTExpression var;

        private static Class currencyClass;

        static {
            try {
                currencyClass = Class.forName("java.util.Currency");
                // container's runtime is J2SE 1.4 or greater
            } catch (Exception cnfe) {
                // EMPTY
            }
        }

        public StartFormatNumber(StartElement raw,
                                 JXTExpression var,
                                 JXTExpression value,
                                 JXTExpression type,
                                 JXTExpression pattern,
                                 JXTExpression currencyCode,
                                 JXTExpression currencySymbol,
                                 JXTExpression isGroupingUsed,
                                 JXTExpression maxIntegerDigits,
                                 JXTExpression minIntegerDigits,
                                 JXTExpression maxFractionDigits,
                                 JXTExpression minFractionDigits,
                                 JXTExpression locale) {
            super(raw);
            this.var = var;
            this.value = value;
            this.type = type;
            this.pattern = pattern;
            this.currencyCode = currencyCode;
            this.currencySymbol = currencySymbol;
            this.isGroupingUsed = isGroupingUsed;
            this.maxIntegerDigits = maxIntegerDigits;
            this.minIntegerDigits = minIntegerDigits;
            this.maxFractionDigits = maxFractionDigits;
            this.minFractionDigits = minFractionDigits;
            this.locale = locale;
        }

        String format(JexlContext jexl, JXPathContext jxp)
            throws Exception {
            // Determine formatting locale
            String var = getStringValue(this.var, jexl, jxp);
            Number input = getNumberValue(this.value, jexl, jxp);
            String type = getStringValue(this.type, jexl, jxp);
            String pattern = getStringValue(this.pattern, jexl, jxp);
            String currencyCode = getStringValue(this.currencyCode, jexl, jxp);
            String currencySymbol = getStringValue(this.currencySymbol,
                                                 jexl, jxp);
            Boolean isGroupingUsed = getBooleanValue(this.isGroupingUsed,
                                                     jexl, jxp);
            Number maxIntegerDigits = getNumberValue(this.maxIntegerDigits,
                                                     jexl, jxp);
            Number minIntegerDigits = getNumberValue(this.minIntegerDigits,
                                                     jexl, jxp);
            Number maxFractionDigits = getNumberValue(this.maxFractionDigits,
                                                      jexl, jxp);
            Number minFractionDigits = getNumberValue(this.minFractionDigits,
                                                      jexl, jxp);
            String localeStr = getStringValue(this.locale, jexl, jxp);
            Locale loc;
            if (localeStr != null) {
                loc = parseLocale(localeStr, null);
            } else {
                loc = Locale.getDefault();
            }
            String formatted;
            if (loc != null) {
                // Create formatter
                NumberFormat formatter = null;
                if (StringUtils.isNotEmpty(pattern)) {
                    // if 'pattern' is specified, 'type' is ignored
                    DecimalFormatSymbols symbols = new DecimalFormatSymbols(loc);
                    formatter = new DecimalFormat(pattern, symbols);
                } else {
                    formatter = createFormatter(loc, type);
                }
                if (StringUtils.isNotEmpty(pattern)
                    || CURRENCY.equalsIgnoreCase(type)) {
                    setCurrency(formatter, currencyCode, currencySymbol);
                }
                configureFormatter(formatter,
                                   isGroupingUsed,
                                   maxIntegerDigits,
                                   minIntegerDigits,
                                   maxFractionDigits,
                                   minFractionDigits);
                formatted = formatter.format(input);
            } else {
                // no formatting locale available, use toString()
                formatted = input.toString();
            }
            if (var != null) {
                jexl.getVars().put(var, formatted);
                jxp.getVariables().declareVariable(var, formatted);
                return null;
            }
            return formatted;
        }

        private NumberFormat createFormatter(Locale loc,
                                             String type)
            throws Exception {
            NumberFormat formatter = null;
            if ((type == null) || NUMBER.equalsIgnoreCase(type)) {
                formatter = NumberFormat.getNumberInstance(loc);
            } else if (CURRENCY.equalsIgnoreCase(type)) {
                formatter = NumberFormat.getCurrencyInstance(loc);
            } else if (PERCENT.equalsIgnoreCase(type)) {
                formatter = NumberFormat.getPercentInstance(loc);
            } else {
                throw new IllegalArgumentException("Invalid type: \"" + type + "\": should be \"number\" or \"currency\" or \"percent\"");
            }
            return formatter;
        }

        /*
         * Applies the 'groupingUsed', 'maxIntegerDigits', 'minIntegerDigits',
         * 'maxFractionDigits', and 'minFractionDigits' attributes to the given
         * formatter.
         */
        private void configureFormatter(NumberFormat formatter,
                                        Boolean isGroupingUsed,
                                        Number maxIntegerDigits,
                                        Number minIntegerDigits,
                                        Number maxFractionDigits,
                                        Number minFractionDigits) {
            if (isGroupingUsed != null)
                formatter.setGroupingUsed(isGroupingUsed.booleanValue());
            if (maxIntegerDigits != null)
                formatter.setMaximumIntegerDigits(maxIntegerDigits.intValue());
            if (minIntegerDigits != null)
                formatter.setMinimumIntegerDigits(minIntegerDigits.intValue());
            if (maxFractionDigits != null)
                formatter.setMaximumFractionDigits(maxFractionDigits.intValue());
            if (minFractionDigits != null)
                formatter.setMinimumFractionDigits(minFractionDigits.intValue());
        }

        /*
         * Override the formatting locale's default currency symbol with the
         * specified currency code (specified via the "currencyCode" attribute) or
         * currency symbol (specified via the "currencySymbol" attribute).
         *
         * If both "currencyCode" and "currencySymbol" are present,
         * "currencyCode" takes precedence over "currencySymbol" if the
         * java.util.Currency class is defined in the container's runtime (that
         * is, if the container's runtime is J2SE 1.4 or greater), and
         * "currencySymbol" takes precendence over "currencyCode" otherwise.
         *
         * If only "currencyCode" is given, it is used as a currency symbol if
         * java.util.Currency is not defined.
         *
         * Example:
         *
         * JDK    "currencyCode" "currencySymbol" Currency symbol being displayed
         * -----------------------------------------------------------------------
         * all         ---            ---         Locale's default currency symbol
         *
         * <1.4        EUR            ---         EUR
         * >=1.4       EUR            ---         Locale's currency symbol for Euro
         *
         * all         ---           \u20AC       \u20AC
         *
         * <1.4        EUR           \u20AC       \u20AC
         * >=1.4       EUR           \u20AC       Locale's currency symbol for Euro
         */
        private void setCurrency(NumberFormat formatter,
                                 String currencyCode,
                                 String currencySymbol) throws Exception {
            String code = null;
            String symbol = null;

            if (currencyCode == null) {
                if (currencySymbol == null) {
                    return;
                }
                symbol = currencySymbol;
            } else if (currencySymbol != null) {
                if (currencyClass != null) {
                    code = currencyCode;
                } else {
                    symbol = currencySymbol;
                }
            } else if (currencyClass != null) {
                code = currencyCode;
            } else {
                symbol = currencyCode;
            }
            if (code != null) {
                Object[] methodArgs = new Object[1];

                /*
                 * java.util.Currency.getInstance()
                 */
                Method m = currencyClass.getMethod("getInstance",
                                                   new Class[] {String.class});

                methodArgs[0] = code;
                Object currency = m.invoke(null, methodArgs);

                /*
                 * java.text.NumberFormat.setCurrency()
                 */
                Class[] paramTypes = new Class[1];
                paramTypes[0] = currencyClass;
                Class numberFormatClass = Class.forName("java.text.NumberFormat");
                m = numberFormatClass.getMethod("setCurrency", paramTypes);
                methodArgs[0] = currency;
                m.invoke(formatter, methodArgs);
            } else {
                /*
                 * Let potential ClassCastException propagate up (will almost
                 * never happen)
                 */
                DecimalFormat df = (DecimalFormat) formatter;
                DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();
                dfs.setCurrencySymbol(symbol);
                df.setDecimalFormatSymbols(dfs);
            }
        }
    }

    // formatDate tag (borrows from Jakarta taglibs JSTL)

    static class StartFormatDate extends StartInstruction {

        private static final String DATE = "date";
        private static final String TIME = "time";
        private static final String DATETIME = "both";

        JXTExpression var;
        JXTExpression value;
        JXTExpression type;
        JXTExpression pattern;
        JXTExpression timeZone;
        JXTExpression dateStyle;
        JXTExpression timeStyle;
        JXTExpression locale;

        StartFormatDate(StartElement raw,
                        JXTExpression var,
                        JXTExpression value,
                        JXTExpression type,
                        JXTExpression pattern,
                        JXTExpression timeZone,
                        JXTExpression dateStyle,
                        JXTExpression timeStyle,
                        JXTExpression locale) {
            super(raw);
            this.var = var;
            this.value = value;
            this.type = type;
            this.pattern = pattern;
            this.timeZone = timeZone;
            this.dateStyle = dateStyle;
            this.timeStyle = timeStyle;
            this.locale = locale;
        }

        String format(JexlContext jexl, JXPathContext jxp)
            throws Exception {
            String var = getStringValue(this.var, jexl, jxp);
            Object value = getValue(this.value, jexl, jxp);
            Object locVal = getValue(this.locale,
                                     jexl, jxp);
            String pattern = getStringValue(this.pattern,
                                            jexl, jxp);
            Object timeZone = getValue(this.timeZone, jexl, jxp);

            String type = getStringValue(this.type, jexl, jxp);
            String timeStyle = getStringValue(this.timeStyle, jexl, jxp);
            String dateStyle = getStringValue(this.dateStyle, jexl, jxp);

            String formatted = null;

            // Create formatter
            Locale locale;
            if (locVal != null) {
                if (locVal instanceof Locale) {
                    locale = (Locale)locVal;
                } else {
                    locale = parseLocale(locVal.toString(), null);
                }
            } else {
                locale = Locale.getDefault();
            }
            DateFormat formatter = createFormatter(locale,
                                                   type,
                                                   dateStyle,
                                                   timeStyle);
            // Apply pattern, if present
            if (pattern != null) {
                try {
                    ((SimpleDateFormat) formatter).applyPattern(pattern);
                } catch (ClassCastException cce) {
                    formatter = new SimpleDateFormat(pattern, locale);
                }
            }
            // Set time zone
            TimeZone tz = null;
            if ((timeZone instanceof String)
                    && ((String) timeZone).equals("")) {
                timeZone = null;
            }
            if (timeZone != null) {
                if (timeZone instanceof String) {
                    tz = TimeZone.getTimeZone((String) timeZone);
                } else if (timeZone instanceof TimeZone) {
                    tz = (TimeZone) timeZone;
                } else {
                    throw new IllegalArgumentException("Illegal timeZone value: \""+timeZone+"\"");
                }
            }
            if (tz != null) {
                formatter.setTimeZone(tz);
            }
            formatted = formatter.format(value);
            if (var != null) {
                jexl.getVars().put(var, formatted);
                jxp.getVariables().declareVariable(var, formatted);
                return null;
            }
            return formatted;
        }

        private DateFormat createFormatter(Locale loc,
                                           String type,
                                           String dateStyle,
                                           String timeStyle)
            throws Exception {
            DateFormat formatter = null;
            if ((type == null) || DATE.equalsIgnoreCase(type)) {
                formatter = DateFormat.getDateInstance(getStyle(dateStyle),
                                                       loc);
            } else if (TIME.equalsIgnoreCase(type)) {
                formatter = DateFormat.getTimeInstance(getStyle(timeStyle),
                                                       loc);
            } else if (DATETIME.equalsIgnoreCase(type)) {
                formatter = DateFormat.getDateTimeInstance(getStyle(dateStyle),
                                                           getStyle(timeStyle),
                                                           loc);
            } else {
                throw new IllegalArgumentException("Invalid type: \""+ type+"\"");
            }
            return formatter;
        }

        private static final String DEFAULT = "default";
        private static final String SHORT = "short";
        private static final String MEDIUM = "medium";
        private static final String LONG = "long";
        private static final String FULL = "full";

        private int getStyle(String style) {
            int ret = DateFormat.DEFAULT;
            if (style != null) {
                if (DEFAULT.equalsIgnoreCase(style)) {
                    ret = DateFormat.DEFAULT;
                } else if (SHORT.equalsIgnoreCase(style)) {
                    ret = DateFormat.SHORT;
                } else if (MEDIUM.equalsIgnoreCase(style)) {
                    ret = DateFormat.MEDIUM;
                } else if (LONG.equalsIgnoreCase(style)) {
                    ret = DateFormat.LONG;
                } else if (FULL.equalsIgnoreCase(style)) {
                    ret = DateFormat.FULL;
                } else {
                    throw new IllegalArgumentException("Invalid style: \"" + style +"\": should be \"default\" or \"short\" or \"medium\" or \"long\" or \"full\"");
                }
            }
            return ret;
        }
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
            return this.startEvent;
        }


        void recycle() {
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
                lastEvent.next = ev;
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
                lastEvent.next = ev;
                lastEvent = ev;
                charLocation = null;
                charBuf = null;
            }
        }

        public void characters(char[] ch, int start, int length)
            throws SAXException {
            if (charBuf == null) {
                charBuf = new StringBuffer(length);
                if (locator != null) {
                    charLocation = new LocatorImpl(locator);
                } else {
                    charLocation = NULL_LOCATOR;
                }
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
                StartInstruction startInstruction =
                    (StartInstruction)start;
                EndInstruction endInstruction =
                    new EndInstruction(locator, startInstruction);
                newEvent = endInstruction;
                if (start instanceof StartWhen) {
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
                } else if (start instanceof StartOtherwise) {
                    StartOtherwise startOtherwise =
                        (StartOtherwise)start;
                    StartChoose startChoose = (StartChoose)stack.peek();
                    startChoose.otherwise = startOtherwise;
                }
            } else {
                StartElement startElement = (StartElement)start;
                newEvent = startElement.endElement =
                    new EndElement(locator, startElement);
            }
            addEvent(newEvent);
            if (start instanceof StartDefine) {
                StartDefine startDefine = (StartDefine)start;
                startDefine.finish();
            }
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

        public void startElement(String namespaceURI, String localName,
                          String qname, Attributes attrs) throws SAXException {
            Event newEvent = null;
            AttributesImpl elementAttributes = new AttributesImpl( attrs );
            int attributeCount = elementAttributes.getLength();
            for (int i = 0; i < attributeCount; i++) {
            	String attributeURI = elementAttributes.getURI(i);
            	if (StringUtils.equals(attributeURI, NS)) {
            		getStartEvent().templateProperties.put(elementAttributes.getLocalName(i), 
								compileExpr(elementAttributes.getValue(i), null, locator));
            		elementAttributes.removeAttribute(i--);
            	}
            }
            StartElement startElement = new StartElement(locator, namespaceURI,
                                                     localName, qname, elementAttributes);
            if (NS.equals(namespaceURI)) {
                if (localName.equals(FOR_EACH)) {
                    String items = attrs.getValue("items");
                    String select = attrs.getValue("select");
                    JXTExpression begin = compileInt(attrs.getValue("begin"),
                                                  FOR_EACH, locator);
                    JXTExpression end = compileInt(attrs.getValue("end"),
                                                FOR_EACH, locator);
                    JXTExpression step = compileInt(attrs.getValue("step"),
                                                 FOR_EACH,
                                                 locator);
                    JXTExpression var =
                        compileExpr(attrs.getValue("var"), null, locator);
                    JXTExpression varStatus =
                        compileExpr(attrs.getValue("varStatus"),
                                null, locator);
                    if (items == null) {
                        if (select == null && (begin == null || end == null)) {
                            throw new SAXParseException("forEach: \"select\", \"items\", or both \"begin\" and \"end\" must be specified", locator, null);
                        }
                    } else if (select != null) {
                        throw new SAXParseException("forEach: only one of \"select\" or \"items\" may be specified", locator, null);
                    }
                    JXTExpression expr =
                        compileExpr(items == null ? select : items,
                                null, locator);
                    String lenientValue = attrs.getValue("lenient");
                    Boolean lenient = (lenientValue == null) ? null : Boolean.valueOf(lenientValue);
                    StartForEach startForEach = new StartForEach(startElement,
                            expr, var, varStatus, begin, end, step,lenient);
                    newEvent = startForEach;
                } else if (localName.equals(FORMAT_NUMBER)) {
                    JXTExpression value = compileExpr(attrs.getValue("value"),
                                    null, locator);
                    JXTExpression type = compileExpr(attrs.getValue("type"),
                                    null, locator);
                    JXTExpression pattern = compileExpr(attrs.getValue("pattern"),
                                    null, locator);
                    JXTExpression currencyCode =
                        compileExpr(attrs.getValue("currencyCode"),
                                    null, locator);
                    JXTExpression currencySymbol =
                        compileExpr(attrs.getValue("currencySymbol"),
                                    null, locator);
                    JXTExpression isGroupingUsed =
                        compileBoolean(attrs.getValue("isGroupingUsed"),
                                       null, locator);
                    JXTExpression maxIntegerDigits =
                        compileInt(attrs.getValue("maxIntegerDigits"),
                                   null, locator);
                    JXTExpression minIntegerDigits =
                        compileInt(attrs.getValue("minIntegerDigits"),
                                   null, locator);
                    JXTExpression maxFractionDigits =
                        compileInt(attrs.getValue("maxFractionDigits"),
                                   null, locator);
                    JXTExpression minFractionDigits =
                        compileInt(attrs.getValue("minFractionDigits"),
                                   null, locator);
                    JXTExpression var = compileExpr(attrs.getValue("var"),
                                    null, locator);
                    JXTExpression locale = compileExpr(attrs.getValue("locale"),
                                    null, locator);
                    StartFormatNumber startFormatNumber =
                        new StartFormatNumber(startElement,
                                              var,
                                              value,
                                              type,
                                              pattern,
                                              currencyCode,
                                              currencySymbol,
                                              isGroupingUsed,
                                              maxIntegerDigits,
                                              minIntegerDigits,
                                              maxFractionDigits,
                                              minFractionDigits,
                                              locale);
                    newEvent = startFormatNumber;
                } else if (localName.equals(FORMAT_DATE)) {
                    JXTExpression var = compileExpr(attrs.getValue("var"),
                                    null, locator);
                    JXTExpression value = compileExpr(attrs.getValue("value"),
                                    null, locator);
                    JXTExpression type = compileExpr(attrs.getValue("type"),
                                    null, locator);
                    JXTExpression pattern = compileExpr(attrs.getValue("pattern"),
                                    null, locator);
                    JXTExpression timeZone =
                        compileExpr(attrs.getValue("timeZone"), null, locator);
                    JXTExpression dateStyle =
                        compileExpr(attrs.getValue("dateStyle"),
                                    null, locator);
                    JXTExpression timeStyle =
                        compileExpr(attrs.getValue("timeStyle"),
                                    null, locator);
                    JXTExpression locale =
                        compileExpr(attrs.getValue("locale"), null, locator);
                    StartFormatDate startFormatDate =
                        new StartFormatDate(startElement,
                                            var,
                                            value,
                                            type,
                                            pattern,
                                            timeZone,
                                            dateStyle,
                                            timeStyle,
                                            locale);
                    newEvent = startFormatDate;
                } else if (localName.equals(CHOOSE)) {
                    StartChoose startChoose = new StartChoose(startElement);
                    newEvent = startChoose;
                } else if (localName.equals(WHEN)) {
                    if (stack.size() == 0 ||
                            !(stack.peek() instanceof StartChoose)) {
                        throw new SAXParseException(
                                "<when> must be within <choose>",
                                locator, null);
                    }
                    String test = attrs.getValue("test");
                    if (test != null) {
                        JXTExpression expr =
                            compileExpr(test, "when: \"test\": ", locator);
                        StartWhen startWhen =
                            new StartWhen(startElement, expr);
                        newEvent = startWhen;
                    } else {
                        throw new SAXParseException("when: \"test\" is required", locator, null);
                    }
                } else if (localName.equals(OUT)) {
                    String value = attrs.getValue("value");
                    if (value != null) {
                        JXTExpression expr =
                            compileExpr(value, "out: \"value\": ", locator);
                        String lenientValue = attrs.getValue("lenient");
                        Boolean lenient = lenientValue == null ?
                                           null : Boolean.valueOf(lenientValue);
                        newEvent = new StartOut(startElement, expr, lenient);
                    } else {
                        throw new SAXParseException("out: \"value\" is required", locator, null);
                    }
                } else if (localName.equals(OTHERWISE)) {
                    if (stack.size() != 0 &&
                            (stack.peek() instanceof StartChoose)) {
                        StartOtherwise startOtherwise =
                            new StartOtherwise(startElement);
                        newEvent = startOtherwise;
                    } else {
                        throw new SAXParseException(
                                "<otherwise> must be within <choose>",
                                locator, null);
                    }
                } else if (localName.equals(IF)) {
                    String test = attrs.getValue("test");
                    if (test != null) {
                        JXTExpression expr =
                            compileExpr(test, "if: \"test\": ", locator);
                        StartIf startIf =
                            new StartIf(startElement, expr);
                        newEvent = startIf;
                    } else {
                        throw new SAXParseException("if: \"test\" is required", locator, null);
                    }
                } else if (localName.equals(MACRO)) {
                    // <macro name="myTag" targetNamespace="myNamespace">
                    // <parameter name="paramName" required="Boolean" default="value"/>
                    // body
                    // </macro>
                    String namespace = StringUtils.defaultString(attrs.getValue("targetNamespace"));
                    String name = attrs.getValue("name");
                    if (name != null) {
                        StartDefine startDefine =
                            new StartDefine(startElement, namespace, name);
                        newEvent = startDefine;
                    } else {
                        throw new SAXParseException(
                                "macro: \"name\" is required", locator, null);
                    }
                } else if (localName.equals(PARAMETER)) {
                    boolean syntaxErr = false;
                    if (stack.size() < 1 ||
                        !(stack.peek() instanceof StartDefine)) {
                        syntaxErr = true;
                    } else {
                        String name = attrs.getValue("name");
                        String optional = attrs.getValue("optional");
                        String default_ = attrs.getValue("default");
                        if (name != null) {
                            StartParameter startParameter = new StartParameter(
                                    startElement, name, optional, default_);
                            newEvent = startParameter;
                        } else {
                            throw new SAXParseException("parameter: \"name\" is required", locator, null);
                        }
                    }
                    if (syntaxErr) {
                        throw new SAXParseException("<parameter> not allowed here", locator, null);
                    }
                } else if (localName.equals(EVALBODY)) {
                    newEvent = new StartEvalBody(startElement);
                } else if (localName.equals(EVAL)) {
                    String value = attrs.getValue("select");
                    JXTExpression valueExpr =
                            compileExpr(value, "eval: \"select\":", locator);
                    newEvent = new StartEval(startElement, valueExpr);
                } else if (localName.equals(SET)) {
                    String var = attrs.getValue("var");
                    String value = attrs.getValue("value");
                    JXTExpression varExpr = null;
                    JXTExpression valueExpr = null;
                    if (var != null) {
                        varExpr =
                            compileExpr(var, "set: \"var\":", locator);
                    }
                    if (value != null) {
                        valueExpr =
                            compileExpr(value, "set: \"value\":", locator);
                    }
                    StartSet startSet = new StartSet(startElement,
                                                     varExpr, valueExpr);
                    newEvent = startSet;
                } else if (localName.equals(IMPORT)) {
                    // <import uri="${root}/foo/bar.xml" context="${foo}"/>
                    AttributeEvent uri = null;
                    Iterator iter = startElement.attributeEvents.iterator();
                    while (iter.hasNext()) {
                        AttributeEvent e = (AttributeEvent)iter.next();
                        if (e.localName.equals("uri")) {
                            uri = e;
                            break;
                        }
                    }
                    if (uri != null) {
                        // If "context" is present then its value will be used
                        // as the context object in the imported template
                        String select = attrs.getValue("context");
                        JXTExpression expr = null;
                        if (select != null) {
                            expr =
                                compileExpr(select, "import: \"context\": ",
                                            locator);
                        }
                        StartImport startImport =
                            new StartImport(startElement, uri, expr);
                        newEvent = startImport;
                    } else {
                        throw new SAXParseException("import: \"uri\" is required", locator, null);
                    }
                } else if (localName.equals(TEMPLATE)) {
                    StartTemplate startTemplate =
                        new StartTemplate(startElement);
                    newEvent = startTemplate;
                } else if (localName.equals(COMMENT)) {
                    // <jx:comment>This will be parsed</jx:comment>
                    StartComment startJXComment = new StartComment(startElement);
                    newEvent = startJXComment;
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

        public void comment(char ch[], int start, int length)
            throws SAXException {
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

    /**
     * Adapter that makes this generator usable as a transformer
     * (Note there is a performance penalty for this however:
     * you effectively recompile the template for every instance document)
     */

    public static class TransformerAdapter extends ServiceableTransformer {
        static class TemplateConsumer extends Parser implements XMLConsumer {

            public TemplateConsumer() {
                this.gen = new JXTemplateGenerator();
            }

            public void setup(SourceResolver resolver, Map objectModel,
                              String src, Parameters parameters) 
                throws ProcessingException, SAXException, IOException {
                this.gen.setup(resolver, objectModel, null, parameters);
            }

            public void service(ServiceManager manager) 
                throws ServiceException {
                this.gen.service(manager);
            }

            public void endDocument() throws SAXException {
                super.endDocument();
                gen.performGeneration(gen.getConsumer(), gen.getJexlContext(),
                          gen.getJXPathContext(), null, getStartEvent(), null);
            }

            void setConsumer(XMLConsumer consumer) {
                gen.setConsumer(consumer);
            }

            void recycle() {
                super.recycle();
                gen.recycle();
            }

            JXTemplateGenerator gen;
        }

        TemplateConsumer templateConsumer = new TemplateConsumer();

        public void recycle() {
            super.recycle();
            templateConsumer.recycle();
        }

        public void setup(SourceResolver resolver, Map objectModel,
                          String src, Parameters parameters)
            throws ProcessingException, SAXException, IOException {
            super.setup(resolver, objectModel, src, parameters);
            templateConsumer.setup(resolver, objectModel, src, parameters);
        }

        public void service(ServiceManager manager) 
            throws ServiceException {
            super.service(manager);
            templateConsumer.service(manager);
        }

        public void setConsumer(XMLConsumer xmlConsumer) {
            super.setConsumer(templateConsumer);
            templateConsumer.setConsumer(xmlConsumer);
        }
    }

    private JXPathContext jxpathContext;
    private MyJexlContext globalJexlContext;
    private Variables variables;
    private static Map cache = new HashMap();
    private Source inputSource;
    private Map definitions;
    private Map cocoon;

    private JXPathContext getJXPathContext() {
        return jxpathContext;
    }

    private MyJexlContext getJexlContext() {
        return globalJexlContext;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        if ( this.resolver != null) {
            this.resolver.release(this.inputSource);
        }
        this.inputSource = null;
        this.jxpathContext = null;
        this.globalJexlContext = null;
        this.variables = null;
        this.definitions = null;
        super.recycle();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel,
                      String src, Parameters parameters)
    throws ProcessingException, SAXException, IOException {

        super.setup(resolver, objectModel, src, parameters);
        if (src != null) {
            try {
                this.inputSource = resolver.resolveURI(src);
            } catch (SourceException se) {
                throw SourceUtil.handle(
                        "Error during resolving of '" + src + "'.", se);
            }
            final String uri = inputSource.getURI();
            boolean regenerate = false;
            StartDocument startEvent = null;
            synchronized (cache) {
                startEvent = (StartDocument)cache.get(uri);
                if (startEvent != null) {
                    int valid = SourceValidity.UNKNOWN;
                    if (startEvent.compileTime != null) {
                        valid = startEvent.compileTime.isValid();
                    }
                    if (valid == SourceValidity.UNKNOWN && startEvent.compileTime != null) {
                        SourceValidity validity = inputSource.getValidity();
                        valid = startEvent.compileTime.isValid(validity);
                    }
                    if (valid != SourceValidity.VALID) {
                        cache.remove(uri);
                        regenerate = true;
                    }
                } else {
                    regenerate = true;
                }
            }
            if (regenerate) {
                Parser parser = new Parser();
                SourceUtil.parse(this.manager, this.inputSource, parser);
                startEvent = parser.getStartEvent();
                startEvent.compileTime = this.inputSource.getValidity();
                synchronized (cache) {
                    cache.put(uri, startEvent);
                }
            }
        }
        Object bean = FlowHelper.getContextObject(objectModel);
        WebContinuation kont = FlowHelper.getWebContinuation(objectModel);
        setContexts(bean, kont, parameters, objectModel);
        this.definitions = new HashMap();
    }

    private void fillContext(Object contextObject, Map map) {
        if (contextObject != null) {
            // Hack: I use jxpath to populate the context object's properties
            // in the jexl context
            final JXPathBeanInfo bi =
                JXPathIntrospector.getBeanInfo(contextObject.getClass());
            if (bi.isDynamic()) {
                Class cl = bi.getDynamicPropertyHandlerClass();
                try {
                    DynamicPropertyHandler h =
                        (DynamicPropertyHandler) cl.newInstance();
                    String[] result = h.getPropertyNames(contextObject);
                    int len = result.length;
                    for (int i = 0; i < len; i++) {
                        try {
                            map.put(result[i], h.getProperty(contextObject, result[i]));
                        } catch (Exception exc) {
                            exc.printStackTrace();
                        }
                    }
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            } else {
                PropertyDescriptor[] props =  bi.getPropertyDescriptors();
                int len = props.length;
                for (int i = 0; i < len; i++) {
                    try {
                        Method read = props[i].getReadMethod();
                        if (read != null) {
                            map.put(props[i].getName(),
                                    read.invoke(contextObject, null));
                        }
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
    }

    private void setContexts(Object contextObject, WebContinuation kont,
                             Parameters parameters, Map objectModel) {
        final Request request = ObjectModelHelper.getRequest(objectModel);
        final Object session = request.getSession(false);
        final Object app =  ObjectModelHelper.getContext(objectModel);
        cocoon = new HashMap();
        cocoon.put("request",
                   FOM_JavaScriptFlowHelper.getFOM_Request(objectModel));
        if (session != null) {
            cocoon.put("session",
                       FOM_JavaScriptFlowHelper.getFOM_Session(objectModel));
        }
        cocoon.put("context",
                   FOM_JavaScriptFlowHelper.getFOM_Context(objectModel));
        cocoon.put("continuation",
                   FOM_JavaScriptFlowHelper.getFOM_WebContinuation(objectModel));
        cocoon.put("parameters", Parameters.toProperties(parameters));
        this.variables = new MyVariables(cocoon, contextObject, kont, request,
                                         session, app, parameters);
        Map map;
        if (contextObject instanceof Map) {
            map = (Map)contextObject;
        } else {
            map = new HashMap();
            fillContext(contextObject, map);
        }
        jxpathContext = jxpathContextFactory.newContext(null, contextObject);
        jxpathContext.setVariables(variables);
        jxpathContext.setLenient(parameters.getParameterAsBoolean(
                "lenient-xpath", false));
        globalJexlContext = new MyJexlContext();
        globalJexlContext.setVars(map);
        map = globalJexlContext.getVars();
        map.put("cocoon", cocoon);
        if (contextObject != null) {
            map.put("flowContext", contextObject);
            // FIXME (VG): Is this required (what it's used for - examples)?
            // Here I use Rhino's live-connect objects to allow Jexl to call
            // java constructors
            Object javaPkg = FOM_JavaScriptFlowHelper.getJavaPackage(objectModel);
            Object pkgs = FOM_JavaScriptFlowHelper.getPackages(objectModel);
            map.put("java", javaPkg);
            map.put("Packages", pkgs);
        }
        if (kont != null) {
            map.put("continuation", kont);
        }
        map.put("request", request);
        map.put("context", app);
        map.put("parameters", parameters);
        if (session != null) {
            map.put("session", session);
        }
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.generation.Generator#generate()
     */
    public void generate()
    throws IOException, SAXException, ProcessingException {
        final String cacheKey = this.inputSource.getURI();

        StartDocument startEvent;
        synchronized (cache) {
            startEvent = (StartDocument)cache.get(cacheKey);
        }
        performGeneration(this.xmlConsumer, globalJexlContext, jxpathContext,
                null, startEvent, null);
    }

    void performGeneration(final XMLConsumer consumer,
                MyJexlContext jexlContext, JXPathContext jxpathContext,
                StartElement macroCall, Event startEvent, Event endEvent)
                throws SAXException {
        cocoon.put("consumer", consumer);
        execute(this.xmlConsumer, globalJexlContext, jxpathContext, null,
                startEvent, null);
    }

    interface CharHandler {
        public void characters(char[] ch, int offset, int length)
            throws SAXException;
    }

    private void characters(JexlContext jexlContext,
                            JXPathContext jxpathContext, TextEvent event,
                            CharHandler handler) throws SAXException {
        Iterator iter = event.substitutions.iterator();
        while (iter.hasNext()) {
            Object subst = iter.next();
            char[] chars;
            if (subst instanceof char[]) {
                chars = (char[])subst;
            } else {
                JXTExpression expr = (JXTExpression)subst;
                try {
                    Object val = getValue(expr, jexlContext, jxpathContext);
                    if (val != null) {
                        chars = val.toString().toCharArray();
                    } else {
                        chars = ArrayUtils.EMPTY_CHAR_ARRAY;
                    }
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(),
                                                    event.location, e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                event.location,
                                                new ErrorHolder(err));
                }
            }
            handler.characters(chars, 0, chars.length);
        }
    }

    private void executeDOM(final XMLConsumer consumer,
                            MyJexlContext jexlContext,
                            JXPathContext jxpathContext,
                            Node node) throws SAXException {
        DOMStreamer streamer = new DOMStreamer(consumer);
        streamer.stream(node);
   }

    private void call(Locator location, StartElement macroCall,
            final XMLConsumer consumer, MyJexlContext jexlContext,
            JXPathContext jxpathContext, Event startEvent, Event endEvent)
            throws SAXException {
        try {
            execute(consumer, jexlContext, jxpathContext, macroCall,
                    startEvent, endEvent);
        } catch (SAXParseException exc) {
            throw new SAXParseException(macroCall.localName + ": "
                    + exc.getMessage(), location, exc);
        }
    }

    public static class LoopTagStatus {
        Object current;
        int index;
        int count;
        boolean first;
        boolean last;
        int begin;
        int end;
        int step;

        public Object getCurrent() {
            return current;
        }
        public int getIndex() {
            return index;
        }
        public int getCount() {
            return count;
        }
        public boolean isFirst() {
            return first;
        }
        public boolean isLast() {
            return last;
        }
        public int getBegin() {
            return begin;
        }
        public int getEnd() {
            return end;
        }
        public int getStep() {
            return step;
        }
    }

    private void execute(final XMLConsumer consumer,
            MyJexlContext jexlContext, JXPathContext jxpathContext,
            StartElement macroCall, Event startEvent, Event endEvent)
            throws SAXException {
        Event ev = startEvent;
        LocatorFacade loc = new LocatorFacade(ev.location);
        consumer.setDocumentLocator(loc);
        while (ev != endEvent) {
            loc.setDocumentLocator(ev.location);
            if (ev instanceof Characters) {
                TextEvent text = (TextEvent)ev;
                Iterator iter = text.substitutions.iterator();
                while (iter.hasNext()) {
                    Object subst = iter.next();
                    char[] chars;
                    if (subst instanceof char[]) {
                        chars = (char[])subst;
                    } else {
                        JXTExpression expr = (JXTExpression)subst;
                        try {
                            Object val = getNode(expr, jexlContext,
                                    jxpathContext);
                            if (val instanceof Node) {
                                executeDOM(consumer, jexlContext,
                                        jxpathContext, (Node)val);
                                continue;
                            } else if (val instanceof NodeList) {
                                NodeList nodeList = (NodeList)val;
                                int len = nodeList.getLength();
                                for (int i = 0; i < len; i++) {
                                    Node n = nodeList.item(i);
                                    executeDOM(consumer, jexlContext,
                                               jxpathContext, n);
                                }
                                continue;
                            } else if (val instanceof Node[]) {
                                Node[] nodeList = (Node[])val;
                                int len = nodeList.length;
                                for (int i = 0; i < len; i++) {
                                    Node n = nodeList[i];
                                    executeDOM(consumer, jexlContext,
                                               jxpathContext, n);
                                }
                                continue;
                            } else if (val instanceof XMLizable) {
                                ((XMLizable)val).toSAX(new IncludeXMLConsumer(consumer));
                                continue;
                            }
                            if (val != null) {
                                chars = val.toString().toCharArray();
                            } else {
                                chars = ArrayUtils.EMPTY_CHAR_ARRAY;
                            }
                        } catch (Exception e) {
                            throw new SAXParseException(e.getMessage(),
                                                        ev.location, e);
                        } catch (Error err) {
                            throw new SAXParseException(err.getMessage(),
                                                        ev.location,
                                                        new ErrorHolder(err));
                        }
                    }
                    consumer.characters(chars, 0, chars.length);
                }
            } else if (ev instanceof EndElement) {
                EndElement endElement = (EndElement)ev;
                StartElement startElement = endElement.startElement;
                consumer.endElement(startElement.namespaceURI,
                                    startElement.localName, startElement.raw);
            } else if (ev instanceof EndPrefixMapping) {
                EndPrefixMapping endPrefixMapping = (EndPrefixMapping)ev;
                consumer.endPrefixMapping(endPrefixMapping.prefix);
            } else if (ev instanceof IgnorableWhitespace) {
                TextEvent text = (TextEvent)ev;
                characters(jexlContext, jxpathContext, text,
                           new CharHandler() {
                               public void characters(char[] ch, int offset,
                                                      int len)
                                   throws SAXException {
                                   consumer.ignorableWhitespace(ch, offset, len);
                               }
                           });
            } else if (ev instanceof SkippedEntity) {
                SkippedEntity skippedEntity = (SkippedEntity)ev;
                consumer.skippedEntity(skippedEntity.name);
            } else if (ev instanceof StartIf) {
                StartIf startIf = (StartIf)ev;
                Object val;
                try {
                    val = getValue(startIf.test, jexlContext,
                            jxpathContext, Boolean.TRUE);
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(),
                                                ev.location, e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                ev.location,
                                                new ErrorHolder(err));
                }
                boolean result = false;
                if (val instanceof Boolean) {
                    result = ((Boolean)val).booleanValue();
                } else {
                    result = (val != null);
                }
                if (!result) {
                    ev = startIf.endInstruction.next;
                    continue;
                }
            } else if (ev instanceof StartForEach) {
                StartForEach startForEach = (StartForEach)ev;
                final Object items = startForEach.items;
                Iterator iter = null;
                int begin, end, step;
                String var, varStatus;
                try {
                    if (items != null) {
                        JXTExpression expr = (JXTExpression)items;
                        if (expr.compiledExpression instanceof CompiledExpression) {
                            CompiledExpression compiledExpression =
                                (CompiledExpression)expr.compiledExpression;
                            Object val =
                                compiledExpression.getPointer(jxpathContext,
                                                              expr.raw).getNode();
                            // FIXME: workaround for JXPath bug
                            if (val instanceof NativeArray) {
                                iter = new JSIntrospector.NativeArrayIterator((NativeArray)val);
                            } else {
                                iter = compiledExpression.iteratePointers(jxpathContext);
                            }
                        } else if (expr.compiledExpression instanceof org.apache.commons.jexl.Expression) {
                            org.apache.commons.jexl.Expression e =
                                (org.apache.commons.jexl.Expression)expr.compiledExpression;
                            Object result = e.evaluate(jexlContext);
                            if (result != null) {
                                iter = Introspector.getUberspect().getIterator(
                                        result,
                                        new Info(ev.location.getSystemId(),
                                                 ev.location.getLineNumber(),
                                                 ev.location.getColumnNumber()));
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
                                }
                            };
                        }
                    } else {
                        iter = NULL_ITER;
                    }
                    begin = startForEach.begin == null ? 0 :
                        getIntValue(startForEach.begin, jexlContext, jxpathContext);
                    end = startForEach.end == null ? Integer.MAX_VALUE :
                            getIntValue(startForEach.end, jexlContext,
                                    jxpathContext);
                    step = startForEach.step == null ? 1 :
                            getIntValue(startForEach.step, jexlContext,
                                    jxpathContext);
                    var = getStringValue(startForEach.var, jexlContext,
                                            jxpathContext);
                    varStatus = getStringValue(startForEach.varStatus,
                                               jexlContext, jxpathContext);
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location, exc);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                ev.location,
                                                new ErrorHolder(err));
                }
                MyJexlContext localJexlContext =
                    new MyJexlContext(jexlContext);
                MyVariables localJXPathVariables =
                    new MyVariables((MyVariables)jxpathContext.getVariables());
                int i = 0;
                // Move to the begin row
                while (i < begin && iter.hasNext()) {
                    iter.next();
                    i++;
                }
                LoopTagStatus status = null;
                if (varStatus != null) {
                    status = new LoopTagStatus();
                    status.begin = begin;
                    status.end = end;
                    status.step = step;
                    status.first = true;
                    localJexlContext.put(varStatus, status);
                    localJXPathVariables.declareVariable(varStatus, status);
                }
                int skipCounter, count = 1;
                JXPathContext localJXPathContext = null;
                while (i <= end && iter.hasNext()) {
                    Object value = iter.next();
                    if (value instanceof Pointer) {
                        Pointer ptr = (Pointer)value;
                        localJXPathContext =
                            jxpathContext.getRelativeContext(ptr);
                        try {
                            value = ptr.getNode();
                        } catch (Exception exc) {
                            throw new SAXParseException(exc.getMessage(),
                                                        ev.location, null);
                        }
                    } else {
                        localJXPathContext =
                            jxpathContextFactory.newContext(jxpathContext,
                                                            value);
                    }
                    localJXPathContext.setVariables(localJXPathVariables);
                    if (var != null) {
                        localJexlContext.put(var, value);
                    }
                    if (status != null) {
                        status.index = i;
                        status.count = count;
                        status.first = i == begin;
                        status.current = value;
                        status.last = (i == end || !iter.hasNext());
                    }
                    execute(consumer, localJexlContext, localJXPathContext,
                            macroCall, startForEach.next,
                            startForEach.endInstruction);
                    // Skip rows
                    skipCounter = step;
                    while (--skipCounter > 0 && iter.hasNext()) {
                        iter.next();
                    }
                    // Increase index
                    i += step;
                    count++;
                }
                ev = startForEach.endInstruction.next;
                continue;
            } else if (ev instanceof StartChoose) {
                StartChoose startChoose = (StartChoose)ev;
                StartWhen startWhen = startChoose.firstChoice;
                while (startWhen != null) {
                    Object val;
                    try {
                        val = getValue(startWhen.test, jexlContext,
                                       jxpathContext, Boolean.TRUE);
                    } catch (Exception e) {
                        throw new SAXParseException(e.getMessage(),
                                                    ev.location, e);
                    }
                    boolean result;
                    if (val instanceof Boolean) {
                        result = ((Boolean)val).booleanValue();
                    } else {
                        result = (val != null);
                    }
                    if (result) {
                        execute(consumer, jexlContext, jxpathContext,
                                macroCall, startWhen.next,
                                startWhen.endInstruction);
                        break;
                    }
                    startWhen = startWhen.nextChoice;
                }
                if (startWhen == null && startChoose.otherwise != null) {
                    execute(consumer,
                            jexlContext, jxpathContext, macroCall,
                            startChoose.otherwise.next,
                            startChoose.otherwise.endInstruction);
                }
                ev = startChoose.endInstruction.next;
                continue;
            } else if (ev instanceof StartSet) {
                StartSet startSet = (StartSet)ev;
                Object value = null;
                String var = null;
                try {
                    if (startSet.var != null) {
                        var = getStringValue(startSet.var, jexlContext,
                                             jxpathContext);
                    }
                    if (startSet.value != null) {
                        value = getNode(startSet.value, jexlContext,
                                            jxpathContext);
                    }
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location, exc);
                }
                if (value == null) {
                    DOMBuilder builder = new DOMBuilder();
                    builder.startDocument();
                    builder.startElement(NS, "set", "set", EMPTY_ATTRS);
                    execute(builder, jexlContext, jxpathContext, macroCall,
                            startSet.next, startSet.endInstruction);
                    builder.endElement(NS, "set", "set");
                    builder.endDocument();
                    Node node = builder.getDocument().getDocumentElement();
                    NodeList nodeList = node.getChildNodes();
                    // JXPath doesn't handle NodeList, so convert it to an array
                    int len = nodeList.getLength();
                    Node[] nodeArr = new Node[len];
                    for (int i = 0; i < len; i++) {
                        nodeArr[i] = nodeList.item(i);
                    }
                    value = nodeArr;
                }
                if (var != null) {
                    jxpathContext.getVariables().declareVariable(var, value);
                    jexlContext.put(var, value);
                }
                ev = startSet.endInstruction.next;
                continue;
            } else if (ev instanceof StartElement) {
                StartElement startElement = (StartElement)ev;
                StartDefine def =
                    (StartDefine)definitions.get(startElement.qname);
                if (def != null) {
                    Map attributeMap = new HashMap();
                    Iterator i = startElement.attributeEvents.iterator();
                    while (i.hasNext()) {
                        String attributeName;
                        Object attributeValue;
                        AttributeEvent attrEvent = (AttributeEvent) i.next();
                        attributeName = attrEvent.localName;
                        if (attrEvent instanceof CopyAttribute) {
                            CopyAttribute copy = (CopyAttribute)attrEvent;
                            attributeValue = copy.value;
                        } else if (attrEvent instanceof
                                   SubstituteAttribute) {
                            SubstituteAttribute substEvent =
                                (SubstituteAttribute)attrEvent;
                            if (substEvent.substitutions.size() == 1 &&
                                    substEvent.substitutions.get(0) instanceof
                                    JXTExpression) {
                                JXTExpression expr = (JXTExpression)
                                    substEvent.substitutions.get(0);
                                Object val;
                                try {
                                    val = getNode(expr, jexlContext,
                                            jxpathContext);
                                } catch (Exception e) {
                                    throw new SAXParseException(e.getMessage(),
                                                                ev.location, e);
                                } catch (Error err) {
                                    throw new SAXParseException(err.getMessage(),
                                                                ev.location,
                                                                new ErrorHolder(err));
                                }
                                if (val == null) {
                                    val = "";
                                }
                                attributeValue = val;
                            } else {
                                StringBuffer buf = new StringBuffer();
                                Iterator iterSubst =
                                    substEvent.substitutions.iterator();
                                while (iterSubst.hasNext()) {
                                    Subst subst = (Subst)iterSubst.next();
                                    if (subst instanceof Literal) {
                                        Literal lit = (Literal)subst;
                                        buf.append(lit.value);
                                    } else if (subst instanceof JXTExpression) {
                                        JXTExpression expr = (JXTExpression)subst;
                                        Object val;
                                        try {
                                            val = getValue(expr, jexlContext,
                                                         jxpathContext);
                                        } catch (Exception e) {
                                            throw new SAXParseException(
                                                    e.getMessage(),
                                                    ev.location, e);
                                        } catch (Error err) {
                                            throw new SAXParseException(
                                                    err.getMessage(),
                                                    ev.location,
                                                    new ErrorHolder(err));
                                        }
                                        if (val == null) {
                                            val = "";
                                        }
                                        buf.append(val.toString());
                                    }
                                }
                                attributeValue = buf.toString();
                            }
                        } else {
                            throw new Error("this shouldn't have happened");
                        }
                        attributeMap.put(attributeName, attributeValue);
                    }
                    MyVariables parent =
                        (MyVariables)jxpathContext.getVariables();
                    MyVariables vars = new MyVariables(parent);
                    MyJexlContext localJexlContext =
                        new MyJexlContext(jexlContext);
                    HashMap macro = new HashMap();
                    macro.put("body", startElement);
                    macro.put("arguments", attributeMap);
                    localJexlContext.put("macro", macro);
                    vars.declareVariable("macro", macro);
                    Iterator iter = def.parameters.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry e = (Map.Entry)iter.next();
                        String key = (String)e.getKey();
                        StartParameter startParam =
                            (StartParameter)e.getValue();
                        Object default_ = startParam.default_;
                        Object val = attributeMap.get(key);
                        if (val == null) {
                            val = default_;
                        }
                        localJexlContext.put(key, val);
                        vars.declareVariable(key, val);
                    }
                    JXPathContext localJXPathContext =
                            jxpathContextFactory.newContext(null,
                                jxpathContext.getContextBean());
                    localJXPathContext.setVariables(vars);
                    call(ev.location, startElement, consumer, localJexlContext,
                            localJXPathContext, def.body, def.endInstruction);
                    ev = startElement.endElement.next;
                    continue;
                }
                Iterator i = startElement.attributeEvents.iterator();
                AttributesImpl attrs = new AttributesImpl();
                while (i.hasNext()) {
                    AttributeEvent attrEvent = (AttributeEvent)
                        i.next();
                    if (attrEvent instanceof CopyAttribute) {
                        CopyAttribute copy = (CopyAttribute)attrEvent;
                        attrs.addAttribute(copy.namespaceURI, copy.localName,
                                           copy.raw, copy.type, copy.value);
                    } else if (attrEvent instanceof SubstituteAttribute) {
                        StringBuffer buf = new StringBuffer();
                        SubstituteAttribute substEvent =
                            (SubstituteAttribute)attrEvent;
                        Iterator iterSubst =
                            substEvent.substitutions.iterator();
                        while (iterSubst.hasNext()) {
                            Subst subst = (Subst)iterSubst.next();
                            if (subst instanceof Literal) {
                                Literal lit = (Literal)subst;
                                buf.append(lit.value);
                            } else if (subst instanceof JXTExpression) {
                                JXTExpression expr = (JXTExpression)subst;
                                Object val;
                                try {
                                    val = getValue(expr, jexlContext,
                                                 jxpathContext);
                                } catch (Exception e) {
                                    throw new SAXParseException(e.getMessage(),
                                                                ev.location, e);
                                } catch (Error err) {
                                    throw new SAXParseException(err.getMessage(),
                                                                ev.location,
                                                                new ErrorHolder(err));
                               }
                                if (val == null) {
                                    val = "";
                                }
                                buf.append(val.toString());
                            }
                        }
                        attrs.addAttribute(attrEvent.namespaceURI,
                               attrEvent.localName, attrEvent.raw,
                               attrEvent.type, buf.toString());
                    }
                }
                consumer.startElement(startElement.namespaceURI,
                              startElement.localName, startElement.raw, attrs);

            } else if (ev instanceof StartFormatNumber) {
                StartFormatNumber startFormatNumber = (StartFormatNumber)ev;
                try {
                    String result =
                        startFormatNumber.format(jexlContext, jxpathContext);
                    if (result != null) {
                        char[] chars = result.toCharArray();
                        consumer.characters(chars, 0, chars.length);
                    }
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(),
                            ev.location, e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                ev.location,
                                                new ErrorHolder(err));
                }
            } else if (ev instanceof StartFormatDate) {
                StartFormatDate startFormatDate = (StartFormatDate)ev;
                try {
                    String result =
                        startFormatDate.format(jexlContext, jxpathContext);
                    if (result != null) {
                        char[] chars = result.toCharArray();
                        consumer.characters(chars, 0, chars.length);
                    }
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(),
                                                ev.location, e);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                ev.location,
                                                new ErrorHolder(err));
                }
            } else if (ev instanceof StartPrefixMapping) {
                StartPrefixMapping startPrefixMapping = (StartPrefixMapping)ev;
                consumer.startPrefixMapping(startPrefixMapping.prefix,
                                            startPrefixMapping.uri);
            } else if (ev instanceof StartComment) {
                StartComment startJXComment = (StartComment)ev;
                // Parse the body of the comment
                DOMBuilder builder = new DOMBuilder();
                builder.startDocument();
                builder.startElement(NS, "comment", "comment", EMPTY_ATTRS);
                execute(builder, jexlContext, jxpathContext, macroCall,
                        startJXComment.next, startJXComment.endInstruction);
                builder.endElement(NS, "comment", "comment");
                builder.endDocument();
                Node node = builder.getDocument().getDocumentElement();
                NodeList nodeList = node.getChildNodes();
                // JXPath doesn't handle NodeList, so convert
                // it to an array
                int len = nodeList.getLength();
                final StringBuffer buf = new StringBuffer();
                Properties omit = XMLUtils.createPropertiesForXML(true);
                for (int i = 0; i < len; i++) {
                    try {
                        String str = XMLUtils.serializeNode(nodeList.item(i), omit);  
                        buf.append(StringUtils.substringAfter(str, ">")); // cut the XML header
                    } catch (ProcessingException e) {
                        throw new SAXParseException(e.getMessage(), startJXComment.location, e);
                    }
                }
                char[] chars = new char[buf.length()];
                buf.getChars(0, chars.length, chars, 0);
                consumer.comment(chars, 0, chars.length);
                ev = startJXComment.endInstruction.next;
                continue;
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
                consumer.startDTD(startDTD.name, startDTD.publicId,
                        startDTD.systemId);
            } else if (ev instanceof StartEntity) {
                consumer.startEntity(((StartEntity)ev).name);
            } else if (ev instanceof StartOut) {
                StartOut startOut = (StartOut)ev;
                Object val;
                try {
                    val = getNode(startOut.compiledExpression, jexlContext,
                                  jxpathContext, startOut.lenient);
                    if (val instanceof Node) {
                        executeDOM(consumer, jexlContext, jxpathContext,
                                   (Node)val);
                    } else if (val instanceof NodeList) {
                        NodeList nodeList = (NodeList)val;
                        int len = nodeList.getLength();
                        for (int i = 0; i < len; i++) {
                            Node n = nodeList.item(i);
                            executeDOM(consumer, jexlContext,
                                       jxpathContext, n);
                        }
                    } else if (val instanceof Node[]) {
                        Node[] nodeList = (Node[])val;
                        int len = nodeList.length;
                        for (int i = 0;i < len; i++) {
                            Node n = nodeList[i];
                            executeDOM(consumer, jexlContext,
                                       jxpathContext, n);
                        }
                    } else if (val instanceof XMLizable) {
                        ((XMLizable)val).toSAX(new IncludeXMLConsumer(consumer));
                    } else {
                        char[] ch;
                        if (val == null) {
                            ch = ArrayUtils.EMPTY_CHAR_ARRAY;
                        } else {
                            ch = val.toString().toCharArray();
                        }
                        consumer.characters(ch, 0, ch.length);
                    }
                } catch (Exception e) {
                    throw new SAXParseException(e.getMessage(),
                                                ev.location, e);
                }
            } else if (ev instanceof StartTemplate) {
            } else if (ev instanceof StartEval) {
                StartEval startEval = (StartEval)ev;
                JXTExpression expr = startEval.value;
                try {
                    Object val = getNode(expr, jexlContext, jxpathContext);
                    if (!(val instanceof StartElement)) {
                        throw new Exception("macro invocation required instead of: " + val);
                    }
                    StartElement call = (StartElement)val;
                    execute(consumer, jexlContext, jxpathContext, call,
                            call.next, call.endElement);
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location, exc);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                ev.location,
                                                new ErrorHolder(err));
                }
                ev = startEval.endInstruction.next;
                continue;
            } else if (ev instanceof StartEvalBody) {
                StartEvalBody startEval = (StartEvalBody)ev;
                try {
                    execute(consumer, jexlContext, jxpathContext, null,
                            macroCall.next, macroCall.endElement);
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location, exc);
                } catch (Error err) {
                    throw new SAXParseException(err.getMessage(),
                                                ev.location,
                                                new ErrorHolder(err));
                }
                ev = startEval.endInstruction.next;
                continue;
            } else if (ev instanceof StartDefine) {
                StartDefine startDefine = (StartDefine)ev;
                definitions.put(startDefine.qname, startDefine);
                ev = startDefine.endInstruction.next;
                continue;
            } else if (ev instanceof StartImport) {
                StartImport startImport = (StartImport)ev;
                String uri;
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
                        } else if (subst instanceof JXTExpression) {
                            JXTExpression expr = (JXTExpression)subst;
                            Object val;
                            try {
                                val = getValue(expr,
                                        jexlContext, jxpathContext);
                            } catch (Exception exc) {
                                throw new SAXParseException(exc.getMessage(),
                                                            ev.location, exc);
                            } catch (Error err) {
                                throw new SAXParseException(err.getMessage(),
                                                            ev.location,
                                                            new ErrorHolder(err));
                            }
                            if (val == null) {
                                val = "";
                            }
                            buf.append(val.toString());
                        }
                    }
                    uri = buf.toString();
                }
                Source input = null;
                StartDocument doc;
                try {
                    input = resolver.resolveURI(uri);
                    SourceValidity validity = null;
                    synchronized (cache) {
                        doc = (StartDocument)cache.get(input.getURI());
                        if (doc != null) {
                            boolean recompile = false;
                            if ( doc.compileTime == null) {
                                recompile = true;
                            } else {
                                int valid = doc.compileTime.isValid();
                                if ( valid == SourceValidity.UNKNOWN ) {
                                    validity = input.getValidity();
                                    valid = doc.compileTime.isValid(validity);
                                }
                                if ( valid != SourceValidity.VALID ) {
                                    recompile = true;
                                }
                            }
                            if ( recompile ) {
                                doc = null; // recompile
                            }
                        }
                    }
                    if (doc == null) {
                        Parser parser = new Parser();
                        // call getValidity before using the stream is faster if the source is a SitemapSource
                        if (validity == null) {
                            validity = input.getValidity();
                        }
                        SourceUtil.parse(this.manager, input, parser);
                        doc = parser.getStartEvent();
                        doc.compileTime = validity;
                        synchronized (cache) {
                            cache.put(input.getURI(), doc);
                        }
                    }
                } catch (Exception exc) {
                    throw new SAXParseException(exc.getMessage(),
                                                ev.location,
                                                exc);
                }
                finally {
                    resolver.release(input);
                }
                JXPathContext selectJXPath = jxpathContext;
                MyJexlContext selectJexl = jexlContext;
                if (startImport.select != null) {
                    try {
                        Object obj = getValue(startImport.select, jexlContext, jxpathContext);
                        selectJXPath = jxpathContextFactory.newContext(null, obj);
                        selectJXPath.setVariables(variables);
                        selectJexl = new MyJexlContext(jexlContext);
                        fillContext(obj, selectJexl);
                    } catch (Exception exc) {
                        throw new SAXParseException(exc.getMessage(),
                                                    ev.location, exc);
                    } catch (Error err) {
                        throw new SAXParseException(err.getMessage(),
                                                    ev.location,
                                                    new ErrorHolder(err));
                    }
                }
                try {
                    execute(consumer, selectJexl, selectJXPath, macroCall, doc.next, doc.endDocument);
                } catch (Exception exc) {
                        throw new SAXParseException(
                                "Exception occurred in imported template "
                                + uri + ": "+ exc.getMessage(),
                                ev.location, exc);
                }
                ev = startImport.endInstruction.next;
                continue;
            } else if (ev instanceof StartDocument) {
                if (((StartDocument)ev).endDocument != null) {
                    // if this isn't a document fragment
                    consumer.startDocument();
                }
            } else if (ev instanceof EndDocument) {
                consumer.endDocument();
            } else if (ev instanceof ProcessingInstruction) {
                ProcessingInstruction pi = (ProcessingInstruction)ev;
                consumer.processingInstruction(pi.target, pi.data);
            }
            ev = ev.next;
        }
    }
	/* (non-Javadoc)
	 * @see org.apache.cocoon.caching.CacheableProcessingComponent#getKey()
	 */
	public Serializable getKey() {
    	JXTExpression cacheKeyExpr = (JXTExpression)getCurrentTemplateProperty(CACHE_KEY);
        try {
			return (Serializable) getValue(cacheKeyExpr, globalJexlContext, jxpathContext);
		} catch (Exception e) {
			getLogger().error("error evaluating cache key", e);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.apache.cocoon.caching.CacheableProcessingComponent#getValidity()
	 */
	public SourceValidity getValidity() {
    	JXTExpression validityExpr = (JXTExpression)getCurrentTemplateProperty(VALIDITY);
        try {
			return (SourceValidity) getValue(validityExpr, globalJexlContext, jxpathContext);
		} catch (Exception e) {
			getLogger().error( "error evaluating cache key", e );
			return null;
		}
	}
	
	private Object getCurrentTemplateProperty(String propertyName) {
    	final String uri = inputSource.getURI();
    	StartDocument startEvent;
        synchronized (cache) {
            startEvent = (StartDocument)cache.get(uri);
        }
        if (startEvent == null)
        	return null;
        return startEvent.templateProperties.get(propertyName);
	}
}
