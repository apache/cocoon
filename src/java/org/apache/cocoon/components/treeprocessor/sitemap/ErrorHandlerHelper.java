/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.components.treeprocessor.sitemap;

import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.NotifyingBuilder;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;

import java.io.IOException;
import java.util.Map;

/**
 * Helps to call error handlers from PipelineNode and PipelinesNode.
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @version CVS $Id$
 */
public class ErrorHandlerHelper extends AbstractLogEnabled
                                implements Serviceable {

    private ServiceManager manager;

    /**
     * Logger for handled errors
     */
    protected Logger handledErrorsLogger;

    /**
     * Error handling node for the ResourceNotFoundException
     */
    private ProcessingNode error404;

    /**
     * Error handling node for all other exceptions
     */
    private ProcessingNode error500;


    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.handledErrorsLogger = logger.getChildLogger("handled-errors");
    }

    /**
     * The component manager is used to create notifying builders.
     */
    public void service(ServiceManager manager) {
        this.manager = manager;
    }

    void setHandledErrorsLogger(Logger logger) {
        this.handledErrorsLogger = logger;
    }

    void set404Handler(ProcessingNode node) {
        this.error404 = node;
    }

    void set500Handler(ProcessingNode node) {
        this.error500 = node;
    }

    public boolean invokeErrorHandler(Exception ex,
                                      Environment env,
                                      InvokeContext context)
    throws Exception {
        if (!env.isExternal() && !env.isInternalRedirect()) {
            // Propagate exception on internal requests
            throw ex;
        } else if (error404 != null && ex instanceof ResourceNotFoundException) {
            // Invoke 404-specific handler
            return invokeErrorHandler(error404, ex, env, context);
        } else if (error500 != null) {
            // Invoke global handler
            return invokeErrorHandler(error500, ex, env, context);
        }

        // No handler : propagate
        throw ex;
    }

    public boolean invokeErrorHandler(ProcessingNode node,
                                      Exception ex,
                                      Environment env,
                                      InvokeContext context)
    throws Exception {
        this.handledErrorsLogger.error(ex.getMessage(), ex);

        try {
            prepare(env, ex);

            // Create error context
            InvokeContext errorContext = new InvokeContext(context.isBuildingPipelineOnly());
            errorContext.enableLogging(getLogger());
            errorContext.setRedirector(context.getRedirector());
            errorContext.service(this.manager);
            try {
                // Process error handling node
                if (node.invoke(env, errorContext)) {
                    // Exception was handled.
                    return true;
                }
            } finally {
                errorContext.dispose();
            }
        } catch (Exception e) {
            getLogger().error("An exception occured while handling errors at " + node.getLocation(), e);
            // Rethrow it: It will either be handled by the parent sitemap or by the environment (e.g. Cocoon servlet)
            throw e;
        }

        // Exception was not handled in this error handler, propagate.
        throw ex;
    }

    private void prepare(Environment env, Exception ex)
    throws IOException, ServiceException {
        Map objectModel = env.getObjectModel();
        if (objectModel.get(Constants.NOTIFYING_OBJECT) == null) {
            // error has not been processed by another handler before

            // Try to reset the response to avoid mixing already produced output
            // and error page.
            env.tryResetResponse();

            // Create a Notifying
            NotifyingBuilder notifyingBuilder = (NotifyingBuilder) this.manager.lookup(NotifyingBuilder.ROLE);
            Notifying currentNotifying = null;
            try {
                currentNotifying = notifyingBuilder.build(this, ex);
            } finally {
                this.manager.release(notifyingBuilder);
            }

            // Add it to the object model
            objectModel.put(Constants.NOTIFYING_OBJECT, currentNotifying);

            // Also add the exception
            objectModel.put(ObjectModelHelper.THROWABLE_OBJECT, ex);
        }
    }
}
