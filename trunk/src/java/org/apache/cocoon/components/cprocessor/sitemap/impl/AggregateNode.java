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
package org.apache.cocoon.components.cprocessor.sitemap.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.sitemap.ViewNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.ContentAggregator;
import org.apache.cocoon.sitemap.PatternException;
import org.apache.commons.lang.StringUtils;

/**
 *
 * View-handling in aggregation :
 * <ul>
 * <li>map:aggregate can have a label, but doesn't match view from-position="first" 
 * like generators
 * </li>
 * <li>each map:part can have a label
 * </li>
 * <li>if at least one of the parts has a label matching the current view, only parts matching
 *     this view are added. Otherwise, all parts are added.
 * </li>
 * </ul>
 * For more info on aggregation and views, see the mail archive
 * <a href="http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=100525751417953">here</a> or
 * <a href="http://marc.theaimsgroup.com/?l=xml-cocoon-dev&m=100517130418424">here</a>.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AggregateNode.java,v 1.4 2004/04/15 13:13:16 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=aggregate-node
 */
public class AggregateNode extends AbstractProcessingNode {

    private VariableResolver m_element;
    private VariableResolver m_nsURI;
    private VariableResolver m_nsPrefix;

    /** All parts */
    private Part[] m_parts;
    private Map m_views;
    private Map m_viewParts;


    public AggregateNode() {
    }

    public void configure(Configuration config) throws ConfigurationException {
        
        try {
            m_element = VariableResolverFactory.getResolver(config.getAttribute("element"), super.m_manager);
            m_nsURI = VariableResolverFactory.getResolver(config.getAttribute("ns", ""), super.m_manager);
            m_nsPrefix = VariableResolverFactory.getResolver(config.getAttribute("prefix", ""), super.m_manager);
        } 
        catch (PatternException e) {
            String msg = "Illegal pattern in aggregate at " + getConfigLocation(config);
            throw new ConfigurationException(msg);
        }
        
        // All parts of the aggregate
        ArrayList parts = new ArrayList();
        
        Configuration[] children = config.getChildren();
        for (int i = 0; i < children.length; i++) {
            Configuration childConfig = children[i];

            if (!"part".equals(childConfig.getName())) {
                String msg = "Unknown element '" + childConfig.getName() 
                    + "' in aggregate at " + getConfigLocation(childConfig);
                throw new ConfigurationException(msg);
            }

            checkNamespace(childConfig);
            Collection labels = splitLabels(childConfig.getAttribute("label",null));

            try {
                AggregateNode.Part part = new AggregateNode.Part(
                    VariableResolverFactory.getResolver(childConfig.getAttribute("src"), super.m_manager),
                    VariableResolverFactory.getResolver(childConfig.getAttribute("element", ""), super.m_manager),
                    VariableResolverFactory.getResolver(childConfig.getAttribute("ns", ""), super.m_manager),
                    VariableResolverFactory.getResolver(childConfig.getAttribute("prefix", ""), super.m_manager),
                    VariableResolverFactory.getResolver(childConfig.getAttribute("strip-root", "false"), super.m_manager),
                    labels
                );
                parts.add(part);
            }
            catch (PatternException e) {
                String msg = "Illegal pattern in aggregate part at " + getConfigLocation(childConfig);
                throw new ConfigurationException(msg);
            }
        }
        m_parts = (Part[]) parts.toArray(new Part[parts.size()]);
        
        if (m_parts.length == 0) {
            String msg = "There must be at least one part in map:aggregate at " 
                + getConfigLocation(config);
            throw new ConfigurationException(msg);
        }
    }
    
    public boolean invoke(Environment env, InvokeContext context) throws Exception {

        Map objectModel = env.getObjectModel();

        // Setup aggregator
        ProcessingPipeline processingPipeline = context.getProcessingPipeline();

        processingPipeline.setGenerator("<aggregator>", null, Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS);

        ContentAggregator aggregator = (ContentAggregator)processingPipeline.getGenerator();
        aggregator.setRootElement(
            m_element.resolve(context, objectModel),
            m_nsURI.resolve(context, objectModel),
            m_nsPrefix.resolve(context, objectModel)
        );

        // Get actual parts, potentially filtered by the view
        Part[] actualParts;

        String cocoonView = env.getView();
        if (cocoonView == null) {
            // Keep all parts
            actualParts = m_parts;

        } else {
            // Are there some parts that match this view ?
            actualParts = getViewParts(cocoonView);

            // If not, keep all parts
            if (actualParts == null) {
                actualParts = m_parts;
            }
        }

        // Add parts
        for (int i = 0; i < actualParts.length; i++) {
            Part part = actualParts[i];
            if (part != null) {
                aggregator.addPart(
                    part.source.resolve(context, objectModel),
                    part.element.resolve(context, objectModel),
                    part.nsURI.resolve(context, objectModel),
                    part.stripRoot.resolve(context, objectModel),
                    part.nsPrefix.resolve(context, objectModel)
                );
            }
        }

        // Bug #7196 : Some parts matched the view: jump to that view
        if (actualParts != m_parts) {
            ProcessingNode viewNode = (ProcessingNode) m_views.get(cocoonView);
            if (viewNode != null) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("Jumping to view '" + cocoonView 
                        + "' from aggregate part at " + getLocation());
                }
                return viewNode.invoke(env,context);
            }
        }

        // Check aggregate-level view
        if (cocoonView != null && m_views != null) {
            ProcessingNode viewNode = (ProcessingNode) m_views.get(cocoonView);
            if (viewNode != null) {
                if (getLogger().isInfoEnabled()) {
                    getLogger().info("Jumping to view '" + cocoonView 
                        + "' from aggregate at " + getLocation());
                }
                return viewNode.invoke(env,context);
            }
        }

        // Return false to continue sitemap invocation
        return false;
    }
    
    private final Part[] getViewParts(String viewName) {
        ViewNode view = getView(viewName);
        if (view == null) {
            return null;
        }
        Part[] parts = (Part[]) m_viewParts.get(viewName);
        if (parts == null) {
            ArrayList list = new ArrayList();
            String label = view.getLabel();
            for (int i = 0; i < m_parts.length; i++) {
                if (m_parts[i].labels.contains(label)) {
                    list.add(m_parts[i]);
                }
            }
            if (list.size() > 0) {
                parts = (Part[]) list.toArray(new Part[list.size()]);
                m_viewParts.put(viewName,parts);
                m_views.put(viewName,view);
            }
            else {
                // TODO: record no parts for view
            }
        }
        return parts;
    }
    
    private final ViewNode getView(String name) {
        ViewNode view = (ViewNode) m_views.get(name);
        if (view == null) {
            try {
                view = (ViewNode) super.m_manager.lookup(
                    ViewNode.ROLE + "/v-" + name);
            }
            catch (ServiceException e) {
                // TODO: record no such view
                if (getLogger().isDebugEnabled()) {
                    String msg = "No such view: " + name;
                    getLogger().debug(msg);
                }
            }
        }
        return view;
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
    
    private static class Part {
       private Part(
            VariableResolver source,
            VariableResolver element,
            VariableResolver nsURI,
            VariableResolver nsPrefix,
            VariableResolver stripRoot,
            Collection labels)
       {
            this.source = source;
            this.element = element;
            this.nsURI = nsURI;
            this.nsPrefix = nsPrefix;
            this.stripRoot = stripRoot;
            this.labels = labels;
        }

        private final VariableResolver source;
        private final VariableResolver element;
        private final VariableResolver nsURI;
        private final VariableResolver nsPrefix;
        private final VariableResolver stripRoot;
        private final Collection labels;
        
    }
}
