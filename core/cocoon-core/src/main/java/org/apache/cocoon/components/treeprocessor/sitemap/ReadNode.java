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

import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.SitemapExecutor;

import java.util.Map;

/**
 * @version $Id$
 */
public class ReadNode extends AbstractProcessingNode
                      implements ParameterizableProcessingNode {

    private String readerName;

    private VariableResolver source;

    private VariableResolver mimeType;

    private int statusCode;

    private Map parameters;


    /**
     * Build a <code>SerializerNode</code> having a name, a mime-type and a status code (HTTP codes).
     *
     * @param name the name of the serializer to use.
     * @param mimeType the mime-type, or <code>null</code> not specified.
     * @param statusCode the HTTP response status code, or <code>-1</code> if not specified.
     */
    public ReadNode(String name, VariableResolver source, VariableResolver mimeType, int statusCode) {
        this.readerName = name;
        this.source = source;
        this.mimeType = mimeType;
        this.statusCode = statusCode;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.treeprocessor.ProcessingNode#invoke(Environment, InvokeContext)
     */
    public final boolean invoke(Environment env,  InvokeContext context)
    throws Exception {

        final Map objectModel = env.getObjectModel();

        final ProcessingPipeline pipeline = context.getProcessingPipeline();

        SitemapExecutor.PipelineComponentDescription desc = new SitemapExecutor.PipelineComponentDescription();
        desc.type = this.readerName;
        desc.source = source.resolve(context, objectModel);
        desc.parameters = VariableResolver.buildParameters(this.parameters, context, objectModel);
        desc.mimeType = this.mimeType.resolve(context, objectModel);

        desc = this.executor.addReader(this, objectModel, desc);

        pipeline.setReader(desc.type,
                           desc.source,
                           desc.parameters,
                           desc.mimeType);

        // Set status code if there is one
        if (this.statusCode >= 0) {
            env.setStatus(this.statusCode);
        }

        if (!context.isBuildingPipelineOnly()) {
            // Process pipeline
            return pipeline.process(env);
        }

        // Return true : pipeline is finished.
        return true;
    }
}
