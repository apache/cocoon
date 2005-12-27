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

import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;

import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.ResourceNotFoundException;
import org.apache.cocoon.components.treeprocessor.AbstractParentProcessingNode;
import org.apache.cocoon.components.treeprocessor.InvokeContext;
import org.apache.cocoon.components.treeprocessor.ParameterizableProcessingNode;
import org.apache.cocoon.components.treeprocessor.ProcessingNode;
import org.apache.cocoon.components.treeprocessor.variables.VariableResolver;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.SitemapErrorHandler;
import org.apache.cocoon.sitemap.SitemapExecutor;

import java.util.Map;

/**
 * Handles &lt;map:pipeline&gt;
 *
 * @version $Id$
 */
public class PipelineNode extends AbstractParentProcessingNode
                          implements Serviceable, ParameterizableProcessingNode {

    // TODO: handle a 'fail-hard' environment attribute
    //       can be useful to stop off-line generation when there's an error

    private ProcessingNode[] children;

    private ErrorHandlerHelper errorHandlerHelper;

    private boolean internalOnly;

    /** Is it the last <pipeline> in the enclosing <pipelines> ? */
    private boolean isLast;

    /** The component name of the processing pipeline */
    protected String processingPipeline;

    /** Optional sitemap parameters */
    protected Map parameters;

    /**
     * A constructor to receive the optional expires parameter
     * and optional parameters for the processing pipeline
     */
    public PipelineNode(String name) {
        this.processingPipeline = name;
        this.errorHandlerHelper = new ErrorHandlerHelper();
    }

    /**
     * The component manager is used to create error pipelines
     */
    public void service(ServiceManager manager) {
        this.errorHandlerHelper.service(manager);
    }

    public void enableLogging(Logger logger) {
        super.enableLogging(logger);
        this.errorHandlerHelper.enableLogging(logger);
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
        this.errorHandlerHelper.set404Handler(node);
    }

    public void set500Handler(ProcessingNode node) {
        this.errorHandlerHelper.set500Handler(node);
    }

    public void setInternalOnly(boolean internalOnly) {
        this.internalOnly = internalOnly;
    }

    public final boolean invoke(Environment env, InvokeContext context)
    throws Exception {
        boolean passThrough;
        Object passThroughRaw = env.getAttribute(MountNode.COCOON_PASS_THROUGH);
        if (passThroughRaw == null) {
            // Use default value
            passThrough = false;
        } else {
            passThrough = ((Boolean) passThroughRaw).booleanValue();
        }

        // Always fail on external request if pipeline is internal only.
        if (this.internalOnly && env.isExternal()) {
            if (!this.isLast || passThrough) {
                return false;
            }

            // Do not use internal-only pipeline error handler for external requests.
            throw new ResourceNotFoundException("No pipeline matched request: " +
                                                env.getURIPrefix() + env.getURI());
        }

        Parameters params = VariableResolver.buildParameters(this.parameters,
                context,
                env.getObjectModel());

        SitemapExecutor.PipelineComponentDescription desc = new SitemapExecutor.PipelineComponentDescription();
        desc.type = this.processingPipeline;
        desc.parameters = params;

        desc = this.executor.enteringPipeline(this, env.getObjectModel(), desc);
        context.inform(desc.type, desc.parameters, env.getObjectModel());

        try {
            if (this.errorHandlerHelper.isInternal()) {
                // Set internal error handler in the pipeline
                context.setErrorHandler(
                        new SitemapErrorHandler(this.errorHandlerHelper, env, context));
            } else {
                // Reset internal error handler (previous pipeline might had set it) 
                context.setErrorHandler(null);
            }

            if (invokeNodes(children, env, context)) {
                return true;
            } else if (!this.isLast || passThrough) {
                return false;
            }

            throw new ResourceNotFoundException("No pipeline matched request: " +
                                                env.getURIPrefix() + env.getURI());

        } catch (ConnectionResetException e) {
            // Will be reported by CocoonServlet, rethrowing
            throw e;
        } catch (Exception e) {
            // Invoke error handler
            return this.errorHandlerHelper.invokeErrorHandler(e, env, context);
        }
    }
}
