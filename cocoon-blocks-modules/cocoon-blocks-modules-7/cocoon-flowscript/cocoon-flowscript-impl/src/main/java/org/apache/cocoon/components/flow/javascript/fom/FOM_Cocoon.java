/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.flow.javascript.fom;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpSession;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.flow.ContinuationsManager;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.Interpreter.Argument;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.container.spring.avalon.AvalonUtils;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.spring.configurator.WebAppContextUtils;
import org.apache.cocoon.util.ClassUtils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.NativeJavaClass;
import org.mozilla.javascript.NativeJavaObject;
import org.mozilla.javascript.Script;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Undefined;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.continuations.Continuation;

import org.springframework.web.context.WebApplicationContext;

/**
 * Implementation of FOM (Flow Object Model).
 *
 * @since 2.1
 * @version $Id$
 */
public class FOM_Cocoon extends ScriptableObject {

    class CallContext {
        CallContext caller;
        Context avalonContext;
        WebApplicationContext webAppContext;
        FOM_JavaScriptInterpreter interpreter;
        Redirector redirector;
        Scriptable request;
        Scriptable response;
        Scriptable session;
        Scriptable context;
        Scriptable parameters;
        Scriptable log;
        WebContinuation lastContinuation;
        FOM_WebContinuation fwk;
        PageLocalScopeImpl currentPageLocal;

        public CallContext(CallContext caller,
                           FOM_JavaScriptInterpreter interp,
                           Redirector redirector,
                           WebApplicationContext waContext,
                           Context avalonContext,
                           WebContinuation lastContinuation) {
            this.caller = caller;
            this.interpreter = interp;
            this.redirector = redirector;
            this.webAppContext = waContext;
            this.avalonContext = avalonContext;
            this.lastContinuation = lastContinuation;
            if (lastContinuation != null) {
                fwk = new FOM_WebContinuation(lastContinuation);
                fwk.setLogger(getLogger());
                Scriptable scope = FOM_Cocoon.this.getParentScope();
                fwk.setParentScope(scope);
                fwk.setPrototype(getClassPrototype(scope, fwk.getClassName()));
                this.currentPageLocal = fwk.getPageLocal();
            }
            if (this.currentPageLocal != null) {
                // "clone" the page local scope
                this.currentPageLocal = this.currentPageLocal.duplicate();
            } else {
                this.currentPageLocal = new PageLocalScopeImpl(getTopLevelScope(FOM_Cocoon.this));
            }
            pageLocal.setDelegate(this.currentPageLocal);
        }

        public FOM_WebContinuation getLastContinuation() {
            return fwk;
        }

        public void setLastContinuation(FOM_WebContinuation fwk) {
            this.fwk = fwk;
            if (fwk != null) {
                pageLocal.setDelegate(fwk.getPageLocal());
                this.lastContinuation = fwk.getWebContinuation();
            } else {
                this.lastContinuation = null;
            }
        }

        public Scriptable getSession() {
            if (session != null) {
                return session;
            }
            session = new FOM_Session(
                    getParentScope(),
                    (HttpSession) ObjectModelHelper.getRequest(this.getObjectModel()).getSession(true));
            return session;
        }

        public Scriptable getRequest() {
            if (request != null) {
                return request;
            }
            request = new FOM_Request(
                    getParentScope(),
                    ObjectModelHelper.getRequest(this.getObjectModel()));
            return request;
        }

        public Scriptable getContext() {
            if (context != null) {
                return context;
            }
            context = new FOM_Context(
                    getParentScope(),
                    ObjectModelHelper.getContext(this.getObjectModel()));
            return context;
        }

        public Scriptable getResponse() {
            if (response != null) {
                return response;
            }
            response = org.mozilla.javascript.Context.toObject(
                    ObjectModelHelper.getResponse(this.getObjectModel()),
                    getParentScope());
            return response;
        }

        public Scriptable getLog() {
            if (log != null) {
                return log;
            }
            log = org.mozilla.javascript.Context.toObject(getLogger(), getParentScope());
            return log;
        }

        public Scriptable getParameters() {
            return parameters;
        }

        public void setParameters(Scriptable parameters) {
            this.parameters = parameters;
        }

        protected Map getObjectModel() {
            return ContextHelper.getObjectModel(this.avalonContext);
        }
    }


    private final Log logger = LogFactory.getLog(getClass());

    private CallContext currentCall;
    protected PageLocalScopeHolder pageLocal;


    public String getClassName() {
        return "FOM_Cocoon";
    }


    // Called by FOM_JavaScriptInterpreter
    static void init(Scriptable scope) throws Exception {
        //FIXME(SW) what is the exact purpose of defineClass() ??
        defineClass(scope, FOM_Cocoon.class);
//        defineClass(scope, FOM_Request.class);
//        defineClass(scope, FOM_Response.class);
//        defineClass(scope, FOM_Cookie.class);
//        defineClass(scope, FOM_Session.class);
//        defineClass(scope, FOM_Context.class);
//        defineClass(scope, FOM_Log.class);
        defineClass(scope, FOM_WebContinuation.class);
        defineClass(scope, PageLocalImpl.class);
    }

    public void pushCallContext(FOM_JavaScriptInterpreter interp,
                                Redirector redirector,
                                Context avalonContext,
                                WebContinuation lastContinuation) {
        if (pageLocal == null) {
            pageLocal = new PageLocalScopeHolder(getTopLevelScope(this));
        }
        
        // The call context will use the current application context when looking up components
        final WebApplicationContext appContext = WebAppContextUtils.getCurrentWebApplicationContext();

        this.currentCall = new CallContext(currentCall, interp, redirector, appContext,
                                           avalonContext, lastContinuation);
    }

    public void popCallContext() {
        // Clear the scope attribute
        FOM_JavaScriptFlowHelper.setFOM_FlowScope(this.getObjectModel(), null);

        this.currentCall = this.currentCall.caller;
        // reset current page locals
        if (this.currentCall != null) {
            pageLocal.setDelegate(this.currentCall.currentPageLocal);
        } else {
            pageLocal.setDelegate(null);
        }
    }


    public FOM_WebContinuation jsGet_continuation() {
        // FIXME: This method can return invalid continuation! Is it OK to do so?
        return currentCall.getLastContinuation();
    }

    public void jsSet_continuation(Object obj) {
        FOM_WebContinuation fwk = (FOM_WebContinuation)unwrap(obj);
        currentCall.setLastContinuation(fwk);
    }

    public FOM_WebContinuation jsFunction_sendPage(String uri,
                                                   Object obj,
                                                   Object wk)
        throws Exception {
        FOM_WebContinuation fom_wk = (FOM_WebContinuation)unwrap(wk);
        if (fom_wk != null) {
            // save page locals
            fom_wk.setPageLocal(pageLocal.getDelegate());
        }
        forwardTo(uri, unwrap(obj), fom_wk);
        return fom_wk;
    }

    public Scriptable jsFunction_createPageLocal() {
        return pageLocal.createPageLocal();
    }

    public void jsFunction_processPipelineTo(String uri,
                                             Object map,
                                             Object outputStream)
    throws Exception {
        Object out = unwrap(outputStream);
        if (!(out instanceof OutputStream)) {
            throw new JavaScriptException("expected a java.io.OutputStream instead of " + out);
        }
        getInterpreter().process(getParentScope(), this, uri, map,
                                 (OutputStream)out);
    }

    public void jsFunction_redirectTo(String uri, boolean isGlobal) throws Exception {
        if (isGlobal) {
            this.currentCall.redirector.globalRedirect(false, uri);
        } else {
            this.currentCall.redirector.redirect(false, uri);
        }
    }

    public void jsFunction_sendStatus(int sc) {
        this.currentCall.redirector.sendStatus(sc);
    }

/*

 NOTE (SM): These are the hooks to the future FOM Event Model that will be
 designed in the future. It has been postponed because we think
 there are more important things to do at the moment, but these
 are left here to indicate that they are planned.

    public void jsFunction_addEventListener(String eventName,
                                            Object function) {
        // what is this?
    }

    public void jsFunction_removeEventListener(String eventName,
                                               Object function) {
        // what is this?
    }

*/

    /**
     * Access components.
     *
     * TODO: Do we want to restrict the access of sitemap components? (RP)
     * TODO: Do we want to raise an error or return null? (RP)
     */
    public Object jsFunction_getComponent(String id)
        throws Exception {
        return org.mozilla.javascript.Context.javaToJS(currentCall.webAppContext.getBean(id), 
                                                       getParentScope());
    }

    /**
     * Release pooled components.
     *
     * @param component a component
     */
    public void jsFunction_releaseComponent(Object component) throws Exception {
        // this will be done by Spring
    }

    /**
     * Load the script file specified as argument.
     *
     * @param filename a <code>String</code> value
     * @return an <code>Object</code> value
     * @exception JavaScriptException if an error occurs
     */
    public Object jsFunction_load(String filename) throws Exception {
        org.mozilla.javascript.Context cx =
                org.mozilla.javascript.Context.getCurrentContext();
        Scriptable scope = getParentScope();

        Script script = getInterpreter().compileScript(cx, filename);
        return script.exec(cx, scope);
    }

    /**
     * Setup an object so that it can access the information provided to regular components.
     * This is done by calling the various Avalon lifecycle interfaces implemented by the object, which
     * are <code>LogEnabled</code>, <code>Contextualizable</code>, <code>Serviceable</code>
     * and <code>Initializable</code>.
     * <p>
     * <code>Contextualizable</code> is of primary importance as it gives access to the whole object model
     * (request, response, etc.) through the {@link org.apache.cocoon.components.ContextHelper} class.
     * <p>
     * Note that <code>Configurable</code> is ignored, as no configuration exists in a flowscript that
     * can be passed to the object.
     *
     * @param obj the object to setup
     * @return the same object (convenience that allows to write <code>var foo = cocoon.setupObject(new Foo());</code>).
     * @throws Exception if something goes wrong during setup.
     */
    public Object jsFunction_setupObject(Object obj) throws Exception {
        LifecycleHelper.setupComponent(unwrap(obj),
                                       getLogger(),
                                       getAvalonContext(),
                                       (ServiceManager) currentCall.webAppContext.getBean(AvalonUtils.SERVICE_MANAGER_ROLE),
                                       null /* configuration */);
        return org.mozilla.javascript.Context.javaToJS(obj, getParentScope());
    }

    /**
     * Create and setup an object so that it can access the information provided to regular components.
     * This is done by calling the various Avalon lifecycle interfaces implemented by the object, which
     * are <code>LogEnabled</code>, <code>Contextualizable</code>, <code>Serviceable</code>
     * and <code>Initializable</code>.
     * <p>
     * <code>Contextualizable</code> is of primary importance as it gives access to the whole object model
     * (request, response, etc.) through the {@link org.apache.cocoon.components.ContextHelper} class.
     * <p>
     * Note that <code>Configurable</code> is ignored, as no configuration exists in a flowscript that
     * can be passed to the object.
     *
     * @param classObj the class to instantiate, either as a String or a Rhino NativeJavaClass object
     * @return an set up instance of <code>clazz</code>
     * @throws Exception if something goes wrong either during instantiation or setup.
     */
    public Object jsFunction_createObject(Object classObj) throws Exception {
        Object result;

        if (classObj instanceof String) {
            result = ClassUtils.newInstance((String)classObj);

        } else if (classObj instanceof NativeJavaClass) {
            Class clazz = ((NativeJavaClass)classObj).getClassObject();
            result = clazz.newInstance();

        } else {
            throw new IllegalArgumentException("cocoon.createObject expects either a String or Class argument, but got "
                + classObj.getClass());
        }

        return jsFunction_setupObject(result);
    }

    /**
     * Dispose an object that has been created using {@link #jsFunction_createObject(Object)}.
     *
     * @param obj
     * @throws Exception
     */
    public void jsFunction_disposeObject(Object obj) throws Exception {
        LifecycleHelper.decommission(obj);
    }

    /**
     * Base JS wrapper for Cocoon's request/session/context objects.
     * <p>
     * FIXME(SW): The only thing added to the regular Java object is the fact that
     * attributes can be accessed as properties. Do we want to keep this?
     */
    private static abstract class AttributeHolderJavaObject extends NativeJavaObject {
        
        private static Map classProps = new HashMap();
        private Set propNames;

        public AttributeHolderJavaObject(Scriptable scope, Object object, Class clazz) {
            super(scope, object, clazz);
            this.propNames = getProperties(object.getClass());
        }
        
        /** Compute the names of JavaBean properties so that we can filter them our in get() */
        private static Set getProperties(Class clazz) {
            Set result = (Set)classProps.get(clazz);
            if (result == null) {
                try {
                    PropertyDescriptor[] descriptors = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
                    result = new HashSet();
                    for (int i = 0; i < descriptors.length; i++) {
                        result.add(descriptors[i].getName());
                    }
                } catch (IntrospectionException e) {
                    // Cannot introspect: just consider there are no properties
                    result = Collections.EMPTY_SET;
                }
                classProps.put(clazz, result);
            }
            return result;
        }
        
        
        protected abstract Enumeration getAttributeNames();
        protected abstract Object getAttribute(String name);
        
        public Object[] getIds() {
            // Get class Ids
            Object [] classIds = super.getIds();
            
            // and add attribute names
            ArrayList idList = new ArrayList(Arrays.asList(classIds));
            Enumeration iter = getAttributeNames();
            while(iter.hasMoreElements()) {
                idList.add(iter.nextElement());
            }
            return idList.toArray();
        }
        
        public boolean has(String name, Scriptable start) {
            return super.has(name, start) || getAttribute(name) != null;
        }
        
        public Object get(String name, Scriptable start) {
            Object result;
            // Filter out JavaBean properties. We only want methods of the underlying object.
            if (this.propNames.contains(name)) {
                result = NOT_FOUND;
            } else {
                result = super.get(name, start);
            }
            if (result == NOT_FOUND) {
                result = getAttribute(name);
                if (result != null) {
                    result = org.mozilla.javascript.Context.javaToJS(result, start);
                } else {
                    result = NOT_FOUND;
                }
            }
            return result;
        }
    }

    /**
     * JS wrapper for Cocoon's request object.
     * <p>
     * Request <em>parameters</em> are also present as properties on this object.
     * Note that this is different from <code>FOM_Context</code> and <code>FOM_Session</code>
     * that do the same with <em>attributes</em>.
     */
    public static class FOM_Request extends AttributeHolderJavaObject {
        private final Request request;
        
        public FOM_Request(Scriptable scope, Request request) {
            super(scope, request, Request.class);
            this.request = request;
        }
        
        protected Enumeration getAttributeNames() {
            return this.request.getParameterNames();
        }
        
        protected Object getAttribute(String name) {
            return this.request.getParameter(name);
        }
    }

    /**
     * JS wrapper for Cocoon's session object.
     * <p>
     * Session attributes are also present as properties on this object.
     */
    public static class FOM_Session extends AttributeHolderJavaObject {
        private final HttpSession session;
        
        public FOM_Session(Scriptable scope, HttpSession session) {
            super(scope, session, HttpSession.class);
            this.session = session;
        }
        
        protected Enumeration getAttributeNames() {
            return this.session.getAttributeNames();
        }
        
        protected Object getAttribute(String name) {
            return this.session.getAttribute(name);
        }
    }

    /**
     * JS wrapper for Cocoon's context object.
     * <p>
     * Context attributes are also present as properties on this object.
     */
    public static class FOM_Context extends AttributeHolderJavaObject {
        private final org.apache.cocoon.environment.Context context;
        
        public FOM_Context(Scriptable scope, org.apache.cocoon.environment.Context context) {
            super(scope, context, org.apache.cocoon.environment.Context.class);
            this.context = context;
        }
        
        protected Enumeration getAttributeNames() {
            return this.context.getAttributeNames();
        }
        
        protected Object getAttribute(String name) {
            return this.context.getAttribute(name);
        }
    }

    public Scriptable jsGet_request() {
        return currentCall.getRequest();
    }

    public Scriptable jsGet_response() {
        return currentCall.getResponse();
    }

    public Scriptable jsGet_log() {
        return currentCall.getLog();
    }

    public Scriptable jsGet_context() {
        return currentCall.getContext();
    }

    public Scriptable jsGet_session() {
        return currentCall.getSession();
    }

    /**
     * Get Sitemap parameters
     *
     * @return a <code>Scriptable</code> value whose properties represent
     * the Sitemap parameters from <map:call>
     */
    public Scriptable jsGet_parameters() {
        return getParameters();
    }

    public Scriptable getParameters() {
        return currentCall.getParameters();
    }

    void setParameters(Scriptable value) {
        currentCall.setParameters(value);
    }

    // unwrap Wrapper's and convert undefined to null
    private static Object unwrap(Object obj) {
        if (obj instanceof Wrapper) {
            obj = ((Wrapper)obj).unwrap();
        } else if (obj == Undefined.instance) {
            obj = null;
        }
        return obj;
    }

    // Make everything available to JavaScript objects implemented in Java:

    /**
     * Get the current request
     * @return The request
     */
    public Request getRequest() {
        return ObjectModelHelper.getRequest(currentCall.getObjectModel());
    }

    /**
     * Get the current session
     * @return The session (may be null)
     */
    public HttpSession getSession() {
        return (HttpSession) ObjectModelHelper.getRequest(currentCall.getObjectModel()).getSession(true);
    }

    /**
     * Get the current response
     * @return The response
     */
    public Response getResponse() {
        return ObjectModelHelper.getResponse(currentCall.getObjectModel());
    }

    /**
     * Get the current context
     * @return The context
     */
    public org.apache.cocoon.environment.Context getContext() {
        return ObjectModelHelper.getContext(currentCall.getObjectModel());
    }

    /**
     * Get the current settings object.
     */
    public Settings getSettings() {
        return (Settings)WebAppContextUtils.getCurrentWebApplicationContext().getBean(Settings.ROLE);
    }

    /**
     * Get the current settings object for java script.
     */
    public Scriptable jsGet_settings() {
        final Settings s = this.getSettings();
        return org.mozilla.javascript.Context.toObject(s, getParentScope());
    }
    
    /**
     * Get the current application context.
     */
    public WebApplicationContext getApplicationContext() {
        return WebAppContextUtils.getCurrentWebApplicationContext();
    }

    /**
     * Get the current application context for java script.
     */
    public Scriptable jsGet_applicationContext() {
        final WebApplicationContext w = this.getApplicationContext();
        return org.mozilla.javascript.Context.toObject(w, getParentScope());
    }

    /**
     * Get the current object model
     * @return The object model
     */
    public Map getObjectModel() {
        return currentCall.getObjectModel();
    }

    private Context getAvalonContext() {
        return currentCall.avalonContext;
    }

    protected Log getLogger() {
        return logger;
    }

    private FOM_JavaScriptInterpreter getInterpreter() {
        return currentCall.interpreter;
    }

    /**
     * Required by FOM_WebContinuation. This way we do not make whole Interpreter public
     * @return interpreter Id associated with this FOM.
     */
    public String getInterpreterId() {
        return getInterpreter().getInterpreterID();
    }
    
    /**
     * Call the Cocoon Sitemap to process a page
     * @param uri Uri to match
     * @param bean Input to page
     * @param fom_wk Current Web continuation (may be null)
     */

    public void forwardTo(String uri,
                          Object bean,
                          FOM_WebContinuation fom_wk)
        throws Exception {
        getInterpreter().forwardTo(getTopLevelScope(this),
                                   this,
                                   uri,
                                   bean,
                                   fom_wk,
                                   this.currentCall.redirector);
    }

    /**
     * Perform the behavior of <map:call continuation="blah">
     * This can be used in cases where the continuation id is not encoded
     * in the request in a form convenient to access in the sitemap.
     * Your script can extract the id from the request and then call
     * this method to process it as normal.
     * @param kontId The continuation id
     * @param parameters Any parameters you want to pass to the continuation (may be null)
     */
    public void handleContinuation(String kontId, Scriptable parameters)
        throws Exception {
        List list;
        if (parameters == null || parameters == Undefined.instance) {
            parameters = getParameters();
        }
        Object[] ids = parameters.getIds();
        list = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            String name = ids[i].toString();
            Argument arg = new Argument(name,
                                        org.mozilla.javascript.Context.toString(getProperty(parameters, name)));
            list.add(arg);
        }
        getInterpreter().handleContinuation(kontId, list, this.currentCall.redirector);
    }

    /**
     * Return this continuation if it is valid, or first valid parent
     */
    private FOM_WebContinuation findValidParent(final FOM_WebContinuation wk) {
        if (wk != null) {
            WebContinuation wc = wk.getWebContinuation();
            while (wc != null && wc.disposed()) {
                wc = wc.getParentContinuation();
            }
            if (wc != null) {
                FOM_WebContinuation parentWk = new FOM_WebContinuation(wc);
                parentWk.setLogger(getLogger());
                return parentWk;
            }
        }

        return null;
    }

    /**
     * Create a Bookmark WebContinuation from a JS Continuation with the last
     * continuation of sendPageAndWait as its parent.
     * PageLocal variables will be shared with the continuation of
     * the next call to sendPageAndWait().
     * @param k The JS continuation
     * @param ttl Lifetime for this continuation (zero means no limit)
     */
    public FOM_WebContinuation jsFunction_makeWebContinuation(Object k,
                                                              Object ttl)
        throws Exception {
        double d = org.mozilla.javascript.Context.toNumber(ttl);
        FOM_WebContinuation result =
            makeWebContinuation((Continuation)unwrap(k),
                                findValidParent(jsGet_continuation()),
                                (int)d);
        result.setPageLocal(pageLocal.getDelegate());
        currentCall.setLastContinuation(result);
        return result;
    }

    /**
     * Create a Web Continuation from a JS Continuation
     * @param k The JS continuation (may be null - null will be returned in that case)
     * @param parent The parent of this continuation (may be null)
     * @param timeToLive Lifetime for this continuation (zero means no limit)
     */
    public FOM_WebContinuation makeWebContinuation(Continuation k,
                                                   FOM_WebContinuation parent,
                                                   int timeToLive)
        throws Exception {
        if (k == null) {
            return null;
        }
        WebContinuation wk;
        ContinuationsManager contMgr;
        contMgr = (ContinuationsManager)currentCall.webAppContext.getBean(ContinuationsManager.ROLE);
        wk = contMgr.createWebContinuation(unwrap(k),
                                           (parent == null ? null : parent.getWebContinuation()),
                                           timeToLive,
                                           getInterpreter().getInterpreterID(),
                                           null);
        FOM_WebContinuation result = new FOM_WebContinuation(wk);
        result.setLogger(getLogger());
        result.setParentScope(getParentScope());
        result.setPrototype(getClassPrototype(getParentScope(),
                                              result.getClassName()));
        return result;
    }
}
