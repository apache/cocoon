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

import org.apache.avalon.excalibur.component.DefaultRoleManager;
import org.apache.avalon.excalibur.component.ExcaliburComponentSelector;
import org.apache.avalon.excalibur.component.RoleManageable;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.ComponentSelector;
import org.apache.avalon.framework.component.Recomposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.NamespacedSAXConfigurationHandler;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.cocoon.Constants;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ExtendedComponentSelector;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.treeprocessor.variables.NOPVariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.excalibur.source.Source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: DefaultTreeBuilder.java,v 1.6 2004/02/06 15:39:57 sylvain Exp $
 */

public class DefaultTreeBuilder extends AbstractLogEnabled implements TreeBuilder,
  Recomposable, Configurable, Contextualizable, RoleManageable, Recyclable, Disposable {

    protected Map attributes = new HashMap();

    /**
     * The tree processor that we're building.
     */
    protected TreeProcessor processor;

    //----- lifecycle-related objects ------
    protected Context context;

    /**
     * The parent component manager, set using <code>compose()</code> and <code>recompose()</code>
     * (implementation of <code>Recomposable</code>).
     */
    protected ComponentManager parentManager;

    /**
     * The parent role manager, set using <code>setRoleManager</code> (implementation of
     * <code>RoleManageable</code>).
     */
    protected RoleManager parentRoleManager;

    protected Configuration configuration;
    // -------------------------------------

    /**
     * Component manager created by {@link #createComponentManager(Configuration)}.
     */
    protected ComponentManager manager;

    /**
     * Role manager result created by {@link #createRoleManager()}.
     */
    protected RoleManager roleManager;

    /** Selector for ProcessingNodeBuilders */
    protected ComponentSelector builderSelector;

    protected LifecycleHelper lifecycle;

    protected String namespace;

    protected String parameterElement;

    protected String languageName;

    protected String fileName;

    /** Nodes gone through setupNode() that implement Initializable */
    private List initializableNodes = new ArrayList();

    /** Nodes gone through setupNode() that implement Disposable */
    private List disposableNodes = new ArrayList();

    /** NodeBuilders created by createNodeBuilder() that implement LinkedProcessingNodeBuilder */
    private List linkedBuilders = new ArrayList();

    /** Are we in a state that allows to get registered nodes ? */
    private boolean canGetNode = false;

    /** Nodes registered using registerNode() */
    private Map registeredNodes = new HashMap();


    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void compose(ComponentManager manager) throws ComponentException {
        this.parentManager = manager;
    }

    public void recompose(ComponentManager manager) throws ComponentException {
        this.parentManager = manager;
    }

    public void setRoleManager(RoleManager rm) {
        this.parentRoleManager = rm;
    }

    /**
     * Configurable
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.configuration = config;

        this.languageName = config.getAttribute("name");
        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("Configuring Builder for language : " + this.languageName);
        }

        this.fileName = config.getChild("file").getAttribute("name");

        this.namespace = config.getChild("namespace").getAttribute("uri", "");

        this.parameterElement = config.getChild("parameter").getAttribute("element", "parameter");
    }

    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }
    
    public Object getAttribute(String name) {
        return this.attributes.get(name);
    }

    /**
     * Create a role manager that will be used by all <code>RoleManageable</code>
     * components. The default here is to create a role manager with the contents of
     * the &lt;roles&gt; element of the configuration.
     * <p>
     * Subclasses can redefine this method to create roles from other sources than
     * the one used here.
     *
     * @return the role manager
     */
    protected RoleManager createRoleManager() throws Exception
    {
        RoleManager roles = new DefaultRoleManager(this.parentRoleManager);

        LifecycleHelper.setupComponent(roles,
            getLogger(),
            this.context,
            this.manager,
            this.parentRoleManager,
            this.configuration.getChild("roles")
        );

        return roles;
    }

    /**
     * Create a component manager that will be used for all <code>Composable</code>
     * <code>ProcessingNodeBuilder</code>s and <code>ProcessingNode</code>s.
     * <p>
     * The default here is to simply return the manager set by <code>compose()</code>,
     * i.e. the component manager set by the calling <code>TreeProcessor</code>.
     * <p>
     * Subclasses can redefine this method to create a component manager local to a tree,
     * such as for sitemap's &lt;map:components&gt;.
     *
     * @return a component manager
     */
    protected ComponentManager createComponentManager(Configuration tree) throws Exception
    {
        return this.parentManager;
    }

    /**
     * Create a <code>ComponentSelector</code> for <code>ProcessingNodeBuilder</code>s.
     * It creates a selector with the contents of the "node" element of the configuration.
     *
     * @return a selector for node builders
     */
    protected ComponentSelector createBuilderSelector() throws Exception {

        // Create the NodeBuilder selector.
        ExcaliburComponentSelector selector = new ExtendedComponentSelector() {
            protected String getComponentInstanceName() {
                return "node";
            }

            protected String getClassAttributeName() {
                return "builder";
            }
        };

        // Automagically initialize the selector
        LifecycleHelper.setupComponent(selector,
            getLogger(),
            this.context,
            this.manager,
            this.roleManager,
            this.configuration.getChild("nodes")
        );

        return selector;
    }

    public void setProcessor(TreeProcessor processor) {
        this.processor = processor;
    }

    public TreeProcessor getProcessor() {
        return this.processor;
    }

    /**
     * Returns the language that is being built (e.g. "sitemap").
     */
    public String getLanguage() {
        return this.languageName;
    }

    /**
     * Returns the name of the parameter element.
     */
    public String getParameterName() {
        return this.parameterElement;
    }

    /**
     * Register a <code>ProcessingNode</code> under a given name.
     * For example, <code>ResourceNodeBuilder</code> stores here the <code>ProcessingNode</code>s
     * it produces for use by sitemap pipelines. This allows to turn the tree into a graph.
     */
    public void registerNode(String name, ProcessingNode node) {
        this.registeredNodes.put(name, node);
    }

    public ProcessingNode getRegisteredNode(String name) {
        if (this.canGetNode) {
            return (ProcessingNode)this.registeredNodes.get(name);
        } else {
            throw new IllegalArgumentException("Categories are only available during buildNode()");
        }
    }

    public ProcessingNodeBuilder createNodeBuilder(Configuration config) throws Exception {
        //FIXME : check namespace
        String nodeName = config.getName();

        if (this.getLogger().isDebugEnabled()) {
            getLogger().debug("Creating node builder for " + nodeName);
        }

        ProcessingNodeBuilder builder;
        try {
            builder = (ProcessingNodeBuilder)this.builderSelector.select(nodeName);

        } catch(ComponentException ce) {
            // Is it because this element is unknown ?
            if (this.builderSelector.hasComponent(nodeName)) {
                // No : rethrow
                throw ce;
            } else {
                // Throw a more meaningful exception
                String msg = "Unknown element '" + nodeName + "' at " + config.getLocation();
                throw new ConfigurationException(msg);
            }
        }

        if (builder instanceof Recomposable) {
            ((Recomposable)builder).recompose(this.manager);
        }

        builder.setBuilder(this);

        if (builder instanceof LinkedProcessingNodeBuilder) {
            this.linkedBuilders.add(builder);
        }

        return builder;
    }

    /**
     * Create the tree once component manager and node builders have been set up.
     * Can be overriden by subclasses to perform pre/post tree creation operations.
     */
    protected ProcessingNode createTree(Configuration tree) throws Exception {
        // Create a node builder from the top-level element
        ProcessingNodeBuilder rootBuilder = createNodeBuilder(tree);

        // Build the whole tree (with an empty buildModel)
        return rootBuilder.buildNode(tree);
    }

    /**
     * Resolve links : call <code>linkNode()</code> on all
     * <code>LinkedProcessingNodeBuilder</code>s.
     * Can be overriden by subclasses to perform pre/post resolution operations.
     */
    protected void linkNodes() throws Exception {
        // Resolve links
        Iterator iter = this.linkedBuilders.iterator();
        while(iter.hasNext()) {
            ((LinkedProcessingNodeBuilder)iter.next()).linkNode();
        }
    }

    /**
     * Get the namespace URI that builders should use to find their nodes.
     */
    public String getNamespace() {
        return this.namespace;
    }

    public ProcessingNode build(Source source)
    throws Exception {

        try {
            // Build a namespace-aware configuration object
            NamespacedSAXConfigurationHandler handler = new NamespacedSAXConfigurationHandler();
            SourceUtil.toSAX( source, handler );
            Configuration treeConfig = handler.getConfiguration();

            return build(treeConfig);
        } catch (ProcessingException e) {
            throw e;
        } catch(Exception e) {
            throw new ProcessingException("Failed to load " + this.languageName + " from " +
                source.getURI(), e);
        }
    }

    public String getFileName() {
        return this.fileName;
    }

    /**
     * Build a processing tree from a <code>Configuration</code>.
     */
    public ProcessingNode build(Configuration tree) throws Exception {

        this.roleManager = createRoleManager();

        this.manager = createComponentManager(tree);

        // Create a helper object to setup components
        this.lifecycle = new LifecycleHelper(getLogger(),
            this.context,
            this.manager,
            this.roleManager,
            null // configuration
        );

        this.builderSelector = createBuilderSelector();

        // Calls to getRegisteredNode() are forbidden
        this.canGetNode = false;

        // Collect all disposable variable resolvers
        VariableResolverFactory.setDisposableCollector(this.disposableNodes);

        ProcessingNode result = createTree(tree);

        // Calls to getRegisteredNode() are now allowed
        this.canGetNode = true;

        linkNodes();

        // Initialize all Initializable nodes
        Iterator iter = this.initializableNodes.iterator();
        while(iter.hasNext()) {
            ((Initializable)iter.next()).initialize();
        }

        // And that's all !
        return result;
    }

    /**
     * Return the list of <code>ProcessingNodes</code> part of this tree that are
     * <code>Disposable</code>. Care should be taken to properly dispose them before
     * trashing the processing tree.
     */
    public List getDisposableNodes() {
        return this.disposableNodes;
    }

    /**
     * Return the sitemap component manager
     */
    public ComponentManager getSitemapComponentManager() {
        return this.manager;
    }
    
    /**
     * Setup a <code>ProcessingNode</code> by setting its location, calling all
     * the lifecycle interfaces it implements and giving it the parameter map if
     * it's a <code>ParameterizableNode</code>.
     * <p>
     * As a convenience, the node is returned by this method to allow constructs
     * like <code>return treeBuilder.setupNode(new MyNode(), config)</code>.
     */
    public ProcessingNode setupNode(ProcessingNode node, Configuration config)
      throws Exception {
        if (node instanceof AbstractProcessingNode) {
            ((AbstractProcessingNode)node).setLocation(config.getLocation());
        }

        this.lifecycle.setupComponent(node, false);

        if (node instanceof ParameterizableProcessingNode) {
            Map params = getParameters(config);
            ((ParameterizableProcessingNode)node).setParameters(params);
        }

        if (node instanceof Initializable) {
            this.initializableNodes.add(node);
        }

        if (node instanceof Disposable) {
            this.disposableNodes.add(node);
        }

        return node;
    }

    /**
     * Get &lt;xxx:parameter&gt; elements as a <code>Map</code> of </code>ListOfMapResolver</code>s,
     * that can be turned into parameters using <code>ListOfMapResolver.buildParameters()</code>.
     *
     * @return the Map of ListOfMapResolver, or <code>null</code> if there are no parameters.
     */
    protected Map getParameters(Configuration config) throws ConfigurationException {

        Configuration[] children = config.getChildren(this.parameterElement);

        if (children.length == 0) {
            // Parameters are only the component's location
            return Collections.singletonMap(new NOPVariableResolver(Constants.SITEMAP_PARAMETERS_LOCATION), new NOPVariableResolver(config.getLocation()));
        }

        Map params = new HashMap();
        params.put(new NOPVariableResolver(Constants.SITEMAP_PARAMETERS_LOCATION), new NOPVariableResolver(config.getLocation()));
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            if (true) { // FIXME : check namespace
                String name = child.getAttribute("name");
                String value = child.getAttribute("value");
                try {
                    params.put(
                        VariableResolverFactory.getResolver(name, this.manager),
                        VariableResolverFactory.getResolver(value, this.manager));
                } catch(PatternException pe) {
                    String msg = "Invalid pattern '" + value + "' at " + child.getLocation();
                    throw new ConfigurationException(msg, pe);
                }
            }
        }

        return params;
    }

    /**
     * Get the type for a statement : it returns the 'type' attribute if present,
     * and otherwhise the default hint of the <code>ExtendedSelector</code> designated by
     * role <code>role</code>.
     *
     * @throws ConfigurationException if the default type could not be found.
     */
    public String getTypeForStatement(Configuration statement, String role) throws ConfigurationException {

        String type = statement.getAttribute("type", null);

        ComponentSelector selector = null;

        try {
            try {
                selector = (ComponentSelector)this.manager.lookup(role);
            } catch(ComponentException ce) {
                String msg = "Cannot get component selector for '" + statement.getName() + "' at " +
                    statement.getLocation();
                throw new ConfigurationException(msg, ce);
            }

            if (type == null && selector instanceof ExtendedComponentSelector) {
                type = ((ExtendedComponentSelector)selector).getDefaultHint();
            }

            if (type == null) {
                String msg = "No default type exists for '" + statement.getName() + "' at " +
                    statement.getLocation();
                throw new ConfigurationException(msg);
            }

            if (!selector.hasComponent(type)) {
                String msg = "Type '" + type + "' is not defined for '" + statement.getName() + "' at " +
                    statement.getLocation();
                throw new ConfigurationException(msg);
            }
        } finally {
            this.manager.release(selector);
        }
        return type;
    }

    public void recycle() {
        this.lifecycle = null; // Created in build()
        this.initializableNodes.clear();
        this.linkedBuilders.clear();
        this.canGetNode = false;
        this.registeredNodes.clear();

        // Don't clear disposableNodes as they're used by the Processor
        this.disposableNodes = new ArrayList();
        VariableResolverFactory.setDisposableCollector(null);

        this.processor = null;
        this.manager = null;
        this.roleManager = null;
    }

    public void dispose() {
        LifecycleHelper.dispose(this.builderSelector);

        // Don't dispose manager or roles : they are used by the built tree
        // and thus must live longer than the builder.
    }
}
