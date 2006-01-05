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
package org.apache.cocoon.components.treeprocessor.sitemap;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;

import org.apache.cocoon.components.treeprocessor.ContainerNode;
import org.apache.cocoon.components.treeprocessor.ContainerNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.generation.VirtualPipelineGenerator;
import org.apache.cocoon.reading.VirtualPipelineReader;
import org.apache.cocoon.serialization.VirtualPipelineSerializer;
import org.apache.cocoon.transformation.VirtualPipelineTransformer;

/**
 * Handles a set of virtual sitemap components.
 *
 * @version $Id$
 */
public class VPCsNodeBuilder extends ContainerNodeBuilder {

    /**
     * Checks if a child element is a VPC, and if not throws a <code>ConfigurationException</code>.
     *
     * @param child the child configuration to check.
     * @return <code>true</code> if this child should be considered or <code>false</code>
     *         if it should be ignored.
     * @throws ConfigurationException if this child isn't allowed.
     */
    protected boolean isChild(Configuration child) throws ConfigurationException {

        checkNamespace(child);

        String clazz = child.getAttribute("src");
        return VirtualPipelineGenerator.class.getName().equals(clazz)
            || VirtualPipelineSerializer.class.getName().equals(clazz)
            || VirtualPipelineTransformer.class.getName().equals(clazz)
            || VirtualPipelineReader.class.getName().equals(clazz);
    }

    protected void setupNode(ContainerNode node, Configuration config)throws Exception {

        this.treeBuilder.setupNode(node, config);

        ProcessingNode[] children = buildChildNodes(config);

        node.setChildren(children);
    }
}
