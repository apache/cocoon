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
package org.apache.cocoon;

import org.apache.avalon.framework.service.ServiceManager;

import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;

/**
 *
 * @version $Id$
 */
public interface Processor {

    /** The role of the root processor */
    String ROLE = Processor.class.getName();

    /**
     * Keeps together processing pipeline, its owners, and environment state variables.
     */
    public static class InternalPipelineDescription {

        /** Processor owning this service manager and pipeline */
        public Processor processor;

        /** Service manager of this pipeline */
        public ServiceManager pipelineManager;

        /** Processing pipeline itself */
        public ProcessingPipeline processingPipeline;

        /** Environment prefix */
        public String prefix;

        /** Environment URI */
        public String uri;

        public InternalPipelineDescription(Processor processor,
                                           ServiceManager manager,
                                           ProcessingPipeline pipeline) {
            this.processor = processor;
            this.pipelineManager = manager;
            this.processingPipeline = pipeline;
        }

        public void release() {
            if (this.pipelineManager != null) {
                this.pipelineManager.release(this.processingPipeline);
            }
            this.processingPipeline = null;
            this.pipelineManager = null;
            this.processor = null;
        }
    }
    
    /**
     * Process the given <code>Environment</code> producing the output.
     * @return If the processing is successfull <code>true</code> is returned.
     *         If no match is found in the sitemap <code>false</code>
     *         is returned.
     * @throws ResourceNotFoundException If a sitemap component tries
     *                                   to access a resource which can not
     *                                   be found, e.g. the generator
     *         ConnectionResetException  If the connection was reset
     */
    boolean process(Environment environment)
    throws Exception;

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * Don't forget to release the pipeline using
     * {@link InternalPipelineDescription#release()}.
     * @since 2.2
     */
    InternalPipelineDescription buildPipeline(Environment environment)
    throws Exception;

    /**
     * Get the root processor parent of this processor.
     * @since 2.1.1
     */
    Processor getRootProcessor();
    
    /**
     * Get the source resolver for this processor
     * @since 2.2
     */
    SourceResolver getSourceResolver();
    
    /**
     * Get the context URI for this processor
     * @since 2.2
     */
    String getContext();

    /**
     * Sets an attribute
     * @since 2.2
     */
    void setAttribute(String name, Object value);

    /**
     * Gets an attribute
     * @since 2.2
     */
    Object getAttribute(String name);

    /**
     * Remove an attribute.
     * @since 2.2
     */
    Object removeAttribute(String name);

    /**
     * Get the parent processor (if any).
     * @return The parent processor or null.
     * @since 2.2
     */
    Processor getParent();
}
