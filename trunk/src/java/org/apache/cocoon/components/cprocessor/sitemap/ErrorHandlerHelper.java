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
 * @version CVS $Id: ErrorHandlerHelper.java,v 1.2 2004/01/05 08:17:30 cziegeler Exp $
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

