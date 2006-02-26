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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.CategoryNodeBuilder;
import org.apache.cocoon.components.treeprocessor.DefaultTreeBuilder;
import org.apache.cocoon.components.treeprocessor.TreeBuilder;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.container.spring.ApplicationContextFactory;
import org.apache.cocoon.core.container.spring.AvalonEnvironment;
import org.apache.cocoon.core.container.spring.CocoonXmlWebApplicationContext;
import org.apache.cocoon.core.container.spring.ConfigReader;
import org.apache.cocoon.core.container.spring.ConfigurationInfo;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.servlet.CocoonServlet;
import org.apache.cocoon.sitemap.EnterSitemapEventListener;
import org.apache.cocoon.sitemap.LeaveSitemapEventListener;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapListener;
import org.apache.cocoon.util.ClassUtils;
import org.apache.cocoon.util.StringUtils;
import org.apache.regexp.RE;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;

/**
 * The tree builder for the sitemap language.
 *
 * @version $Id$
 */
public class SitemapLanguage
    extends DefaultTreeBuilder
    implements BeanFactoryAware {

    /** Spring application context. */
    protected ConfigurableBeanFactory beanFactory;

    // Regexp's for splitting expressions
    private static final String COMMA_SPLIT_REGEXP = "[\\s]*,[\\s]*";
    private static final String EQUALS_SPLIT_REGEXP = "[\\s]*=[\\s]*";

//    protected ClassLoader createClassLoader(Configuration config)
//    throws Exception {
//        ClassLoader newClassLoader;
//        Configuration classpathConfig = config.getChild("classpath", false);
//        if (classpathConfig == null) {
//            return Thread.currentThread().getContextClassLoader();
//        }
//        
//        String factoryRole = config.getAttribute("factory-role", ClassLoaderFactory.ROLE + "/ReloadingClassLoaderFactory");
//        // Create a new classloader
//        ClassLoaderFactory clFactory = (ClassLoaderFactory)this.parentProcessorManager.lookup(factoryRole);
//        try {
//            return clFactory.createClassLoader(
//                    Thread.currentThread().getContextClassLoader(),
//                    classpathConfig
//            );
//        } finally {
//            this.parentProcessorManager.release(clFactory);
//        }
//    }
    
    /**
     * Build a component manager with the contents of the &lt;map:components&gt; element of
     * the tree.
     */
    protected BeanFactory createApplicationContext(ClassLoader classloader, Context context, Configuration config)
    throws Exception {

        // Create the classloader, if needed.
        ServiceManager newManager;
        
        // before we pass the configuration we have to strip the
        // additional configuration parts, like classpath etc. as these
        // are not configurations for the service manager
        final DefaultConfiguration c = new DefaultConfiguration(config.getName(), 
                                                                config.getLocation(),
                                                                config.getNamespace(),
                                                                "");
        c.addAll(config);
        c.removeChild(config.getChild("application-container"));
        c.removeChild(config.getChild("classpath"));
        c.removeChild(config.getChild("listeners"));

        // setup spring container
        // first, get the correct parent
        ConfigurableBeanFactory parentContext = this.beanFactory;
        final Request request = ContextHelper.getRequest(context);
        if ( request.getAttribute(CocoonXmlWebApplicationContext.APPLICATION_CONTEXT_REQUEST_ATTRIBUTE) != null ) {
            parentContext = (ConfigurableBeanFactory)request.getAttribute(CocoonXmlWebApplicationContext.APPLICATION_CONTEXT_REQUEST_ATTRIBUTE);
        }

        final AvalonEnvironment ae = new AvalonEnvironment();
        ae.context = context;
        ae.core = (Core)this.beanFactory.getBean(Core.ROLE);
        ae.logger = this.getLogger();
        ae.servletContext = ((ServletConfig)context.get(CocoonServlet.CONTEXT_SERVLET_CONFIG)).getServletContext();
        ae.settings = ae.core.getSettings();
        final ConfigurationInfo parentConfigInfo = (ConfigurationInfo)parentContext.getBean(ConfigurationInfo.class.getName());
        final ConfigurationInfo ci = ConfigReader.readConfiguration(c, parentConfigInfo, ae);

        final ConfigurableBeanFactory sitemapContext = 
            ApplicationContextFactory.createApplicationContext(ae, ci, parentContext, false);
        newManager = (ServiceManager) sitemapContext.getBean(ServiceManager.class.getName());
        Logger sitemapLogger = sitemapLogger = (Logger)sitemapContext.getBean(Logger.class.getName());

        // and finally the listeners
        final Configuration listenersWrapper = config.getChild("listeners", false);
        if ( listenersWrapper != null ) {
            final Configuration[] listeners = listenersWrapper.getChildren("listener");                
            for(int i = 0; i < listeners.length; i++) {
                final Configuration current = listeners[i];
                final TreeBuilder.EventComponent listener = this.createListener(newManager, sitemapLogger, context, current);
                if ( !(listener.component instanceof SitemapListener) ) {
                    throw new ConfigurationException("Listener must implement the SitemapListener interface.");
                }
                this.addListener(listener);
            }
        }

        return sitemapContext;
    }

    /**
     * Create a listener
     */
    protected TreeBuilder.EventComponent createListener(ServiceManager manager,
                                                        Logger sitemapLogger,
                                                        Context context,
                                                        Configuration config) 
    throws Exception {
        // role or class?
        final String role = config.getAttribute("role", null);
        if ( role != null ) {
            return new TreeBuilder.EventComponent(manager.lookup(role), true);
        }
        final String className = config.getAttribute("class");
        final Object component = ClassUtils.newInstance(className);

        LifecycleHelper.setupComponent(component, sitemapLogger, context, manager, config);

        return new TreeBuilder.EventComponent(component, false);
    }

    /**
     * Add a listener
     */
    protected void addListener(TreeBuilder.EventComponent listener) {
        if ( listener.component instanceof EnterSitemapEventListener ) {
            this.enterSitemapEventListeners.add(listener);
        } else if ( listener.component instanceof LeaveSitemapEventListener ) {
            this.leaveSitemapEventListeners.add(listener);
        }
    }

    /**
     * @see org.apache.cocoon.components.treeprocessor.DefaultTreeBuilder#createContext(org.apache.avalon.framework.configuration.Configuration)
     */
    protected Context createContext(Configuration tree) throws Exception {
        // Create sub-context for this sitemap
        DefaultContext newContext = new DefaultContext(super.createContext(tree));
        Environment env = EnvironmentHelper.getCurrentEnvironment();
        newContext.put(Constants.CONTEXT_ENV_URI, env.getURI());
        newContext.put(Constants.CONTEXT_ENV_PREFIX, env.getURIPrefix());
        // FIXME How to get rid of EnvironmentHelper?
        newContext.put(Constants.CONTEXT_ENV_HELPER, getProcessor().getWrappingProcessor().getEnvironmentHelper());

        return newContext;
    }

    //---- Views management

    /** Collection of view names for each label */
    private Map labelViews = new HashMap();

    /** The views CategoryNode */
    private CategoryNode viewsNode;

    /** Are we currently building a view ? */
    private boolean isBuildingView = false;

    /** Are we currently building a view ? */
    private boolean isBuildingErrorHandler = false;

    /**
     * Pseudo-label for views <code>from-position="first"</code> (i.e. generator).
     */
    public static final String FIRST_POS_LABEL = "!first!";

    /**
     * Pseudo-label for views <code>from-position="last"</code> (i.e. serializer).
     */
    public static final String LAST_POS_LABEL = "!last!";

    /* (non-Javadoc)
     * @see org.apache.avalon.excalibur.pool.Recyclable#recycle()
     */
    public void recycle() {
        super.recycle();

        // Go back to initial state
        this.labelViews.clear();
        this.viewsNode = null;
        this.isBuildingView = false;
        this.isBuildingErrorHandler = false;
    }

    /**
     * Set to <code>true</code> while building the internals of a &lt;map:view&gt;
     */
    public void setBuildingView(boolean building) {
        this.isBuildingView = building;
    }

    /**
     * Are we currently building a view ?
     */
    public boolean isBuildingView() {
        return this.isBuildingView;
    }

    /**
     * Set to <code>true</code> while building the internals of a &lt;map:handle-errors&gt;
     */
    public void setBuildingErrorHandler(boolean building) {
        this.isBuildingErrorHandler = building;
    }

    /**
     * Are we currently building an error handler ?
     */
    public boolean isBuildingErrorHandler() {
        return this.isBuildingErrorHandler;
    }

    /**
     * Add a view for a label. This is used to register all views that start from
     * a given label.
     *
     * @param label the label (or pseudo-label) for the view
     * @param view the view name
     */
    public void addViewForLabel(String label, String view) {
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("views:addViewForLabel(" + label + ", " + view + ")");
        }
        Set views = (Set)this.labelViews.get(label);
        if (views == null) {
            views = new HashSet();
            this.labelViews.put(label, views);
        }

        views.add(view);
    }

    /**
     * Get the names of views for a given statement. If the cocoon view exists in the returned
     * collection, the statement can directly branch to the view-handling node.
     *
     * @param role the component role (e.g. <code>Generator.ROLE</code>)
     * @param hint the component hint, i.e. the 'type' attribute
     * @param statement the sitemap statement
     * @return the view names for this statement
     */
    public Collection getViewsForStatement(String role, String hint, Configuration statement) throws Exception {

        String statementLabels = statement.getAttribute("label", null);

        if (this.isBuildingView) {
            // Labels are forbidden inside view definition
            if (statementLabels != null) {
                String msg = "Cannot put a 'label' attribute inside view definition at " + statement.getLocation();
                throw new ConfigurationException(msg);
            }

            // We are currently building a view. Don't recurse !
            return null;
        }

        // Compute the views attached to this component
        Set views = null;

        // Build the set for all labels for this statement
        Set labels = new HashSet();

        // 1 - labels defined on the component
        if (role != null && role.length() > 0) {
            String[] compLabels = this.itsComponentInfo.getLabels(role, hint);
            if (compLabels != null) {
                for (int i = 0; i < compLabels.length; i++) {
                    labels.add(compLabels[i]);
                }
            }
        }

        // 2 - labels defined on this statement
        if (statementLabels != null) {
            labels.addAll(splitLabels(statementLabels));
        }

        // 3 - pseudo-label depending on the role
        if (Generator.ROLE.equals(role)) {
            labels.add("!first!");
        } else if (Serializer.ROLE.equals(role)) {
            labels.add("!last!");
        }

        // Build the set of views attached to these labels
        views = new HashSet();

        // Iterate on all labels for this statement
        Iterator labelIter = labels.iterator();
        while(labelIter.hasNext()) {

            // Iterate on all views for this labek
            Collection coll = (Collection)this.labelViews.get(labelIter.next());
            if (coll != null) {
                Iterator viewIter = coll.iterator();
                while(viewIter.hasNext()) {
                    String viewName = (String)viewIter.next();

                    views.add(viewName);
                }
            }
        }

        // Don't keep empty result
        if (views.size() == 0) {
            views = null;

            if (getLogger().isDebugEnabled()) {
                getLogger().debug(statement.getName() + " has no views at " + statement.getLocation());
            }
        } else {
            if (getLogger().isDebugEnabled()) {
                // Dump matching views
                StringBuffer buf = new StringBuffer(statement.getName() + " will match views [");
                Iterator iter = views.iterator();
                while(iter.hasNext()) {
                    buf.append(iter.next()).append(" ");
                }
                buf.append("] at ").append(statement.getLocation());

                getLogger().debug(buf.toString());
            }
        }

        return views;
    }

    /**
     * Before linking nodes, lookup the view category node used in {@link #getViewNodes(Collection)}.
     */
    protected void linkNodes() throws Exception {
        // Get the views category node
        this.viewsNode = CategoryNodeBuilder.getCategoryNode(this, "views");

        super.linkNodes();
    }

    /**
     * Get the {view name, view node} map for a collection of view names.
     * This allows to resolve view nodes at build time, thus avoiding runtime lookup.
     *
     * @param viewNames the view names
     * @return association of names to views
     */
    public Map getViewNodes(Collection viewNames) throws Exception {
        if (viewNames == null || viewNames.size() == 0) {
            return null;
        }

        if (this.viewsNode == null) {
            return null;
        }

        Map result = new HashMap();

        Iterator iter = viewNames.iterator();
        while(iter.hasNext()) {
            String viewName = (String)iter.next();
            result.put(viewName, viewsNode.getNodeByName(viewName));
        }

        return result;
    }

    /**
     * Extract pipeline-hints from the given statement (if any exist)
     *
     * @param role the component role (e.g. <code>Generator.ROLE</code>)
     * @param hint the component hint, i.e. the 'type' attribute
     * @param statement the sitemap statement
     * @return the hint params <code>Map</code> for this statement, or null
     *         if none exist
     */
    public Map getHintsForStatement(String role, String hint, Configuration statement) throws Exception {
        // This method implemets the hintParam Syntax as follows:
        //     A hints attribute has one or more comma separated hints
        //     hints-attr :: hint [ ',' hint ]*
        //     A hint is a name and an optional (string) value
        //     If there is no value, it is considered as boolean string "true"
        //     hint :: literal [ '=' litteral ]
        //     literal :: <a character string where the chars ',' and '=' are not permitted>
        //
        //  A ConfigurationException is thrown if there is a problem "parsing"
        //  the hint.

        String statementHintParams = statement.getAttribute("pipeline-hints", null);
        String componentHintParams = null;
        String hintParams = null;

        // firstly, determine if any pipeline-hints are defined at the component level
        // if so, inherit these pipeline-hints (these hints can be overriden by local pipeline-hints)
        componentHintParams = this.itsComponentInfo.getPipelineHint(role, hint);

        if (componentHintParams != null) {
            hintParams = componentHintParams;

            if (statementHintParams != null) {
                hintParams = hintParams + "," + statementHintParams;
            }
        } else {
            hintParams = statementHintParams;
        }

        // if there are no pipeline-hints defined then
        // it makes no sense to continue so, return null
        if (hintParams == null) {
            return null;
        }

        Map params = new HashMap();

        RE commaSplit = new RE(COMMA_SPLIT_REGEXP);
        RE equalsSplit = new RE(EQUALS_SPLIT_REGEXP);

        String[]  expressions = commaSplit.split(hintParams.trim());

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("pipeline-hints: (aggregate-hint) " + hintParams);
        }

        for (int i=0; i<expressions.length;i++) {
            String [] nameValuePair = equalsSplit.split(expressions[i]);

            try {
                if (nameValuePair.length < 2) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("pipeline-hints: (name) " + nameValuePair[0]
                                       + "\npipeline-hints: (value) [implicit] true");
                    }

                    params.put(resolve(nameValuePair[0]), resolve("true"));
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("pipeline-hints: (name) " + nameValuePair[0]
                                          + "\npipeline-hints: (value) " + nameValuePair[1]);
                    }

                    params.put(resolve(nameValuePair[0]), resolve(nameValuePair[1]));
                }
            } catch(PatternException pe) {
                String msg = "Invalid pattern '" + hintParams + "' at " + statement.getLocation();
                getLogger().error(msg, pe);
                throw new ConfigurationException(msg, pe);
            }
        }

        return params;
    }
    
    /**
     * Get the mime-type for a component (either a serializer or a reader)
     *
     * @param role the component role (e.g. <code>Serializer.ROLE</code>)
     * @param hint the component hint, i.e. the 'type' attribute
     * @return the mime-type, or <code>null</code> if none was set
     */
    public String getMimeType(String role, String hint) {
        return this.itsComponentInfo.getMimeType(role, hint);
    }

    /**
     * Split a list of space/comma separated labels into a Collection
     *
     * @return the collection of labels (may be empty, nut never null)
     */
    private static final Collection splitLabels(String labels) {
        if (labels == null) {
            return Collections.EMPTY_SET;
        }
        return Arrays.asList(StringUtils.split(labels, ", \t\n\r"));
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        if ( ! (beanFactory instanceof ConfigurableBeanFactory) ) {
            throw new BeanCreationException("Bean factory for tree processor must be an instance of " + ConfigurableBeanFactory.class.getName());            
        }
        this.beanFactory = (ConfigurableBeanFactory)beanFactory;
    }
}
