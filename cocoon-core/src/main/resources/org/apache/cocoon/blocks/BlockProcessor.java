/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.blocks;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.container.CocoonServiceManager;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * @version $Id$
 */
public class BlockProcessor
    extends AbstractLogEnabled
    implements Processor, Configurable, Contextualizable, Disposable, Initializable, Serviceable { 

    private Context context;
    private ServiceManager parentServiceManager;
    private ServiceManager serviceManager;
    private Configuration config;
    private SourceResolver sourceResolver;
    private Processor processor;
    private EnvironmentHelper environmentHelper;

    /** Processor attributes */
    protected Map processorAttributes = new HashMap();

    // Life cycle

    public void service(ServiceManager manager) throws ServiceException {
        this.parentServiceManager = manager;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void configure(Configuration config)
        throws ConfigurationException {
        this.config = config;
    }

    public void initialize() throws Exception {
        // Create an own service manager
        this.serviceManager = new CocoonServiceManager(this.parentServiceManager);

        String sitemapPath = this.config.getAttribute("src");

        // Hack to put a sitemap configuration for the main sitemap of
        // the block into the service manager
        getLogger().debug("BlockProcessor: create sitemap " + sitemapPath);
        DefaultConfiguration sitemapConf =
            new DefaultConfiguration("sitemap", "BlockProcessor sitemap: " + " for " + sitemapPath);
        sitemapConf.setAttribute("file", sitemapPath);
        sitemapConf.setAttribute("check-reload", "yes");
        // The source resolver must be defined in this service
        // manager, otherwise the root path will be the one from the
        // parent manager
        DefaultConfiguration resolverConf =
            new DefaultConfiguration("source-resolver", "BlockProcessor source resolver");
        DefaultConfiguration conf =
            new DefaultConfiguration("components", "BlockProcessor components");
        conf.addChild(sitemapConf);
        conf.addChild(resolverConf);

        LifecycleHelper.setupComponent(this.serviceManager,
                                       this.getLogger(),
                                       this.context,
                                       null,
                                       conf);

        this.sourceResolver = (SourceResolver)this.serviceManager.lookup(SourceResolver.ROLE);
        final Processor processor = EnvironmentHelper.getCurrentProcessor();
        if (processor != null) {
            getLogger().debug("processor context" + processor.getContext());
        }
        Source sitemapSrc = this.sourceResolver.resolveURI(sitemapPath);
        getLogger().debug("Sitemap Source " + sitemapSrc.getURI());
        this.sourceResolver.release(sitemapSrc);

        // Get the Processor and keep it
        this.processor = (Processor)this.serviceManager.lookup(Processor.ROLE);

        this.environmentHelper =
            new EnvironmentHelper((URL)this.context.get(ContextHelper.CONTEXT_ROOT_URL));
        LifecycleHelper.setupComponent(this.environmentHelper,
                                       this.getLogger(),
                                       null,
                                       this.serviceManager,
                                       null);
    }

    public void dispose() {
        if (this.serviceManager != null) {
            this.serviceManager.release(this.sourceResolver);
            this.sourceResolver = null;
            LifecycleHelper.dispose(this.serviceManager);
            this.serviceManager = null;
        }
        if (this.environmentHelper != null) {
            LifecycleHelper.dispose(this.environmentHelper);
            this.environmentHelper = null;
        }
        this.parentServiceManager = null;
    }

    // The Processor methods

    public boolean process(Environment environment) throws Exception {
        return this.processor.process(environment);
    }


    // FIXME: Not consistently supported for blocks yet. Most of the
    // code just use process.
    public InternalPipelineDescription buildPipeline(Environment environment)
        throws Exception {
        return this.processor.buildPipeline(environment);
    }

    public Configuration[] getComponentConfigurations() {
        return null;
    }

    // A block is supposed to be an isolated unit so it should not have
    // any direct access to the global root sitemap
    public Processor getRootProcessor() {
        return this;
    }
    
    public org.apache.cocoon.environment.SourceResolver getSourceResolver() {
        return this.environmentHelper;
    }
    
    public String getContext() {
        return this.environmentHelper.getContext();
    }

    /**
     * @see org.apache.cocoon.Processor#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.processorAttributes.get(name);
    }

    /**
     * @see org.apache.cocoon.Processor#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String name) {
        return this.processorAttributes.remove(name);
    }

    /**
     * @see org.apache.cocoon.Processor#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.processorAttributes.put(name, value);
    }
}
