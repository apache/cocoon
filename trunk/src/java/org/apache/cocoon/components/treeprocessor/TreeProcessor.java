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

import org.apache.avalon.excalibur.component.RoleManageable;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.excalibur.logger.LogKitManageable;
import org.apache.avalon.excalibur.logger.LogKitManager;
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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.CocoonComponentManager;
import org.apache.cocoon.components.ExtendedComponentSelector;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.source.impl.DelayedRefreshSourceWrapper;
import org.apache.cocoon.environment.Environment;
import org.apache.excalibur.source.Source;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Interpreted tree-traversal implementation of a pipeline assembly language.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: TreeProcessor.java,v 1.2 2003/03/20 08:54:17 cziegeler Exp $
 */

public class TreeProcessor
    extends AbstractLogEnabled
    implements ThreadSafe,
               Processor,
               Composable,
               Configurable,
               LogKitManageable,
               RoleManageable,
               Contextualizable,
               Disposable {

    private static final String XCONF_URL =
        "resource://org/apache/cocoon/components/treeprocessor/treeprocessor-builtins.xml";

    /** The parent TreeProcessor, if any */
    protected TreeProcessor parent;

    /** The context */
    protected Context context;

    /** The component manager */
    protected ComponentManager manager;

    /** The logkit manager to get Loggers */
    protected LogKitManager logKit;

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
    protected Source source;

    /** Delay for <code>sourceLastModified</code>. */
    protected long lastModifiedDelay;

    /** The current language configuration */
    protected Configuration currentLanguage;

    /** The file to process */
    protected String fileName;

    /** Check for reload? */
    protected boolean checkReload;

    /** component configurations */
    protected Configuration componentConfigurations;

    /** The different sitemap component configurations */
    protected Map sitemapComponentConfigurations;
    
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
        this.context = parent.context;
        this.logKit = parent.logKit;
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

        // FIXME(VG): Why child isn't configure()d/contextualize(d)/etc ???
        TreeProcessor child = new TreeProcessor(this, manager, language);
        child.enableLogging(getLogger());
        child.source = new DelayedRefreshSourceWrapper(source, lastModifiedDelay);
        return child;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.manager = manager;
    }

    public void setLogKitManager(LogKitManager logKit) {
        this.logKit = logKit;
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
        org.apache.excalibur.source.SourceResolver resolver = null;
        try {
            resolver = (org.apache.excalibur.source.SourceResolver)this.manager.lookup(org.apache.excalibur.source.SourceResolver.ROLE);
            org.apache.excalibur.source.Source source = resolver.resolveURI( xconfURL );
            try {
                SAXConfigurationHandler handler = new SAXConfigurationHandler();
                SourceUtil.toSAX(source, handler);
                builtin = handler.getConfiguration();
            } finally {
                resolver.release( source );
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
                this.logKit,
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
     * @throws ResourceNotFoundException If a sitemap component tries
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

    protected boolean process(Environment environment, InvokeContext context)
    throws Exception {

        // first, check for sitemap changes
        if (this.rootNode == null ||
            (this.checkReload && this.source.getLastModified() > this.lastModified)) {
            setupRootNode(environment);
        }

        // and now process
        CocoonComponentManager.enterEnvironment(environment, this.manager, this);
        try {
            return this.rootNode.invoke(environment, context);
        } finally {
            CocoonComponentManager.leaveEnvironment();
        }
    }

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public ProcessingPipeline processInternal(Environment environment)
    throws Exception {
        InvokeContext context = new InvokeContext( true );

        context.enableLogging(getLogger());

        CocoonComponentManager.enterEnvironment(environment, this.manager, this);
        try {
            if ( process(environment, context) ) {
                return context.getProcessingPipeline();
            } else {
                return null;
            }
        } finally {
            CocoonComponentManager.leaveEnvironment();
            context.dispose();
        }
    }

    /**
     * Set the sitemap component configurations
     */
    public void setComponentConfigurations(Configuration componentConfigurations) {
        this.componentConfigurations = componentConfigurations;
    }

    /**
     * Get the sitemap component configurations
     * @since 2.1
     */
    public Configuration getComponentConfigurations() {
        // do we have the sitemap configurations prepared for this processor?
        /*if ( null == sitemapComponentConfigurations ) {

            // do we have configurations?
            final Configuration[] childs = (this.componentConfigurations == null ? null : this.componentConfigurations.getChildren());
            
            if ( childs == null ) {
                Map configurationMap = null;
                if ( null == this.parent ) {
                    this.sitemapComponentConfigurations = Collections.EMPTY_MAP;
                } else {
                    // use configuration from parent
                    this.sitemapComponentConfigurations = (Map)this.parent.getComponentConfigurations(); 
                }

            } else {

                Map configurationMap = null;
                if ( null == this.parent ) {
                    configurationMap = new HashMap(12);
                } else {
                    // copy all configurations from parent
                    configurationMap = new HashMap((Map)this.parent.getComponentConfigurations()); 
                }
                
                // and now check for new configurations
                for(int m = 0; m < childs.length; m++) {
                    
                    //final String r = this.roleManager.getRoleForName(childs[m].getName());
                }
            }
        }*/
        
        return this.componentConfigurations;
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
                this.source = new DelayedRefreshSourceWrapper(env.resolveURI(this.fileName), lastModifiedDelay);
            }
            root = builder.build(this.source);

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
    }

    /**
     * Dispose all nodes in the tree that are disposable
     */
    protected void disposeTree() {
        if (this.disposableNodes != null) {
            Iterator iter = this.disposableNodes.iterator();
            while (iter.hasNext()) {
                ((Disposable)iter.next()).dispose();
            }
            this.disposableNodes = null;
        }
    }
}
