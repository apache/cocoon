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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.component.DefaultRoleManager;
import org.apache.avalon.excalibur.component.ExcaliburComponentSelector;
import org.apache.avalon.excalibur.component.RoleManageable;
import org.apache.avalon.excalibur.component.RoleManager;
import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.component.WrapperComponentManager;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.configuration.NamespacedSAXConfigurationHandler;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.service.WrapperServiceSelector;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.components.ExtendedComponentSelector;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapParameters;
import org.apache.excalibur.source.Source;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: DefaultTreeBuilder.java,v 1.13 2004/07/15 12:49:50 sylvain Exp $
 */
public class DefaultTreeBuilder
        extends AbstractLogEnabled
        implements TreeBuilder, Configurable, Contextualizable, Serviceable,
                   RoleManageable, Recyclable, Disposable {

    protected Map attributes = new HashMap();

    /**
     * The tree processor that we're building.
     */
    protected ConcreteTreeProcessor processor;

    //----- lifecycle-related objects ------
    protected Context context;

    /**
     * The parent component manager. Either the one of the parent processor, or that provided
     * by Cocoon in service()
     */
    protected ServiceManager ownManager;

    /**
     * The parent role manager, set using <code>setRoleManager</code> (implementation of
     * <code>RoleManageable</code>).
     */
    protected RoleManager ownRoleManager;

    protected Configuration configuration;
    // -------------------------------------
    
    /**
     * Component processor of the parent manager (can be null for the root sitemap)
     */
    protected ServiceManager parentProcessorManager;

    /**
     * Component manager created by {@link #createComponentManager(Configuration)}.
     */
    protected ServiceManager processorManager;

    /**
     * Role manager result created by {@link #createRoleManager()}.
     */
    protected RoleManager processorRoleManager;

    /** Selector for ProcessingNodeBuilders */
    protected ServiceSelector builderSelector;

    protected LifecycleHelper lifecycle;

    protected String namespace;

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


    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.component.Composable#compose(org.apache.avalon.framework.component.ComponentManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.ownManager = manager;
    }

    public void setParentProcessorManager(ServiceManager manager) {
        this.parentProcessorManager = manager;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.component.RoleManageable#setRoleManager(org.apache.avalon.excalibur.component.RoleManager)
     */
    public void setRoleManager(RoleManager rm) {
        this.ownRoleManager = rm;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.configuration.Configurable#configure(org.apache.avalon.framework.configuration.Configuration)
     */
    public void configure(Configuration config) throws ConfigurationException {
        this.configuration = config;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        this.attributes.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#getAttribute(java.lang.String)
     */
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
        RoleManager roles = new DefaultRoleManager(this.ownRoleManager);

        LifecycleHelper.setupComponent(roles,
            getLogger(),
            this.context,
            this.ownManager,
            this.ownRoleManager,
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
    protected ServiceManager createServiceManager(Configuration tree) throws Exception
    {
        return this.ownManager;
    }

    /**
     * Create a <code>ComponentSelector</code> for <code>ProcessingNodeBuilder</code>s.
     * It creates a selector with the contents of the "node" element of the configuration.
     *
     * @return a selector for node builders
     */
    protected ServiceSelector createBuilderSelector(String sitemapVersion)
    throws Exception {

        // Create the NodeBuilder selector.
        ExcaliburComponentSelector selector = new ExtendedComponentSelector() {
            protected String getComponentInstanceName() {
                return "node";
            }

            protected String getClassAttributeName() {
                return "builder";
            }
        };

        // Merge configuration for tree builder from
        // <nodes> and <nodes-{sitemapVersion}>
        final Configuration base = this.configuration.getChild("nodes");
        final Configuration layer = this.configuration.getChild("nodes-" + sitemapVersion);
        if ( layer != null ) {
            final DefaultConfiguration merged =
                  new DefaultConfiguration( base.getName(),
                                      "Merged [layer: " + layer.getLocation()
                                      + ", base: " + base.getLocation() + "]" );
            merged.addAllChildren(base);
            final Configuration[] lc = layer.getChildren();
            for( int i = 0; i < lc.length; i++ ) {
                final String nodeName = lc[i].getAttribute("name");
                final Configuration[] bc = base.getChildren();
                boolean found = false;
                int m = 0;
                while ( m < bc.length && ! found ) {
                    if ( nodeName.equals(bc[m].getAttribute("name")) ) {
                        found = true;
                    } else {
                        m++;
                    }
                }
                if ( found ) {
                    merged.removeChild( bc[m] );
                }
                merged.addChild( lc[i] );
            }
        }
        // Automagically initialize the selector
        LifecycleHelper.setupComponent(selector,
            getLogger(),
            this.context,
            this.ownManager,
            new WrapperComponentManager(this.ownManager),
            this.ownRoleManager,
            base,
            true
        );

        return new WrapperServiceSelector("BuilderSelector", selector);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#setProcessor(org.apache.cocoon.components.treeprocessor.ConcreteTreeProcessor)
     */
    public void setProcessor(ConcreteTreeProcessor processor) {
        this.processor = processor;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#getProcessor()
     */
    public ConcreteTreeProcessor getProcessor() {
        return this.processor;
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

        } catch(ServiceException ce) {
            // Is it because this element is unknown ?
            if (this.builderSelector.isSelectable(nodeName)) {
                // No : rethrow
                throw ce;
            } else {
                // Throw a more meaningful exception
                String msg = "Unknown element '" + nodeName + "' at " + config.getLocation();
                throw new ConfigurationException(msg);
            }
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

            this.namespace = treeConfig.getNamespace();

            // get the namespace version
            final int pos = this.namespace.lastIndexOf('/');
            if ( pos == -1 ) {
                throw new ProcessingException("Namespace " + this.namespace + " does not have a version number.");
            }
            if ( !this.namespace.substring(0, pos).equals("http://apache.org/cocoon/sitemap") ) {
                throw new ProcessingException("Namespace " + this.namespace + " is not a valid sitemap namespace.");
            }
            return build(treeConfig, this.namespace.substring(pos+1));
        } catch (ProcessingException e) {
            throw e;
        } catch(Exception e) {
            throw new ProcessingException("Failed to load sitemap from " +
                source.getURI(), e);
        }
    }

    /**
     * Build a processing tree from a <code>Configuration</code>.
     */
    protected ProcessingNode build(Configuration tree, String sitemapVersion) throws Exception {

        this.processorRoleManager = createRoleManager();

        this.processorManager = createServiceManager(tree);

        // Create a helper object to setup components
        this.lifecycle = new LifecycleHelper(getLogger(),
            this.context,
            this.processorManager,
            this.processorRoleManager,
            null // configuration
        );

        this.builderSelector = createBuilderSelector(sitemapVersion);

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
            ((AbstractProcessingNode)node).setSitemapExecutor(this.processor.getSitemapExecutor());
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

        Configuration[] children = config.getChildren("parameter");

        if (children.length == 0) {
            // Parameters are only the component's location
            // TODO Optimize this
            return new SitemapParameters.ExtendedHashMap(config);
        }

        Map params = new SitemapParameters.ExtendedHashMap(config, children.length+1);
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            if (true) { // FIXME : check namespace
                String name = child.getAttribute("name");
                String value = child.getAttribute("value");
                try {
                    params.put(
                        VariableResolverFactory.getResolver(name, this.processorManager),
                        VariableResolverFactory.getResolver(value, this.processorManager));
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
     * and otherwhise the default type defined for this role in the components declarations.
     *
     * @throws ConfigurationException if the type could not be found.
     */
    public String getTypeForStatement(Configuration statement, String role) throws ConfigurationException {

        // Get the component type for the statement
        String type = statement.getAttribute("type", null);
        if (type == null) {
            type = getProcessor().getComponentInfo().getDefaultType(role);
        }
        
        if (type == null) {
            throw new ConfigurationException("No default type exists for 'map:" + statement.getName() +
                "' at " + statement.getLocation()
            );
        }

        // Check that this type actually exists
        ServiceSelector selector = null;

        try {
            selector = (ServiceSelector)this.processorManager.lookup(role + "Selector");
        } catch(ServiceException ce) {
            throw new ConfigurationException("Cannot get service selector for 'map:" +
                statement.getName() + "' at " + statement.getLocation(),
                ce
            );
        }            

        boolean selectable = selector.isSelectable(type);
        this.processorManager.release(selector);

        if (!selector.isSelectable(type)) {
            throw new ConfigurationException("Type '" + type + "' does not exist for 'map:" +
                statement.getName() + "' at " + statement.getLocation()
            );
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
        this.processorManager = null;
        this.processorRoleManager = null;
    }

    public void dispose() {
        LifecycleHelper.dispose(this.builderSelector);

        // Don't dispose manager or roles: they are used by the built tree
        // and thus must live longer than the builder.
    }
}
