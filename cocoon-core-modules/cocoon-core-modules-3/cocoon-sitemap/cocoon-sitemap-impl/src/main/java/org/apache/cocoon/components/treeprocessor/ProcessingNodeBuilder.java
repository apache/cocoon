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

/**
 * A <code>ProcessingNode</code> builder.
 * <p>
 * A processing node builder is used to create the processing statements
 * for a sitemap.
 * A node builder can either be implemented as a singleton, using
 * the ThreadSafe marker interface, or each time a builder is required
 * a new instance is created.
 * All builders are managed by the {@link NodeBuilderSelector}.
 * A node builder can implement the following marker interfaces from
 * Avalon: LogEnabled, Contextualizable, Initializable
 * and Configurable. Other marker interfaces, like Recyclable, Poolable
 * or Disposable are not supported!
 * If the builder needs a service manager it can fetch this one from
 * the tree builder.
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
