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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.Map;

import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.PipelineEventComponentProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.PatternException;
/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: SerializeNode.java,v 1.1 2003/03/09 00:09:22 pier Exp $
 */

public class SerializeNode extends PipelineEventComponentProcessingNode {

    private String serializerName;

    private VariableResolver mimeType;

    private int statusCode;


    /**
     * Build a <code>SerializerNode</code> having a name, a mime-type and a status code (HTTP codes).
     *
     * @param name the name of the serializer to use.
     * @param mimeType the mime-type, or <code>null</code> not specified.
     * @param statusCode the HTTP response status code, or <code>-1</code> if not specified.
     */
    public SerializeNode(String name, VariableResolver mimeType, int statusCode) throws PatternException {
        this.serializerName = name;
        this.mimeType = mimeType;
        this.statusCode = statusCode;
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

        // Perform link translation if requested
        if (env.getObjectModel().containsKey(Constants.LINK_OBJECT)) {
            context.getProcessingPipeline().addTransformer(
                "<translator>", null, Parameters.EMPTY_PARAMETERS, Parameters.EMPTY_PARAMETERS
            );
        }

        ProcessingPipeline pipeline = context.getProcessingPipeline();

        Map objectModel = env.getObjectModel();

        pipeline.setSerializer(
            this.serializerName,
            null,
            Parameters.EMPTY_PARAMETERS, // No parameters on serializers
            this.pipelineHints == null
                ? Parameters.EMPTY_PARAMETERS
                : VariableResolver.buildParameters(this.pipelineHints, context, objectModel),
            this.mimeType.resolve(context, env.getObjectModel())
        );

        // Set status code if there is one
        if (this.statusCode >= 0) {
            env.setStatus(this.statusCode);
        }

        if (! context.isInternalRequest()) {
            // Process pipeline
            return pipeline.process(env);

        } else {
            // Return true : pipeline is finished.
            return true;
        }
    }
}
