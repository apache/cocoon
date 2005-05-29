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
package org.apache.cocoon.components.blocks;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Processor;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.container.CocoonServiceManager;
import org.apache.cocoon.components.container.ComponentContext;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.xml.sax.SAXException;

/**
 * @version SVN $Id$
 */
public class BlockManager
    extends AbstractLogEnabled
    implements Configurable, Contextualizable, Disposable, Initializable, Processor, Serviceable { 

    public static String ROLE = BlockManager.class.getName();
    public static String NAME = BlockManager.class.getName() + "-name";

    private ServiceManager parentServiceManager;
    private ServiceManager serviceManager;
    private SourceResolver sourceResolver;
    private DefaultContext context;
    private Processor processor;
    private BlocksManager blocksManager;
    private EnvironmentHelper environmentHelper;

    private String id;
    private String location;
    private String mountPath;
    private String sitemapPath;
    private Map connections = new HashMap();
    private Map properties = new HashMap();

    public void service(ServiceManager manager) throws ServiceException {
        this.parentServiceManager = manager;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = new ComponentContext(context);
    }

    public void configure(Configuration config)
        throws ConfigurationException {
        this.id = config.getAttribute("id");
        this.location = config.getAttribute("location");
        this.mountPath = config.getChild("mount").getAttribute("path", null);

        getLogger().debug("BlockManager configure: " +
                          " id=" + this.id +
                          " location=" + this.location +
                          " mountPath=" + this.mountPath);

        Configuration[] connections =
            config.getChild("connections").getChildren("connection");
        for (int i = 0; i < connections.length; i++) {
            Configuration connection = connections[i];
            this.connections.put(connection.getAttribute("name"),
                                 connection.getAttribute("block"));
            getLogger().debug("connection: " +
                              " name=" + connection.getAttribute("name") +
                              " block=" + connection.getAttribute("block"));
        }

        Configuration[] properties =
            config.getChild("properties").getChildren("property");
        for (int i = 0; i < properties.length; i++) {
            Configuration property = properties[i];
            this.properties.put(property.getAttribute("name"),
                                 property.getAttribute("value"));
            getLogger().debug("property: " +
                              " name=" + property.getAttribute("name") +
                              " value=" + property.getAttribute("value"));
        }

        // Read the block.xml file
        String blockPath = this.location + "COB-INF/block.xml";
        SourceResolver resolver = null;
        Source source = null;
        Configuration block = null;

        try {
            resolver = 
                (SourceResolver) this.parentServiceManager.lookup(SourceResolver.ROLE);
            source = resolver.resolveURI(blockPath);
            DefaultConfigurationBuilder builder = new DefaultConfigurationBuilder();
            block = builder.build( source.getInputStream() );
        } catch (ServiceException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } catch (IOException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } catch (SAXException e) {
            String msg = "Exception while reading " + blockPath + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } finally {
            if (resolver != null) {
                resolver.release(source);
                this.parentServiceManager.release(resolver);
            }
        }
        this.sitemapPath = block.getChild("sitemap").getAttribute("src");
        getLogger().debug("sitemapPath=" + this.sitemapPath);
    }

    public void initialize() throws Exception {
        getLogger().debug("Initializing new Block Manager: " + this.id);

        // A block is supposed to be an isolated unit so it should not have
        // any direct access to the global root context
        getLogger().debug("Root URL " +
                          ((URL) this.context.get(ContextHelper.CONTEXT_ROOT_URL)).toExternalForm());
        String blockRoot =
            ((URL) this.context.get(ContextHelper.CONTEXT_ROOT_URL)).toExternalForm() +
            this.location;
        this.context.put(ContextHelper.CONTEXT_ROOT_URL, new URL(blockRoot));
        getLogger().debug("Block Root URL " +
                          ((URL) this.context.get(ContextHelper.CONTEXT_ROOT_URL)).toExternalForm());
        this.context.makeReadOnly();

        // Create an own service manager
        this.serviceManager = new CocoonServiceManager(this.parentServiceManager);

        // Hack to put a sitemap configuration for the main sitemap of
        // the block into the service manager
        getLogger().debug("Block Manager: create sitemap " + this.sitemapPath);
        DefaultConfiguration sitemapConf =
            new DefaultConfiguration("sitemap", "BlockManager sitemap: " + this.id +
                                     " for " + this.sitemapPath);
        sitemapConf.setAttribute("file", this.sitemapPath);
        sitemapConf.setAttribute("check-reload", "yes");
        // The source resolver must be defined in this service
        // manager, otherwise the root path will be the one from the
        // parent manager
        DefaultConfiguration resolverConf =
            new DefaultConfiguration("source-resolver", "BlockManager source resolver: " + this.id);
        DefaultConfiguration conf =
            new DefaultConfiguration("components", "BlockManager components: " + this.id);
        conf.addChild(sitemapConf);
        conf.addChild(resolverConf);

        LifecycleHelper.setupComponent(this.serviceManager,
                                       this.getLogger(),
                                       this.context,
                                       null,
                                       conf);

        this.sourceResolver = (SourceResolver)this.serviceManager.lookup(SourceResolver.ROLE);
        final Processor processor = EnvironmentHelper.getCurrentProcessor();
        if ( processor != null ) {
            getLogger().debug("processor context" + processor.getContext());
        }
        Source sitemapSrc = this.sourceResolver.resolveURI(this.sitemapPath);
        getLogger().debug("Sitemap Source " + sitemapSrc.getURI());
        this.sourceResolver.release(sitemapSrc);

        // Get the Processor and keep it
        this.processor = (Processor)this.serviceManager.lookup(Processor.ROLE);

        this.environmentHelper = new EnvironmentHelper(
                (URL)this.context.get(ContextHelper.CONTEXT_ROOT_URL));
        LifecycleHelper.setupComponent(this.environmentHelper,
                                       this.getLogger(),
                                       null,
                                       this.serviceManager,
                                       null);
    }

    public void dispose() {
        if (this.environmentHelper != null) {
            LifecycleHelper.dispose(this.environmentHelper);
            this.environmentHelper = null;
        }
        if (this.serviceManager != null) {
            this.serviceManager.release(this.sourceResolver);
            this.sourceResolver = null;
            LifecycleHelper.dispose(this.serviceManager);
            this.serviceManager = null;
        }
        this.parentServiceManager = null;
    }

    public void setBlocksManager(BlocksManager blocksManager) {
        this.blocksManager = blocksManager;
    }

    // The Processor methods

    public boolean process(Environment environment) throws Exception {
        String blockName = (String)environment.getAttribute(BlockManager.NAME);

        if (blockName != null) {
            // Request to other block.
            // The block name should not be used in the recieving block.
            environment.removeAttribute(BlockManager.NAME);
            String blockId = (String)this.connections.get(blockName);
            if (blockId == null) {
                throw new ProcessingException("Unknown block name " + blockName);
            }
            getLogger().debug("Resolving block: " + blockName + " to " + blockId);
            return this.blocksManager.process(blockId, environment);
        } else {
            getLogger().debug("Enter processing in block " + this.id);
            // Request to the own block
            EnvironmentHelper.enterProcessor(this, this.serviceManager, environment);
            try {
                return this.processor.process(environment);
            } finally {
                EnvironmentHelper.leaveProcessor();
                getLogger().debug("Leaving processing in block " + this.id);
            }
        }
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
}
