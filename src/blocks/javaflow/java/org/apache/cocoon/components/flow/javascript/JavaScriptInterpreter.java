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
package org.apache.cocoon.components.flow.javascript;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
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
 * Implementation of the java flow interpreter.
 *
 * @author <a href="mailto:stephan@apache.org">Stephan Michels</a>
 * @version CVS $Id: JavaScriptInterpreter.java,v 1.1 2004/06/24 16:48:53 stephan Exp $
 */
public class JavaScriptInterpreter extends AbstractInterpreter implements Configurable {

    private boolean initialized = false;

    private int timeToLive = 600000;

    /**
     * Key for storing a global scope object in the Cocoon session
     */
    public static final String USER_GLOBAL_SCOPE = "JAVASCRIPT GLOBAL SCOPE";

    private ContinuationClassLoader continuationclassloader;

    private Configuration configuration;

    static {
        JXPathIntrospector.registerDynamicClass(VarMap.class, VarMapHandler.class);
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        
        continuationclassloader = new ContinuationClassLoader(Thread.currentThread().getContextClassLoader());
        continuationclassloader.setDebug(config.getAttributeAsBoolean("debug", false));

        Configuration[] includes = config.getChildren("include");
        for (int i = 0; i < includes.length; i++)
            continuationclassloader.addIncludeClass(includes[i].getAttribute("class"));
        
        this.configuration = config;
    }

    
    public ScriptHelper getScriptHelper() throws Exception {
        
        ScriptHelper flow = (ScriptHelper)continuationclassloader
			.loadClass("org.apache.cocoon.components.flow.javascript.JavaScriptHelper").newInstance();

        for (Iterator scripts = needResolve.iterator(); scripts.hasNext();) {

            String name = (String) scripts.next();
            flow.register(name);
        }
        
        flow.configure(configuration);
        flow.initialize();
        
        return flow;
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

        if (getLogger().isDebugEnabled())
            getLogger().debug("calling function \"" + function + "\"");

        Request request = ContextHelper.getRequest(this.avalonContext);
        Session session = request.getSession(true);
        ScriptHelper flow = (ScriptHelper) session.getAttribute(USER_GLOBAL_SCOPE);

        ContinuationContext context = new ContinuationContext();
        context.setObject(flow);
        context.setAvalonContext(avalonContext);
        context.setLogger(getLogger());
        context.setServiceManager(manager);
        context.setRedirector(redirector);
        Parameters parameters = new Parameters();
        for(Iterator i=params.iterator(); i.hasNext();) {
        	Argument argument = (Argument)i.next();
        	parameters.setParameter(argument.name, argument.value);
        }
        context.setParameters(parameters);

        Continuation continuation = new Continuation(context);

        WebContinuation wk = continuationsMgr.createWebContinuation(continuation, null, timeToLive, null);
        FlowHelper.setWebContinuation(ContextHelper.getObjectModel(this.avalonContext), wk);

        continuation.registerThread();
        try {
            if (flow == null) {
            	
                if (getLogger().isDebugEnabled())
                    getLogger().debug("create new instance of the script helper");
                
                flow = getScriptHelper();
                context.setObject(flow);
            }

            flow.callFunction(function, params);

        } finally {
            // remove last object reference, which is not needed to
            // reconstruct the invocation path
            if (continuation.isCapturing())
                continuation.getStack().popReference();
            continuation.deregisterThread();
        }
        session.setAttribute(USER_GLOBAL_SCOPE, flow);
    }

    public void handleContinuation(String id, List params, Redirector redirector) throws Exception {

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
        context.setAvalonContext(avalonContext);
        context.setLogger(getLogger());
        context.setServiceManager(manager);
        context.setRedirector(redirector);
        Parameters parameters = new Parameters();
        for(Iterator i=params.iterator(); i.hasNext();) {
        	Argument argument = (Argument)i.next();
        	parameters.setParameter(argument.name, argument.value);
        }
        context.setParameters(parameters);
        
        Continuation continuation = new Continuation(parentContinuation, context);

        Request request = ContextHelper.getRequest(this.avalonContext);
        Session session = request.getSession(true);
        HashMap userScopes = (HashMap) session.getAttribute(USER_GLOBAL_SCOPE);

        ScriptHelper flow = (ScriptHelper) context.getObject();

        WebContinuation wk = continuationsMgr.createWebContinuation(continuation, parentwk, timeToLive, null);
        FlowHelper.setWebContinuation(ContextHelper.getObjectModel(this.avalonContext), wk);

        continuation.registerThread();
        try {

            flow.callFunction(null, null);

        } finally {
            // remove last object reference, which is not needed to reconstruct
            // the invocation path
            if (continuation.isCapturing())
                continuation.getStack().popReference();
            continuation.deregisterThread();
        }

        session.setAttribute(USER_GLOBAL_SCOPE, flow);
    }
}