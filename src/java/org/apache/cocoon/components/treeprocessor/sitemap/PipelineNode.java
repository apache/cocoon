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
package org.apache.cocoon.components.treeprocessor.sitemap;

import java.util.Map;

import org.apache.avalon.framework.component.ComponentManager;
import org.apache.avalon.framework.component.Composable;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.environment.Environment;

/**
 * Handles &lt;map:pipeline&gt;
 *
 * @author <a href="mailto:juergen.seitz@basf-it-services.com">J&uuml;rgen Seitz</a>
 * @author <a href="mailto:bluetkemeier@s-und-n.de">Bj&ouml;rn L&uuml;tkemeier</a>
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @author <a href="mailto:gianugo@apache.org">Gianugo Rabellino</a>
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: PipelineNode.java,v 1.14 2004/03/11 01:31:59 joerg Exp $
 */
public class PipelineNode
        extends AbstractParentProcessingNode
        implements Composable, ParameterizableProcessingNode {

    // TODO : handle a 'fail-hard' environment attribute
    // can be useful to stop off-line generation when there's an error

    private ProcessingNode[] children;

    private ProcessingNode error404;

    private ProcessingNode error500;

    private ErrorHandlerHelper errorHandlerHelper = new ErrorHandlerHelper();

    protected Logger handledErrorsLogger;

    private boolean internalOnly = false;

    /** Is it the last <pipeline> in the enclosing <pipelines> ? */
    private boolean isLast = false;

    /** The component name of the processing pipeline */
    protected String processingPipeline;

    /** Optional Sitemap parameters */
    protected Map parameters;

    /**
     * A constructor to receive the optional expires parameter
     * and optional parameters for the processing pipeline
     */
    public PipelineNode(String name) {
        this.processingPipeline = name;
    }

    /**
     * The component manager is used to create error pipelines
     */
    public void compose(ComponentManager manager) {
        this.errorHandlerHelper.compose(manager);
    }

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.errorHandlerHelper.enableLogging(logger);
        handledErrorsLogger = logger.getChildLogger("handled-errors");
    }

    public void setChildren(ProcessingNode[] nodes) {
        this.children = nodes;
    }

    public void setParameters(Map parameterMap) {
        this.parameters = parameterMap;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    public void set404Handler(ProcessingNode node) {
        this.error404 = node;
    }

    public void set500Handler(ProcessingNode node) {
        this.error500 = node;
    }

    public void setInternalOnly(boolean internalOnly) {
        this.internalOnly = internalOnly;
    }

    public final boolean invoke(Environment env, InvokeContext context)
            throws Exception {

        boolean externalRequest = env.isExternal();

        // Always fail on external request if pipeline is internal only.
        if (this.internalOnly && externalRequest) {
            return false;
        }
        context.inform(this.processingPipeline, this.parameters,
                env.getObjectModel());
        try {
            if (invokeNodes(children, env, context)) {
                return true;
            } else if (!this.isLast) {
                return false;
            } else {
                throw new ResourceNotFoundException(
                        "No pipeline matched request: " + env.getURIPrefix()
                        + env.getURI());
            }
        } catch (ConnectionResetException cre) {
            // Will be reported by CocoonServlet, rethrowing
            throw cre;
        } catch (Exception ex) {
            if (!externalRequest) {
                // Propagate exception on internal requests
                throw ex;
            } else if (error404 != null && ex instanceof ResourceNotFoundException) {
                // Invoke 404-specific handler
                handledErrorsLogger.error(ex.getMessage(), ex);
                return errorHandlerHelper.invokeErrorHandler(error404, ex, env);
            } else if (error500 != null) {
                // Invoke global handler
                handledErrorsLogger.error(ex.getMessage(), ex);
                return errorHandlerHelper.invokeErrorHandler(error500, ex, env);
            } else {
                // No handler : propagate
                throw ex;
            }
        }
    }
}
