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

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Constants;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.NotifyingBuilder;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;

/**
 * Helps to call error handlers from PipelineNode and PipelinesNode.
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @version CVS $Id: ErrorHandlerHelper.java,v 1.2 2004/03/08 13:57:38 cziegeler Exp $
 */
public class ErrorHandlerHelper extends AbstractLogEnabled implements Serviceable {

    private ServiceManager m_manager;

    /**
     * The service manager is used to lookup notifying builders.
     */
    public void service(ServiceManager manager) {
        m_manager = manager;
    }

    public boolean invokeErrorHandler(ProcessingNode node, Exception ex, Environment env)
    throws Exception {
		Map objectModel = env.getObjectModel();
        
        InvokeContext errorContext = null;
		boolean nodeSuccessful = false;
		
        try {
        	if (objectModel.get(Constants.NOTIFYING_OBJECT) == null) {
				// error has not been processed by another handler before
				
	            // Try to reset the response to avoid mixing already produced output
	            // and error page.
	            env.tryResetResponse();
	
	            // Create a Notifying
	            NotifyingBuilder notifyingBuilder= (NotifyingBuilder) 
                    m_manager.lookup(NotifyingBuilder.ROLE);
	            Notifying currentNotifying = null;
	            try {
	                currentNotifying = notifyingBuilder.build(this, ex);
	            } finally {
	                m_manager.release(notifyingBuilder);
	            }
	
	            // Add it to the object model
	            objectModel.put(Constants.NOTIFYING_OBJECT, currentNotifying);
	            
	            // Also add the exception
	            objectModel.put(ObjectModelHelper.THROWABLE_OBJECT, ex);
        	}

			// Build a new context
			errorContext = new InvokeContext();
			errorContext.enableLogging(getLogger());
			errorContext.service(m_manager);
			
			nodeSuccessful = node.invoke(env, errorContext);
        } catch (Exception subEx) {
            getLogger().error("An exception occured in while handling errors at " + node.getLocation(), subEx);
            // Rethrow it : it will either be handled by the parent sitemap or by the environment (e.g. Cocoon servlet)
            throw subEx;
        } finally {
            if (errorContext != null) {
                errorContext.dispose();
            }
        }
        
        if (nodeSuccessful) {
        	return true;
        } else {
			throw ex;
        }
    }
}

