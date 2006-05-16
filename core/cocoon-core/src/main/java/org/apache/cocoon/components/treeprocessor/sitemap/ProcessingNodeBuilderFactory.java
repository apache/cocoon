/*
 * Copyright 2006 The Apache Software Foundation.
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

import java.util.HashMap;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.TreeBuilder;

/**
 * A factory that creates <code>ProcessingNodeBuilder</code> instances.
 * 
 * @version $Id$
 */
public class ProcessingNodeBuilderFactory {

    private TreeBuilder treeBuilder;
    private Map builderNodesMap = new HashMap();
    private Logger logger;

    public ProcessingNodeBuilderFactory(TreeBuilder treeBuilder,
            Configuration nodeBuildersConfiguration, Logger logger)
            throws ConfigurationException {
        this.logger = logger;
        this.treeBuilder = treeBuilder;
        createBuilderNodes(nodeBuildersConfiguration);
    }

    public ProcessingNodeBuilder createBuilder(String nodeName) {
        BuilderNode builderNode = (BuilderNode) this.builderNodesMap.get(nodeName);

        // if its a ThreadSafe builder and already instanciated, use it again
        if (builderNode.builderInstance != null) {
            return builderNode.builderInstance;
        }

        // if it's not a ThreadSafe builder or if it hasn't been created
        ProcessingNodeBuilder processingNodeBuilder = null;
        try {
            processingNodeBuilder = (ProcessingNodeBuilder) Class.forName(
                    builderNode.builderClass).newInstance();
            if (processingNodeBuilder instanceof LogEnabled) {
                ((LogEnabled) processingNodeBuilder).enableLogging(this.logger);
            }            
            if (processingNodeBuilder instanceof Configurable) {
                ((Configurable) processingNodeBuilder).configure(builderNode.configuration);
            }
            if (processingNodeBuilder instanceof ThreadSafe) {
                builderNode.builderInstance = processingNodeBuilder;
            }
            processingNodeBuilder.setBuilder(this.treeBuilder);
        } catch (Exception e) {
            new RuntimeException(
                    "Can't create ProcessingNodeBuilder for element '" + nodeName
                            + "'", e);
        }
        return processingNodeBuilder;
    }

    private void createBuilderNodes(Configuration nodeBuildersConfiguration)
            throws ConfigurationException {
        Configuration[] nodes = nodeBuildersConfiguration.getChild("nodes").getChildren();
        for (int i = 0; i < nodes.length; i++) {
            BuilderNode builderNode = new BuilderNode();
            builderNode.name = nodes[i].getAttribute("name");
            builderNode.builderClass = nodes[i].getAttribute("builder");
            builderNode.configuration = nodes[i];
            builderNodesMap.put(builderNode.name, builderNode);
        }
    }

    private static class BuilderNode {
        String name;
        String builderClass;
        Configuration configuration;
        ProcessingNodeBuilder builderInstance;
    }

}
