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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.cocoon.components.container.CocoonComponentManager;
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.CategoryNodeBuilder;
import org.apache.cocoon.components.treeprocessor.DefaultTreeBuilder;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.cocoon.sitemap.SitemapComponentSelector;
import org.apache.cocoon.util.StringUtils;
import org.apache.regexp.RE;

/**
 * The tree builder for the sitemap language.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SitemapLanguage.java,v 1.12 2004/06/11 20:03:35 vgritsenko Exp $
 */
public class SitemapLanguage extends DefaultTreeBuilder {

    // Regexp's for splitting expressions
    private static final String COMMA_SPLIT_REGEXP = "[\\s]*,[\\s]*";
    private static final String EQUALS_SPLIT_REGEXP = "[\\s]*=[\\s]*";

    /**
     * Build a component manager with the contents of the &lt;map:components&gt; element of
     * the tree.
     */
    protected ComponentManager createComponentManager(Configuration tree) throws Exception {

        // Get the map:component node
        // Don't check namespace here : this will be done by node builders
        Configuration config = tree.getChild("components", false);

        if (config == null) {
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Sitemap has no components definition at " + tree.getLocation());
            }
            config = new DefaultConfiguration("", "");
        }

        final CocoonComponentManager manager = new CocoonComponentManager(this.parentManager);

        manager.enableLogging(getLogger());

        final LoggerManager loggerManager = (LoggerManager) this.parentManager.lookup(LoggerManager.ROLE);
        manager.setLoggerManager(loggerManager);

        if (null != this.context ) {
            manager.contextualize(this.context);
        }

        if (null != this.roleManager) {
            manager.setRoleManager(this.roleManager);
        }

        manager.configure(config);
        manager.initialize();

        return manager;
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
            SitemapComponentSelector selector = null;
            try {
                selector = (SitemapComponentSelector)this.manager.lookup(role + "Selector");
                String[] compLabels = selector.getLabels(hint);
                if (compLabels != null) {
                    for (int i = 0; i < compLabels.length; i++) {
                        labels.add(compLabels[i]);
                    }
                }
            } catch(Exception e) {
                // Ignore (no selector for this role)
                getLogger().warn("No selector for role " + role);
            } finally {
                this.manager.release( selector );
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
        SitemapComponentSelector selector = null;
        try {
            selector = (SitemapComponentSelector)this.manager.lookup(role + "Selector");
            componentHintParams = selector.getPipelineHint(hint);
        } catch (Exception ex) {
            if (getLogger().isWarnEnabled()) {
                getLogger().warn("pipeline-hints: Component Exception: could not " +
                             "check for component level hints " + ex);
            }
        } finally {
            this.manager.release(selector);
        }

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

                    params.put( VariableResolverFactory.getResolver(nameValuePair[0], this.manager),
                                VariableResolverFactory.getResolver("true", this.manager));
                } else {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("pipeline-hints: (name) " + nameValuePair[0]
                                          + "\npipeline-hints: (value) " + nameValuePair[1]);
                    }

                    params.put( VariableResolverFactory.getResolver(nameValuePair[0], this.manager),
                                VariableResolverFactory.getResolver(nameValuePair[1], this.manager));
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
     * Split a list of space/comma separated labels into a Collection
     *
     * @return the collection of labels (may be empty, nut never null)
     */
    private static final Collection splitLabels(String labels) {
        if (labels == null) {
            return Collections.EMPTY_SET;
        } else {
            return Arrays.asList(StringUtils.split(labels, ", \t\n\r"));
        }
    }
}
