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

/**
 * A <code>ProcessingNode</code> builder.
 * <p>
 * Lifecycle information : a <code>TreeBuilder</code> can be recycled
 * and used to build several <code>Processor</code>s, each one defining
 * a different <code>ServiceManager</code>.
 * <p>
 *
 * @version $Id$
 */
public interface ProcessingNodeBuilder {

    /**
     * Set the builder for which we are building.
     */
    void setBuilder(TreeBuilder builder);

    /**
     * Build the {@link ProcessingNode} and its children from the given
     * <code>Configuration</code>, and optionnaly register it in the tree builder
     * for lookup by other <code>LinkedProcessingNodeBuilder</code>s.
     */
    ProcessingNode buildNode(Configuration config) throws Exception;
}
