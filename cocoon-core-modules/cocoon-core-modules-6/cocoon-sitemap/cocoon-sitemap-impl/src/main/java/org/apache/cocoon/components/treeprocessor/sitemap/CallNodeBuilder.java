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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.ArrayList;
import java.util.List;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.flow.Interpreter;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.CategoryNode;
import org.apache.cocoon.components.treeprocessor.CategoryNodeBuilder;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;

/**
 *
 * @version $Id$
 */
public class CallNodeBuilder extends AbstractProcessingNodeBuilder
                             implements LinkedProcessingNodeBuilder {

    protected ProcessingNode node;

    protected String resourceName;

    protected String functionName;

    protected String continuationId;

    /**
     * @see org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder#buildNode(org.apache.avalon.framework.configuration.Configuration)
     */
    public ProcessingNode buildNode(Configuration config)
    throws Exception {
        resourceName = config.getAttribute("resource", null);
        functionName = config.getAttribute("function", null);
        continuationId = config.getAttribute("continuation", null);

        if (resourceName == null) {
            // Building a CallFunction node
            if (functionName == null && continuationId == null) {
                throw new ConfigurationException(
                    "<map:call> must have either a 'resource', 'function' or 'continuation' attribute, at "
                    + config.getLocation()
                );
            }

            // Build the ordered list of parameter names
            // FIXME(SW): remove this in the future (see comment in FlowNode)
            List argumentNames = new ArrayList();
            Configuration[] params = config.getChildren("parameter");
            for (int i = 0; i < params.length; i++) {
                argumentNames.add(params[i].getAttribute("name"));
            }

            node = new CallFunctionNode(VariableResolverFactory.getResolver(
                functionName, this.manager),
                VariableResolverFactory.getResolver(continuationId, this.manager),
                (String[]) argumentNames.toArray(new String[argumentNames.size()])
            );

        } else {
            // Building a Call(Resource)Node
            if (functionName != null || continuationId != null) {
                throw new ConfigurationException(
                    "<map:call> cannot have both a 'resource' and a 'function' or 'continuation' attribute, at "
                    + config.getLocation());
            }
            node = new CallNode();
        }

        this.treeBuilder.setupNode(this.node, config);
        if (node instanceof Configurable) {
            ((Configurable) this.node).configure(config);
        }

        return this.node;
    }

    /**
     * @see org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder#linkNode()
     */
    public void linkNode() throws Exception {
        if (resourceName != null) {
            // We have a <map:call resource="..."/>
            CategoryNode resources = CategoryNodeBuilder.getCategoryNode(treeBuilder, "resources");

            if (resources == null)
                throw new ConfigurationException(
                    "This sitemap contains no resources. Cannot call at " + node.getLocation());

            ((CallNode) this.node).setResource(
                resources,
                VariableResolverFactory.getResolver(this.resourceName, this.manager)
            );
        } else {
            // We have a <map:call> with either "function" or
            // "continuation", or both specified

            // Check to see if a flow has been defined in this sitemap
            FlowNode flow = (FlowNode) treeBuilder.getRegisteredNode("flow");
            if (flow == null) {
                throw new ConfigurationException(
                    "This sitemap contains no control flows defined, cannot call at "
                    + node.getLocation()
                    + ". Define a control flow using <map:flow>, with embedded <map:script> elements.");
            }

            // Get the Interpreter instance and set it up in the
            // CallFunctionNode function
            Interpreter interpreter = flow.getInterpreter();
            ((CallFunctionNode) node).setInterpreter(interpreter);
        }
    }
}