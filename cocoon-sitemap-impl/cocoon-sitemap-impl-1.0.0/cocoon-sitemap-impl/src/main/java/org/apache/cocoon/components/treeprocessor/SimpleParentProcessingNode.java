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

import org.apache.cocoon.environment.Environment;

/**
 * @version $Id$
 */
public abstract class SimpleParentProcessingNode extends AbstractParentProcessingNode {

    /** The children of this node */
    protected ProcessingNode[] children;


    public SimpleParentProcessingNode(String type) {
        super(type);
    }
    
    public void setChildren(ProcessingNode[] children) {
        this.children = children;
    }

    /**
     * Boolean method with returns true if this Node has children
     * and false otherwise.
     *
     * @return boolean true if has children.
     */
    public boolean hasChildren() {
        return this.children != null && this.children.length > 0;
    }

    /**
     * Define common invoke behavior here
     */
    public boolean invoke(Environment env, InvokeContext context) throws Exception {
        // Inform the pipeline (if available) that we have come across
        // a possible branch point
        if (context.hasPipeline() && hasChildren()) {
            context.getProcessingPipeline().informBranchPoint();
        }

        // processing not yet complete, so return false
        return false;
    }
}
