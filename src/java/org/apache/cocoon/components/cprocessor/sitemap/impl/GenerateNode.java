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
package org.apache.cocoon.components.cprocessor.sitemap.impl;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.sitemap.ViewNode;
import org.apache.cocoon.components.cprocessor.sitemap.ViewablePipelineComponentNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: GenerateNode.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=generate-node
 */
public class GenerateNode extends ViewablePipelineComponentNode 
implements ProcessingNode {

    private VariableResolver m_src;

    public GenerateNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        try {
            m_src = VariableResolverFactory.getResolver(
                config.getAttribute("src", null), super.m_manager);
        }
        catch (PatternException e) {
            throw new ConfigurationException(e.toString());
        }
        super.m_labels.add(ViewNode.FIRST_POS_LABEL);
    }
    
    public final boolean invoke(Environment env, InvokeContext context) throws Exception {
        
        Map objectModel = env.getObjectModel();
        
        context.getProcessingPipeline().setGenerator(
            super.m_component.getComponentHint(),
            m_src.resolve(context, objectModel),
            VariableResolver.buildParameters(super.m_parameters, context, objectModel),
            super.m_pipelineHints == null
                ? Parameters.EMPTY_PARAMETERS
                : VariableResolver.buildParameters(super.m_pipelineHints, context, objectModel)
        );
        
        //inform the pipeline that we have a branch point
        context.getProcessingPipeline().informBranchPoint();
        
        String cocoonView = env.getView();
        if (cocoonView != null) {

            // Get view node
            ProcessingNode viewNode = (ProcessingNode) getViewNode(cocoonView);

            if (viewNode != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Jumping to view " + cocoonView + 
                        " from generator at " + getLocation());
                }
                return viewNode.invoke(env, context);
            }
        }

        // Return false to continue sitemap invocation
        return false;
    }
    
    protected String getComponentNodeRole() {
        return GeneratorNode.ROLE;
    }
    
    /**
     * @return  <code>true</code>.
     */
    protected boolean hasParameters() {
        return true;
    }

}
