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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.components.cprocessor.AbstractProcessingNode;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.variables.VariableResolver;
import org.apache.cocoon.components.cprocessor.variables.VariableResolverFactory;
import org.apache.cocoon.environment.Environment;

/**
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: CallResourceNode.java,v 1.1 2004/02/22 19:08:14 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=call-resource
 */
public class CallResourceNode extends AbstractProcessingNode implements Initializable {

    /** The 'resource' attribute */
    private String m_resourceName;

    private VariableResolver m_resourceResolver;

    private ProcessingNode m_resourceNode;
    
    
    public CallResourceNode() {
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_resourceName = config.getAttribute("resource");
    }
    
    public void initialize() throws Exception {
        
        if (VariableResolverFactory.needsResolve(m_resourceName)) {
            // Will always be resolved at invoke time
            m_resourceResolver = VariableResolverFactory.getResolver(m_resourceName, super.m_manager);
        } else {
            // Static name : get it now
            String name = VariableResolverFactory.unescape(m_resourceName);
            m_resourceNode = (ProcessingNode) super.m_manager.lookup(
                ProcessingNode.ROLE + "/" + name);
        }
    }
    
    public final boolean invoke(Environment env, InvokeContext context) throws Exception {
        
        Map objectModel = env.getObjectModel();
        // Resolve parameters, but push them only once the resource name has been
        // resolved, otherwise it adds an unwanted nesting level
        Map params = VariableResolver.buildMap(super.m_parameters, context, objectModel);

        if (m_resourceNode != null) {
            // Static resource name
            context.pushMap(null,params);
            
            try {
                return m_resourceNode.invoke(env, context);
            } finally {
                context.popMap();
            }
    
        } else {
            // Resolved resource name
            String name = m_resourceResolver.resolve(context, objectModel);
            ProcessingNode resourceNode = (ProcessingNode) super.m_manager.lookup(
                ProcessingNode.ROLE + "/" + name);
            
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Calling resource " + name);
            }
            
            // and only now push the parameters
            context.pushMap(null,params);
            
            try {
                return resourceNode.invoke(env,context);
            } finally {
                context.popMap();
            }
        }
    }
    
    /**
     * @return <code>true</code>.
     */
    protected boolean hasParameters() {
        return true;
    }

}
