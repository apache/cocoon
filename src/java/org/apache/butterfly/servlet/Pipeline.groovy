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

import org.apache.butterfly.xml.dom.DOMBuilder
import org.apache.butterfly.components.pipeline.impl.NonCachingProcessingPipeline

class Pipeline {
    public beanFactory;
    private pipeline;
    
    protected Pipeline() {
        this.pipeline = new NonCachingProcessingPipeline()
    }
            
    protected void generate(src) {
        generator = beanFactory.getBean("fileGenerator")
        generator.inputSource = src
        this.pipeline.generator = generator
    }
    
    protected void transform(type, src) {
        factory = beanFactory.getBean(type + "TransformerFactory")
        transformer = factory.getTransformer(src)
        this.pipeline.addTransformer(transformer)
    }
    
    protected void serialize(type) {
        serializer = beanFactory.getBean(type + "Serializer")
        this.pipeline.serializer = serializer
    }
    
    protected void read(src, type) {
        reader = beanFactory.getBean("resourceReader");
        reader.inputSource = src;
        this.pipeline.reader = reader;
    }
    
    public void process(environment, consumer) {
        this.pipeline.process(environment, consumer)
    }
    
    public void process(environment) {
        this.pipeline.process(environment)
    }
}