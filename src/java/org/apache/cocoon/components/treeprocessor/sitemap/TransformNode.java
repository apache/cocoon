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

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.PipelineEventComponentProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.PatternException;

import java.util.Map;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: TransformNode.java,v 1.2 2004/03/05 13:02:52 bdelacretaz Exp $
 */

public class TransformNode extends PipelineEventComponentProcessingNode implements ParameterizableProcessingNode {

    private String transformerName;

    private VariableResolver source;

    private Map parameters;


    public TransformNode(String name, VariableResolver source) throws PatternException {
        this.transformerName = name;
        this.source = source;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }


    public final boolean invoke(Environment env, InvokeContext context)
      throws Exception {

        Map objectModel = env.getObjectModel();

        context.getProcessingPipeline().addTransformer(
            this.transformerName,
            source.resolve(context, objectModel),
            VariableResolver.buildParameters(this.parameters, context, objectModel),
            this.pipelineHints == null
                ? Parameters.EMPTY_PARAMETERS
                : VariableResolver.buildParameters(this.pipelineHints, context, objectModel)
        );

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
                        getLogger().info("Jumping to view " + cocoonView + " from transformer at " + this.getLocation());
                    }
                    return viewNode.invoke(env, context);
                }
            }
        }

        // Return false to contine sitemap invocation
        return false;
    }
}
