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
package org.apache.cocoon.components.cprocessor.sitemap;

import java.util.Map;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.sitemap.PatternException;

/**
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: ReadNode.java,v 1.2 2004/01/05 08:17:30 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=read-node
 */
public class ReadNode extends AbstractProcessingNode implements ProcessingNode {

    private String m_type;
    private VariableResolver m_src;
    private VariableResolver m_mimeType;
    private int m_statusCode;
    private String m_readerLookupKey;

    public ReadNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        try {
            m_src = VariableResolverFactory.getResolver(
                config.getAttribute("src", null), m_manager);
            m_mimeType = VariableResolverFactory.getResolver(
                config.getAttribute("mime-type", null), m_manager);
        }
        catch (PatternException e) {
            throw new ConfigurationException(e.toString());
        }
        m_statusCode = config.getAttributeAsInteger("status-code",-1);
        m_type = config.getAttribute("type",null);
        
        m_readerLookupKey = Reader.ROLE;
        if (m_type != null) {
            m_readerLookupKey += "/" + m_type;
        }
    }

    public final boolean invoke(Environment env,  InvokeContext context) throws Exception {

        Map objectModel = env.getObjectModel();

        ProcessingPipeline pipeline = context.getProcessingPipeline();

        String mimeType = m_mimeType.resolve(context, objectModel);
        /* TODO: 
         * either
         * 
           if (mimeType == null) {
               mimeType = m_readerComponentNode.getMimeType();
           }
         * 
         * or do this during sitemap2xconf transformation
         */
        pipeline.setReader(
            m_readerLookupKey,
            m_src.resolve(context, objectModel),
            VariableResolver.buildParameters(m_parameters, context, objectModel),
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
    
    /**
     * @return <code>true</code>
     */
    protected boolean hasParameters() {
        return true;
    }

}
