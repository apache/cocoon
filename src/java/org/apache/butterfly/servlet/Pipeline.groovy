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

import org.apache.butterfly.components.pipeline.impl.NonCachingProcessingPipeline

/**
 * The Pipeline class is the base for all sitemaps.
 *
 * @version CVS $Id$
 */
class Pipeline {
    public beanFactory;
    private pipeline;
    
    protected Pipeline() {
        this.pipeline = new NonCachingProcessingPipeline()
    }
            
    protected void generate(type, src, parameters) {
        generator = beanFactory.getBean(type + "Generator")
        generator.inputSource = src
        parameters.each { generator[it.key] = it.value }
        this.pipeline.generator = generator
    }
    
    protected void transform(type, src, parameters) {
        factory = beanFactory.getBean(type + "TransformerFactory")
        transformer = factory.getTransformer(src)
        parameters.each { transformer[it.key] = it.value }
        this.pipeline.addTransformer(transformer)
    }
    
    protected void serialize(type, parameters) {
        serializer = beanFactory.getBean(type + "Serializer")
        parameters.each { serializer[it.key] = it.value }
        this.pipeline.serializer = serializer
    }
    
    protected void read(src, type, parameters) {
        reader = beanFactory.getBean("resourceReader");
        reader.inputSource = src;
        parameters.each { reader[it.key] = it.value }
        this.pipeline.reader = reader;
    }
    
    public void process(environment, consumer) {
        this.pipeline.process(environment, consumer)
    }
    
    public void process(environment) {
        this.pipeline.process(environment)
    }
}