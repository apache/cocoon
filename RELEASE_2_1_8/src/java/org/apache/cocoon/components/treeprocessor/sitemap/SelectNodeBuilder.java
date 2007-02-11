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
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.selection.Selector;
import org.apache.cocoon.selection.SwitchSelector;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SelectNodeBuilder.java,v 1.2 2004/03/05 13:02:52 bdelacretaz Exp $
 */

public class SelectNodeBuilder extends AbstractParentProcessingNodeBuilder {

    private static final String SELECTOR_ROLE = Selector.ROLE + "Selector";

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String type = this.treeBuilder.getTypeForStatement(config, SELECTOR_ROLE);

        // Lists of ProcessingNode[] and test resolvers for each "when"
        List whenChildren = new ArrayList();
        List whenTests = new ArrayList();

        // Nodes for otherwise (if any)
        ProcessingNode[] otherwiseNodes = null;

        Configuration[] childrenConfig = config.getChildren();
        for (int i = 0; i < childrenConfig.length; i++) {

            Configuration childConfig = childrenConfig[i];
            String name = childConfig.getName();

            if ("when".equals(name)) {

                checkNamespace(childConfig);
                whenTests.add(
                    VariableResolverFactory.getResolver(childConfig.getAttribute("test"), this.manager)
                );
                whenChildren.add(buildChildNodes(childConfig));

            } else if ("otherwise".equals(name)) {

                checkNamespace(childConfig);
                if (otherwiseNodes != null) {
                    String msg = "Duplicate " + name + " (only one is allowed) at " + childConfig.getLocation();
                    getLogger().error(msg);
                    throw new ConfigurationException(msg);
                }

                otherwiseNodes = buildChildNodes(childConfig);

            } else if (isParameter(childConfig)) {
                // ignore it. It is handled automatically in setupNode()

            } else {
                // Unknown element
                String msg = "Unknown element '" + name + "' in select at " + childConfig.getLocation();
                throw new ConfigurationException(msg);
            }
        }

        ProcessingNode[][] whenChildrenNodes = (ProcessingNode[][])whenChildren.toArray(new ProcessingNode[0][0]);
        VariableResolver[] whenResolvers = (VariableResolver[])whenTests.toArray(new VariableResolver[whenTests.size()]);

        // Get the type and class for this selector
        ComponentsSelector compSelector = (ComponentsSelector)this.manager.lookup(SELECTOR_ROLE);

        Class clazz = null;
        try {
            // Find selector class
            Selector selector = (Selector)compSelector.select(type);
            try {
                clazz = selector.getClass();
            } finally {
                compSelector.release(selector);
            }
        } finally {
            this.manager.release(compSelector);
        }

        if (SwitchSelector.class.isAssignableFrom(clazz)) {
            SwitchSelectNode node = new SwitchSelectNode(type);
            this.treeBuilder.setupNode(node, config);
            node.setCases(whenChildrenNodes, whenResolvers, otherwiseNodes);
            return node;
        } else {
            SelectNode node = new SelectNode(type);
            this.treeBuilder.setupNode(node, config);
            node.setCases(whenChildrenNodes, whenResolvers, otherwiseNodes);
            return node;
        }
    }
}
