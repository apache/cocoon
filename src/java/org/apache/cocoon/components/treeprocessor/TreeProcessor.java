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

import java.net.URL;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.NamespacedSAXConfigurationHandler;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;

import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.SitemapExecutor;
import org.apache.cocoon.sitemap.impl.DefaultExecutor;

import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.regexp.RE;

/**
 * Interpreted tree-traversal implementation of a pipeline assembly language.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public class TreeProcessor extends AbstractLogEnabled
                           implements ThreadSafe, Processor, Serviceable,
                                      Configurable, Contextualizable,
                                      Disposable, Initializable {

    private static final String XCONF_URL =
        "resource://org/apache/cocoon/components/treeprocessor/sitemap-language.xml";

    /** The parent TreeProcessor, if any */
    protected TreeProcessor parent;

    /** The context */
    protected Context context;

    /**
     * The component manager given by the upper level
     * (root manager or parent concrete processor)
     */
    protected ServiceManager manager;

    /** Last modification time */
    protected long lastModified = 0;

    /** The source of the tree definition */
    protected DelayedRefreshSourceWrapper source;

    /** Delay for <code>sourceLastModified</code>. */
    protected long lastModifiedDelay;

    /** The file to process */
    protected String fileName;

    /** Check for reload? */
    protected boolean checkReload;

    /** The source resolver */
    protected SourceResolver resolver;

    /** The environment helper */
    private EnvironmentHelper environmentHelper;

    /** The actual sitemap executor */
    private SitemapExecutor sitemapExecutor;

    /** Indicates whether this is our component or not */
    private boolean releaseSitemapExecutor;

    /** The actual processor */
    protected ConcreteTreeProcessor concreteProcessor;

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
        enableLogging(parent.getLogger());

        // Copy all that can be copied from the parent
        this.context = parent.context;
        this.source = sitemapSource;
        this.treeBuilderConfiguration = parent.treeBuilderConfiguration;
        this.checkReload = checkReload;
        this.lastModifiedDelay = parent.lastModifiedDelay;

        this.manager = parent.concreteProcessor.getServiceManager();

        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
        this.environmentHelper = new EnvironmentHelper(parent.environmentHelper);
        // Setup environment helper
        ContainerUtil.enableLogging(this.environmentHelper, this.getLogger());
        ContainerUtil.service(this.environmentHelper, this.manager);
        this.environmentHelper.changeContext(sitemapSource, prefix);
        this.sitemapExecutor = parent.sitemapExecutor;
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
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
    }

    public void initialize() throws Exception {
        // setup the environment helper
        if (this.environmentHelper == null ) {
            this.environmentHelper = new EnvironmentHelper(
                    (URL) this.context.get(ContextHelper.CONTEXT_ROOT_URL));
        }
        ContainerUtil.enableLogging(this.environmentHelper, getLogger());
        ContainerUtil.service(this.environmentHelper, this.manager);

        // Create sitemap executor
        if (this.parent == null) {
            try {
                this.sitemapExecutor = (SitemapExecutor) this.manager.lookup(SitemapExecutor.ROLE);
                this.releaseSitemapExecutor = true;
            } catch (ServiceException e) {
                this.sitemapExecutor = new DefaultExecutor();
            }
        } else {
            this.sitemapExecutor = this.parent.sitemapExecutor;
        }
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
            Source source = this.resolver.resolveURI(xconfURL);
            try {
                SAXConfigurationHandler handler = new SAXConfigurationHandler();
                SourceUtil.toSAX(this.manager, source, null, handler);
                this.treeBuilderConfiguration = handler.getConfiguration();
            } finally {
                this.resolver.release(source);
            }
        } catch (Exception e) {
            String msg = "Error while reading " + xconfURL + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
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
        // Get the concrete processor and delegate it the job
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
        // Get the concrete processor and delegate it the job
        setupConcreteProcessor(environment);
        return this.concreteProcessor.buildPipeline(environment);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getRootProcessor()
     */
    public Processor getRootProcessor() {
        TreeProcessor result = this;
        while (result.parent != null) {
            result = result.parent;
        }

        return result;
    }

//    /**
//     * Set the sitemap component configurations
//     */
//    public void setComponentConfigurations(Configuration componentConfigurations) {
//        this.concreteProcessor.setComponentConfigurations(componentConfigurations);
//    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Configuration[] getComponentConfigurations() {
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

    /**
     * Get the tree builder role from the sitemap program (as a configuration object).
     * This method should report very any problem very clearly, as it is the entry point of any
     * Cocoon application.
     *
     * @param sitemapProgram the sitemap
     * @return the treebuilder role
     * @throws ConfigurationException if a suitable role could not be found
     */
    private TreeBuilder getTreeBuilder(Configuration sitemapProgram) throws ConfigurationException {
        String ns = sitemapProgram.getNamespace();

        RE re = new RE("http://apache.org/cocoon/sitemap/(\\d\\.\\d)");
        if (!re.match(ns)) {
            throw new ConfigurationException("Unknown sitemap namespace (" + ns + ") at " +
                    this.source.getURI());
        }

        String version = re.getParen(1);
        String result = TreeBuilder.ROLE + "/sitemap-" + version;

        try {
            return (TreeBuilder) this.manager.lookup(result);
        } catch (Exception e) {
            throw new ConfigurationException("This version of Cocoon does not handle sitemap version " +
                                             version + " at " + this.source.getURI(), e);
        }
    }

    /**
     * Sets up the concrete processor, building or rebuilding it if necessary.
     */
    private void setupConcreteProcessor(Environment env) throws Exception {
        // first, check for sitemap changes
        if (this.concreteProcessor == null ||
                (this.checkReload && this.source.getLastModified() != this.lastModified)) {
            buildConcreteProcessor(env);
        }
    }

    /**
     * Build the concrete processor (i.e. loads the sitemap). Should be called
     * only by setupProcessor();
     */
    private synchronized void buildConcreteProcessor(Environment env) throws Exception {

        // Now that we entered the synchronized area, recheck what's already
        // been checked in process().
        if (this.concreteProcessor != null && source.getLastModified() == this.lastModified) {
            // Nothing changed
            return;
        }

        long startTime = System.currentTimeMillis();
        long newLastModified;
        ConcreteTreeProcessor newProcessor;

        // We have to do a call to enterProcessor() here as during building
        // of the tree, components (e.g. actions) are already instantiated
        // (ThreadSafe ones mostly).
        // If these components try to access the current processor or the
        // current service manager they must get this one - which is currently
        // in the process of initialization.
        EnvironmentHelper.enterProcessor(this, this.manager, env);

        try {
            // Load the sitemap file
            if (this.fileName == null) {
                this.fileName = "sitemap.xmap";
            }
            if (this.source == null) {
                this.source = new DelayedRefreshSourceWrapper(this.resolver.resolveURI(this.fileName),
                                                              lastModifiedDelay);
            }

            // Build a namespace-aware configuration object
            NamespacedSAXConfigurationHandler handler = new NamespacedSAXConfigurationHandler();
            SourceUtil.toSAX(this.source, handler);
            Configuration sitemapProgram = handler.getConfiguration();
            newLastModified = this.source.getLastModified();

            newProcessor = createConcreteTreeProcessor();

            // Get the treebuilder that can handle this version of the sitemap.
            TreeBuilder treeBuilder = getTreeBuilder(sitemapProgram);
            try {
                treeBuilder.setProcessor(newProcessor);
                treeBuilder.setParentProcessorManager(this.manager);

                ProcessingNode root = treeBuilder.build(sitemapProgram);
                newProcessor.setProcessorData(
                        treeBuilder.getBuiltProcessorManager(),
                        treeBuilder.getBuiltProcessorClassLoader(),
                        root, treeBuilder.getDisposableNodes(),
                        treeBuilder.getComponentLocator(),
                        treeBuilder.getEnterSitemapEventListeners(),
                        treeBuilder.getLeaveSitemapEventListeners());
            } finally {
                this.manager.release(treeBuilder);
            }
        } finally {
            EnvironmentHelper.leaveProcessor();
        }

        if (getLogger().isDebugEnabled()) {
            double time = (System.currentTimeMillis() - startTime) / 1000.0;
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

    private ConcreteTreeProcessor createConcreteTreeProcessor() {
        ConcreteTreeProcessor processor = new ConcreteTreeProcessor(this, this.sitemapExecutor);
        setupLogger(processor);
        return processor;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        // Dispose the concrete processor. No need to check for existing requests, as there
        // are none when a TreeProcessor is disposed.
        ContainerUtil.dispose(this.concreteProcessor);
        this.concreteProcessor = null;

        if (this.releaseSitemapExecutor) {
            this.manager.release(this.sitemapExecutor);
            this.sitemapExecutor = null;
        }

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
