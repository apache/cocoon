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

import org.apache.cocoon.environment.Environment;

import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: AbstractParentProcessingNode.java,v 1.4 2004/06/09 11:59:23 cziegeler Exp $
 */

public abstract class AbstractParentProcessingNode extends AbstractProcessingNode {

    public AbstractParentProcessingNode(String type) {
        super(type);
    }
    
    public AbstractParentProcessingNode() {
    }

    /**
     * Invoke all nodes of a node array in order, until one succeeds.
     *
     * @param currentMap the <code>Map<code> of parameters produced by this node,
     *            which is added to <code>listOfMap</code>.
     */

    protected final boolean invokeNodes(
        ProcessingNode[] nodes,
        Environment env,
        InvokeContext context,
        String currentName,
        Map currentMap)
      throws Exception {

        context.pushMap(currentName,currentMap);

        try {
            for (int i = 0; i < nodes.length; i++) {
                if (nodes[i].invoke(env, context)) {
                    // Success
                    return true;
                }
            }
        }
        finally {
            // No success
            context.popMap();
        }
        return false;
    }

    /**
     * Invoke all nodes of a node array in order, until one succeeds.
     */
    protected final boolean invokeNodes (
        ProcessingNode[] nodes,
        Environment env,
        InvokeContext context)
      throws Exception {

        for (int i = 0; i < nodes.length; i++) {
            if (nodes[i].invoke(env, context)) {
                return true;
            }
        }
        return false;
    }
}
