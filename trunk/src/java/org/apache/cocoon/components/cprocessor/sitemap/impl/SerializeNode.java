/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

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
 * @version CVS $Id: SerializeNode.java,v 1.1 2004/02/22 19:08:14 unico Exp $
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


