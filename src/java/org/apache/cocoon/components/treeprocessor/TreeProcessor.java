/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.components.treeprocessor;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.component.RoleManageable;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.ChainedConfiguration;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.ExtendedComponentSelector;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.components.treeprocessor.sitemap.PipelinesNode;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ForwardRedirector;
import org.apache.cocoon.environment.wrapper.EnvironmentWrapper;
import org.apache.cocoon.environment.wrapper.MutableEnvironmentFacade;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * Interpreted tree-traversal implementation of a pipeline assembly language.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: TreeProcessor.java,v 1.17 2004/01/18 22:27:14 sylvain Exp $
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

    public static final String REDIRECTOR_ATTR = "sitemap:redirector";

    private static final String OBJECT_SOURCE_RESOLVER = "sitemap:sourceresolver";

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

    /** The language used by this processor */
    protected String language;

    /** Selector of TreeBuilders, the hint is the language name */
    protected ExtendedComponentSelector builderSelector;

    /** The root node of the processing tree */
    protected ProcessingNode rootNode;

    /** The list of processing nodes that should be disposed when disposing this processor */
    protected List disposableNodes;

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

    /** The component configurations from the sitemap (if any) */
    protected Configuration componentConfigurations;
    
    /** The different sitemap component configurations */
    protected Map sitemapComponentConfigurations;
    
    /** The component manager for the sitemap */
    protected ComponentManager sitemapComponentManager;
    
    /** The source resolver */
    protected SourceResolver resolver;
    
    /**
     * Create a TreeProcessor.
     */
    public TreeProcessor() {
        // Language can be overriden in the configuration.
        this.language = "sitemap";

        this.checkReload = true;
        this.lastModifiedDelay = 1000;
    }

    /**
     * Create a child processor for a given language
     */
    protected TreeProcessor(TreeProcessor parent, ComponentManager manager, String language) {
        this.parent = parent;
        this.language = (language == null) ? parent.language : language;

        // Copy all that can be copied from the parent
        this.enableLogging(parent.getLogger());
        this.context = parent.context;
        this.roleManager = parent.roleManager;
        this.builderSelector = parent.builderSelector;
        this.checkReload = parent.checkReload;
        this.lastModifiedDelay = parent.lastModifiedDelay;

        // We have our own CM
        this.manager = manager;
        
        // Other fields are setup in initialize()
    }

    /**
     * Create a new child of this processor (used for mounting submaps).
     *
     * @param manager the component manager to be used by the child processor.
     * @param language the language to be used by the child processor.
     * @return a new child processor.
     */
    public TreeProcessor createChildProcessor(
        ComponentManager manager,
        String language,
        Source source)
      throws Exception {

        // Note: lifecycle methods aren't called, since this constructors copies all
        // that can be copied from the parent (see above)
        TreeProcessor child = new TreeProcessor(this, manager, language);
        child.source = new DelayedRefreshSourceWrapper(source, lastModifiedDelay);
        return child;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE);
    }

    public void setRoleManager(RoleManager rm) {
        this.roleManager = rm;
    }


/*
  <processor>
    <reload delay="10"/>
    <root-language name="sitemap"/>
    <language>...</language>
  </processor>
*/
    public void configure(Configuration config)
    throws ConfigurationException {
        this.fileName = config.getAttribute("file", null);
        this.checkReload = config.getAttributeAsBoolean("check-reload", true);

        Configuration rootLangConfig = config.getChild("root-language", false);
        if (rootLangConfig != null) {
            this.language = rootLangConfig.getAttribute("name");
        }

        // Obtain the configuration file, or use the XCONF_URL if none
        // is defined
        String xconfURL = config.getAttribute("config", XCONF_URL);

        // Reload check delay. Default is 1 second.
        this.lastModifiedDelay = config.getChild("reload").getAttributeAsLong("delay", 1000L);

        // Read the builtin languages definition file
        Configuration builtin;
        try {
            Source source = this.resolver.resolveURI( xconfURL );
            try {
                SAXConfigurationHandler handler = new SAXConfigurationHandler();
                SourceUtil.toSAX( this.manager, source, null, handler);
                builtin = handler.getConfiguration();
            } finally {
                this.resolver.release( source );
            }
        } catch(Exception e) {
            String msg = "Error while reading " + xconfURL + ": " + e.getMessage();
            throw new ConfigurationException(msg, e);
        } finally {
            this.manager.release( resolver );
        }

        // Create a selector for tree builders of all languages
        this.builderSelector = new ExtendedComponentSelector(Thread.currentThread().getContextClassLoader());
        try {
            LifecycleHelper.setupComponent(this.builderSelector,
                getLogger(),
                this.context,
                this.manager,
                this.roleManager,
                builtin
            );
        } catch(ConfigurationException ce) {
            throw ce;
        } catch(Exception e) {
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
        InvokeContext context = new InvokeContext();

        context.enableLogging(getLogger());

        try {
            return process(environment, context);
        } finally {
            context.dispose();
        }
    }

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public ProcessingPipeline buildPipeline(Environment environment)
    throws Exception {
        InvokeContext context = new InvokeContext( true );

        context.enableLogging(getLogger());

        try {
            if ( process(environment, context) ) {
                return context.getProcessingPipeline();
            } else {
                return null;
            }
        } finally {
            context.dispose();
        }
    }

    /**
     * Do the actual processing, be it producing the response or just building the pipeline
     * @param environment
     * @param context
     * @return true if the pipeline was successfully built, false otherwise.
     * @throws Exception
     */
    protected boolean process(Environment environment, InvokeContext context)
    throws Exception {

        // first, check for sitemap changes
        if (this.rootNode == null ||
            (this.checkReload && this.source.getLastModified() > this.lastModified)) {
            setupRootNode(environment);
        }

        // and now process
        CocoonComponentManager.enterEnvironment(environment, this.sitemapComponentManager, this);

        Map objectModel = environment.getObjectModel();

        Object oldResolver = objectModel.get(OBJECT_SOURCE_RESOLVER);
        Object oldRedirector = environment.getAttribute(REDIRECTOR_ATTR);

        // Build a redirector
        TreeProcessorRedirector redirector = new TreeProcessorRedirector(environment, context);
        setupLogger(redirector);

        objectModel.put(OBJECT_SOURCE_RESOLVER, environment);
        environment.setAttribute(REDIRECTOR_ATTR, redirector);
        try {
            boolean success = this.rootNode.invoke(environment, context);
            
            return success;

        } finally {
            CocoonComponentManager.leaveEnvironment();
            // Restore old redirector and resolver
            environment.setAttribute(REDIRECTOR_ATTR, oldRedirector);
            objectModel.put(PipelinesNode.OBJECT_SOURCE_RESOLVER, oldResolver);
        }
    }
        
    private boolean handleCocoonRedirect(String uri, Environment environment, InvokeContext context) throws Exception {
        
        // Build an environment wrapper
        // If the current env is a facade, change the delegate and continue processing the facade, since
        // we may have other redirects that will in turn also change the facade delegate
        
        MutableEnvironmentFacade facade = environment instanceof MutableEnvironmentFacade ?
            ((MutableEnvironmentFacade)environment) : null;
        
        if (facade != null) {
            // Consider the facade delegate (the real environment)
            environment = facade.getDelegate();
        }
        
        Environment newEnv = new ForwardEnvironmentWrapper(environment, this.manager, uri, getLogger());
        
        if (facade != null) {
            // Change the facade delegate
            facade.setDelegate((EnvironmentWrapper)newEnv);
            newEnv = facade;
        }
        
        // Get the processor that should process this request
        TreeProcessor processor;
        if (newEnv.getRootContext() == newEnv.getContext()) {
            processor = (TreeProcessor)getRootProcessor();
        } else {
            processor = this;
        }
        
        // Process the redirect
// No more reset since with TreeProcessorRedirector, we need to pop values from the redirect location
//        context.reset();
        return processor.process(newEnv, context);
    }
    
    /**
     * Get the root parent of this processor
     * @since 2.1.1
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
        this.componentConfigurations = componentConfigurations;
        this.sitemapComponentConfigurations = null;
    }

    /**
     * Get the sitemap component configurations
     * @since 2.1
     */
    public Map getComponentConfigurations() {
        // do we have the sitemap configurations prepared for this processor?
        if ( null == this.sitemapComponentConfigurations ) {
            
            synchronized (this) {

                if ( this.sitemapComponentConfigurations == null ) {
                    // do we have configurations?
                    final Configuration[] childs = (this.componentConfigurations == null 
                                                     ? null 
                                                     : this.componentConfigurations.getChildren());
                    
                    if ( null != childs ) {
        
                        if ( null == this.parent ) {
                            this.sitemapComponentConfigurations = new HashMap(12);
                        } else {
                            // copy all configurations from parent
                            this.sitemapComponentConfigurations = new HashMap(this.parent.getComponentConfigurations()); 
                        }
                        
                        // and now check for new configurations
                        for(int m = 0; m < childs.length; m++) {
                            
                            final String r = this.roleManager.getRoleForName(childs[m].getName());
                            this.sitemapComponentConfigurations.put(r, new ChainedConfiguration(childs[m], 
                                                                             (ChainedConfiguration)this.sitemapComponentConfigurations.get(r)));
                        }
                    } else {
                        // we don't have configurations
                        if ( null == this.parent ) {
                            this.sitemapComponentConfigurations = Collections.EMPTY_MAP;
                        } else {
                            // use configuration from parent
                            this.sitemapComponentConfigurations = this.parent.getComponentConfigurations(); 
                        }
                    }
                }
            }
        }
        return this.sitemapComponentConfigurations;
    }

    protected synchronized void setupRootNode(Environment env) throws Exception {

        // Now that we entered the synchronized area, recheck what's already
        // been checked in process().
        if (this.rootNode != null && source.getLastModified() <= this.lastModified) {
            // Nothing changed
            return;
        }

        long startTime = System.currentTimeMillis();

        // Dispose the previous tree, if any
        disposeTree();

        // Get a builder
        TreeBuilder builder = (TreeBuilder)this.builderSelector.select(this.language);
        ProcessingNode root;
        try {
            if (builder instanceof Recomposable) {
                ((Recomposable)builder).recompose(this.manager);
            }
            builder.setProcessor(this);
            if (this.fileName == null) {
                this.fileName = builder.getFileName();
            }

            if (this.source == null) {
                this.source = new DelayedRefreshSourceWrapper(this.resolver.resolveURI(this.fileName), lastModifiedDelay);
            }
            root = builder.build(this.source);

            this.sitemapComponentManager = builder.getSitemapComponentManager();
            
            this.disposableNodes = builder.getDisposableNodes();
        } finally {
            this.builderSelector.release(builder);
        }

        this.lastModified = System.currentTimeMillis();

        if (getLogger().isDebugEnabled()) {
            double time = (this.lastModified - startTime) / 1000.0;
            getLogger().debug("TreeProcessor built in " + time + " secs from " + source.getURI());
        }

        // Finished
        this.rootNode = root;
    }

    public void dispose() {
        disposeTree();
        if (this.parent == null) {
            // root processor : dispose the builder selector
            this.builderSelector.dispose();
        }
        if ( this.manager != null ) {
            if ( this.source != null ) {
                this.resolver.release(this.source.getSource());
                this.source = null;
            }
            this.manager.release(this.resolver);
            this.resolver = null;
            this.manager = null;
        }
    }

    /**
     * Dispose all nodes in the tree that are disposable
     */
    protected void disposeTree() {
        if (this.disposableNodes != null) {
            // we must dispose the nodes in reverse order
            // otherwise selector nodes are freed before the components node
            for(int i=this.disposableNodes.size()-1; i>-1; i--) {
                ((Disposable)disposableNodes.get(i)).dispose();
            }
            this.disposableNodes = null;
        }
    }
    
    private class TreeProcessorRedirector extends ForwardRedirector {
        
        private InvokeContext context;
        public TreeProcessorRedirector(Environment env, InvokeContext context) {
            super(env);
            this.context = context;
        }
        
        protected void cocoonRedirect(String uri) throws IOException, ProcessingException {
            try {
                TreeProcessor.this.handleCocoonRedirect(uri, this.env, this.context);
            } catch(IOException ioe) {
                throw ioe;
            } catch(ProcessingException pe) {
                throw pe;
            } catch(RuntimeException re) {
                throw re;
            } catch(Exception ex) {
                throw new ProcessingException(ex);
            }
        }
    }
    
    /**
     * Local extension of EnvironmentWrapper to propagate otherwise blocked
     * methods to the actual environment.
     */
    private static final class ForwardEnvironmentWrapper extends EnvironmentWrapper {

        public ForwardEnvironmentWrapper(Environment env,
            ComponentManager manager, String uri, Logger logger) throws MalformedURLException {
            super(env, manager, uri, logger);
        }

        public void setStatus(int statusCode) {
            environment.setStatus(statusCode);
        }

        public void setContentLength(int length) {
            environment.setContentLength(length);
        }

        public void setContentType(String contentType) {
            environment.setContentType(contentType);
        }

        public String getContentType() {
            return environment.getContentType();
        }
    }

}
