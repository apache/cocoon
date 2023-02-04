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

import java.util.Collection;
import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.serialization.Serializer;

/**
 *
 * @version $Id$
 */
public class SerializeNodeBuilder extends AbstractProcessingNodeBuilder
  implements LinkedProcessingNodeBuilder {

    private SerializeNode node;

    private Collection views;
    private Map  pipelineHints;

    /** Serializers can have parameters -- return <code>true</code> */
    protected boolean hasParameters() {
        return true;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {

        String type = this.treeBuilder.getTypeForStatement(config, Serializer.ROLE);
        
        SitemapLanguage sitemapBuilder = (SitemapLanguage)this.treeBuilder;

        String mimeType = config.getAttribute("mime-type", null);
        if (mimeType == null) {
            mimeType = sitemapBuilder.getMimeType(Serializer.ROLE, type);
        }

        this.views = sitemapBuilder.getViewsForStatement(Serializer.ROLE, type, config);
        this.pipelineHints = sitemapBuilder.getHintsForStatement(Serializer.ROLE, type, config);

        this.node = new SerializeNode(
            type,
            VariableResolverFactory.getResolver(config.getAttribute("src", null), this.manager),
            VariableResolverFactory.getResolver(mimeType, this.manager),
            VariableResolverFactory.getResolver(config.getAttribute("status-code", null), this.manager)
        );
        this.node.setPipelineHints(this.pipelineHints);
        return this.treeBuilder.setupNode(node, config);
    }

    public void linkNode() throws Exception {
        this.node.setViews(
            ((SitemapLanguage)this.treeBuilder).getViewNodes(this.views)
        );
    }
}
