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
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.util.ReflectionUtils;
import org.apache.commons.jxpath.JXPathIntrospector;
import org.apache.javaflow.Continuable;
import org.apache.javaflow.Continuation;

/**
 * Implementation of the java flow interpreter.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id$
 */
public class JavaInterpreter extends AbstractInterpreter implements Configurable {

    private int timeToLive = 600000;

    /**
     * Key for storing a global scope object in the Cocoon session
     */
    public static final String USER_GLOBAL_SCOPE = "JAVA GLOBAL SCOPE";

    static {
        JXPathIntrospector.registerDynamicClass(VarMap.class, VarMapHandler.class);
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
    }

    public void initialize() throws Exception {
    }

    public void continueFlow( final WebContinuation parentwk, final List params, final Redirector redirector, final Continuation parentContinuation, final CocoonContinuationContext context) throws Exception {

        context.setAvalonContext(avalonContext);
        context.setLogger(getLogger());
        context.setServiceManager(manager);
        context.setRedirector(redirector);

        final Parameters parameters = new Parameters();
        for(Iterator i = params.iterator(); i.hasNext();) {
            final Argument argument = (Argument)i.next();
            parameters.setParameter(argument.name, argument.value);
        }
        context.setParameters(parameters);

        final Continuation continuation = (parentContinuation != null)
        ? new Continuation(parentContinuation, context)
        : new Continuation(context);

        System.out.println("created new continuation " + continuation);
        if (getLogger().isDebugEnabled()) { 
            getLogger().debug("created new continuation " + continuation);
        }

        final WebContinuation wk = continuationsMgr.createWebContinuation(
                continuation, parentwk, timeToLive, getInterpreterID(), null);

        FlowHelper.setWebContinuation(ContextHelper.getObjectModel(this.avalonContext), wk);
                
        continuation.registerThread();

        if (parentContinuation != null) {
            System.out.println("resuming continuation " + continuation + continuation.getStack());
            if (getLogger().isDebugEnabled()) { 
                getLogger().debug("resuming continuation " + continuation + continuation.getStack());
            }            
        }
        
        try {
            System.out.println("calling " + context.getMethod());
            if (getLogger().isDebugEnabled()) { 
                getLogger().debug("calling " + context.getMethod());
            }

            context.getMethod().invoke(context.getObject(), new Object[0]);

            System.out.println("back from " + context.getMethod());
            if (getLogger().isDebugEnabled()) { 
                getLogger().debug("back from " + context.getMethod());
            }

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

            if (continuation.isCapturing()) {
                continuation.getStack().popReference();
            }

            continuation.deregisterThread();

            System.out.println("state saved in continuation" + continuation + continuation.getStack());                
            if (getLogger().isDebugEnabled()) { 
                getLogger().debug("state saved in continuation" + continuation + continuation.getStack());
            }
        }
        
    }
    
    public void callFunction(String function, List params, Redirector redirector) throws Exception {

        final Map methods = new HashMap();

        // REVISIT: this is ugly as hell!
        // but something better would require an
        // imcompatible change of the flow handling.
        // Make sure you don't have overlapping method
        // names you want to call
        
        for (Iterator it = needResolve.iterator(); it.hasNext();) {
            final String clazzName = (String) it.next();

            System.out.println("loading " + clazzName);
            if (getLogger().isDebugEnabled()) { 
                getLogger().debug("loading " + clazzName);
            }        

            final Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
            final Map m = ReflectionUtils.discoverMethods(clazz);
            methods.putAll(m);
        }

        final Method method = (Method) methods.get(function);

        if (method == null) {
            throw new ProcessingException("no method '" + function + "' found. " + methods);
        }

        if (getLogger().isDebugEnabled()) { 
            getLogger().debug("setting up continuation context");
        }

        final Request request = ContextHelper.getRequest(this.avalonContext);
        final Session session = request.getSession(true);

        HashMap userScopes = (HashMap) session.getAttribute(USER_GLOBAL_SCOPE);
        if (userScopes == null) {
            userScopes = new HashMap();
        }

        final CocoonContinuationContext context = new CocoonContinuationContext();
        
        Continuable flow = null; //(Continuable) userScopes.get(method.getDeclaringClass());

        if (flow == null) {
            
            if (getLogger().isDebugEnabled()) { 
                getLogger().debug("creating flow class " + method.getDeclaringClass().getName());
            }

            flow = (Continuable) method.getDeclaringClass().newInstance();

            userScopes.put(method.getDeclaringClass(), flow);
            //session.setAttribute(USER_GLOBAL_SCOPE, userScopes);
        }

        context.setObject(flow);
        context.setMethod(method);

        continueFlow(null, params, redirector, null, context);
    }

    public void handleContinuation(String id, List params, Redirector redirector)
            throws Exception {

        final WebContinuation parentwk = continuationsMgr.lookupWebContinuation(id, getInterpreterID());

        if (parentwk == null) {
            /*
             * Throw an InvalidContinuationException to be handled inside the
             * <map:handle-errors> sitemap element.
             */
            throw new InvalidContinuationException("Invalid continuation id " + id);
        }

        if (getLogger().isDebugEnabled()) { 
            getLogger().debug("continue with continuation " + id);
        }

        final CocoonContinuationContext context = new CocoonContinuationContext();

        final Continuation parentContinuation = (Continuation) parentwk.getContinuation();
        final CocoonContinuationContext parentContext = (CocoonContinuationContext) parentContinuation.getContext();
        context.setObject(parentContext.getObject());
        context.setMethod(parentContext.getMethod());

        continueFlow(parentwk, params, redirector, parentContinuation, context);
    }
        
}
