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

import java.util.HashSet;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.treeprocessor.CategoryNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.LinkedProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.NamedContainerNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;

/**
 * Handles a virtual sitemap component.
 *
 * @version $Id$
 */
public class VPCNodeBuilder extends NamedContainerNodeBuilder
    implements Contextualizable, LinkedProcessingNodeBuilder {

    private DefaultContext context;
    private String type;
    private String name;

    public void contextualize(Context context) throws ContextException {
        this.context = (DefaultContext) context;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {
        this.type = config.getName();
        this.name = config.getAttribute(this.nameAttr);

        // Find out which parameters that should be handled as sources
        // and put the info in the context.
        Configuration[] sources = config.getChildren("source");
        HashSet sourceSet = new HashSet();
        for (int j = 0; j < sources.length; j++)
            sourceSet.add(sources[j].getAttribute("param"));
        
        VPCNode node = new VPCNode(this.name, sourceSet);
        this.setupNode(node, config);

        return node;
    }

    public void linkNode() throws Exception {
        // Stuff this node into the context of current Sitemap so that
        // VirtualPipelineComponent can find it. 
        //
        // This probably doesn't work if the component is redifined in
        // a subsitemap, either the stack functionality in DefaultContext
        // should be used for context switches, or this info should
        // be put in the current processor instead.
        //
        // The plural "s" is because the category name is from the
        // embeding container and I didn't found a way to get that name.
        // But for VPCs we know that a generator is part of the
        // category geerators etc.

        ProcessingNode node = 
            CategoryNodeBuilder.getNamedNode(this.treeBuilder, this.type + "s", this.name);
        
        this.context.put(Constants.CONTEXT_VPC_PREFIX + this.type + "-" + this.name, node);
    }
}
