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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.cprocessor.InvokeContext;
import org.apache.cocoon.components.cprocessor.ProcessingNode;
import org.apache.cocoon.components.cprocessor.SimpleParentProcessingNode;
import org.apache.cocoon.environment.Environment;

/**
 * Handles &lt;map:pipeline&gt;
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @author <a href="mailto:unico@apache.org">Unico Hommes</a>
 * @version CVS $Id: PipelineNode.java,v 1.2 2004/01/05 08:17:30 cziegeler Exp $
 * 
 * @avalon.component
 * @avalon.service type=ProcessingNode
 * @x-avalon.lifestyle type=singleton
 * @x-avalon.info name=pipeline-node
 */
public class PipelineNode extends SimpleParentProcessingNode implements Initializable {

    // TODO : handle a 'fail-hard' environment attribute
    // can be useful to stop off-line generation when there's an error

    // the 'type' attribute
    private String m_type;
    
    private ProcessingNode m_error404;

    private ProcessingNode m_error500;

    private ErrorHandlerHelper errorHandlerHelper = new ErrorHandlerHelper();

    protected Logger handledErrorsLogger;

    private boolean m_internalOnly;

    /** Is it the last <pipeline> in the enclosing <pipelines> ? */
    private boolean isLast = false;

    public PipelineNode() {
    }
    
    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.errorHandlerHelper.enableLogging(logger);
        handledErrorsLogger = logger.getChildLogger("handled-errors");
    }
    
    public void configure(Configuration config) throws ConfigurationException {
        super.configure(config);
        m_type = config.getAttribute("type", null);
        m_internalOnly = config.getAttributeAsBoolean("internal-only", false);
    }
    
    public void initialize() throws ConfigurationException {
        ProcessingNode[] children = getChildNodes();
        
        ProcessingNode mainHandler = null;
        ProcessingNode error404Handler = null;
        ProcessingNode error500Handler = null;
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof HandleErrorsNode) {
                HandleErrorsNode node = (HandleErrorsNode) children[i];
                int type = node.getStatusCode();
                switch(type) {
                    case -1: // main handler (needs generator)
                        if (mainHandler != null) {
                            String msg = "Duplicate <handle-errors> at " + node.getLocation();
                            throw new ConfigurationException(msg);
                        } else if (error500Handler != null || error404Handler != null) {
                            String msg = "Cannot mix <handle-errors> with and without 'type' attribute at " +
                                node.getLocation();
                            throw new ConfigurationException(msg);
                        } else {
                            mainHandler = node;
                        }
                    break;
                    
                    case 404:
                        if (error404Handler != null) {
                            String msg = "Duplicate <handle-errors type='404' at " + node.getLocation();
                            throw new ConfigurationException(msg);
                        } else if(mainHandler != null) {
                            String msg = "Cannot mix <handle-errors> with and without 'type' attribute at " +
                                node.getLocation();
                            throw new ConfigurationException(msg);
                        } else {
                            error404Handler = node;
                        }
                    break;
                    
                    case 500:
                        if (error500Handler != null) {
                            String msg = "Duplicate <handle-errors type='500' at " + node.getLocation();
                            throw new ConfigurationException(msg);
                        } else if (mainHandler != null) {
                            String msg = "Cannot mix <handle-errors> with and without 'type' attribute at " +
                                node.getLocation();
                            throw new ConfigurationException(msg);
                        } else {
                            error500Handler = node;
                        }
                    break;
                        
                    default:
                        String msg = "Unknown handle-errors type (" + type + ") at " + node.getLocation();
                        throw new ConfigurationException(msg);
                }
            }
        }
        m_error404 = error404Handler;
        // Set either main or error500 handler as only one can exist
        m_error500 = error500Handler == null ? mainHandler : error500Handler;
    }

    public final boolean invoke(Environment env, InvokeContext context) throws Exception {

        boolean externalRequest = env.isExternal();

        // Always fail on external resquests if internal only.
        if (m_internalOnly && externalRequest) {
            return false;
        }

        context.inform(m_type, m_parameters, env.getObjectModel());

        try {
            if (invokeNodes(getChildNodes(), env, context)) {
                return true;
            } else {
                return false;
            }
        } catch (ConnectionResetException cre) {
            // Will be reported by CocoonServlet, rethrowing
            throw cre;

        } catch (Exception ex) {

            if (!externalRequest) {
                // Propagate exception on internal requests
                throw ex;

            } else if (m_error404 != null && ex instanceof ResourceNotFoundException) {
                // Invoke 404-specific handler
                handledErrorsLogger.error(ex.getMessage(), ex);
                return errorHandlerHelper.invokeErrorHandler(m_error404, ex, env);

            } else if (m_error500 != null) {
                // Invoke global handler
                handledErrorsLogger.error(ex.getMessage(), ex);
                return errorHandlerHelper.invokeErrorHandler(m_error500, ex, env);

            } else {
                // No handler : propagate
                throw ex;
            }
        }
    }
    
    /**
     * @return <code>true</code>
     */
    protected boolean hasParameters() {
        return true;
    }

}

