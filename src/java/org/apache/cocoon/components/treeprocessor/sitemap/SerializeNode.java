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
import org.apache.cocoon.sitemap.PatternException;
/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @version CVS $Id: SerializeNode.java,v 1.8 2004/03/05 13:02:52 bdelacretaz Exp $
 */
public class SerializeNode extends PipelineEventComponentProcessingNode implements ParameterizableProcessingNode {

    private String serializerName;

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
    public SerializeNode(String name, VariableResolver source, VariableResolver mimeType, int statusCode) throws PatternException {
        this.serializerName = name;
        this.source = source;
        this.mimeType = mimeType;
        this.statusCode = statusCode;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

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
        
        Map objectModel = env.getObjectModel();
        ProcessingPipeline pipeline = context.getProcessingPipeline();

        // Perform link translation if requested
        if (objectModel.containsKey(Constants.LINK_OBJECT)) {
            pipeline.addTransformer("<translator>", null, Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS);
        }
        
        if (objectModel.containsKey(Constants.LINK_COLLECTION_OBJECT) && env.isExternal()) {
            pipeline.addTransformer("<gatherer>", null, Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS);
        }

        pipeline.setSerializer(
            this.serializerName,
            source.resolve(context, objectModel),
            VariableResolver.buildParameters(this.parameters, context, objectModel),
            this.pipelineHints == null
                ? Parameters.EMPTY_PARAMETERS
                : VariableResolver.buildParameters(this.pipelineHints, context, objectModel),
            this.mimeType.resolve(context, env.getObjectModel())
        );

        // Set status code if there is one
        if (this.statusCode >= 0) {
            env.setStatus(this.statusCode);
        }

        if (! context.isBuildingPipelineOnly()) {
            // Process pipeline
            return pipeline.process(env);

        } else {
            // Return true : pipeline is finished.
            return true;
        }
    }
}
