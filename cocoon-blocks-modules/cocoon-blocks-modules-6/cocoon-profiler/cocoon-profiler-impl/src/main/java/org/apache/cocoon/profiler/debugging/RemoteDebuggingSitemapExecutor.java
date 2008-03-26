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
package org.apache.cocoon.profiler.debugging;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;

import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.profiler.Profiler;
import org.apache.cocoon.components.profiler.ProfilerResult;
import org.apache.cocoon.components.sax.XMLByteStreamInterpreter;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Redirector;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.matching.Matcher;
import org.apache.cocoon.matching.PreparableMatcher;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;
import org.apache.cocoon.sitemap.ExecutionContext;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapExecutor;
import org.apache.cocoon.util.AbstractLogEnabled;
import org.apache.cocoon.xml.XMLUtils;
import org.apache.cocoon.xml.dom.DOMBuilder;

/**
 * Sample sitemap executor that prints out everything to a logger
 * 
 * @since 2.2
 * @version $Id$
 */
public class RemoteDebuggingSitemapExecutor extends AbstractLogEnabled
                                            implements ThreadSafe, SitemapExecutor, Serviceable,
                                                       Contextualizable {

    protected ServiceManager manager;
    protected Context        context;

    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeAction(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.acting.Action, org.apache.cocoon.environment.Redirector, org.apache.cocoon.environment.SourceResolver, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokeAction(final ExecutionContext context,
                            final Map              objectModel, 
                            final Action           action, 
                            final Redirector       redirector, 
                            final SourceResolver   resolver, 
                            final String           resolvedSource, 
                            final Parameters       resolvedParams )
    throws Exception {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            String configuration = Debugger.xmlElement("src",resolvedSource);
            debugger.sendSitemapElement("act", configuration, resolvedParams);
        }
        final Map result = action.act(redirector, resolver, objectModel, resolvedSource, resolvedParams);
        return result;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeMatcher(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.matching.Matcher, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokeMatcher(ExecutionContext context, 
                             Map objectModel,
                             Matcher matcher, 
                             String pattern, 
                             Parameters resolvedParams)
    throws PatternException {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            String p = Debugger.xmlElement("pattern", pattern);
            debugger.sendSitemapElement("match", p, resolvedParams);
        }
        final Map result = matcher.match(pattern, objectModel, resolvedParams);
        return result;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokePreparableMatcher(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.matching.PreparableMatcher, java.lang.String, java.lang.Object, org.apache.avalon.framework.parameters.Parameters)
     */
    public Map invokePreparableMatcher(ExecutionContext  context,
                                       Map               objectModel,
                                       PreparableMatcher matcher,
                                       String            pattern,
                                       Object            preparedPattern,
                                       Parameters        resolvedParams )
    throws PatternException {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            String p = Debugger.xmlElement("pattern", pattern);
            debugger.sendSitemapElement("match", p, resolvedParams);
        }
        final Map result = matcher.preparedMatch(preparedPattern, objectModel, resolvedParams);
        return result;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeSelector(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.selection.Selector, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public boolean invokeSelector(ExecutionContext context,
                                  Map objectModel,
                                  Selector selector,
                                  String expression,
                                  Parameters parameters) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if ( debugger != null ) {
            debugger.sendSitemapElement("select", 
                                        null, 
                                        parameters);
            String configuration = Debugger.xmlElement("test", expression);
            debugger.sendSitemapElement("when", 
                                        configuration, 
                                        parameters);
        }
        final boolean result = selector.select(expression, objectModel, parameters);
        return result;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#invokeSwitchSelector(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.selection.SwitchSelector, java.lang.String, org.apache.avalon.framework.parameters.Parameters, java.lang.Object)
     */
    public boolean invokeSwitchSelector(ExecutionContext context,
                                        Map objectModel,
                                        SwitchSelector selector,
                                        String expression,
                                        Parameters parameters,
                                        Object selectorContext) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if ( debugger != null ) {
            debugger.sendSitemapElement("select", 
                                        null, 
                                        parameters);
            String configuration = Debugger.xmlElement("test", expression);
            debugger.sendSitemapElement("when", 
                                        configuration, 
                                        parameters);
        }
        final boolean result = selector.select(expression, selectorContext);
        return result;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#popVariables(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map)
     */
    public void popVariables(ExecutionContext context,
                             Map              objectModel) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if ( debugger != null ) {
            debugger.popInformation();
        }
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#pushVariables(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String, java.util.Map)
     */
    public Map pushVariables(ExecutionContext context, 
                             Map              objectModel,
                             String           key, 
                             Map              variables) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if ( debugger != null ) {
            debugger.pushInformation(variables);
            if ( key != null ) {
                debugger.addNamedInformation(key, variables);
            }
        }
        return variables;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#enterSitemap(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String)
     */
    public void enterSitemap(ExecutionContext context, 
                               Map objectModel,
                               String source) {
        Integer sitemapCounter = Debugger.getSitemapCounter(objectModel);
        Debugger debugger = Debugger.getDebugger(objectModel);
        if ( debugger == null ) {
            // is this the first sitemap?
            if ( sitemapCounter.intValue() == 0 ) {
                final Request request = ObjectModelHelper.getRequest(objectModel);
                if (request.getParameter(Debugger.REQUEST_PARAMETER) != null) {
                    String value = request.getParameter(Debugger.REQUEST_PARAMETER);
                    debugger = new Debugger();
                    try {
                        debugger.setDebugInfo(value);
                        ContainerUtil.contextualize(debugger, this.context);
                        ContainerUtil.service(debugger, this.manager);
                        ContainerUtil.initialize(debugger);
                    } catch (Exception ignore) {
                        // we simply ignore this and turn off debugging
                        debugger = null;
                    }
                }
            }
        }
        if (debugger != null) {
            // if this is not the first sitemap, then we have a mount
            if ( sitemapCounter.intValue() > 0 ) {
                String p = Debugger.xmlElement("src", source);
                debugger.sendSitemapElement("mount", p, null);
            }

            // first we send the sitemap
            org.apache.excalibur.source.SourceResolver resolver = null;
            Source src = null;
            try {
                resolver = (org.apache.excalibur.source.SourceResolver) this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
                src = resolver.resolveURI(source);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                InputStream inputStream = src.getInputStream();
                byte[] buffer = new byte[4096];
                int length;
                while ((length = inputStream.read(buffer)) > -1) {
                    baos.write(buffer, 0, length);
                }
                inputStream.close();
                baos.close();
                StringBuffer buf = new StringBuffer("<sitemap src=\"");
                buf.append(source).append("\">\n");
                buf.append(baos.toString("utf-8"));
                buf.append("\n</sitemap>\n");
                debugger.send(buf.toString());
            } catch (Exception ignore ) {
                // we ignore this for now
            } finally {
                if (source != null) {
                    resolver.release(src);
                }
                this.manager.release(resolver);
            }
            
            debugger.sendSitemapElement("pipelines", null);
                                
        }
        Debugger.incSitemapCounter(objectModel);
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#leaveSitemap(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map)
     */
    public void leaveSitemap(ExecutionContext context, Map objectModel) {
        Integer count = Debugger.decSitemapCounter(objectModel);
        if ( count.intValue() == 0 ) {
            final Debugger debugger = Debugger.getDebugger(objectModel);
            if ( debugger != null ) {
                this.notifyPipelineProcessed(context, objectModel);
                debugger.sendFinal("<finished/>");
                debugger.close();
                ContainerUtil.dispose(debugger);
            }
        }
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addGenerator(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addGenerator(ExecutionContext context,
                                                     Map objectModel,
                                                     PipelineComponentDescription desc) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            String configuration = Debugger.xmlElement("src", desc.source);
            debugger.sendSitemapElement("generate", 
                                        configuration, 
                                        desc.parameters);
        }
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addReader(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addReader(ExecutionContext context,
                                                  Map objectModel,
                                                  PipelineComponentDescription desc) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            String configuration = Debugger.xmlElement("src", desc.source);
            debugger.sendSitemapElement("read", 
                                        configuration, 
                                        desc.parameters);
        }
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addSerializer(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addSerializer(ExecutionContext context,
                                                      Map objectModel,
                                                      PipelineComponentDescription desc) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            debugger.sendSitemapElement("serialize", 
                                        null,
                                        desc.parameters);
        }
        return desc;
    }
    
    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#addTransformer(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription addTransformer(ExecutionContext context,
                                                       Map objectModel,
                                                       PipelineComponentDescription desc) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            String configuration = Debugger.xmlElement("src", desc.source);
            debugger.sendSitemapElement("transform", 
                                        configuration, 
                                        desc.parameters);
        }
        return desc;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#redirectTo(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, java.lang.String, boolean, boolean, boolean)
     */
    public String redirectTo(ExecutionContext context,
                             Map objectModel,
                             String uri,
                             boolean createSession,
                             boolean global,
                             boolean permanent) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            String configuration = Debugger.xmlElement("uri", uri);
            debugger.sendSitemapElement("redirect-to", 
                                     configuration, 
                                     null);
        }
        return uri;
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapExecutor#enteringPipeline(org.apache.cocoon.sitemap.ExecutionContext, java.util.Map, org.apache.cocoon.sitemap.SitemapExecutor.PipelineComponentDescription)
     */
    public PipelineComponentDescription enteringPipeline(ExecutionContext context, Map objectModel, PipelineComponentDescription desc) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if ( debugger != null ) {
            // we always use the profiling noncaching pipeline
            desc.type = "profile-noncaching";
            debugger.sendSitemapElement("pipeline", null, desc.parameters);
        }
        return desc;
    }

    protected void notifyPipelineProcessed(ExecutionContext context, Map objectModel) {
        final Debugger debugger = Debugger.getDebugger(objectModel);
        if (debugger != null) {
            Profiler profiler = null;
            try {
                profiler = (Profiler) this.manager.lookup(Profiler.ROLE);
                ProfilerResult data = null; 
                Collection c = profiler.getResultKeys();
                Iterator it = c.iterator();
                while (it.hasNext()) {
                    Object o = it.next();
                    data = profiler.getResult(o);
                }
                if (data != null) {
                    // TODO - is this right, it was getLatestSAXFragments()
                    Object[][] frags = data.getSAXFragments();
                    Object[] os = frags[0];
                    for(int i = 0; i<os.length-1; i++) {
                        Object o = os[i];
                        try {
                            DOMBuilder builder = new DOMBuilder();
                            XMLByteStreamInterpreter deserializer = new XMLByteStreamInterpreter();
                            deserializer.setConsumer(builder);
                            deserializer.deserialize(o);
                            String xml = "<stream>\n"+
                                         XMLUtils.serializeNode(builder.getDocument()) +
                                         "\n</stream>\n";
                            debugger.send(xml);
                        } catch (Exception ignore) {
                            // ignore this
                        }
                    }
                }
                profiler.clearResults();
            } catch (Exception ignore) {
                // ignore this
            } finally {
                this.manager.release(profiler);
            }
        }
    }
}
