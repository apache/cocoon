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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder;

/**
 * Builds all nodes below the top-level &lt;sitemap&gt; element, and returns the
 * &lt;pipelines&gt; node. There is no node for &gt;sitemap&lt; since no processing
 * occurs at this level.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SitemapNodeBuilder.java,v 1.3 2004/03/05 13:02:52 bdelacretaz Exp $
 */

public class SitemapNodeBuilder extends AbstractParentProcessingNodeBuilder implements ThreadSafe {
    
    // Name of children that have to be built in a particular order.
    // For example, views have to be built before resources and both before pipelines.
    private static final String[] orderedNames = { "components", "views", "resources" };

    public ProcessingNode buildNode(Configuration config) throws Exception {
        
        // Start by explicitely ordered children
        for (int i = 0; i < orderedNames.length; i++) {
            Configuration childConfig = config.getChild(orderedNames[i], false);
            if (childConfig != null) {
                ProcessingNodeBuilder builder = this.treeBuilder.createNodeBuilder(childConfig);
                // Don't build them since "pipelines" is not present in this list
                builder.buildNode(childConfig);
            }
        }
        
        ProcessingNode pipelines = null;

        // Now build all those that have no particular order
        Configuration[] childConfigs = config.getChildren();
        
        loop: for (int i = 0; i < childConfigs.length; i++) {
            
            Configuration childConfig = childConfigs[i];
            if (isChild(childConfig)) {
                // Is it in the ordered list ?
                for (int j = 0; j < orderedNames.length; j++) {
                    if (orderedNames[j].equals(childConfig.getName())) {
                        // yep : already built above
                        continue loop;
                    }
                }
                
                ProcessingNodeBuilder builder = this.treeBuilder.createNodeBuilder(childConfig);
                ProcessingNode node = builder.buildNode(childConfig);
                if (node instanceof PipelinesNode) {
                    if (pipelines != null) {
                        String msg = "Only one 'pipelines' is allowed, at " + childConfig.getLocation();
                        throw new ConfigurationException(msg);
                    }
                    pipelines = node;
                }
            }
        }

        if (pipelines == null) {
            String msg = "Invalid sitemap : there must be a 'pipelines' at " + config.getLocation();
            throw new ConfigurationException(msg);
        }

        return pipelines;
    }
}
