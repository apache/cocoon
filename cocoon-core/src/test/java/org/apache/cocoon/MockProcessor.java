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

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Mock processor
 * 
 * @version $Id$
 */
public class MockProcessor implements Processor {

    private final ConfigurableListableBeanFactory beanFactory;

    public MockProcessor(ConfigurableListableBeanFactory factory) {
        this.beanFactory = factory;
    }

    /**
     * @see org.apache.cocoon.Processor#buildPipeline(org.apache.cocoon.environment.Environment)
     */
    public InternalPipelineDescription buildPipeline(Environment environment)
    throws Exception {
        return null;
    }
    
    /**
     * @see org.apache.cocoon.Processor#getComponentConfigurations()
     */
    public Map getComponentConfigurations() {
        return null;
    }
    
    /**
     * @see org.apache.cocoon.Processor#getContext()
     */
    public String getContext() {
        return null;
    }
    /**
     * @see org.apache.cocoon.Processor#getRootProcessor()
     */
    public Processor getRootProcessor() {
        return this;
    }
    
    /**
     * @see org.apache.cocoon.Processor#getSourceResolver()
     */
    public SourceResolver getSourceResolver() {
        return null;
    }

    /**
     * @see org.apache.cocoon.Processor#process(org.apache.cocoon.environment.Environment)
     */
    public boolean process(Environment environment) throws Exception {
        return false;
    }

    /**
     * @see org.apache.cocoon.Processor#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return null;
    }

    /**
     * @see org.apache.cocoon.Processor#removeAttribute(java.lang.String)
     */
    public Object removeAttribute(String name) {
        return null;
    }

    /**
     * @see org.apache.cocoon.Processor#setAttribute(java.lang.String, java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        // nothing to do
    }

    /**
     * @see org.apache.cocoon.Processor#getParent()
     */
    public Processor getParent() {
        return null;
    }

    /**
     * @see org.apache.cocoon.Processor#getBeanFactory()
     */
    public ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }
}
