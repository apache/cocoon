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
package org.apache.cocoon.components.flow.groovy;

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
import org.apache.cocoon.components.flow.java.Continuable;
import org.apache.cocoon.components.flow.java.Continuation;
import org.apache.cocoon.components.flow.java.ContinuationClassLoader;
import org.apache.cocoon.components.flow.java.ContinuationContext;
import org.apache.cocoon.components.flow.java.VarMap;
import org.apache.cocoon.components.flow.java.VarMapHandler;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.commons.jxpath.JXPathIntrospector;

/**
 * Implementation of the groovy flow interpreter.
 *
 * @version CVS $Id: GroovyInterpreter.java,v 1.1 2004/06/07 01:04:18 antonio Exp $
 */
public class GroovyInterpreter extends AbstractInterpreter implements Configurable {

    private boolean initialized = false;
    private int timeToLive = 600000;
    private static final String ACTION_METHOD_PREFIX = "do";

    /**
     * Key for storing a global scope object in the Cocoon session
     */
    public static final String USER_GLOBAL_SCOPE = "JAVA GLOBAL SCOPE";
    private ContinuationClassLoader continuationclassloader;
    private HashMap flowableMethods = new HashMap(); // Store flowableMehods found in all classes on the sitemap.
    private GroovyCompilingClassLoader groovyClassLoader;

    static {
        JXPathIntrospector.registerDynamicClass(VarMap.class, VarMapHandler.class);
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);

        groovyClassLoader = new GroovyCompilingClassLoader(Thread.currentThread().getContextClassLoader());
        try {
            groovyClassLoader.service(this.manager);
        } catch (ServiceException e) {
            throw new ConfigurationException(e.getMessage());
        }
        continuationclassloader = new ContinuationClassLoader(groovyClassLoader);

        Configuration[] includes = config.getChildren("include");
        for (int i = 0; i < includes.length; i++) {
            continuationclassloader.addIncludeClass(includes[i].getAttribute("class"));
        }
    }

    private static String removePrefix(String name) {
        int prefixLen = ACTION_METHOD_PREFIX.length();
        return name.substring(prefixLen, prefixLen + 1).toLowerCase() + name.substring(prefixLen + 1);
    }

    /**
     * Initialize the Groovy interpreter by compiling all the groovy scripts defined in the sitemap.
     * @throws Exception
     */
    public void initialize() throws Exception {

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("initialize java flow interpreter");
        }
        initialized = true;

        for (Iterator scripts = needResolve.iterator(); scripts.hasNext();) {
            String className = (String) scripts.next();
            Class groovyClass;

            if (className.endsWith(".gy")) {
                groovyClass = groovyClassLoader.getGroovyClass(className);
                className = groovyClass.getName();
            }
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("registered java class \"" + className + "\" for flow");
            }
            Class clazz = continuationclassloader.loadClass(className);
            if (!Continuable.class.isAssignableFrom(clazz)) {
                getLogger().error("java class \"" + className + "\" doesn't implement Continuable");
                continue;
            }
            // Detect methods that are "Flowable" and build a list of them
            try {
                Method[] classMethods = clazz.getMethods();
                for (int i = 0; i < classMethods.length; i++) {
                    String methodName = classMethods[i].getName();
                    if (methodName.startsWith(ACTION_METHOD_PREFIX)) {
                        String function = removePrefix(methodName);
                        flowableMethods.put(function, classMethods[i]);
                        if (getLogger().isDebugEnabled()) {
                            getLogger().debug("registered method \"" + methodName + "\" as function \"" + function + "\"");
                        }
                    }
                }
            } catch (Exception e) {
                throw new ConfigurationException("cannot get methods by reflection", e);
            }
        }
    }

    /**
     * Calls a groovy function, passing <code>params</code> as its
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
        if (!initialized) {
            initialize();
        }
        Method method = (Method)flowableMethods.get(function);
        if (method == null) {
            throw new ProcessingException("No method found for '" + function + "'");
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("calling method \"" + method + "\"");
        }
        Request request = ContextHelper.getRequest(this.avalonContext);
        Session session = request.getSession(true);
        HashMap userScopes = (HashMap) session.getAttribute(USER_GLOBAL_SCOPE);
        if (userScopes == null) {
            userScopes = new HashMap();
        }
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
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("create new instance of \"" + method.getDeclaringClass() + "\"");
                }
                flow = (Continuable) method.getDeclaringClass().newInstance();
                context.setObject(flow);
            }
            method.invoke(flow, new Object[0]);
        } catch (InvocationTargetException ite) {
            if (ite.getTargetException() != null) {
                if (ite.getTargetException() instanceof Exception) {
                    throw (Exception) ite.getTargetException();
                } else if (ite.getTargetException() instanceof Error) {
                    throw new ProcessingException("An internal error occured", ite.getTargetException());
                } else if (ite.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) ite.getTargetException();
                } else {
                    throw ite;
                }
            } else {
                throw ite;
            }
        } finally {
            // remove last object reference, which is not needed to
            // reconstruct the invocation path
            if (continuation.isCapturing()) {
                continuation.getStack().popReference();
            }
            continuation.deregisterThread();
        }
        userScopes.put(method.getDeclaringClass(), flow);
        session.setAttribute(USER_GLOBAL_SCOPE, userScopes);
    }

    public void handleContinuation(String id, List params, Redirector redirector) throws Exception {
        if (!initialized) {
            initialize();
        }
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
                if (ite.getTargetException() instanceof Exception) {
                    throw (Exception) ite.getTargetException();
                } else if (ite.getTargetException() instanceof Error) {
                    throw new ProcessingException("An internal error occured", ite.getTargetException());
                } else if (ite.getTargetException() instanceof RuntimeException) {
                    throw (RuntimeException) ite.getTargetException();
                } else {
                    throw ite;
                }
            } else {
                throw ite;
            }
        } finally {
            // remove last object reference, which is not needed to reconstruct
            // the invocation path
            if (continuation.isCapturing()) {
                continuation.getStack().popReference();
            }
            continuation.deregisterThread();
        }
        userScopes.put(method.getDeclaringClass(), flow);
        session.setAttribute(USER_GLOBAL_SCOPE, userScopes);
    }
}