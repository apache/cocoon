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

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.PipelineEventComponentProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.SitemapExecutor;

/**
 * @version $Id$
 */
public class GenerateNode extends PipelineEventComponentProcessingNode
                          implements ParameterizableProcessingNode {

    private String generatorName;

    private VariableResolver source;

    private Map parameters;


    public GenerateNode(String name, VariableResolver source) {
        this.generatorName = name;
        this.source = source;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    /* (non-Javadoc)
     * @see ProcessingNode#invoke(Environment, InvokeContext)
     */
    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        final Map objectModel = env.getObjectModel();

        SitemapExecutor.PipelineComponentDescription desc = new SitemapExecutor.PipelineComponentDescription();
        desc.type = this.generatorName;
        desc.source = source.resolve(context, objectModel);
        desc.parameters = VariableResolver.buildParameters(this.parameters, context, objectModel);
        desc.hintParameters = this.pipelineHints == null
                ? Parameters.EMPTY_PARAMETERS
                : VariableResolver.buildParameters(this.pipelineHints, context, objectModel);

        desc = this.executor.addGenerator(this, objectModel, desc);

        context.getProcessingPipeline().setGenerator(desc.type,
                                                     desc.source,
                                                     desc.parameters,
                                                     desc.hintParameters);

        // Check view
        if (this.views != null) {
            //inform the pipeline that we have a branch point
            context.getProcessingPipeline().informBranchPoint();

            String cocoonView = env.getView();
            if (cocoonView != null) {

                // Get view node
                ProcessingNode viewNode = (ProcessingNode) this.views.get(cocoonView);

                if (viewNode != null) {
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("Jumping to view " + cocoonView + " from generator at " + this.getLocation());
                    }
                    return viewNode.invoke(env, context);
                }
            }
        }

        // Return false to continue sitemap invocation
        return false;
    }
}
