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
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
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
public class SerializeNode extends PipelineEventComponentProcessingNode
                           implements ParameterizableProcessingNode {

    private static final int DEFAULT_STATUS_CODE = 200;

    private String serializerName;

    private VariableResolver source;

    private VariableResolver mimeType;

    private VariableResolver statusCode;

    private Map parameters;


    /**
     * Build a <code>SerializerNode</code> having a name, a mime-type and a status code (HTTP codes).
     *
     * @param name the name of the serializer to use.
     * @param mimeType the mime-type, or <code>null</code> not specified.
     * @param statusCode the HTTP response status code, or <code>-1</code> if not specified.
     */
    public SerializeNode(String name,
                         VariableResolver source,
                         VariableResolver mimeType,
                         VariableResolver statusCode) {
        this.serializerName = name;
        this.source = source;
        this.mimeType = mimeType;
        this.statusCode = statusCode;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.ProcessingNode#invoke(org.apache.cocoon.environment.Environment, org.apache.cocoon.components.treeprocessor.InvokeContext)
     */
    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {

        // Check view
        if (this.views != null) {

            //inform the pipeline that we have a branch point
            context.getProcessingPipeline().informBranchPoint();

            String cocoonView = env.getView();
            if (cocoonView != null) {

                // Get view node
                ProcessingNode viewNode = (ProcessingNode)this.views.get(cocoonView);

                if (viewNode != null) {
                    if (getLogger().isInfoEnabled()) {
                        getLogger().info("Jumping to view " + cocoonView + " from serializer at " + this.getLocation());
                    }
                    return viewNode.invoke(env, context);
                }
            }
        }

        final Map objectModel = env.getObjectModel();
        final ProcessingPipeline pipeline = context.getProcessingPipeline();

        // Perform link translation if requested
        if (objectModel.containsKey(Constants.LINK_OBJECT)) {
            pipeline.addTransformer("<translator>", null, Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS);
        }

        if (objectModel.containsKey(Constants.LINK_COLLECTION_OBJECT) && env.isExternal()) {
            pipeline.addTransformer("<gatherer>", null, Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS);
        }

        SitemapExecutor.PipelineComponentDescription desc = new SitemapExecutor.PipelineComponentDescription();
        desc.type = this.serializerName;
        desc.source = source.resolve(context, objectModel);
        desc.parameters = VariableResolver.buildParameters(this.parameters, context, objectModel);
        desc.hintParameters = this.pipelineHints == null
                ? Parameters.EMPTY_PARAMETERS
                : VariableResolver.buildParameters(this.pipelineHints, context, objectModel);
        desc.mimeType = this.mimeType.resolve(context, objectModel);

        // inform executor
        desc = this.executor.addSerializer(this, objectModel, desc);

        pipeline.setSerializer(desc.type,
                               desc.source,
                               desc.parameters,
                               desc.hintParameters,
                               desc.mimeType);

        // Set status code *only* if there is one - do not override status
        // code if it was set elsewhere.
        String statusCodeString = this.statusCode.resolve(context, objectModel);
        if (statusCodeString != null) {
            int statusCodeInt = DEFAULT_STATUS_CODE;
            try {
                statusCodeInt = Integer.parseInt(statusCodeString);
            } catch (NumberFormatException e) {
                getLogger().warn("Status code value '" + statusCodeString + "' is not an integer. " +
                                 "Using " + DEFAULT_STATUS_CODE + " instead.", e);
            }
            if (statusCodeInt >= 0) {
                env.setStatus(statusCodeInt);
            }
        }

        if (!context.isBuildingPipelineOnly()) {
            // Process pipeline
            return pipeline.process(env);
        }
        // Return true : pipeline is finished.
        return true;
    }
}
