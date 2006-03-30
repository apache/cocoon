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

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.SitemapSourceInfo;
import org.apache.cocoon.components.pipeline.VirtualProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.sitemap.VPCNode;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.xml.AbstractXMLPipe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceException;
import org.apache.excalibur.xml.sax.XMLizable;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * TODO List:
 * <ul>
 * <li>Refactor ProcessingPipelines implementations
 * <li>Implement caching
 * </ul>
 */
public abstract class AbstractVirtualSitemapComponent extends AbstractXMLPipe
    implements SitemapModelComponent, Serviceable, Contextualizable, Configurable {

    private ProcessingNode node;
    private String sourceMapName;
    private Map sourceMap = new HashMap();
    private Set sources;
    private VirtualProcessingPipeline pipeline;
    // An environment containing a map with the souces from the calling environment
    private EnvironmentWrapper mappedSourceEnvironment;
    // An environment with the URI and URI prefix of the sitemap where the VPC is defined
    private EnvironmentWrapper vpcEnvironment;

    protected Context context;
    protected SourceResolver resolver;
    protected ServiceManager manager;

    private class MyInvokeContext extends InvokeContext {
        public MyInvokeContext(Logger logger) throws Exception {
            super(true);
            super.processingPipeline = new VirtualProcessingPipeline();
            ((VirtualProcessingPipeline)super.processingPipeline).enableLogging(logger);
        }
    }

    abstract protected String getTypeName();

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
     * Release all resources.
     */
    public void recycle() {
        // FIXME
        // These are allready disposed, why?
        //Iterator sources = this.sourceMap.values().iterator();
        //while (sources.hasNext()) {
        //    Source source = (Source)sources.next();
        //    this.resolver.release(source);
        //}
        this.sourceMap.clear();
        super.recycle();
    }

    /**
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration configuration) throws ConfigurationException {
        String name = configuration.getAttribute("name");
        this.sourceMapName =
            Constants.CONTEXT_ENV_PREFIX + "-" + getTypeName() + "-source-map-" + name;
        try {
            this.node = (ProcessingNode)this.context.get(Constants.CONTEXT_VPC_PREFIX +
                                                         getTypeName() + "-" + name);
            this.sources = ((VPCNode)node).getSources();
        } catch (Exception e) {
            throw new ConfigurationException("Can not find VirtualPipelineComponent '" +
                                             name + "' configuration");
        }
    }

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver, Map objectModel, String src, Parameters par)
    throws ProcessingException, SAXException, IOException {
        this.resolver = resolver;

        Environment env = EnvironmentHelper.getCurrentEnvironment();

        // Hack to get an info object with the right uri for the
        // current sitemap, there is no vpc protocol.
        SitemapSourceInfo mappedSourceEnvironmentInfo =
            SitemapSourceInfo.parseURI(env, "vpc:/");

        this.mappedSourceEnvironment =
            new EnvironmentWrapper(env, mappedSourceEnvironmentInfo, getLogger());
        // place for resolved sources
        this.mappedSourceEnvironment.setAttribute(this.sourceMapName, this.sourceMap);

        MyInvokeContext invoker = null;

        try {
            // resolve the sources in the parameter map before switching context
            Map resolvedParams = resolveParams(par, src);

            // set up info object for VPC environment wrapper, would
            // better be done in a constructor for the info.
            SitemapSourceInfo vpcEnvironmentInfo = new SitemapSourceInfo();
            vpcEnvironmentInfo.prefix = (String) this.context.get(Constants.CONTEXT_ENV_PREFIX);
            // FIXME - What should the value of uri be?
            vpcEnvironmentInfo.uri = "fixme";
            vpcEnvironmentInfo.requestURI = vpcEnvironmentInfo.prefix + vpcEnvironmentInfo.uri;
            vpcEnvironmentInfo.rawMode = false;

            // set up the vpc environment
            this.vpcEnvironment =
                new EnvironmentWrapper(this.mappedSourceEnvironment,
                                       vpcEnvironmentInfo, getLogger());

            EnvironmentHelper.enterEnvironment(this.vpcEnvironment);

            // set up invoker with sitemap params
            invoker = new MyInvokeContext(getLogger());
            invoker.enableLogging(getLogger());
            invoker.service(this.manager);
            invoker.pushMap(null, resolvedParams);

            this.node.invoke(this.vpcEnvironment, invoker);
            this.pipeline = (VirtualProcessingPipeline)invoker.getProcessingPipeline();
        } catch (Exception e) {
            throw new ProcessingException("Oops", e);
        } finally {
            if (invoker != null) {
                invoker.popMap();
                invoker.dispose();
            }
            // Restore context
            EnvironmentHelper.leaveEnvironment();
        }
    }

    protected VirtualProcessingPipeline getPipeline() {
        return this.pipeline;
    }

    // An environment containing a map with the souces from the calling environment
    protected EnvironmentWrapper getMappedSourceEnvironment() {
        return this.mappedSourceEnvironment;
    }

    // An environment with the URI and URI prefix of the sitemap where the VPC is defined
    protected EnvironmentWrapper getVPCEnvironment() {
        return this.vpcEnvironment;
    }

    private Map resolveParams(Parameters par, String src)
        throws ProcessingException, IOException {
        HashMap map = new HashMap();

        // resolve and map params
        String[] names = par.getNames();
        for(int i=0; i<names.length; i++) {
            String name = names[i];
            String value = par.getParameter(name, null);
            if (this.sources.contains(name)) {
                value = resolveAndMapSourceURI(name, value);
            }
            map.put(name, value);
        }

        // resolve and map src
        if (src != null) {
            map.put("src", resolveAndMapSourceURI("src", src));
        }

        return map;
    }

    private String resolveAndMapSourceURI(String name, String uri)
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
        if (src instanceof XMLizable) {
            mappedURI = "xmodule:environment-attr:" + this.sourceMapName + "#" + name;
        } else {
            mappedURI = "module:environment-attr:" + this.sourceMapName + "#" + name;
        }

        return mappedURI;
    }
}
