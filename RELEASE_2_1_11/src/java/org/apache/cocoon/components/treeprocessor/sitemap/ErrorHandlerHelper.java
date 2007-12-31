/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.avalon.framework.component.ComponentException;
import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.logger.Logger;

import org.apache.cocoon.Constants;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.notification.Notifying;
import org.apache.cocoon.components.notification.NotifyingBuilder;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
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
 * @version $Id$
 */
public class ErrorHandlerHelper extends AbstractLogEnabled
                                implements Composable {

    private ComponentManager manager;

    /**
     * Logger for handled errors
     */
    protected Logger handledErrorsLogger;

    /**
     * Error handling node for the ResourceNotFoundException
     * (deprecated)
     */
    private HandleErrorsNode error404;

    /**
     * Error handling node for all other exceptions
     */
    private HandleErrorsNode error500;

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.handledErrorsLogger = logger.getChildLogger("handled-errors");
    }

    /**
     * The component manager is used to create notifying builders.
     */
    public void compose(ComponentManager manager) {
        this.manager = manager;
    }

    void set404Handler(ProcessingNode node) {
        this.error404 = (HandleErrorsNode) node;
    }

    void set500Handler(ProcessingNode node) {
        this.error500 = (HandleErrorsNode) node;
    }

    /**
     * @return true if has no error handler nodes set
     */
    public boolean isEmpty() {
        return this.error404 == null && this.error500 == null;
    }

    public boolean isInternal() {
        return this.error500 != null && this.error500.isInternal();
    }

    public boolean isExternal() {
        return this.error500 != null && this.error500.isExternal();
    }

    /**
     * Handle error.
     */
    public boolean invokeErrorHandler(Exception ex,
                                      Environment env,
                                      InvokeContext context)
    throws Exception {
        return prepareErrorHandler(ex, env, context) != null;
    }

    /**
     * Prepare error handler for the internal pipeline error handling.
     *
     * <p>If building pipeline only, error handling pipeline will be
     * built and returned. If building and executing pipeline,
     * error handling pipeline will be built and executed.</p>
     */
    public ProcessingPipeline prepareErrorHandler(Exception ex,
                                                  Environment env,
                                                  InvokeContext context)
    throws Exception {
        boolean internal = !env.isExternal() && !env.isInternalRedirect();

        if (internal && !isInternal()) {
            // Propagate exception on internal request: No internal handler.
            throw ex;
        } else if (!internal && !isExternal()) {
            // Propagate exception on external request: No external handler.
            throw ex;
        } else if (!internal && error404 != null && ex instanceof ResourceNotFoundException) {
            // Invoke 404-specific handler: Only on external requests. Deprecated.
            return prepareErrorHandler(error404, ex, env, context);
        } else if (error500 != null) {
            // Invoke global handler
            return prepareErrorHandler(error500, ex, env, context);
        }

        // Exception was not handled in this error handler, propagate.
        throw ex;
    }

    /**
     * Handle error using specified error handler processing node.
     */
    public boolean invokeErrorHandler(ProcessingNode node,
                                      Exception ex,
                                      Environment env,
                                      InvokeContext context)
    throws Exception {
        return prepareErrorHandler(node, ex, env, context) != null;
    }

    /**
     * Prepare (or execute) error handler using specified error handler
     * processing node.
     *
     * <p>If building pipeline only, error handling pipeline will be
     * built and returned. If building and executing pipeline,
     * error handling pipeline will be built and executed.</p>
     */
    private ProcessingPipeline prepareErrorHandler(ProcessingNode node,
                                                   Exception ex,
                                                   Environment env,
                                                   InvokeContext context)
    throws Exception {
        if (ex instanceof ResourceNotFoundException) {
            this.handledErrorsLogger.error(ex.getMessage());
        } else {
            this.handledErrorsLogger.error(ex.getMessage(), ex);
        }

        try {
            prepare(context, env, ex);

            // Create error context
            InvokeContext errorContext = new InvokeContext(context.isBuildingPipelineOnly());
            errorContext.enableLogging(getLogger());
            errorContext.setRedirector(context.getRedirector());
            errorContext.compose(this.manager);
            try {
                // Process error handling node
                if (node.invoke(env, errorContext)) {
                    // Exception was handled.
                    return errorContext.getProcessingPipeline();
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

    /**
     * Build notifying object
     */
    private void prepare(InvokeContext context, Environment env, Exception ex)
    throws IOException, ComponentException {
        Map objectModel = env.getObjectModel();
        if (objectModel.get(Constants.NOTIFYING_OBJECT) == null) {
            // error has not been processed by another handler before

            // Try to reset the response to avoid mixing already produced output
            // and error page.
            if (!context.isBuildingPipelineOnly()) {
                env.tryResetResponse();
            }

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
