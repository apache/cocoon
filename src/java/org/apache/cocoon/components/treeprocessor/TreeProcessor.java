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
package org.apache.cocoon.components.treeprocessor;

import java.util.Map;

import org.apache.avalon.excalibur.component.RoleManageable;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.container.ComponentManagerWrapper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * Interpreted tree-traversal implementation of a pipeline assembly language.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: TreeProcessor.java,v 1.38 2004/06/11 20:03:35 vgritsenko Exp $
 */

public class TreeProcessor
    extends AbstractLogEnabled
    implements ThreadSafe,
               Processor,
               Composable,
               Configurable,
               RoleManageable,
               Contextualizable,
               Disposable,
               Initializable {

    private static final String XCONF_URL =
        "resource://org/apache/cocoon/components/treeprocessor/sitemap-language.xml";

    /** The parent TreeProcessor, if any */
    protected TreeProcessor parent;

    /** The context */
    protected Context context;

    /** The component manager */
    protected ComponentManager manager;

    /** The role manager */
    protected RoleManager roleManager;

    /** Sitemap TreeBuilder */
    protected TreeBuilder treeBuilder;

    /** Last modification time */
    protected long lastModified = 0;

    /** The source of the tree definition */
    protected DelayedRefreshSourceWrapper source;

    /** Delay for <code>sourceLastModified</code>. */
    protected long lastModifiedDelay;

    /** The current language configuration */
    protected Configuration currentLanguage;

    /** The file to process */
    protected String fileName;

    /** Check for reload? */
    protected boolean checkReload;

    /** The source resolver */
    protected SourceResolver resolver;

    /** The environment helper */
    private EnvironmentHelper environmentHelper;

    /** The actual processor (package-private as needs to be accessed by ConcreteTreeProcessor) */
    ConcreteTreeProcessor concreteProcessor;

    /** The tree builder configuration */
    private Configuration treeBuilderConfiguration;

    /**
     * Create a TreeProcessor.
     */
    public TreeProcessor() {
        this.checkReload = true;
        this.lastModifiedDelay = 1000;
    }

    /**
     * Create a child processor for a given language
     */
    protected TreeProcessor(TreeProcessor parent,
                            DelayedRefreshSourceWrapper sitemapSource,
                            boolean checkReload,
                            String prefix)
    throws Exception {
        this.parent = parent;

        // Copy all that can be copied from the parent
        this.enableLogging(parent.getLogger());
        this.context = parent.context;
        this.roleManager = parent.roleManager;
        this.source = sitemapSource;
        this.treeBuilderConfiguration = parent.treeBuilderConfiguration;
        this.checkReload = checkReload;
        this.lastModifiedDelay = parent.lastModifiedDelay;

        // We have our own CM
        this.manager = parent.concreteProcessor.sitemapComponentManager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
        this.environmentHelper = new EnvironmentHelper(parent.environmentHelper);
        // Setup environment helper
        ContainerUtil.enableLogging(this.environmentHelper, this.getLogger());
        ContainerUtil.service(this.environmentHelper, new ComponentManagerWrapper(this.manager));
        this.environmentHelper.changeContext(sitemapSource, prefix);
        this.createTreeBuilder();
    }

    /**
     * Create a new child of this processor (used for mounting submaps).
     *
     * @return a new child processor.
     */
    public TreeProcessor createChildProcessor(String src,
                                              boolean checkReload,
                                              String  prefix)
    throws Exception {
        DelayedRefreshSourceWrapper delayedSource = new DelayedRefreshSourceWrapper(
            this.resolver.resolveURI(src), this.lastModifiedDelay);
        return new TreeProcessor(this, delayedSource, checkReload, prefix);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.component.RoleManageable#setRoleManager(org.apache.avalon.excalibur.component.RoleManager)
     */
    public void setRoleManager(RoleManager rm) {
        this.roleManager = rm;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        // setup the environment helper
        if (this.environmentHelper == null ) {
            this.environmentHelper = new EnvironmentHelper(
                (String) this.context.get(ContextHelper.CONTEXT_ROOT_URL));
        }
        ContainerUtil.enableLogging(this.environmentHelper,getLogger());
        ContainerUtil.service(this.environmentHelper, new ComponentManagerWrapper(manager));
    }

    /**
     * Configure the tree processor:
     * &lt;processor file="{Location of the sitemap}"
     *               check-reload="{true|false}"
     *               config="{Location of sitemap tree processor config}&gt;
     *   &lt;reload delay="10"/&gt;
     * &lt;/processor&gt;
     *
     * Only the file attribute is required; everything else is optional.
     *
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config)
    throws ConfigurationException {
        this.fileName = config.getAttribute("file", null);
        this.checkReload = config.getAttributeAsBoolean("check-reload", true);

        // Obtain the configuration file, or use the XCONF_URL if none
        // is defined
        String xconfURL = config.getAttribute("config", XCONF_URL);

        // Reload check delay. Default is 1 second.
        this.lastModifiedDelay = config.getChild("reload").getAttributeAsLong("delay", 1000L);

        // Read the builtin languages definition file
        try {
            Source source = this.resolver.resolveURI( xconfURL );
            try {
                SAXConfigurationHandler handler = new SAXConfigurationHandler();
                SourceUtil.toSAX( new ComponentManagerWrapper(this.manager), source, null, handler);
                this.treeBuilderConfiguration = handler.getConfiguration();
            } finally {
                this.resolver.release( source );
            }
        } catch(Exception e) {
            String msg = "Error while reading " + xconfURL + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        }

        this.createTreeBuilder();
    }

    /**
     * Create a new tree builder for this sitemap
     */
    protected void createTreeBuilder()
    throws ConfigurationException {
        // Create a builder for the sitemap language
        try {
            this.treeBuilder = (TreeBuilder)Thread.currentThread()
                    .getContextClassLoader()
                    .loadClass("org.apache.cocoon.components.treeprocessor.sitemap.SitemapLanguage").newInstance();

            LifecycleHelper.setupComponent(this.treeBuilder,
                                           getLogger(),
                                           this.context,
                                           this.manager,
                                           this.roleManager,
                                           this.treeBuilderConfiguration);
        } catch(ConfigurationException ce) {
            throw ce;
        } catch(Exception e) {
            throw new ConfigurationException("Could not setup sitemap builder.", e);
        }
    }

    /**
     * Process the given <code>Environment</code> producing the output.
     * @return If the processing is successfull <code>true</code> is returned.
     *         If not match is found in the sitemap <code>false</code>
     *         is returned.
     * @throws org.apache.cocoon.ResourceNotFoundException If a sitemap component tries
     *                                   to access a resource which can not
     *                                   be found, e.g. the generator
     *         ConnectionResetException  If the connection was reset
     */
    public boolean process(Environment environment) throws Exception {

        setupConcreteProcessor(environment);

        return this.concreteProcessor.process(environment);
    }


    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public InternalPipelineDescription buildPipeline(Environment environment)
    throws Exception {

        setupConcreteProcessor(environment);

        return this.concreteProcessor.buildPipeline(environment);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getRootProcessor()
     */
    public Processor getRootProcessor() {
        TreeProcessor result = this;
        while(result.parent != null) {
            result = result.parent;
        }

        return result;
    }

    /**
     * Set the sitemap component configurations
     */
    public void setComponentConfigurations(Configuration componentConfigurations) {
        this.concreteProcessor.setComponentConfigurations(componentConfigurations);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Map getComponentConfigurations() {
        return this.concreteProcessor.getComponentConfigurations();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getContext()
     */
    public String getContext() {
        return this.environmentHelper.getContext();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getEnvironmentHelper()
     */
    public org.apache.cocoon.environment.SourceResolver getSourceResolver() {
        return this.environmentHelper;
    }

    /**
     * The current environment helper used by the MountNode
     * @return EnvironmentHelper
     */
    public EnvironmentHelper getEnvironmentHelper() {
        return this.environmentHelper;
    }

    private void setupConcreteProcessor(Environment env) throws Exception {
        // first, check for sitemap changes
        if (this.concreteProcessor == null ||
            (this.checkReload && this.source.getLastModified() != this.lastModified)) {
            buildConcreteProcessor(env);
        }
    }

    private synchronized void buildConcreteProcessor(Environment env) throws Exception {

        // Now that we entered the synchronized area, recheck what's already
        // been checked in process().
        if (this.concreteProcessor != null && source.getLastModified() == this.lastModified) {
            // Nothing changed
            return;
        }

        long startTime = System.currentTimeMillis();

        // Get a builder
        ConcreteTreeProcessor newProcessor = new ConcreteTreeProcessor(this);
        long newLastModified;
        this.setupLogger(newProcessor);

        // We have to do a call to enterProcessor() here as during building
        // of the tree, components (e.g. actions) are already instantiated
        // (ThreadSafe ones mostly).
        // If these components try to access the current processor or the
        // current service manager they must get this one - which is currently
        // in the process of initialization.
        EnvironmentHelper.enterProcessor(this, new ComponentManagerWrapper(this.manager), env);
        try {
            if (this.treeBuilder instanceof Recyclable) {
                ((Recyclable)this.treeBuilder).recycle();
            }
            if (this.treeBuilder instanceof Recomposable) {
                ((Recomposable)this.treeBuilder).recompose(this.manager);
            }
            this.treeBuilder.setProcessor(newProcessor);
            if (this.fileName == null) {
                this.fileName = "sitemap.xmap";
            }

            if (this.source == null) {
                this.source = new DelayedRefreshSourceWrapper(this.resolver.resolveURI(this.fileName),
                                                              lastModifiedDelay);
            }

            newLastModified = this.source.getLastModified();

            ProcessingNode root = this.treeBuilder.build(this.source);

            newProcessor.setProcessorData(this.treeBuilder.getSitemapComponentManager(),
                                          root,
                                          this.treeBuilder.getDisposableNodes());
        } finally {
            EnvironmentHelper.leaveProcessor();
        }

        if (getLogger().isDebugEnabled()) {
            double time = (this.lastModified - startTime) / 1000.0;
            getLogger().debug("TreeProcessor built in " + time + " secs from " + source.getURI());
        }

        // Switch to the new processor (ensure it's never temporarily null)
        ConcreteTreeProcessor oldProcessor = this.concreteProcessor;

        this.concreteProcessor = newProcessor;
        this.lastModified = newLastModified;

        // Dispose the old processor, if any
        if (oldProcessor != null) {
            oldProcessor.markForDisposal();
        }
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        // Dispose the concrete processor. No need to check for existing requests, as there
        // are none when a TreeProcessor is disposed.
        ContainerUtil.dispose(this.concreteProcessor);
        this.concreteProcessor = null;

        ContainerUtil.dispose(this.treeBuilder);
        this.treeBuilder = null;

        if (this.manager != null) {
            if (this.source != null) {
                this.resolver.release(this.source.getSource());
                this.source = null;
            }
            this.manager.release(this.resolver);
            this.resolver = null;
            this.manager = null;
        }
    }
}
