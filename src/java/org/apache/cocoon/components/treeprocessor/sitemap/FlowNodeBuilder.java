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

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;

/**
 * Builder of a {@link FlowNode} instance, corresponding to a
 * <code>&lt;map:flow&gt;</code> element in the sitemap.
 *
 * @author <a href="mailto:ovidiu@apache.org">Ovidiu Predescu</a>
 * @since September 13, 2002
 * @version CVS $Id$
 */
public class FlowNodeBuilder extends AbstractParentProcessingNodeBuilder {

    public ProcessingNode buildNode(Configuration config)
    throws Exception {
        String language = config.getAttribute("language", "javascript");
        FlowNode node = new FlowNode(language);

        if ( !this.treeBuilder.registerNode("flow", node) ) {
            throw new ConfigurationException("Only one <map:flow> is allowed in a sitemap. Another one is declared at " +
                    config.getLocation());
        }
        this.treeBuilder.setupNode(node, config);

        buildChildNodesList(config);

        return node;
    }
}
