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
import java.net.URI;
import java.net.URISyntaxException;
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
    implements Block, Configurable, Contextualizable, Disposable, Initializable, Serviceable { 

    public static String ROLE = BlockManager.class.getName();

    private ServiceManager parentServiceManager;
    private ServiceManager serviceManager;
    private SourceResolver sourceResolver;
    private Context context;
    private Processor processor;
    private BlocksManager blocksManager;
    private EnvironmentHelper environmentHelper;

    private Configuration config;
    private BlockContext blockContext;

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
        this.blockContext = new BlockContext();
        LifecycleHelper.setupComponent(this.blockContext,
                                       this.getLogger(),
                                       this.context,
                                       this.parentServiceManager,
                                       this.config);    

        getLogger().debug("Initializing new Block Manager: " + this.blockContext.getId());

        ComponentContext newContext = new ComponentContext(context);
        // A block is supposed to be an isolated unit so it should not have
        // any direct access to the global root context
        newContext.put(ContextHelper.CONTEXT_ROOT_URL, new URL(this.blockContext.getContextURL()));
        newContext.makeReadOnly();
        this.context = newContext;
	

        // Create an own service manager
        this.serviceManager = new CocoonServiceManager(this.parentServiceManager);

        // Hack to put a sitemap configuration for the main sitemap of
        // the block into the service manager
        getLogger().debug("Block Manager: create sitemap " + this.blockContext.getSitemapPath());
        DefaultConfiguration sitemapConf =
            new DefaultConfiguration("sitemap", "BlockManager sitemap: " + this.blockContext.getId() +
                                     " for " + this.blockContext.getSitemapPath());
        sitemapConf.setAttribute("file", this.blockContext.getSitemapPath());
        sitemapConf.setAttribute("check-reload", "yes");
        // The source resolver must be defined in this service
        // manager, otherwise the root path will be the one from the
        // parent manager
        DefaultConfiguration resolverConf =
            new DefaultConfiguration("source-resolver", "BlockManager source resolver: " + this.blockContext.getId());
        DefaultConfiguration conf =
            new DefaultConfiguration("components", "BlockManager components: " + this.blockContext.getId());
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
        Source sitemapSrc = this.sourceResolver.resolveURI(this.blockContext.getSitemapPath());
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

    // Block methods

    // The blocks manager should not be available within a block so I
    // didn't want to make it part of the parent manager. But this is
    // a little bit clumsy. Question is what components, if any, the
    // blocks should have in common.
    public void setBlocksManager(BlocksManager blocksManager) {
        this.blocksManager = blocksManager;
        this.blockContext.setBlocksManager(blocksManager);
    }

    /**
     * Get the mount path of the block
     */
    public String getMountPath() {
        return this.blockContext.getMountPath();
    }

    /**
     * Get a block property
     */
    public String getProperty(String name) {
        return this.blockContext.getProperty(name);
    }

    // TODO: We should have a reflection friendly Map getProperties() also

    /**
     * Takes the scheme specific part of a block URI (the scheme is
     * the responsibilty of the BlockSource) and resolve it with
     * respect to the blocks mount point.
     */
    public URI absolutizeURI(URI uriToResolve, URI base) throws URISyntaxException {
        URI uri = resolveURI(uriToResolve, base);
        String blockName = uri.getScheme();
        Block block = null;
        if (blockName == null)
            // this block
            block = this;
        else
            // another block
            block = this.blockContext.getBlock(blockName);
        if (block == null)
            throw new URISyntaxException(uriToResolve.toString(), "Unknown block name");

        String mountPath = block.getMountPath();
        if (mountPath == null)
            throw new URISyntaxException(uri.toString(), "No mount point for this URI");
        if (mountPath.endsWith("/"))
            mountPath = mountPath.substring(0, mountPath.length() - 1);
        String absoluteURI = mountPath + uri.getSchemeSpecificPart();
        getLogger().debug("Resolving " + uri.toString() + " to " + absoluteURI);
        return new URI(absoluteURI);
    }

    /**
     * Parses and resolves the scheme specific part of a block URI
     * with respect to the base URI of the current sitemap. The scheme
     * specific part of the block URI has the form
     * <code>foo:/bar</code> when refering to another block, in this
     * case only an absolute path is allowed. For reference to the own
     * block, both absolute <code>/bar</code> and relative
     * <code>./foo</code> paths are allowed.
     */
    public URI resolveURI(URI uri, URI base) throws URISyntaxException {
        getLogger().debug("BlockManager: resolving " + uri.toString() + " with scheme " +
                          uri.getScheme() + " and ssp " + uri.getSchemeSpecificPart());
        if (uri.getPath() != null && uri.getPath().length() >= 2 &&
            uri.getPath().startsWith("./")) {
            // self reference relative to the current sitemap, e.g. ./foo
            if (uri.isAbsolute())
                throw new URISyntaxException(uri.toString(), "When the protocol refers to other blocks the path must be absolute");
            URI resolvedURI = base.resolve(uri);
            getLogger().debug("BlockManager: resolving " + uri.toString() +
                              " to " + resolvedURI.toString() + " with base URI " + base.toString());
            uri = resolvedURI;
        }
        return uri;
    }

    // The Processor methods

    public boolean process(Environment environment) throws Exception {
        String blockName = (String)environment.getAttribute(Block.NAME);

        if (blockName != null) {
            // Request to other block.
            if (BlockManager.SUPER.equals(blockName)) {
                // Explicit call to super block
                // The block name should not be used in the recieving block.
                environment.removeAttribute(Block.NAME);
                return this.process(Block.SUPER, environment, true);
            } else {
                // Call to named block
                Block block = this.blockContext.getBlock(blockName);
                if (block != null) {
                    // The block name should not be used in the recieving block.
                    environment.removeAttribute(Block.NAME);
                    return this.process(blockName, environment);
                } else {
                    // If there is a super block, the connection might
                    // be defined there instead.
                    return this.process(Block.SUPER, environment, true);
                }
            }
        } else {
            // Request to the own block
            boolean result = this.processor.process(environment);

            return result;

            // Pipelines seem to throw an exception instead of
            // returning false when the pattern is not found. For the
            // moment an explicit call of the super block is called in
            // the end of the sitemap. It might be better to be
            // explicit about it anyway.

//             if (result) {
//                 return true;
//             } else if (this.superId != null) {
//                 // Wasn't defined in the current block try super block
//                 return this.process(this.superId, environment, true);
//             } else {
//                 return false;
//             }
        }
    }

    private boolean process(String blockName, Environment environment) throws Exception {
        return this.process(blockName, environment, false);
    }

    private boolean process(String blockName, Environment environment, boolean superCall)
        throws Exception {
        Block block = this.blockContext.getBlock(blockName);
        if (block == null) {
            return false;
        } else if (superCall) {
            getLogger().debug("Enter processing in super block ");
            try {
                // A super block should be called in the context of
                // the called block to get polymorphic calls resolved
                // in the right way. Therefore no new current block is
                // set.
                return block.process(environment);
            } finally {
                getLogger().debug("Leaving processing in super block ");
            }
        } else {
            getLogger().debug("Enter processing in block " + blockName);
            try {
                // It is important to set the current block each time
                // a new block is entered, this is used for the block
                // protocol
                EnvironmentHelper.enterProcessor(block, null, environment);
                return block.process(environment);
            } finally {
                EnvironmentHelper.leaveProcessor();
                getLogger().debug("Leaving processing in block " + blockName);
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
