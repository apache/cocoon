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
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.sitemap.ViewablePipelineComponentNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:uv@upaya.co.uk">Upayavira</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: SerializeNode.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=serialize-node
 */
public class SerializeNode extends ViewablePipelineComponentNode {

    private VariableResolver m_mimeType;
    private int m_statusCode;

    public SerializeNode() {
    }

    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        try {
            m_mimeType = VariableResolverFactory.getResolver(
                config.getAttribute("mime-type",null),m_manager);
        }
        catch (PatternException e) {
            throw new ConfigurationException(e.toString());
        }
        m_statusCode = config.getAttributeAsInteger("status-code",-1);
    }
        
    public final boolean invoke(Environment env, InvokeContext context) throws Exception {

        //inform the pipeline that we have a branch point
        context.getProcessingPipeline().informBranchPoint();

        String cocoonView = env.getView();
        if (cocoonView != null) {

            // Get view node
            ProcessingNode viewNode = (ProcessingNode) getViewNode(cocoonView);

            if (viewNode != null) {
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("Jumping to view " + cocoonView + " from serializer at " 
                        + getLocation());
                }
                return viewNode.invoke(env, context);
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
        
        // resolve the mime-type 
        // first from the node itself then from the component node
        String mimeType = m_mimeType.resolve(context, env.getObjectModel());
        if (mimeType == null) {
            mimeType = super.m_component.getMimeType();
        }
        
        pipeline.setSerializer(
            m_component.getComponentHint(),
            null,
            VariableResolver.buildParameters(m_parameters, context, objectModel),
            super.m_pipelineHints == null
                ? Parameters.EMPTY_PARAMETERS
                : VariableResolver.buildParameters(super.m_pipelineHints, context, objectModel),
                mimeType
        );

        // Set status code if there is one
        if (m_statusCode >= 0) {
            env.setStatus(m_statusCode);
        }
        
        if (!context.isBuildingPipelineOnly()) {
            // Process pipeline
            return pipeline.process(env);
        } else {
            // Return true : pipeline is finished.
            return true;
        }
    }

    protected String getComponentNodeRole() {
        return SerializerNode.ROLE;
    }

    /**
     * @return  <code>true</code>.
     */
    protected boolean hasParameters() {
        return true;
    }
    
}


