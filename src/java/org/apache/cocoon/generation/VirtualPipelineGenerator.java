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
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.sitemap.VPCNode;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.xml.XMLConsumer;
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
 * <li>Implement parameters support
 * <li>Resolve src parameter and pass to the pipeline
 * <li>Refactor ProcessingPipelines implementations
 * <li>Implement caching
 * </ul>
 */
public class VirtualPipelineGenerator extends AbstractLogEnabled
    implements Generator, Serviceable, Disposable, Contextualizable, Configurable {

    private SourceResolver resolver;
    private DefaultContext context;
    private ServiceManager manager;
    private XMLConsumer consumer;
    private ProcessingNode node;
    private ProcessingPipeline pipeline;
    private String sourceMapName;
    private Map sourceMap = new HashMap();
    private Set sources;
    private String name;


    private class MyInvokeContext extends InvokeContext {
        public MyInvokeContext() throws Exception {
            super(true);
            super.processingPipeline = new VirtualProcessingPipeline(VirtualPipelineGenerator.this.context);
        }
    }

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
        this.sourceMapName = Constants.CONTEXT_ENV_PREFIX + "-source-map-" + this.name;
        try {
            this.node =
                (ProcessingNode)this.context.get(Constants.CONTEXT_VPC_PREFIX +
                                                 "generator-" + this.name);
            this.sources = ((VPCNode)node).getSources();
        } catch (Exception e) {
            throw new ConfigurationException("Can not find VirtualPipelineGenerator '" +
                                             this.name + "' configuration");
        }
    }

    public void setConsumer(XMLConsumer consumer) {
        this.consumer = consumer;
    }

    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {

        this.resolver = resolver;

        // save callers resolved sources if there are any
        Map oldSourceMap = null;
        try {
            oldSourceMap = (Map)this.context.get(this.sourceMapName);
        } catch (ContextException e) {
            // This VPC has not been used by the caller
        }
        // place for resolved sources
        this.context.put(this.sourceMapName, this.sourceMap);

        Environment env = EnvironmentHelper.getCurrentEnvironment();
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
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
            this.context.put(this.sourceMapName, oldSourceMap);
        }
    }

    private Map resolveParams(Parameters par, String src)
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

    private String resolveAndMapSourceURI(String name, String uri)
        throws ProcessingException, IOException {

        // Resolve the URI
        getLogger().debug("VPCGenerator: resolve " + name + " = " + uri);
        Source src = null;
        try {
            src = this.resolver.resolveURI(uri);
        } catch (SourceException se) {
            throw SourceUtil.handle("Error during resolving of " + uri, se);
        }

        getLogger().debug("VPCGenerator: URI " + name + " = " + src.getURI());

        // Save the source
        this.sourceMap.put(name, src);

        // Create a new URI that refers to the source in the context
        String mappedURI;
        if (src instanceof XMLizable)
            mappedURI = "xmodule:avalon-context:" + this.sourceMapName + "#" + name;
        else
            mappedURI = "module:avalon-context:" + this.sourceMapName + "#" + name;
        
        getLogger().debug("VPCGenerator: mapped URI " + name + " = " + mappedURI);

        return mappedURI;
    }

    public void generate()
    throws IOException, SAXException, ProcessingException {

        // save callers resolved sources if there are any
        Map oldSourceMap = null;
        try {
            oldSourceMap = (Map)this.context.get(this.sourceMapName);
        } catch (ContextException e) {
            // This VPC has not been used by the caller
        }
        // place for resolved sources
        this.context.put(this.sourceMapName, this.sourceMap);

        // Should use SourceResolver of the this components' sitemap, not caller sitemap
        // Have to switch to another environment...
        Environment env = EnvironmentHelper.getCurrentEnvironment();
        String oldPrefix = env.getURIPrefix();
        String oldURI    = env.getURI();
        try {
            String uri = (String) this.context.get(Constants.CONTEXT_ENV_URI);
            String prefix = (String) this.context.get(Constants.CONTEXT_ENV_PREFIX);
            env.setURI(prefix, uri);

            this.pipeline.prepareInternal(env);
        } catch (Exception e) {
            throw new ProcessingException("Oops", e);
        } finally {
            // Restore context
            env.setURI(oldPrefix, oldURI);
        }

        this.pipeline.process(env, this.consumer);

        // restore sourceMap
        this.context.put(this.sourceMapName, oldSourceMap);
    }
}
