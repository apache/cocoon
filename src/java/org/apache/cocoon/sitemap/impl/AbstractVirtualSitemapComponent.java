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
package org.apache.cocoon.sitemap.impl;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.pipeline.VirtualProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.sitemap.VPCNode;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.sax.XMLizable;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * TODO List:
 * <ul>
 * <li>Refactor ProcessingPipelines implementations
 * <li>Implement caching
 * </ul>
 */
public abstract class AbstractVirtualSitemapComponent extends AbstractLogEnabled
    implements SitemapModelComponent, Serviceable, Disposable, Contextualizable, Configurable {

    protected SourceResolver resolver;
    protected DefaultContext context;
    protected ServiceManager manager;
    protected ProcessingNode node;
    protected ProcessingPipeline pipeline;
    protected String sourceMapName;
    protected Map sourceMap = new HashMap();
    protected Set sources;
    protected String name;


    protected class MyInvokeContext extends InvokeContext {
        public MyInvokeContext() throws Exception {
            super(true);
            super.processingPipeline = new VirtualProcessingPipeline(AbstractVirtualSitemapComponent.this.context);
        }
    }

    abstract protected String getTypeName();

    public void contextualize(Context context) throws ContextException {
        this.context = (DefaultContext)context;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Release all resources.
     */
    public void dispose() {
        try {
            Iterator sources =
                this.sourceMap.values().iterator();
            while (sources.hasNext()) {
                Source source = (Source)sources.next();
                // FIXME
                // These are allready disposed, why?
                //this.resolver.release(source);
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not dispose sources", e);
        }
        this.manager = null;
    }

    public void configure(Configuration configuration) throws ConfigurationException {
        this.name = configuration.getAttribute("name");
        this.sourceMapName =
            Constants.CONTEXT_ENV_PREFIX + "-" + getTypeName() + "-source-map-" + this.name;
        try {
            this.node = (ProcessingNode)this.context.get(Constants.CONTEXT_VPC_PREFIX +
                                                         getTypeName() + "-" + this.name);
            this.sources = ((VPCNode)node).getSources();
        } catch (Exception e) {
            throw new ConfigurationException("Can not find VirtualPipelineComponent '" +
                                             this.name + "' configuration");
        }
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        this.resolver = resolver;

        Environment env = EnvironmentHelper.getCurrentEnvironment();
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();

        // save callers resolved sources if there are any
        Map oldSourceMap = (Map)env.getAttribute(this.sourceMapName);
        // place for resolved sources
        env.setAttribute(this.sourceMapName, this.sourceMap);

        MyInvokeContext invoker = null;

        try {
            // resolve the sources in the parameter map before switching context
            Map resolvedParams = resolveParams(par, src);

            String uri = (String) this.context.get(Constants.CONTEXT_ENV_URI);
            String prefix = (String) this.context.get(Constants.CONTEXT_ENV_PREFIX);
            env.setURI(prefix, uri);

            invoker = new MyInvokeContext();
            invoker.enableLogging(getLogger());
            invoker.service(this.manager);
            invoker.pushMap(null, resolvedParams);

            this.node.invoke(env, invoker);
            this.pipeline = invoker.getProcessingPipeline();
        } catch (Exception e) {
            throw new ProcessingException("Oops", e);
        } finally {
            if (invoker != null) {
                invoker.popMap();
                invoker.dispose();
            }
            // Restore context
            env.setURI(oldPrefix, oldURI);
            // restore sourceMap
            if (oldSourceMap != null)
                env.setAttribute(this.sourceMapName, oldSourceMap);
            else
                env.removeAttribute(this.sourceMapName);
        }
    }

    protected Map resolveParams(Parameters par, String src)
        throws ProcessingException, IOException {
        HashMap map = new HashMap();

        // resolve and map params
        Iterator names = par.getParameterNames();
        while (names.hasNext()) {
            String name = (String)names.next();
            String value = par.getParameter(name, null);
            if (this.sources.contains(name))
                value = resolveAndMapSourceURI(name, value);
            map.put(name, value);
        }

        // resolve and map src
        if (src != null)
            map.put("src", resolveAndMapSourceURI("src", src));
        
        return map;
    }

    protected String resolveAndMapSourceURI(String name, String uri)
        throws ProcessingException, IOException {

        // Resolve the URI
        Source src = null;
        try {
            src = this.resolver.resolveURI(uri);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of " + uri, se);
        }

        // Save the source
        this.sourceMap.put(name, src);

        // Create a new URI that refers to the source in the context
        String mappedURI;
        if (src instanceof XMLizable)
            mappedURI = "xmodule:environment-attribute:" + this.sourceMapName + "#" + name;
        else
            mappedURI = "module:environment-attribute:" + this.sourceMapName + "#" + name;
        
        return mappedURI;
    }
}
