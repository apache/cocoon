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
package org.apache.cocoon.transformation;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.source.SourceValidity;
import org.apache.cocoon.generation.Generator;
import org.apache.commons.jexl.*;
import org.apache.commons.jexl.util.*;
import org.apache.commons.jexl.util.introspection.*;
import org.apache.commons.jexl.util.Introspector;
import org.apache.commons.jxpath.DynamicPropertyHandler;
import org.apache.commons.jxpath.JXPathBeanInfo;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;
import java.beans.PropertyDescriptor;
import org.mozilla.javascript.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.apache.cocoon.xml.dom.DOMStreamer;
import org.w3c.dom.DocumentFragment;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Stack;
import java.util.Iterator;
import org.apache.avalon.framework.CascadingRuntimeException;
/**
 * Jexl Transformer.
 *
 * <p>
 *  Transformer implementation using Apache Commons Jexl. 
 *  Provides a tag library and expression language similar
 *  to a read-only subset of core JSTL.
 * </p>
 *
 *
 * @author <a href="mailto:coliver@apache.org">Christopher Oliver</a>
 * @version CVS $Id: JexlTransformer.java,v 1.1 2003/03/27 19:01:42 coliver Exp $
 */

public class JexlTransformer
    extends AbstractSAXTransformer implements Initializable, Generator {

    /**
     * Jexl Introspector that supports Rhino JavaScript objects
     * as well as Java Objects
     */
    static public class JSIntrospector extends UberspectImpl {
        
        public static class JSMethod implements VelMethod {
            
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
                        for (int i = 0; i < args.length; i++) {
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
                        result == ScriptableObject.NOT_FOUND) {
                        result = null;
                    } else while (result instanceof Wrapper) {
                        result = ((Wrapper)result).unwrap();
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
        
        public static class JSPropertyGet implements VelPropertyGet {
            
            Scriptable scope;
            String name;
            
            public JSPropertyGet(Scriptable scope, String name) {
                this.scope = scope;
                this.name = name;
            }
            
            public Object invoke(Object thisArg) throws Exception {
                Context cx = Context.enter();
                try {
                    Scriptable thisObj;
                    if (!(thisArg instanceof Scriptable)) {
                        thisObj = Context.toObject(thisArg, scope);
                    } else {
                        thisObj = (Scriptable)thisArg;
                    }
                    Object result = ScriptableObject.getProperty(thisObj, name);
                    if (result == Undefined.instance || 
                        result == ScriptableObject.NOT_FOUND) {
                        result = null;
                    } else while (result instanceof Wrapper) {
                        result = ((Wrapper)result).unwrap();
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
        
        public static class JSPropertySet implements VelPropertySet {
            
            Scriptable scope;
            String name;
            
            public JSPropertySet(Scriptable scope, String name) {
                this.scope = scope;
                this.name = name;
            }
            
            public Object invoke(Object thisArg, Object rhs) throws Exception {
                Context cx = Context.enter();
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
        
        public static class NativeArrayIterator implements Iterator {
            
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
                Context cx = Context.enter();
                try {
                    Object result = arr.get(index++, arr);
                    if (result == Undefined.instance ||
                        result == ScriptableObject.NOT_FOUND) {
                        result = null;
                    } else while (result instanceof Wrapper) {
                        result = ((Wrapper)result).unwrap();
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
        
        public static class ScriptableIterator implements Iterator {
            
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
                Context cx = Context.enter();
                try {
                    Object result = 
                        ScriptableObject.getProperty(scope, 
                                                     ids[index++].toString());
                    if (result == Undefined.instance ||
                        result == ScriptableObject.NOT_FOUND) {
                        result = null;
                    } else while (result instanceof Wrapper) {
                        result = ((Wrapper)result).unwrap();
                    }
                    return result;
                } finally {
                    Context.exit();
                }
            }
            
            public void remove() {
                Context cx = Context.enter();
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

    /** namespace constant */
    public static final String JEXL_NAMESPACE_URI  
	= "http://cocoon.apache.org/transformation/jexl/1.0";

    public static final String JEXL_FOR_EACH = "forEach";
    public static final String JEXL_CHOOSE = "choose";
    public static final String JEXL_WHEN = "when";
    public static final String JEXL_WHEN_TEST = "test";
    public static final String JEXL_OTHERWISE = "otherwise";

    public static final String JEXL_OUT = "out";
    public static final String JEXL_OUT_VALUE = "value";
    public static final String JEXL_OUT_DEFAULT = "default";
    public static final String JEXL_OUT_ESCAPE_XML = "escapeXml";

    public static final String JEXL_IF = "if";
    public static final String JEXL_IF_TEST = "test";

    public static final String JEXL_FOREACH = "forEach";
    public static final String JEXL_FOREACH_ITEMS = "items";
    public static final String JEXL_FOREACH_BEGIN = "begin";
    public static final String JEXL_FOREACH_END = "end";
    public static final String JEXL_FOREACH_STEP = "step";
    public static final String JEXL_FOREACH_VAR = "var";
    public static final String JEXL_FOREACH_VAR_STATUS = "varStatus";

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


    // web contination
    private WebContinuation kont;

    JexlContext jexlContext;
    JXPathContext jxpathContext;

    Iterator foreachIter;
    String foreachVar;
    int foreachBegin;
    int foreachEnd;
    int foreachStep;

    //
    // Contains a stack of Boolean values:
    // Each time we enter a <choose> we push TRUE on this stack:
    // which indicates that the <otherwise> clause should be executed.
    // When we enter a <when> clause whose test condition is true, this value is 
    // popped and FALSE is pushed in its place. The test condition is then also pushed
    // on the stack, and popped when we reach </when>, where it is checked
    // to see if ignoreEventsCount should be updated
    // 
    Stack chooseStack;

    // 
    // Marker set when we enter <choose>: used to validate that <when> and
    // <otherwise> are always directly nested in <choose>
    // 
    //
    private boolean inChoose;

    // Contains a stack of Boolean values:
    // Each time we enter an <if> we push the test condition on this stack and
    // pop it when we reach </if>, where it is checked to see if ignoreEventsCount
    // should be updated
    Stack ifStack;

    // Run as a generator for debugging: to get line numbers in error messages

    private Source inputSource;

    public void generate()
        throws IOException, SAXException, ProcessingException {
	try {
            this.resolver.toSAX(this.inputSource, this);
        } catch (SAXException e) {
            final Exception cause = e.getException();
            if( cause != null ) {
                if ( cause instanceof ProcessingException )
                    throw (ProcessingException)cause;
                if ( cause instanceof IOException )
                    throw (IOException)cause;
                if ( cause instanceof SAXException )
                    throw (SAXException)cause;
                throw new ProcessingException("Could not read resource "
                                              + this.inputSource.getURI(), cause);
            }
            throw e;
        }
    }

    /**
     * Initialize this transformer.
     *
     * @exception Exception if an error occurs
     */
    public void initialize()
        throws Exception {
        namespaceURI = JEXL_NAMESPACE_URI;
    }

    /**
     * Setup this transformer
     *
     * @param resolver a {@link SourceResolver} instance
     * @param objectModel the objectModel
     * @param src <code>src</code> parameter
     * @param parameters optional parameters
     * @exception ProcessingException if an error occurs
     * @exception SAXException if an error occurs
     * @exception IOException if an error occurs
     */
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
        Object bean = ((Environment)resolver).getAttribute("bean-dict");
        kont = (WebContinuation)((Environment)resolver).getAttribute("kont");
        chooseStack = new Stack();
        ifStack = new Stack();
        setContexts(bean);
        getJexlContext().getVars().put("this", bean);
        getJexlContext().getVars().put("continuation", kont);
    }
    
    /**
     * Evaluate a Jexl expr contained in ${} but don't do substitution:
     * just return its value
     */

    Object eval(String inStr) /* throws Exception */ {
        try {
            StringReader in = new StringReader(inStr.trim());
            int ch;
            StringBuffer expr = new StringBuffer();
            boolean inExpr = false;
            while ((ch = in.read()) != -1) {
                char c = (char)ch;
                if (inExpr) {
                    if (c == '}') {
                        String str = expr.toString();
                        return getValue(str);
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
                } else {
                    if (c == '$') {
                        ch = in.read();
                        if (ch == '{') {
                            inExpr = true;
                            continue;
                        }
                    }
                    // hack: invalid expression?
                    // just return the original and swallow exception
                    return inStr;
                }
            }
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }
        return inStr;
    }

    /**
     * Substitute the values of JEXL expr's (contained in ${}) within attribute values
     */

    private void substitute(Reader in, Writer out) throws Exception {
        int ch;
        StringBuffer expr = new StringBuffer();
        boolean inExpr = false;
        while ((ch = in.read()) != -1) {
            char c = (char)ch;
            if (inExpr) {
                if (c == '}') {
                    String str = expr.toString();
                    expr.setLength(0);
                    str = String.valueOf(getValue(str));
                    out.write(str);
                    inExpr = false;
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
            } else {
                if (c == '\\') {
                    ch = in.read();
                    if (ch == -1) {
                        out.write('\\');
                    } else {
                        out.write((char)ch);
                    }
                } else {
                    if (c == '$') {
                        ch = in.read();
                        if (ch == '{') {
                            inExpr = true;
                            continue;
                        }
                        out.write('$');
                    }
                    if (ch != -1) {
                        out.write((char)ch);
                    }
                }
            }
        }
    }

    public void startElement(String uri, String name, 
                             String raw, Attributes attr) 
        throws SAXException {
        if (ignoreEventsCount == 0 && foreachIter == null) {
            if (!uri.equals(JEXL_NAMESPACE_URI)) {
                // substitute EL values
                AttributesImpl impl = new AttributesImpl(attr);
                for (int i = 0, len = impl.getLength(); i < len; i++) {
                    try {
                        String value = impl.getValue(i);
                        StringReader reader = new StringReader(value);
                        StringWriter writer = new StringWriter();
                        substitute(reader, writer);
                        impl.setValue(i, writer.toString());
                    } catch (Exception exc) {
                        exc.printStackTrace();
                    }
                }
                attr = impl;
            }
        }
        super.startElement(uri, name, raw, attr);
    }
    /**
     * Entry method for all elements in our namespace
     *
     * @param uri a <code>String</code> value
     * @param name a <code>String</code> value
     * @param raw a <code>String</code> value
     * @param attr an <code>Attributes</code> value
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     * @exception SAXException if an error occurs
     */
    public void startTransformingElement(String uri, String name, 
                                         String raw, Attributes attr) 
        throws ProcessingException ,IOException, SAXException {
        if (foreachIter != null) {
            // just record the SAX event
            ignoreHooksCount++;
            super.startElement(uri, name, raw, attr);
            ignoreHooksCount--;
            return;
        }
        if (JEXL_OUT.equals(name)) {
            if (ignoreEventsCount == 0) {
                doOut(attr);
            }
        } else if (JEXL_IF.equals(name)) {
            doIf(attr);
        } else if (JEXL_FOREACH.equals(name)) {
            doForEach(attr);
        } else if (JEXL_CHOOSE.equals(name)) {
            inChoose = true;
            doChoose(attr);
            return;
        } else if (JEXL_WHEN.equals(name)) {
            if (!inChoose) {
                throw new ProcessingException("<when> must be contained in <choose>");
            }
            doWhen(attr);
        } else if (JEXL_OTHERWISE.equals(name)) {
            if (!inChoose) {
                throw new ProcessingException("<otherwise> must be contained in <choose>");
            }
            doOtherwise(attr);
        } else {
            throw new ProcessingException("unknown jexl-transformer element: " + name);
        }
        inChoose = false;
    }

    /**
     * Exit method for all elements in our namespace
     *
     * @param uri a <code>String</code> value
     * @param name a <code>String</code> value
     * @param raw a <code>String</code> value
     * @exception ProcessingException if an error occurs
     * @exception IOException if an error occurs
     * @exception SAXException if an error occurs
     */
    public void endTransformingElement(String uri, String name, String raw) 
        throws ProcessingException, IOException, SAXException {
        if (JEXL_FOREACH.equals(name)) {
            finishForEach();
        } else if (foreachIter != null) {
            ignoreHooksCount++;
            super.endElement(uri, name, raw);
            ignoreHooksCount--;
        } else {
            if (JEXL_OUT.equals(name)) {
                return; // do nothing
            } else if (JEXL_IF.equals(name)) {
                finishIf();
            } else if (JEXL_CHOOSE.equals(name)) {
                finishChoose();
                inChoose = false;
            } else if (JEXL_WHEN.equals(name)) {
                finishWhen();
                inChoose = true;
            } else if (JEXL_OTHERWISE.equals(name)) {
                finishOtherwise();
                inChoose = true;
            }
        }
    }


    private JexlContext getJexlContext() {
        return jexlContext;
    }

    private JXPathContext getJXPathContext() {
        return jxpathContext;
    }

    private void setContexts(Object contextObject) {
        Map map;
        if (contextObject instanceof Map) {
            map = (Map)contextObject;
        } else {
            // Hack: I use jxpath to populate the context object's properties
            // in the jexl context
            final JXPathBeanInfo bi = 
                JXPathIntrospector.getBeanInfo(contextObject.getClass());
            map = new HashMap();
            if (bi.isDynamic()) {
                Class cl = bi.getDynamicPropertyHandlerClass();
                try {
                    DynamicPropertyHandler h = (DynamicPropertyHandler) cl.newInstance();
                    String[] result = h.getPropertyNames(contextObject);
                    for (int i = 0; i < result.length; i++) {
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
                for (int i = 0; i < props.length; i++) {
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
        jxpathContext = JXPathContext.newContext(contextObject);
        jxpathContext.setVariables(new Variables() {

                public boolean isDeclaredVariable(String varName) {
                    return varName.equals("continuation");
                }

                public Object getVariable(String varName) {
                    if (varName.equals("continuation")) {
                        return kont;
                    }
		    return null;
                }

                public void declareVariable(String varName, Object value) {
                }

                public void undeclareVariable(String varName) {
                }
            });
        jexlContext = JexlHelper.createContext();
        jexlContext.setVars(map);
    }

    /**
     * Helper method for obtaining the value of a particular variable.
     *
     * @param variable variable name
     * @return variable value as an <code>Object</code>
     */
    private Object getValue(final String variable) {
        Object value;
        JexlContext context = getJexlContext();
        try {
            Expression e = ExpressionFactory.createExpression(variable);
            return e.evaluate(context);
        } catch (Exception e) {
            throw new CascadingRuntimeException(e.getMessage(), e);
        }

    }

    /**
     * Helper method to process a &lt;jexl-transformer:value-of select="."&gt; tag
     *
     * @param a an {@link Attributes} instance
     * @exception SAXException if a SAX error occurs
     * @exception ProcessingException if a processing error occurs
     */
    private void doOut(final Attributes a)
        throws SAXException, ProcessingException {

        final String select = a.getValue(JEXL_OUT_VALUE);
        final String def = a.getValue(JEXL_OUT_DEFAULT);

        if (null != select) {
            Object value = eval(select);
            if (value == null) {
                value = def;
                if (value == null) {
                    value = "";
                }
            }
            sendTextEvent(value.toString());
        } else {
            throw new ProcessingException(
                "jexl-transformer:" + JEXL_OUT + " specified without a "+JEXL_OUT_VALUE+" attribute"
            );
        }
    }

    /**
     * Helper method to process a &lt;jexl-transformer:if test="..."&gt; element.
     *
     * @param a an <code>Attributes</code> value
     * @exception SAXException if an error occurs
     */
    private void doIf(final Attributes a)
        throws SAXException {

        if (ignoreEventsCount > 0) {
            ifStack.push(Boolean.FALSE);
            ++ignoreEventsCount;
            return;
        }

        // get the test variable
        String expr = a.getValue(JEXL_IF_TEST);
        if (expr == null) {
            throw new SAXException("if: \"test\" is required");
        }
        final Object value = eval(expr);
        final boolean isTrueBoolean =
            value instanceof Boolean && ((Boolean)value).booleanValue();
        ifStack.push(isTrueBoolean ? Boolean.TRUE : Boolean.FALSE);
        if (isTrueBoolean) {
            // do nothing, allow all subelements
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("jexl-transformer:if results in allowing subelements");
            }
        } else {
            // disallow all subelements
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("jexl-transformer:if results in disallowing subelements");
            }
            ++ignoreEventsCount;
        }
    }


    /**
     * Helper method to process a &lt;jexl-transformer:if test="..."&gt; element.
     *
     * @param a an <code>Attributes</code> value
     * @exception SAXException if an error occurs
     */
    private void doForEach(final Attributes a)
        throws SAXException {
        if (ignoreEventsCount == 0) {
            String items = a.getValue(JEXL_FOREACH_ITEMS);
            String s = a.getValue(JEXL_FOREACH_BEGIN);
            int begin = s == null ? -1 : Integer.parseInt(s);
            s = a.getValue(JEXL_FOREACH_END);
            int end = s == null ? -1 : Integer.parseInt(s);
            s = a.getValue(JEXL_FOREACH_STEP);
            foreachStep = s == null ? 1 : Integer.parseInt(s);
            if (foreachStep < 1) {
                throw new SAXException("forEach: \"step\" must be a positive integer");
            }
            String var = a.getValue(JEXL_FOREACH_VAR);
            if (items == null && (begin == -1 || end == -1)) {
                throw new SAXException("forEach: \"items\" or both \"begin\" and \"end\" must be specified");
            }
            foreachBegin = begin == -1 ? 0 : begin;
            foreachEnd = end == -1 ? Integer.MAX_VALUE: end;
            if (items != null) {
                Object value = eval(items);
                if (value instanceof Collection) {
                    foreachIter = ((Collection)value).iterator();
                } else {
                    try {
                        foreachIter = Introspector.getUberspect().getIterator(value, null);
                    } catch (Exception exc) {
                        throw new SAXException(exc.getMessage(), exc);
                    }
                }
            } else {
                foreachIter = new Iterator() {
                        public boolean hasNext() {
                            return true;
                        }
                        public Object next() {
                            return null;
                        }
                        public void remove() {
                        }
                    };
            }
            foreachVar = var;
            startRecording();
        }
    }

    private void finishForEach()
        throws SAXException {
        if (ignoreEventsCount == 0) {
            DocumentFragment frag = endRecording();
            String varName = foreachVar;
            Iterator iter = foreachIter;
            int begin = foreachBegin;
            int end = foreachEnd;
            int step = foreachStep;
            foreachIter = null;
            foreachVar = null;
            int i;
            for (i = 0; i < begin && iter.hasNext(); i++) {
                iter.next();
            }
            for (; i < end && iter.hasNext(); i++) {
                Object value = iter.next();
                if (varName != null) {
                    jexlContext.getVars().put(varName, value);
                }
                sendEvents(frag);
                for (int skip = step-1; skip > 0 && iter.hasNext(); --skip) {
                    iter.next();
                }
            }
        }
    }

    private void doChoose(final Attributes a) {
        // do otherwise by default unless ignoreEventsCount > 0
        chooseStack.push(ignoreEventsCount > 0 ? Boolean.FALSE : Boolean.TRUE);
    }

    private void doWhen(final Attributes a) throws SAXException {
        if (ignoreEventsCount > 0) {
            ++ignoreEventsCount;
            chooseStack.push(Boolean.FALSE); 
            return;
        }
        // get the test variable
        final Object value = eval(a.getValue(JEXL_WHEN_TEST));

        final boolean isTrueBoolean =
            value instanceof Boolean && ((Boolean)value).booleanValue() == true;
        if (isTrueBoolean) {
            Boolean otherwise = (Boolean)chooseStack.pop();
            chooseStack.push(Boolean.FALSE); // don't do otherwise
        } else {
            ++ignoreEventsCount;
        }
        chooseStack.push(isTrueBoolean ? Boolean.TRUE : Boolean.FALSE);

    }

    private void doOtherwise(final Attributes a) {
        Boolean otherwise = (Boolean)chooseStack.peek();
        if (!otherwise.booleanValue()) {
            ++ignoreEventsCount;
        } 
    }

    private void finishChoose() {
        chooseStack.pop();
    }

    private void finishWhen() {
        Boolean when = (Boolean)chooseStack.pop();
        if (!when.booleanValue()) {
            // we skipped this when
            --ignoreEventsCount;
        }
    }

    private void finishOtherwise() {
        Boolean otherwise = (Boolean)chooseStack.peek();
        if (!otherwise.booleanValue()) {
            // we skipped this otherwise
            --ignoreEventsCount;
        }

    }
    /**
     * Helper method to process a &lt;/jexl-transformer:if&gt; element.
     *
     * @exception SAXException if an error occurs
     */
    private void finishIf()
        throws SAXException {

        Boolean didIf = (Boolean)ifStack.pop();
        if (!didIf.booleanValue()) {
            --ignoreEventsCount;
        }

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("jexl-transformer:if closed");
        }
    }

    /**
     * Release all held resources.
     */
    public void recycle() {
        super.recycle();
        kont = null;
        jexlContext = null;
        foreachIter = null;
        foreachVar = null;
        chooseStack = null;
        ifStack = null;
    }
}
