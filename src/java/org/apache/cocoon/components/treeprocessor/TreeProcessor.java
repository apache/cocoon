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

import java.util.Map;

import org.apache.avalon.excalibur.component.RoleManageable;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
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
import org.apache.cocoon.util.Settings;
import org.apache.cocoon.util.SettingsHelper;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.ExtendedComponentSelector;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.PropertyAwareSAXConfigurationHandler;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * Interpreted tree-traversal implementation of a pipeline assembly language.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id$
 */

public class TreeProcessor
    extends AbstractLogEnabled
    implements ThreadSafe,
               Processor,
               Composable,
               Configurable,
               RoleManageable,
               Contextualizable,
               Disposable {

    public static final String COCOON_REDIRECT_ATTR = "sitemap:cocoon-redirect";

    private static final String XCONF_URL =
        "resource://org/apache/cocoon/components/treeprocessor/treeprocessor-builtins.xml";

    /** The parent TreeProcessor, if any */
    protected TreeProcessor parent;

    /** The context */
    protected Context context;

    /** The component manager */
    protected ComponentManager manager;

    /** The role manager */
    protected RoleManager roleManager;

    /** Selector of TreeBuilders, the hint is the language name */
    protected ExtendedComponentSelector builderSelector;

    /** Last modification time */
    protected long lastModified = 0;

    /** The source of the tree definition */
    protected DelayedRefreshSourceWrapper source;

    /** Delay for <code>sourceLastModified</code>. */
    protected long lastModifiedDelay;

    /** The current language configuration */
    protected Configuration currentLanguage;

    /** Check for reload? */
    protected boolean checkReload;

    /** The source resolver */
    protected SourceResolver resolver;

    /** The actual processor (package-private as needs to be accessed by ConcreteTreeProcessor) */
    ConcreteTreeProcessor concreteProcessor;

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
    protected TreeProcessor(TreeProcessor parent, ComponentManager manager) {
        this.parent = parent;

        // Copy all that can be copied from the parent
        this.enableLogging(parent.getLogger());
        this.context = parent.context;
        this.roleManager = parent.roleManager;
        this.builderSelector = parent.builderSelector;
        this.checkReload = parent.checkReload;
        this.lastModifiedDelay = parent.lastModifiedDelay;

        // We have our own CM
        this.manager = manager;
    }

    /**
     * Create a new child of this processor (used for mounting submaps).
     *
     * @param manager the component manager to be used by the child processor.
     * @return a new child processor.
     */
    public TreeProcessor createChildProcessor(ComponentManager manager,
                                              String           actualSource,
                                              boolean          checkReload)
    throws Exception {

        // Note: lifecycle methods aren't called, since this constructors copies all
        // that can be copied from the parent (see above)
        TreeProcessor child = new TreeProcessor(this, manager);
        child.checkReload = checkReload;
        child.resolver = (SourceResolver)manager.lookup(SourceResolver.ROLE);
        child.source = new DelayedRefreshSourceWrapper(child.resolver.resolveURI(actualSource), this.lastModifiedDelay);

        return child;
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


/*
  <processor>
    <reload delay="10"/>
    <language>...</language>
  </processor>
*/
    public void configure(Configuration config)
    throws ConfigurationException {

        this.checkReload = config.getAttributeAsBoolean("check-reload", true);

        // Obtain the configuration file, or use the XCONF_URL if none
        // is defined
        String xconfURL = config.getAttribute("config", XCONF_URL);

        // Reload check delay. Default is 1 second.
        this.lastModifiedDelay = config.getChild("reload").getAttributeAsLong("delay", 1000L);

        String fileName = config.getAttribute("file", "sitemap.xmap");

        try {
            this.source = new DelayedRefreshSourceWrapper(this.resolver.resolveURI(fileName), this.lastModifiedDelay);
        } catch (Exception e) {
            throw new ConfigurationException("Cannot resolve " + fileName, e);
        }

        // Read the builtin languages definition file
        Configuration builtin;
        try {
            Source source = this.resolver.resolveURI(xconfURL);
            try {
                Settings settings = SettingsHelper.getSettings(this.context);
                SAXConfigurationHandler handler = new PropertyAwareSAXConfigurationHandler(settings, this.getLogger());
                SourceUtil.toSAX( this.manager, source, null, handler);
                builtin = handler.getConfiguration();
            } finally {
                this.resolver.release(source);
            }
        } catch(Exception e) {
            String msg = "Error while reading " + xconfURL + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        }

        // Create a selector for tree builders of all languages
        this.builderSelector = new ExtendedComponentSelector(Thread.currentThread().getContextClassLoader());
        try {
            LifecycleHelper.setupComponent(this.builderSelector,
                                           this.getLogger(),
                                           this.context,
                                           this.manager,
                                           this.roleManager,
                                           builtin);
        } catch (ConfigurationException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigurationException("Could not setup builder selector", e);
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

        this.setupConcreteProcessor(environment);

        return this.concreteProcessor.process(environment);
    }

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public ProcessingPipeline buildPipeline(Environment environment)
    throws Exception {

    		this.setupConcreteProcessor(environment);

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

    private void setupConcreteProcessor(Environment env) throws Exception {

        if (this.parent == null) {
            // Ensure root sitemap uses the correct context, even if not located in the webapp context
            env.changeContext("", this.source.getURI());
        }

        // check for sitemap changes
        if (this.concreteProcessor == null ||
            (this.checkReload && this.source.getLastModified() != this.lastModified)) {
            this.buildConcreteProcessor(env);
        }
    }

    private synchronized void buildConcreteProcessor(Environment env) throws Exception {

        // Now that we entered the synchronized area, recheck what's already
        // been checked in process().
        if (this.concreteProcessor != null && this.source.getLastModified() == this.lastModified) {
            // Nothing changed
            return;
        }

        long startTime = System.currentTimeMillis();

        // Dispose the old processor, if any
        if (this.concreteProcessor != null) {
            this.concreteProcessor.markForDisposal();
        }

        // Get a builder
        TreeBuilder builder = (TreeBuilder)this.builderSelector.select("sitemap");
        ConcreteTreeProcessor newProcessor = new ConcreteTreeProcessor(this);
        long newLastModified;
        this.setupLogger(newProcessor);
        //FIXME (SW): why do we need to enterProcessor here?
        CocoonComponentManager.enterEnvironment(env, this.manager, this);
        try {
            if (builder instanceof Recomposable) {
                ((Recomposable)builder).recompose(this.manager);
            }
            builder.setProcessor(newProcessor);

            newLastModified = this.source.getLastModified();

            ProcessingNode root = builder.build(this.source);

            newProcessor.setProcessorData(builder.getSitemapComponentManager(), root, builder.getDisposableNodes());
        } finally {
            CocoonComponentManager.leaveEnvironment();
            this.builderSelector.release(builder);
        }

        if (this.getLogger().isDebugEnabled()) {
            double time = (this.lastModified - startTime) / 1000.0;
            this.getLogger().debug("TreeProcessor built in " + time + " secs from " + this.source.getURI());
        }

        // Switch to the new processor (ensure it's never temporarily null)
        this.concreteProcessor = newProcessor;
        this.lastModified = newLastModified;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        // Dispose the concrete processor. No need to check for existing requests, as there
        // are none when a TreeProcessor is disposed.
        ContainerUtil.dispose(this.concreteProcessor);
        this.concreteProcessor = null;

        if (this.manager != null) {
	        if (this.source != null) {
	            this.resolver.release(this.source.getSource());
	            this.source = null;
	        }

            if (this.parent == null) {
                // root processor : dispose the builder selector
                this.builderSelector.dispose();
                this.builderSelector = null;
            }

            // Release resolver looked up in compose()
            this.manager.release((Component)this.resolver);
            this.resolver = null;

            this.manager = null;
	    }
	}

    public String toString() {
        return "TreeProcessor - " + (this.source == null ? "[unknown location]" : this.source.getURI());
    }
}
