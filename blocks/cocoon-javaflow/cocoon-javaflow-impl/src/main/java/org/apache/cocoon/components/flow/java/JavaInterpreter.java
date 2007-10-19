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
package org.apache.cocoon.components.flow.java;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.commons.javaflow.Continuation;
import org.apache.commons.javaflow.utils.ReflectionUtils;
import org.apache.commons.jxpath.JXPathIntrospector;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.flow.AbstractInterpreter;
import org.apache.cocoon.components.flow.FlowHelper;
import org.apache.cocoon.components.flow.InvalidContinuationException;
import org.apache.cocoon.components.flow.WebContinuation;
import org.apache.cocoon.environment.Redirector;

/**
 * Implementation of the java flow interpreter.
 *
 * @version $Id$
 */
public final class JavaInterpreter extends AbstractInterpreter {

    private int timeToLive = 600000;

    static {
        JXPathIntrospector.registerDynamicClass(VarMap.class, VarMapHandler.class);
    }

    private class ContinuationHandler {
        private Continuation continuation;
        
        public Continuation getContinuation() {
            return continuation;
        }
        public void setContinuation(final Continuation continuation) {
            this.continuation = continuation;
        }
    }
    
    private Map methods = new HashMap();

    private CocoonContinuationContext createContinuationContext( final List params, final Redirector redirector ) {
        final CocoonContinuationContext context = new CocoonContinuationContext();
        
        context.setAvalonContext(avalonContext);
        context.setLogger(getLogger());
        context.setServiceManager(manager);
        context.setRedirector(redirector);

        final Parameters parameters = new Parameters();
        for (final Iterator i = params.iterator(); i.hasNext();) {
            final Argument argument = (Argument) i.next();
            parameters.setParameter(argument.name, argument.value);
        }
        context.setParameters(parameters);
        
        return context;
    }
    
    
    private void updateMethodIndex() throws ClassNotFoundException {
        final Map methods = new HashMap();

        for (final Iterator it = needResolve.iterator(); it.hasNext();) {
            final String clazzName = (String) it.next();

            if (getLogger().isDebugEnabled()) { 
                getLogger().debug("loading " + clazzName);
            }        

            final Class clazz = Thread.currentThread().getContextClassLoader().loadClass(clazzName);
            
            final Map m = ReflectionUtils.discoverMethods(
                    clazz,
                    new ReflectionUtils.DefaultMatcher(),
                    new ReflectionUtils.Indexer() {
                        public void put(final Map pMap, final String pKey, final Object pObject) {
                            final Method method = (Method) pObject;
                            
                            final String fullName = method.getDeclaringClass().getName() + "." + method.getName();
                            final String shortName = method.getName();
                            
                            pMap.put(shortName, method);
                            pMap.put(fullName, method);

                            if (getLogger().isDebugEnabled()) { 
                                getLogger().debug("registered method " + shortName + ", " + fullName); 
                            }        
                        }
                    }
                    );

            for (Iterator i = m.entrySet().iterator(); i.hasNext(); ) {
                final Map.Entry e = (Map.Entry) i.next();
                
                if (getLogger().isWarnEnabled()) { 
                    if (methods.containsKey(e.getKey())) {
                            getLogger().warn("method name clash for " + e.getKey()); 
                        
                    }
                }        
                
                methods.put(e.getKey(), e.getValue());
            }        

        }
        
        // REVISIT: synchronize?
        this.methods = methods;
    }
    
    public void callFunction( final String methodName, final List params, final Redirector redirector ) throws Exception {

        // REVISIT: subscribe to jci events and only update accordingly
        updateMethodIndex();
        
        final Method method = (Method) methods.get(methodName);
        if (method == null) {
            throw new ProcessingException("no method '" + methodName + "' found in " + methods);
        }

        final CocoonContinuationContext context = createContinuationContext(params, redirector);
        
        final ContinuationHandler handler = new ContinuationHandler();
        
        final WebContinuation wk = continuationsMgr.createWebContinuation(
                handler, null, timeToLive, getInterpreterID(), null);

        FlowHelper.setWebContinuation(
                ContextHelper.getObjectModel(avalonContext), newObjectModel, wk);

        final Continuation newContinuation = Continuation.startWith(new Invoker(method), context);

        handler.setContinuation(newContinuation);
        
        getLogger().debug("generated javaflow continuation " + newContinuation);
    }

    public void handleContinuation( final String id, final List params, final Redirector redirector ) throws Exception {

        final WebContinuation oldWebContinuation = continuationsMgr.lookupWebContinuation(
                id, getInterpreterID());

        if (oldWebContinuation == null) {
            throw new InvalidContinuationException("invalid continuation id " + id);
        }

        final CocoonContinuationContext context = createContinuationContext(params, redirector);

        final ContinuationHandler oldHandler = (ContinuationHandler) oldWebContinuation.getContinuation();
        final ContinuationHandler newHandler = new ContinuationHandler();

        final WebContinuation newWebContinuation = continuationsMgr.createWebContinuation(
                newHandler, oldWebContinuation, timeToLive, getInterpreterID(), null);

        FlowHelper.setWebContinuation(
                ContextHelper.getObjectModel(avalonContext), newObjectModel, newWebContinuation);

        final Continuation oldContinuation = oldHandler.getContinuation();

        getLogger().debug("resuming javaflow continuation " + oldContinuation);

        final Continuation newContinuation = Continuation.continueWith(oldContinuation, context);

        newHandler.setContinuation(newContinuation);
        getLogger().debug("generated javaflow continuation " + newContinuation);
    }
}
