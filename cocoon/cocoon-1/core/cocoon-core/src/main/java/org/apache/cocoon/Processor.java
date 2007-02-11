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
package org.apache.cocoon;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.springframework.beans.factory.BeanFactory;

/**
 *
 * @version $Id$
 */
public interface Processor {

    /** The role of the root processor */
    String ROLE = Processor.class.getName();

    public static class InternalPipelineDescription {
        
        public InternalPipelineDescription(ProcessingPipeline pp, 
                                           ServiceSelector selector,
                                           ServiceManager manager) {
            this.processingPipeline = pp;
            this.pipelineSelector = selector;
            this.pipelineManager = manager;
        }
        public ProcessingPipeline processingPipeline;
        public ServiceManager pipelineManager;
        public ServiceSelector pipelineSelector;
        public Processor lastProcessor;
        public String prefix;
        public String uri;
        
        public void release() {
            if (this.pipelineSelector != null) {
                this.pipelineSelector.release(this.processingPipeline);
                this.pipelineManager.release(this.pipelineSelector);
            }
            this.lastProcessor = null;
            this.processingPipeline = null;
            this.pipelineManager = null;
            this.pipelineSelector = null;
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

    /** FIXME - Remove this and use ProcessingUtil. */
    BeanFactory getBeanFactory();

    /**
     * Get the parent processor (if any).
     * @return The parent processor or null.
     * @since 2.2
     */
    Processor getParent();
}
