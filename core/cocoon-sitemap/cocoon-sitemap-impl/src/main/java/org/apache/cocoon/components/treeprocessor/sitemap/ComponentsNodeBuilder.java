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
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
//import org.apache.cocoon.components.treeprocessor.ProcessingNodeBuilder;

/**
 * Handles &lt;map:components&gt;. It doesn't actually create a <code>ProcessingNode</code>.
 *
 * @version $Id$
 */
public class ComponentsNodeBuilder extends AbstractProcessingNodeBuilder {

    private static String[] VPCTypes =
    {"generators", "transformers", "serializers", "readers"};

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {
        // Handle the VPCs
/*
        for (int i = 0; i < VPCTypes.length; i++) {
            Configuration child = config.getChild(VPCTypes[i], false);
            if (child != null) {
                ProcessingNodeBuilder childBuilder =
                    this.treeBuilder.createNodeBuilder(child);
                childBuilder.buildNode(child);
            }
        }
*/
        return null;
    }
}
