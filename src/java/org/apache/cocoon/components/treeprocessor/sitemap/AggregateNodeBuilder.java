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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AggregateNodeBuilder.java,v 1.3 2004/03/05 13:02:51 bdelacretaz Exp $
 */

public class AggregateNodeBuilder extends AbstractProcessingNodeBuilder
  implements LinkedProcessingNodeBuilder {

    /** The views for the aggregate element */
    private Collection views;

    /** The built node */
    private AggregateNode node;

    public ProcessingNode buildNode(Configuration config) throws Exception {

        // Get root node data
        this.node = new AggregateNode(
            VariableResolverFactory.getResolver(config.getAttribute("element"), this.manager),
            VariableResolverFactory.getResolver(config.getAttribute("ns", ""), this.manager),
            VariableResolverFactory.getResolver(config.getAttribute("prefix", ""), this.manager)
        );
        this.treeBuilder.setupNode(this.node, config);

        this.views = ((SitemapLanguage)this.treeBuilder).getViewsForStatement("", "", config);
        
        // Bug #7196 : ensure this.views is never null (see continuation of fix below)
        if (this.views == null) {
            this.views = new HashSet();
        }
        
        // The sitemap builder
        SitemapLanguage sitemap = (SitemapLanguage)this.treeBuilder;

        // All parts of the aggregate
        List allParts = new ArrayList();
   
        // For each view that a part matches, the list of all parts that match it
        Map viewParts = new HashMap();
        
        Configuration[] childConfigs = config.getChildren();
        for (int i = 0; i < childConfigs.length; i++) {
            Configuration childConfig = childConfigs[i];

            if (!"part".equals(childConfig.getName())) {
                String msg = "Unknown element '" + childConfig.getName() + " in aggregate ' at " +
                    childConfig.getLocation();
                throw new ConfigurationException(msg);
            }

            checkNamespace(childConfig);

            AggregateNode.Part currentPart = new AggregateNode.Part(
                VariableResolverFactory.getResolver(childConfig.getAttribute("src"), this.manager),
                VariableResolverFactory.getResolver(childConfig.getAttribute("element", ""), this.manager),
                VariableResolverFactory.getResolver(childConfig.getAttribute("ns", ""), this.manager),
                VariableResolverFactory.getResolver(childConfig.getAttribute("prefix", ""), this.manager),
                VariableResolverFactory.getResolver(childConfig.getAttribute("strip-root", "false"), this.manager)
            );
            
            allParts.add(currentPart);
            
            // Get the views for this part
            Collection viewsForPart = sitemap.getViewsForStatement("", "", childConfig);
            
            // Associate this part to all the views it belongs to
            if (viewsForPart != null) {
                
                // Bug #7196 : add part view to aggregate views
                this.views.addAll(viewsForPart);
                
                Iterator iter = viewsForPart.iterator();
                while(iter.hasNext()) {
                    String currentView = (String)iter.next();
                    
                    // Get collection of parts for current view
                    Collection currentViewParts = (Collection)viewParts.get(currentView);
                    if (currentViewParts == null) {
                        // None for now : create the collection
                        currentViewParts = new ArrayList();
                        viewParts.put(currentView, currentViewParts);
                    }
                    
                    // Add the current part to the parts list of the view
                    currentViewParts.add(currentPart);
                }
            }
        }

        if (allParts.size() == 0) {
            String msg = "There must be at least one part in map:aggregate at " + config.getLocation();
            throw new ConfigurationException(msg);
        }

        // Now convert all Collections to Array for faster traversal
        AggregateNode.Part[] allPartsArray = (AggregateNode.Part[])allParts.toArray(
            new AggregateNode.Part[allParts.size()]);
            
        Iterator iter = viewParts.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            
            // Get collection of parts for this entry
            Collection coll = (Collection)entry.getValue();

            // Convert to array and replace the entry value
            entry.setValue(
                coll.toArray(new AggregateNode.Part[coll.size()])
            );
        }

        node.setParts(allPartsArray, viewParts);

        return node;

    }

    public void linkNode() throws Exception {

        // Give the AggregateNode a Node for each view
        SitemapLanguage sitemap = (SitemapLanguage)this.treeBuilder;
        
        this.node.setViewNodes(sitemap.getViewNodes(this.views));
    }
}
