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

import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
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
 * @version CVS $Id: PipelineNode.java,v 1.3 2004/03/18 15:15:10 cziegeler Exp $
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

    public void service(ServiceManager manager) throws ServiceException {
        super.service(manager);
        this.errorHandlerHelper.service(manager);
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

            if (!externalRequest && !env.isInternalRedirect()) {
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

