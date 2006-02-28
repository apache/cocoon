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
package org.apache.cocoon.components.treeprocessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.AbstractConfiguration;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.SAXConfigurationHandler;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.source.SourceUtil;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapParameters;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.util.location.LocationImpl;
import org.apache.cocoon.util.location.LocationUtils;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 *
 * @version $Id$
 */
public abstract class DefaultTreeBuilder
    extends AbstractLogEnabled
    implements TreeBuilder, Contextualizable, Serviceable, Recyclable, Disposable {

    protected Map attributes = new HashMap();

    //----- lifecycle-related objects ------

    /**
     * This component's avalon context
     */
    private Context context;

    /**
     * This component's service manager
     */
    private ServiceManager manager;

    // -------------------------------------

    /**
     * The tree processor that we are building.
     */
    protected ConcreteTreeProcessor processor;

    /**
     * The namespace of configuration for the processor that we are building.
     */
    protected String itsNamespace;

    /**
     * The context for the processor that we are building
     * It is created by {@link #createContext(Configuration)}.
     */
    private Context itsContext;

    /**
     * The service manager for the processor that we are building.
     * It is created by {@link #createServiceManager(ClassLoader, Context, Configuration)}.
     */
    private ServiceManager itsManager;

    private ConfigurableBeanFactory itsBeanFactory;
    
    /**
     * The classloader for the processor that we are building.
     * It is created by {@link #createServiceManager(ClassLoader, Context, Configuration)}.
     */
    protected ClassLoader itsClassLoader;

    /**
     * Helper object which sets up components in the context
     * of the processor that we are building.
     */
    private LifecycleHelper itsLifecycle;

    /**
     * Selector for ProcessingNodeBuilders which is set up
     * in the context of the processor that we are building.
     */
    private ServiceSelector itsBuilders;

    /**
     * The sitemap component information grabbed while building itsMaanger
     */
    protected ProcessorComponentInfo itsComponentInfo;

    /** Optional event listeners for the enter sitemap event */
    protected List enterSitemapEventListeners = new ArrayList();

    /** Optional event listeners for the leave sitemap event */
    protected List leaveSitemapEventListeners = new ArrayList();

    // -------------------------------------

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


    /**
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    /**
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    /**
     * Get the location of the treebuilder config file. Can be overridden for other versions.
     * @return The location of the treebuilder config file
     */
    protected String getBuilderConfigURL() {
        return "resource://org/apache/cocoon/components/treeprocessor/sitemap-language.xml";
    }

    /**
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
     * Create a context that will be used for all <code>Contextualizable</code>
     * <code>ProcessingNodeBuilder</code>s and <code>ProcessingNode</code>s.
     *
     * <p>The default here is to simply return the context set in
     * <code>contextualize()</code>, i.e. the context set by the calling
     * <code>TreeProcessor</code>.
     *
     * <p>Subclasses can redefine this method to create a context local to
     * a tree, such as for sitemap's &lt;map:components&gt;.
     *
     * @return a context
     */
    protected Context createContext(Configuration tree)
    throws Exception {
        return this.context;
    }

    protected ClassLoader createClassLoader(Configuration tree) throws Exception {
        // Useless method as it's redefined in SitemapLanguage
        // which is the only used incarnation.
        return Thread.currentThread().getContextClassLoader();        
    }
    
    /**
     * Create a service manager that will be used for all <code>Serviceable</code>
     * <code>ProcessingNodeBuilder</code>s and <code>ProcessingNode</code>s.
     *
     * <p>The default here is to simply return the manager set in
     * <code>compose()</code>, i.e. the component manager set by the calling
     * <code>TreeProcessor</code>.
     *
     * <p>Subclasses can redefine this method to create a service manager local to
     * a tree, such as for sitemap's &lt;map:components&gt;.
     *
     * @return a component manager
     */
    protected abstract ConfigurableBeanFactory createApplicationContext(ClassLoader classloader, Context context, Configuration tree)
    throws Exception;


    /**
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#setProcessor(ConcreteTreeProcessor)
     */
    public void setProcessor(ConcreteTreeProcessor processor) {
        this.processor = processor;
    }

    /**
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#getProcessor()
     */
    public ConcreteTreeProcessor getProcessor() {
        return this.processor;
    }
    
    /**
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#getBeanFactory()
     */
    public ConfigurableBeanFactory getBeanFactory() {
        return this.itsBeanFactory;
    }
    
    public ServiceManager getServiceManager() {
        return this.itsManager;
    }

    public ClassLoader getBuiltProcessorClassLoader() {
        return this.itsClassLoader;
    }

    /**
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#getEnterSitemapEventListeners()
     */
    public List getEnterSitemapEventListeners() {
        // we make a copy here, so we can clear(recylce) the list after the
        // sitemap is build
        return (List)((ArrayList)this.enterSitemapEventListeners).clone();
    }

    /**
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#getLeaveSitemapEventListeners()
     */
    public List getLeaveSitemapEventListeners() {
        // we make a copy here, so we can clear(recylce) the list after the
        // sitemap is build
        return (List)((ArrayList)this.leaveSitemapEventListeners).clone();
    }

    /**
     * @see org.apache.cocoon.components.treeprocessor.TreeBuilder#registerNode(java.lang.String, org.apache.cocoon.components.treeprocessor.ProcessingNode)
     */
    public boolean registerNode(String name, ProcessingNode node) {
        if ( this.registeredNodes.containsKey(name) ) {
            return false;
        }
        this.registeredNodes.put(name, node);
        return true;
    }

    public ProcessingNode getRegisteredNode(String name) {
        if (this.canGetNode) {
            return (ProcessingNode)this.registeredNodes.get(name);
        }
        throw new IllegalArgumentException("Categories are only available during buildNode()");
    }

    public ProcessingNodeBuilder createNodeBuilder(Configuration config) throws Exception {
        // FIXME : check namespace
        String nodeName = config.getName();

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Creating node builder for " + nodeName);
        }

        ProcessingNodeBuilder builder;
        try {
            builder = (ProcessingNodeBuilder) this.itsBuilders.select(nodeName);
        } catch (ServiceException ce) {
            // Is it because this element is unknown ?
            if (this.itsBuilders.isSelectable(nodeName)) {
                // No : rethrow
                throw ce;
            }
            // Throw a more meaningful exception
            String msg = "Unknown element '" + nodeName + "' at " + config.getLocation();
            throw new ConfigurationException(msg);
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
        return this.itsNamespace;
    }

    /**
     * Build a processing tree from a <code>Configuration</code>.
     */
    public ProcessingNode build(Configuration tree) throws Exception {
        // The namespace used in the whole sitemap is the one of the root element
        this.itsNamespace = tree.getNamespace();

        Configuration componentConfig = tree.getChild("components", false);

        if (componentConfig == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Sitemap has no components definition at " + tree.getLocation());
            }
            //componentConfig = new DefaultConfiguration("", "");
        }

        // Context and manager and classloader for the sitemap we build
        this.itsContext = createContext(tree);
//        this.itsClassLoader = createClassLoader(componentConfig);
//        
//        Thread currentThread = Thread.currentThread();
        //ClassLoader oldClassLoader = currentThread.getContextClassLoader();
//        currentThread.setContextClassLoader(this.itsClassLoader);
        this.itsClassLoader = Thread.currentThread().getContextClassLoader();

        // Only create an sitemap internal component manager if there really is a configuration
        // FIXME: Internal configurations doesn't work in a non bean factory environment
        if (componentConfig != null) {
            this.itsBeanFactory = createApplicationContext(this.itsClassLoader, this.itsContext, componentConfig);
            this.itsManager = (ServiceManager)this.itsBeanFactory.getBean(ServiceManager.class.getName());
        } else {
            this.itsManager = manager;
        }
        this.itsComponentInfo = (ProcessorComponentInfo)this.itsManager.lookup(ProcessorComponentInfo.ROLE);
        // Create a helper object to setup components
        this.itsLifecycle = new LifecycleHelper(getLogger(),
                                             this.itsContext,
                                             this.itsManager,
                                             null /* configuration */);

        // Create & initialize the NodeBuilder selector.
        {
            StandaloneServiceSelector selector = new StandaloneServiceSelector();

            // Load the builder config file
            SourceResolver resolver = (SourceResolver) this.manager.lookup(SourceResolver.ROLE);
            String url = getBuilderConfigURL();
            Configuration config;
            try {
                Source src = resolver.resolveURI(url);
                try {
                    SAXConfigurationHandler handler = new SAXConfigurationHandler();
                    SourceUtil.toSAX(this.manager, src, null, handler);
                    config = handler.getConfiguration();
                } finally {
                    resolver.release(src);
                }
            } catch (Exception e) {
                throw new ConfigurationException("Could not load TreeBuilder configuration from " + url, e);
            } finally {
                this.manager.release(resolver);
            }
            LifecycleHelper.setupComponent(selector,
                                           getLogger(),
                                           this.itsContext,
                                           this.itsManager,
                                           config.getChild("nodes", false),
                                           true);
            this.itsBuilders = selector;
        }

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
        Location location = getLocation(config);
        if (node instanceof AbstractProcessingNode) {
            ((AbstractProcessingNode)node).setLocation(location);
            ((AbstractProcessingNode)node).setSitemapExecutor(this.processor.getSitemapExecutor());
        }

        this.itsLifecycle.setupComponent(node, false);

        if (node instanceof ParameterizableProcessingNode) {
            Map params = getParameters(config, location);
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

    protected LocationImpl getLocation(Configuration config) {
        String prefix = "";

        if (config instanceof AbstractConfiguration) {
            //FIXME: AbstractConfiguration has a _protected_ getPrefix() method.
            // So make some reasonable guess on the prefix until it becomes public
            String namespace = null;
            try {
                namespace = ((AbstractConfiguration)config).getNamespace();
            } catch (ConfigurationException e) {
                // ignore
            }
            if ("http://apache.org/cocoon/sitemap/1.0".equals(namespace)) {
                prefix="map";
            }
        }
        
        StringBuffer desc = new StringBuffer().append('<');
        if (prefix.length() > 0) {
            desc.append(prefix).append(':').append(config.getName());
        } else {
            desc.append(config.getName());
        }
        String type = config.getAttribute("type", null);
        if (type != null) {
            desc.append(" type=\"").append(type).append('"');
        }
        desc.append('>');
        
        Location rawLoc = LocationUtils.getLocation(config);
        return new LocationImpl(desc.toString(), rawLoc.getURI(), rawLoc.getLineNumber(), rawLoc.getColumnNumber());
    }

    /**
     * Get &lt;xxx:parameter&gt; elements as a <code>Map</code> of </code>ListOfMapResolver</code>s,
     * that can be turned into parameters using <code>ListOfMapResolver.buildParameters()</code>.
     *
     * @return the Map of ListOfMapResolver, or <code>null</code> if there are no parameters.
     */
    protected Map getParameters(Configuration config, Location location) throws ConfigurationException {

        Configuration[] children = config.getChildren("parameter");

        if (children.length == 0) {
            // Parameters are only the component's location
            // TODO Optimize this
            return new SitemapParameters.LocatedHashMap(location, 0);
        }

        Map params = new SitemapParameters.LocatedHashMap(location, children.length+1);
        for (int i = 0; i < children.length; i++) {
            Configuration child = children[i];
            if (true) { // FIXME : check namespace
                String name = child.getAttribute("name");
                String value = child.getAttribute("value");
                try {
                    params.put(resolve(name), resolve(value));
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
            type = this.itsComponentInfo.getDefaultType(role);
        }

        if (type == null) {
            throw new ConfigurationException("No default type exists for 'map:" + statement.getName() +
                "' at " + statement.getLocation()
            );
        }

        // Check that this type actually exists
        ServiceSelector selector = null;
        try {
            selector = (ServiceSelector) this.itsManager.lookup(role + "Selector");
        } catch (ServiceException e) {
            throw new ConfigurationException("Cannot get service selector for 'map:" +
                                             statement.getName() + "' at " + statement.getLocation(),
                                             e);
        }

        if (!selector.isSelectable(type)) {
            throw new ConfigurationException("Type '" + type + "' does not exist for 'map:" +
                                             statement.getName() + "' at " + statement.getLocation());
        }

        this.itsManager.release(selector);

        return type;
    }

    /**
     * Resolve expression using its manager
     */
    protected VariableResolver resolve (String expression)
    throws PatternException {
        return VariableResolverFactory.getResolver(expression, this.itsManager);
    }

    public void recycle() {
        // Reset all data created during the build
        this.attributes.clear();
        this.canGetNode = false;
        this.disposableNodes = new ArrayList(); // Must not be cleared as it's used for processor disposal
        this.initializableNodes.clear();
        this.linkedBuilders.clear();
        this.processor = null;          // Set in setProcessor()

        this.itsNamespace = null;       // Set in build()
        LifecycleHelper.dispose(this.itsBuilders);
        this.itsBuilders = null;        // Set in build()
        this.itsLifecycle = null;       // Set in build()
        this.itsManager = null;         // Set in build()
        this.itsContext = null;         // Set in build()

        this.registeredNodes.clear();
        this.initializableNodes.clear();
        this.linkedBuilders.clear();
        this.canGetNode = false;
        this.registeredNodes.clear();

        VariableResolverFactory.setDisposableCollector(null);
        this.enterSitemapEventListeners.clear();
        this.leaveSitemapEventListeners.clear();
    }

    public void dispose() {
        // Don't dispose manager or roles: they are used by the built tree
        // and thus must live longer than the builder.
    }
}
