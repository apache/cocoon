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
 * @version CVS $Id: CallResourceNode.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
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
