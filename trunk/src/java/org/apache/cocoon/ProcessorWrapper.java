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
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;

/**
 * This class is a wrapper around the real processor (the <code>Cocoon</code> class).
 * It is necessary to avoid infinite dispose loops
 * 
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: ProcessorWrapper.java,v 1.13 2004/03/08 13:57:35 cziegeler Exp $
 */
public final class ProcessorWrapper
implements Processor, Disposable, ThreadSafe {

    private Processor processor;

    public ProcessorWrapper(Processor processor) {
        this.processor = processor;
    }

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.activity.Disposable#dispose()
     */
    public void dispose() {
        this.processor = null;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#process(org.apache.cocoon.environment.Environment)
     */
    public boolean process(Environment environment)
    throws Exception {
        return this.processor.process(environment);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#buildPipeline(org.apache.cocoon.environment.Environment)
     */
    public InternalPipelineDescription buildPipeline(Environment environment)
    throws Exception {
        return this.processor.buildPipeline(environment);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Map getComponentConfigurations() {
        return this.processor.getComponentConfigurations();
    }
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getRootProcessor()
     */
    public Processor getRootProcessor() {
        return this.processor.getRootProcessor();
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.Processor#getEnvironmentHelper()
     */
    public SourceResolver getSourceResolver() {
        return this.processor.getSourceResolver();
    }

	/* (non-Javadoc)
	 * @see org.apache.cocoon.Processor#getContext()
	 */
	public String getContext() {
		return this.processor.getContext();
	}
}
