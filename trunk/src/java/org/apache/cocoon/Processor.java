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
package org.apache.cocoon;

import java.util.Map;

import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;

/**
 * 
 * @author <a href="mailto:pier@apache.org">Pierpaolo Fumagalli</a>
 *         (Apache Software Foundation)
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: Processor.java,v 1.17 2004/03/08 13:57:35 cziegeler Exp $
 */
public interface Processor {

    /** The role of the root processor */
    String ROLE = Processor.class.getName();

    public class InternalPipelineDescription {
        
        public InternalPipelineDescription(ProcessingPipeline pp, ServiceManager s) {
            this.processingPipeline = pp;
            this.pipelineManager = s;
        }
        public ProcessingPipeline processingPipeline;
        public ServiceManager pipelineManager;
        public Processor lastProcessor;
        public String prefix;
        public String uri;
        
        public void release() {
            if (this.pipelineManager != null) {
                this.pipelineManager.release(this.processingPipeline);
                this.pipelineManager = null;
            }
            this.lastProcessor = null;
            this.processingPipeline = null;
        }
    }
    
    /**
     * Process the given <code>Environment</code> producing the output.
     * @return If the processing is successful <code>true</code> is returned.
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
     * {@link releasePipeline(Environment, InternalPipelineDescription)}.
     * @since 2.1
     */
    InternalPipelineDescription buildPipeline(Environment environment)
    throws Exception;

    /**
     * Get the sitemap component configurations
     * @since 2.1
     */
    Map getComponentConfigurations();

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
}
