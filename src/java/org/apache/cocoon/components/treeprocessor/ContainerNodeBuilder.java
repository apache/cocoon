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
package org.apache.cocoon.components.treeprocessor;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.thread.ThreadSafe;

/**
 * Builds a generic container node.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: ContainerNodeBuilder.java,v 1.2 2004/03/05 13:02:51 bdelacretaz Exp $
 */

public class ContainerNodeBuilder extends AbstractParentProcessingNodeBuilder implements ThreadSafe {

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {

        ContainerNode node = new ContainerNode();
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
