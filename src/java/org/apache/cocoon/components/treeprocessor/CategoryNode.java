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

import java.util.HashMap;
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;

/**
 * A generic container node that just invokes its children.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: CategoryNode.java,v 1.4 2004/06/09 11:59:23 cziegeler Exp $
 */

public final class CategoryNode extends AbstractParentProcessingNode {

    public CategoryNode(String type) {
        super(type);
    }
    
    /** The name of this category */
    private String categoryName;

    /** The Map of named nodes in this category */
    private Map nodes;

    public void setCategory(String categoryName, Map nodes) {
        this.categoryName = categoryName;
        this.nodes = (nodes != null) ? nodes : new HashMap(0);
    }

    public final boolean invoke(Environment env, InvokeContext context) throws Exception {
        String msg = "Cannot invoke " + this.categoryName + " at " + getLocation();
        throw new ProcessingException(msg);
    }

    public final ProcessingNode getNodeByName(String name) throws Exception {
        ProcessingNode node = (ProcessingNode)nodes.get(name);
        if (node == null) {
            String msg = "Unknown " + this.categoryName + " named '" + name + "' at " + getLocation();
            throw new ProcessingException(msg);
        }

        return node;
    }

    public final boolean invokeByName(String name, Environment env, InvokeContext context)
      throws Exception {

        return getNodeByName(name).invoke(env, context);
    }
}
