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

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ActionSetNodeBuilder.java,v 1.5 2004/03/05 13:02:51 bdelacretaz Exp $
 */

public class ActionSetNodeBuilder extends AbstractParentProcessingNodeBuilder implements ThreadSafe {
    
    /** The TreeBuilder attribute indicating that an ActionSet is being built */
    public static final String IN_ACTION_SET = ActionSetNodeBuilder.class.getName() + "/inActionSet";

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String actionSetName = config.getAttribute("name");

        Configuration[] childrenConfig = config.getChildren();
        // Inform other builders that we're in an action-set
        this.treeBuilder.setAttribute(IN_ACTION_SET, Boolean.TRUE);
        
        // Get the child actions
        ProcessingNode[] nodes = this.buildChildNodes(config);
        
        // And get their names
        String[] actions = new String[nodes.length];
        for (int i = 0; i < childrenConfig.length; i++) {
            Configuration childConfig = childrenConfig[i];
            String name = childConfig.getName();

            if ("act".equals(name)) {
                actions[i] = childConfig.getAttribute("action", null);
            } else {
                // Unknown element
                String msg = "Unknown element " + name + " in action-set at " + childConfig.getLocation();
                throw new ConfigurationException(msg);
            }            
        }

        ActionSetNode node = new ActionSetNode(actionSetName, nodes, actions);
        this.treeBuilder.setupNode(node, config);

        // Inform other builders that we're no more in an action-set
        this.treeBuilder.setAttribute(IN_ACTION_SET, null);

        return node;
    }
}
