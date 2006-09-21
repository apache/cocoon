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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Builds a generic container node.
 *
 * @version $Id$
 */
public class ContainerNodeBuilder extends AbstractParentProcessingNodeBuilder implements ThreadSafe {

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {

        ContainerNode node = new ContainerNode(null);
        setupNode(node, config);

        return node;
    }

    protected void setupNode(ContainerNode node, Configuration config)throws Exception {

        this.treeBuilder.setupNode(node, config);

        ProcessingNode[] children = buildChildNodes(config);
        if (children.length == 0) {
            String msg = "There must be at least one child at " + config.getLocation();
            throw new ConfigurationException(msg);
        }

        node.setChildren(children);
    }
}
