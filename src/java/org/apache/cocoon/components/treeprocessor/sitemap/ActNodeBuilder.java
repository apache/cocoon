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
import org.apache.cocoon.acting.Action;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.CategoryNodeBuilder;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ActNodeBuilder.java,v 1.5 2004/03/05 13:02:51 bdelacretaz Exp $
 */
public class ActNodeBuilder extends AbstractParentProcessingNodeBuilder
                            implements LinkedProcessingNodeBuilder {

    private ActSetNode  actSetNode;
    private String      actSetName;

    public ProcessingNode buildNode(Configuration config) throws Exception {
        
        boolean inActionSet = this.treeBuilder.getAttribute(ActionSetNodeBuilder.IN_ACTION_SET) != null;

        // Is it an action-set call ?
        this.actSetName = config.getAttribute("set", null);
        if (actSetName == null) {
            
            if (inActionSet) {
                // Check that children are only parameters or actions
                Configuration children[] = config.getChildren();
                for (int i = 0; i < children.length; i++) {
                    String name = children[i].getName();
                    if (!"act".equals(name) && !"parameter".equals(name)) {
                        throw new ConfigurationException("An action set can only contain actions and not '" 
                            + name + "' at " + children[i].getLocation());
                    }
                }
            }

            String name = config.getAttribute("name", null);
            String source = config.getAttribute("src", null);
            String type = this.treeBuilder.getTypeForStatement(config, Action.ROLE + "Selector");

            ActTypeNode actTypeNode = new ActTypeNode(
                type,
                VariableResolverFactory.getResolver(source, this.manager),
                name,
                inActionSet
            );
            this.treeBuilder.setupNode(actTypeNode, config);

            actTypeNode.setChildren(buildChildNodes(config));

            return actTypeNode;

        } else {

            if (inActionSet) {
                throw new ConfigurationException("Cannot call an action set from an action set at " + config.getLocation());
            }

            // Action set call
            if (config.getAttribute("src", null) != null) {
                getLogger().warn("The 'src' attribute is ignored for action-set call at " + config.getLocation());
            }
            this.actSetNode = new ActSetNode();
            this.treeBuilder.setupNode(this.actSetNode, config);

            this.actSetNode.setChildren(buildChildNodes(config));

            return this.actSetNode;
        }
    }

    public void linkNode() throws Exception {

        if (this.actSetNode != null) {
            // Link action-set call to the action set
            CategoryNode actionSets = CategoryNodeBuilder.getCategoryNode(this.treeBuilder, "action-sets");

            if (actionSets == null)
                throw new ConfigurationException("This sitemap contains no action sets. Cannot call at " + actSetNode.getLocation());

            ActionSetNode actionSetNode = (ActionSetNode)actionSets.getNodeByName(this.actSetName);

            this.actSetNode.setActionSet(actionSetNode);
        }
    }
}
