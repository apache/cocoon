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
package org.apache.cocoon.components.treeprocessor;

import java.io.IOException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.NamespacedSAXConfigurationHandler;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.regexp.RE;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.components.source.util.SourceUtil;
import org.apache.cocoon.components.treeprocessor.sitemap.FlowNode;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.sitemap.SitemapExecutor;
import org.apache.cocoon.sitemap.impl.DefaultExecutor;
import org.apache.cocoon.util.AbstractLogEnabled;

import org.xml.sax.SAXException;

/**
 * Interpreted tree-traversal implementation of a pipeline assembly language.
 *
 * @version $Id$
 */
public class TreeProcessor extends AbstractLogEnabled
                           implements ThreadSafe, Processor, Serviceable,
                                      Configurable, Disposable, Initializable {
    // TODO: RCL: implement NotificationListener

    /** The parent TreeProcessor, if any */
    protected TreeProcessor parent;

    /**
     * The component manager given by the upper level
     * (root manager or parent concrete processor)
     */
    protected ServiceManager manager;

    /** The settings. */
    protected Settings settings;

    /** Last modification time */
    protected long lastModified = 0;

    /** The source of the tree definition */
    protected DelayedRefreshSourceWrapper source;

    /** Delay for <code>sourceLastModified</code>. */
    protected long lastModifiedDelay;

    /** Check for reload? */
    protected boolean checkReload;
    
    /** Need component classes to be reloaded, due to jci notification? */
    protected boolean classReload;
    
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

    /** The sitemap schema used for validation. */
    protected Schema sitemapSchema;

    /**
     * Create a TreeProcessor.
     */
    public TreeProcessor() {
        this.checkReload = true;
        this.lastModifiedDelay = 1000;
        // create sitemap schema
        final SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
        final StreamSource ss = new StreamSource(this.getClass().getResourceAsStream("/org/apache/cocoon/sitemap/schema/cocoon-sitemap-1.0.xsd"));
        try {
            this.sitemapSchema = factory.newSchema(ss);
        } catch (SAXException se) {
            throw new RuntimeException("Unable to parse sitemap schema.", se);
        }
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
        this.source = sitemapSource;
        this.checkReload = checkReload;
        this.lastModifiedDelay = parent.lastModifiedDelay;

        this.manager = parent.concreteProcessor.getServiceManager();

        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
        this.settings = (Settings) this.manager.lookup(Settings.ROLE);
        this.environmentHelper = new EnvironmentHelper(parent.environmentHelper);
        // Setup environment helper
        ContainerUtil.service(this.environmentHelper, this.manager);
        this.environmentHelper.changeContext(sitemapSource, prefix);
        this.sitemapExecutor = parent.sitemapExecutor;
        this.sitemapSchema = parent.sitemapSchema;
    }

    /**
     * Create a new child of this processor (used for mounting submaps).
     *
     * @return a new child processor.
     */
    public TreeProcessor createChildProcessor(String  src,
                                              boolean configuredCheckReload,
                                              String  prefix)
    throws Exception {
        DelayedRefreshSourceWrapper delayedSource = new DelayedRefreshSourceWrapper(
                this.resolver.resolveURI(src), this.lastModifiedDelay);
        return new TreeProcessor(this, delayedSource, configuredCheckReload, prefix);
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager serviceManager) throws ServiceException {
        this.manager = serviceManager;
        this.resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
        this.settings = (Settings) this.manager.lookup(Settings.ROLE);
    }

    /**
     * @see org.apache.avalon.framework.activity.Initializable#initialize()
     */
    public void initialize() throws Exception {
        // setup the environment helper
        if (this.environmentHelper == null) {
            // We already have resolved our sitemap, so our context is the
            // directory of this sitemap.
            int pos = this.source.getURI().lastIndexOf('/');
            this.environmentHelper = new EnvironmentHelper(new URL(this.source.getURI().substring(0, pos + 1)));
        }
        ContainerUtil.service(this.environmentHelper, this.manager);

        // Create sitemap executor
        if (this.parent == null) {
            if (this.manager.hasService(SitemapExecutor.ROLE)) {
                this.sitemapExecutor = (SitemapExecutor) this.manager.lookup(SitemapExecutor.ROLE);
                this.releaseSitemapExecutor = true;
            } else {
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

        this.checkReload = config.getAttributeAsBoolean("check-reload",
                             this.settings.isReloadingEnabled("sitemap"));

        // Reload check delay. Default is 1 second.
        this.lastModifiedDelay = config.getChild("reload").getAttributeAsLong("delay", this.settings.getReloadDelay("sitemap"));

        String fileName = config.getAttribute("file", "sitemap.xmap");
        
        try {
            this.source = new DelayedRefreshSourceWrapper(this.resolver.resolveURI(fileName), lastModifiedDelay);
        } catch (Exception e) {
            throw new ConfigurationException("Cannot resolve " + fileName, e);
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

    /**
     * @see org.apache.cocoon.Processor#getRootProcessor()
     */
    public Processor getRootProcessor() {
        TreeProcessor result = this;
        while (result.parent != null) {
            result = result.parent;
        }

        return result;
    }

    /**
     * @see org.apache.cocoon.Processor#getContext()
     */
    public String getContext() {
        return this.environmentHelper.getContext();
    }

    /**
     * @see org.apache.cocoon.Processor#getSourceResolver()
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

        if (this.parent == null) {
            // Ensure root sitemap uses the correct context, even if not located in the webapp context
            this.environmentHelper.changeContext(this.source, "");
        }

        if (this.concreteProcessor == null ||
                (this.checkReload && (this.classReload || (this.source.getLastModified() != this.lastModified)))) {
            buildConcreteProcessor(env);
            this.classReload = false;
        }
    }
    
    private Configuration createSitemapProgram(Source sitemapSource)
    throws ProcessingException, SAXException, IOException {
        // do we validate? Default is false
        final String value = this.settings.getProperty("org.apache.cocoon.sitemap.validating", "false");
        if ( Boolean.valueOf(value).booleanValue() ) {
            if ( this.getLogger().isDebugEnabled() ) {
                this.getLogger().debug("Validating sitemap " + sitemapSource.getURI());
            }
            final Validator validator = this.sitemapSchema.newValidator();
            validator.validate(new StreamSource(sitemapSource.getInputStream()));
        }
        NamespacedSAXConfigurationHandler handler = new NamespacedSAXConfigurationHandler();
        AnnotationsFilter annotationsFilter = new AnnotationsFilter(handler);
        SourceUtil.toSAX(this.manager, sitemapSource, null, annotationsFilter);
        return handler.getConfiguration();        
    }
    
    /**
     * Build the concrete processor (i.e. loads the sitemap). Should be called
     * only by setupProcessor();
     */
    private synchronized void buildConcreteProcessor(Environment env) throws Exception {

        // Now that we entered the synchronized area, recheck what's already
        // been checked in process().
        if (this.concreteProcessor != null && !this.classReload && source.getLastModified() == this.lastModified) {
            // Nothing changed
            return;
        }

        long startTime = System.currentTimeMillis();
        long newLastModified;
        ConcreteTreeProcessor newProcessor;
        ConcreteTreeProcessor oldProcessor = this.concreteProcessor;
                
        if (oldProcessor != null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("found a previous ConcreteTreeProcessor");
            }            
        } else {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("first version of the ConcreteTreeProcessor");
            }            
        }
        
        // Dispose the old processor, if any
        if (oldProcessor != null) {
            oldProcessor.markForDisposal();
        }


        // We have to do a call to enterProcessor() here as during building
        // of the tree, components (e.g. actions) are already instantiated
        // (ThreadSafe ones mostly).
        // If these components try to access the current processor or the
        // current service manager they must get this one - which is currently
        // in the process of initialization.
        EnvironmentHelper.enterProcessor(this, env);

        try {

            Configuration sitemapProgram = createSitemapProgram(this.source);
            newLastModified = this.source.getLastModified();

            newProcessor = new ConcreteTreeProcessor(this, this.sitemapExecutor);

            // Get the treebuilder that can handle this version of the sitemap.
            TreeBuilder treeBuilder = getTreeBuilder(sitemapProgram);
            try {
                treeBuilder.setProcessor(newProcessor);
                ProcessingNode root = treeBuilder.build(sitemapProgram, this.source.getURI());
                newProcessor.setProcessorData(
                        treeBuilder.getWebApplicationContext(),
                        root,
                        treeBuilder.getDisposableNodes(),
                        treeBuilder.getEnterSitemapEventListeners(),
                        treeBuilder.getLeaveSitemapEventListeners());
                
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("ConcreteTreeProcessor ready");
                }

                // Get the actual interpreter
                FlowNode flowNode = (FlowNode)treeBuilder.getRegisteredNode("flow");
                if ( flowNode != null ) {
                    final Interpreter interpreter = flowNode.getInterpreter();
                    newProcessor.setAttribute(Interpreter.ROLE, interpreter);
                }
                
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
        this.concreteProcessor = newProcessor;
        this.lastModified = newLastModified;
    }

    /**
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
            this.manager.release(this.settings);
            this.resolver = null;
            this.manager = null;
            this.settings = null;
        }
    }

    /**
     * @see org.apache.cocoon.Processor#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return this.concreteProcessor.getAttribute(name);
    }

    /**
     * @see org.apache.cocoon.Processor#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String name) {
        return this.concreteProcessor.removeAttribute(name);
    }

    /**
     * @see org.apache.cocoon.Processor#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.concreteProcessor.setAttribute(name, value);
    }

    /**
     * @see org.apache.cocoon.Processor#getParent()
     */
    public Processor getParent() {
        return this.parent;
    }

    public void handleNotification() {
        this.classReload = true;
    }
}
