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

import java.util.ArrayList;

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.SimpleParentProcessingNode;
import org.apache.cocoon.environment.Environment;

/**
 * Handles &lt;map:pipelines&gt;
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: PipelinesNode.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=pipelines-node
 */
public final class PipelinesNode extends SimpleParentProcessingNode implements Initializable {
    
    private ErrorHandlerHelper m_errorHandlerHelper = new ErrorHandlerHelper();

    private ProcessingNode[] m_pipelines;
    
    private ProcessingNode m_errorHandler;

    public PipelinesNode() {
    }

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        m_errorHandlerHelper.enableLogging(logger);
    }
    
    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        m_errorHandlerHelper.service(manager);
    }
    
    public void initialize() throws ConfigurationException {
        ProcessingNode[] children = getChildNodes();
        ArrayList pipelines = new ArrayList(children.length);
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof HandleErrorsNode) {
                if (m_errorHandler != null) {
                    String msg = "Duplicate <handle-errors> at " + children[i].getLocation();
                    throw new ConfigurationException(msg);
                }
                m_errorHandler = children[i];
            }
            else {
                pipelines.add(children[i]);
            }
        }
        if (pipelines.size() == 0) {
            String msg = "There must be at least one pipeline at " + getLocation();
            throw new ConfigurationException(msg);
        }
        m_pipelines = (ProcessingNode[]) pipelines.toArray(new ProcessingNode[pipelines.size()]);
    }

    /**
     * Process the environment. Also adds a <code>SourceResolver</code>
     * and a <code>Redirector</code> in the object model. The previous resolver and
     * redirector, if any, are restored before return.
     */
    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
    
        // Perform any common invoke functionality 
        super.invoke(env, context);

        // Recompose context (and pipelines) to the local component manager
        context.reservice(super.m_manager);

        try {
            // FIXME : is there any useful information that can be passed as top-level parameters,
            //         such as the URI of the mount point ?
            
            if (!invokeNodes(m_pipelines, env, context)) {
                String msg = "No pipeline matched request: " + env.getURIPrefix() + env.getURI();
                throw new ResourceNotFoundException(msg);
            }
            return true;
        } catch (Exception ex) {
            if (m_errorHandler != null) {
                // Invoke pipelines handler
                return m_errorHandlerHelper.invokeErrorHandler(m_errorHandler, ex, env);
            } else {
                // No handler : propagate
                throw ex;
            }
        }
    }

}
