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
package org.apache.cocoon.components.flow.java;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.components.flow.javascript.JavaScriptCompilingClassLoader;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.commons.jxpath.JXPathIntrospector;

/**
 * Implementation of the java flow interpreter.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: JavaInterpreter.java,v 1.8 2004/06/14 14:53:56 stephan Exp $
 */
public class JavaInterpreter extends AbstractInterpreter implements Configurable {

    private boolean initialized = false;

    private int timeToLive = 600000;

    private static final String ACTION_METHOD_PREFIX = "do";

    /**
     * Key for storing a global scope object in the Cocoon session
     */
    public static final String USER_GLOBAL_SCOPE = "JAVA GLOBAL SCOPE";

    private ContinuationClassLoader continuationclassloader;

    private HashMap methods = new HashMap();

    private JavaScriptCompilingClassLoader javascriptclassloader;

    static {
        JXPathIntrospector.registerDynamicClass(VarMap.class, VarMapHandler.class);
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        
        javascriptclassloader = new JavaScriptCompilingClassLoader(Thread.currentThread().getContextClassLoader());
        try {
            javascriptclassloader.service(this.manager);
        } catch (ServiceException e) {
            throw new ConfigurationException(e.getMessage());
        }
        continuationclassloader = new ContinuationClassLoader(javascriptclassloader);

        Configuration[] includes = config.getChildren("include");
        for (int i = 0; i < includes.length; i++)
            continuationclassloader.addIncludeClass(includes[i].getAttribute("class"));
    }

    private static String removePrefix(String name) {
        int prefixLen = ACTION_METHOD_PREFIX.length();
        return name.substring(prefixLen, prefixLen + 1).toLowerCase() + name.substring(prefixLen + 1);
    }
    
    public void initialize() throws Exception {
        
        if (getLogger().isDebugEnabled())
            getLogger().debug("initialize java flow interpreter");

        initialized = true;

        for (Iterator scripts = needResolve.iterator(); scripts.hasNext();) {

            String name = (String) scripts.next();

            if (name.endsWith(".js")) {
                javascriptclassloader.addSource(name);
                name = JavaScriptCompilingClassLoader.getClassName(name);
            }

            if (getLogger().isDebugEnabled())
                getLogger().debug("registered java class \"" + name + "\" for flow");
            
            Class clazz = continuationclassloader.loadClass(name);

            if (!Continuable.class.isAssignableFrom(clazz)) {
                getLogger().error("java class \"" + name + "\" doesn't implement Continuable");
                continue;
            }

            //Class clazz = continuationclassloader.loadClass(name);

            try {
                Method[] methods = clazz.getMethods();

                for (int i = 0; i < methods.length; i++) {
                    String methodName = methods[i].getName();
                    if (methodName.startsWith(ACTION_METHOD_PREFIX)) {
                        String function = removePrefix(methodName);
                        this.methods.put(function, methods[i]);

                        if (getLogger().isDebugEnabled())
                            getLogger().debug(
                                    "registered method \"" + methodName + "\" as function \"" + function + "\"");
                    }
                }
            } catch (Exception e) {
                throw new ConfigurationException("cannot get methods by reflection", e);
            }

        }
    }

    /**
     * Calls a Java function, passing <code>params</code> as its
     * arguments. In addition to this, it makes available the parameters
     * through the <code>cocoon.parameters</code> Java array
     * (indexed by the parameter names).
     *
     * @param function a <code>String</code> value
     * @param params a <code>List</code> value
     * @param redirector
     * @exception Exception if an error occurs
     */
    public void callFunction(String function, List params, Redirector redirector) throws Exception {

        if (!initialized)
            initialize();

        Method method = (Method) methods.get(function);

        if (method == null)
            throw new ProcessingException("No method found for '" + function + "'");

        if (getLogger().isDebugEnabled())
            getLogger().debug("calling method \"" + method + "\"");

        Request request = ContextHelper.getRequest(this.avalonContext);
        Session session = request.getSession(true);
        HashMap userScopes = (HashMap) session.getAttribute(USER_GLOBAL_SCOPE);
        if (userScopes == null)
            userScopes = new HashMap();

        Continuable flow = (Continuable) userScopes.get(method.getDeclaringClass());

        ContinuationContext context = new ContinuationContext();
        context.setObject(flow);
        context.setMethod(method);
        context.setAvalonContext(avalonContext);
        context.setLogger(getLogger());
        context.setServiceManager(manager);
        context.setRedirector(redirector);

        Continuation continuation = new Continuation(context);

        WebContinuation wk = continuationsMgr.createWebContinuation(continuation, null, timeToLive, null);
        FlowHelper.setWebContinuation(ContextHelper.getObjectModel(this.avalonContext), wk);

        continuation.registerThread();
        try {
            if (flow == null) {
                if (getLogger().isDebugEnabled())
                    getLogger().debug("create new instance of \"" + method.getDeclaringClass() + "\"");

                flow = (Continuable) method.getDeclaringClass().newInstance();
                context.setObject(flow);
            }

            method.invoke(flow, new Object[0]);

        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() != null) {
                if (ite.getTargetException() instanceof Exception)
                    throw (Exception) ite.getTargetException();
                else if (ite.getTargetException() instanceof Error)
                    throw new ProcessingException("An internal error occured", ite.getTargetException());
                else if (ite.getTargetException() instanceof RuntimeException)
                    throw (RuntimeException) ite.getTargetException();
                else
                    throw ite;
            } else {
                throw ite;
            }
        } finally {
            // remove last object reference, which is not needed to
            // reconstruct the invocation path
            if (continuation.isCapturing())
                continuation.getStack().popReference();
            continuation.deregisterThread();
        }
        userScopes.put(method.getDeclaringClass(), flow);
        session.setAttribute(USER_GLOBAL_SCOPE, userScopes);
    }

    public void handleContinuation(String id, List params, Redirector redirector) throws Exception {
        if (!initialized)
            initialize();

        WebContinuation parentwk = continuationsMgr.lookupWebContinuation(id);

        if (parentwk == null) {
        /*
         * Throw an InvalidContinuationException to be handled inside the
         * <map:handle-errors> sitemap element.
         */
        throw new InvalidContinuationException("The continuation ID " + id + " is invalid."); }

        Continuation parentContinuation = (Continuation) parentwk.getContinuation();
        ContinuationContext parentContext = (ContinuationContext) parentContinuation.getContext();
        ContinuationContext context = new ContinuationContext();
        context.setObject(parentContext.getObject());
        context.setMethod(parentContext.getMethod());
        context.setAvalonContext(avalonContext);
        context.setLogger(getLogger());
        context.setServiceManager(manager);
        context.setRedirector(redirector);
        Continuation continuation = new Continuation(parentContinuation, context);

        Request request = ContextHelper.getRequest(this.avalonContext);
        Session session = request.getSession(true);
        HashMap userScopes = (HashMap) session.getAttribute(USER_GLOBAL_SCOPE);

        Continuable flow = (Continuable) context.getObject();
        Method method = context.getMethod();

        WebContinuation wk = continuationsMgr.createWebContinuation(continuation, parentwk, timeToLive, null);
        FlowHelper.setWebContinuation(ContextHelper.getObjectModel(this.avalonContext), wk);

        continuation.registerThread();
        try {

            method.invoke(flow, new Object[0]);

        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() != null) {
                if (ite.getTargetException() instanceof Exception)
                    throw (Exception) ite.getTargetException();
                else if (ite.getTargetException() instanceof Error)
                    throw new ProcessingException("An internal error occured", ite.getTargetException());
                else if (ite.getTargetException() instanceof RuntimeException)
                    throw (RuntimeException) ite.getTargetException();
                else
                    throw ite;
            } else {
                throw ite;
            }
        } finally {
            // remove last object reference, which is not needed to reconstruct
            // the invocation path
            if (continuation.isCapturing())
                continuation.getStack().popReference();
            continuation.deregisterThread();
        }

        userScopes.put(method.getDeclaringClass(), flow);
        session.setAttribute(USER_GLOBAL_SCOPE, userScopes);
    }
}