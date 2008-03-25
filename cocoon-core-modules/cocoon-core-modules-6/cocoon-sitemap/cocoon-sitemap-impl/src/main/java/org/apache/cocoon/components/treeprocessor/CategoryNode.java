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
import java.util.Map;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.Environment;

/**
 * A generic container node that just invokes its children.
 *
 * @version $Id$
 */
public final class CategoryNode extends AbstractParentProcessingNode {

    /** The name of this category */
    private String categoryName;

    /** The Map of named nodes in this category */
    private Map nodes;


    public CategoryNode(String type) {
        super(type);
    }

    public void setCategory(String categoryName, Map nodes) {
        this.categoryName = categoryName;
        this.nodes = nodes != null ? nodes : new HashMap(0);
    }

    public boolean invoke(Environment env, InvokeContext context) throws Exception {
        throw new ProcessingException("Cannot invoke " + this.categoryName, getLocation());
    }

    public ProcessingNode getNodeByName(String name) throws Exception {
        ProcessingNode node = (ProcessingNode)nodes.get(name);
        if (node == null) {
            throw new ProcessingException("Unknown " + this.categoryName + " named '" + name + "'",
                                          getLocation());
        }

        return node;
    }

    public boolean invokeByName(String name, Environment env, InvokeContext context)
    throws Exception {

        return getNodeByName(name).invoke(env, context);
    }
}
