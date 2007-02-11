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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.component.Component;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.pipeline.ProcessingPipeline;
import org.apache.cocoon.environment.Environment;

/**
 * This class is a wrapper around the real processor (the <code>Cocoon</code> class).
 * It is necessary to avoid infinite dispose loops
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ProcessorWrapper.java,v 1.5 2004/03/05 13:02:42 bdelacretaz Exp $
 */
public final class ProcessorWrapper
implements Processor, Component, Disposable, ThreadSafe {

    private Processor processor;

    public void dispose() {
        this.processor = null;
    }

    public ProcessorWrapper(Processor processor) {
        this.processor = processor;
    }

    /**
     * Process the given <code>Environment</code> producing the output
     */
    public boolean process(Environment environment)
    throws Exception {
        return this.processor.process(environment);
    }

    /**
     * Process the given <code>Environment</code> to assemble
     * a <code>ProcessingPipeline</code>.
     * @since 2.1
     */
    public ProcessingPipeline buildPipeline(Environment environment)
    throws Exception {
        return this.processor.buildPipeline(environment);
    }

    /**
     * Get the sitemap component configurations
     * @since 2.1
     */
    public Map getComponentConfigurations() {
        return this.processor.getComponentConfigurations();
    }
    
    /**
     * Get the root parent processor of this processor
     * @since 2.1.1
     */
    public Processor getRootProcessor() {
        return this.processor.getRootProcessor();
    }

}
