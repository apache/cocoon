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
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.context.DefaultContext;

import org.apache.cocoon.Constants;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNodeBuilder;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.generation.VirtualPipelineGenerator;

/**
 * Handles &lt;map:components&gt;. It doesn't actually create a <code>ProcessingNode</code>.
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public class ComponentsNodeBuilder extends AbstractProcessingNodeBuilder
                                   implements Contextualizable {

    private DefaultContext context;

    public void contextualize(Context context) throws ContextException {
        this.context = (DefaultContext) context;
    }

    /** This builder has no parameters -- return <code>false</code> */
    protected boolean hasParameters() {
        return false;
    }

    public ProcessingNode buildNode(Configuration config) throws Exception {
        // Check for component configurations
        Configuration child = config.getChild("generators", false);
        if (child != null) {
            Configuration[] generators = child.getChildren("generator");
            for (int i = 0; i < generators.length; i++) {
                Configuration generator = generators[i];

                String clazz = generator.getAttribute("src");
                if (VirtualPipelineGenerator.class.getName().equals(clazz)) {
                    // Got it
                    PipelineNodeBuilder builder = new PipelineNodeBuilder();
                    builder.setBuilder(this.treeBuilder);
                    ProcessingNode node = builder.buildNode(generator);

                    // Stuff this node into the context of current Sitemap
                    // so that VirtualPipelineComponent can find it.
                    String name = generator.getAttribute("name");
                    context.put(Constants.CONTEXT_VPC_PREFIX + "generator-" + name, node);
                }
            }
        }

        return null;
    }
}
