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
package org.apache.cocoon.components.treeprocessor;

import org.apache.cocoon.Processor;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.components.treeprocessor.sitemap.ErrorHandlerHelper;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.sitemap.SitemapErrorHandler;

/**
 * Class providing error handling capabilities to the pipeline
 * as configured in the sitemap.
 *
 * @since 2.2
 * @version $Id$
 */
public class TreeProcessorSitemapErrorHandler implements SitemapErrorHandler {
    /**
     * Error handler helper of the pipeline node
     */
    private ErrorHandlerHelper handler;

    /**
     * Environment of the pipeline node
     */
    private Environment environment;

    /**
     * Sitemap invocation context
     */
    private InvokeContext context;

    // Environment state
    private String envPrefix;
    private String envURI;

    /**
     * Construct error handler with everything needed to handle an error.
     */
    public TreeProcessorSitemapErrorHandler(ErrorHandlerHelper handler,
                                            Environment environment,
                                            InvokeContext context) {
        this.handler = handler;
        this.environment = environment;
        this.context = context;

        this.envPrefix = environment.getURIPrefix();
        this.envURI = environment.getURI();
    }

    /**
     * Handle an error.
     * @return true if error was handled.
     */
    public boolean handleError(Exception e) throws Exception {
        // Restore environment state
        this.environment.setURI(this.envPrefix, this.envURI);

        return this.handler.invokeErrorHandler(e, this.environment, this.context);
    }

    /**
     * Build error handling pipeline.
     * @return error handling pipeline, or null if error was not handled.
     */
    public ProcessingPipeline prepareErrorPipeline(Exception e) throws Exception {
        // Restore environment state
        this.environment.setURI(this.envPrefix, this.envURI);
        
        Processor.InternalPipelineDescription pipelineDescription = 
            this.handler.prepareErrorHandler(e, this.environment, this.context);

        return pipelineDescription != null ? pipelineDescription.processingPipeline : null; 
    }
}
