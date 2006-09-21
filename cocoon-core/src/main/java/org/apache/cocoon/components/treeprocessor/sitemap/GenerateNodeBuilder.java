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
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.generation.Generator;

import java.util.Collection;
import java.util.Map;

/**
 *
 * @version $Id$
 */
public class GenerateNodeBuilder extends AbstractProcessingNodeBuilder
  implements LinkedProcessingNodeBuilder {

    private GenerateNode node;

    private Collection views;
    private Map  pipelineHints;

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String type = this.treeBuilder.getTypeForStatement(config, Generator.ROLE);

        this.views = ((SitemapLanguage)this.treeBuilder).getViewsForStatement(Generator.ROLE, type, config);
        this.pipelineHints = ((SitemapLanguage)this.treeBuilder).getHintsForStatement(Generator.ROLE, type, config);

        this.node = new GenerateNode(
            type,
            VariableResolverFactory.getResolver(config.getAttribute("src", null), this.manager)
        );
        this.node.setPipelineHints(this.pipelineHints);
        return this.treeBuilder.setupNode(this.node, config);
    }

    public void linkNode() throws Exception {
        this.node.setViews(
            ((SitemapLanguage)this.treeBuilder).getViewNodes(this.views)
        );
    }
}
