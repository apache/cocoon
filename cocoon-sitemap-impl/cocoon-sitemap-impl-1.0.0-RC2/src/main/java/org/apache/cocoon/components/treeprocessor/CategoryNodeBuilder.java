/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Builds a generic container node.
 *
 * @version $Id$
 */
public class CategoryNodeBuilder extends AbstractParentProcessingNodeBuilder
  implements ThreadSafe {

    // Prefix used for registering as a TreeBuilder attribute
    private static String PREFIX = CategoryNodeBuilder.class.getName() + "/";

    protected String name;

    /**
     * The category name is the value of the "category-name" child, or if not
     * present, the name of the configuration element.
     */
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        this.name = config.getChild("category-name").getValue(config.getAttribute("name"));
    }

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {

        CategoryNode node = new CategoryNode(null);
        this.treeBuilder.setupNode(node, config);

        // Get all children and associate them to their name
        Map category = new HashMap();

        List children = buildChildNodesList(config);
        Iterator iter = children.iterator();
        while(iter.hasNext()) {
            NamedProcessingNode child = (NamedProcessingNode)iter.next();
            category.put(child.getName(), child);
        }

        node.setCategory(this.name, category);

        // Register node to allow lookup by other nodes
        if ( !this.treeBuilder.registerNode(PREFIX + this.name, node) ) {
            throw new ConfigurationException("Only one <map:" + this.name +
                    "> is allowed in a sitemap. Another one is declared at " +
                    config.getLocation());
        }

        return node;
    }

    public static CategoryNode getCategoryNode(TreeBuilder builder, String categoryName) {
        return (CategoryNode)builder.getRegisteredNode(PREFIX + categoryName);
    }

    public static ProcessingNode getNamedNode(TreeBuilder builder, String categoryName, String nodeName)
      throws Exception {
        CategoryNode category = getCategoryNode(builder, categoryName);

        return category.getNodeByName(nodeName);
    }
}
