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

import java.util.ArrayList;
import java.util.Map;

import org.apache.avalon.framework.activity.Disposable;
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
import org.apache.cocoon.environment.ForwardRedirector;
import org.apache.cocoon.environment.Redirector;

/**
 * Handles &lt;map:pipelines&gt;
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: PipelinesNode.java,v 1.1 2003/12/28 21:03:17 unico Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=pipelines-node
 */
public final class PipelinesNode extends SimpleParentProcessingNode implements Initializable {

    private static final String REDIRECTOR_ATTR = "sitemap:redirector";
    
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

    public static Redirector getRedirector(Environment env) {
        return (Redirector) env.getAttribute(REDIRECTOR_ATTR);
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

        // Build a redirector
        ForwardRedirector redirector = new ForwardRedirector(env);
        setupLogger(redirector);

        Map objectModel = env.getObjectModel();
        
        Object oldResolver = objectModel.get(OBJECT_SOURCE_RESOLVER);
        Object oldRedirector = env.getAttribute(REDIRECTOR_ATTR);

        objectModel.put(OBJECT_SOURCE_RESOLVER, env);
        env.setAttribute(REDIRECTOR_ATTR, redirector);

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
        } finally {
            // Restore old redirector and resolver
            env.setAttribute(REDIRECTOR_ATTR, oldRedirector);
            objectModel.put(OBJECT_SOURCE_RESOLVER, oldResolver);
        }
    }

}
