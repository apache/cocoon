/*
 * Copyright 2004, Ugo Cei.
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
package org.apache.butterfly.components.pipeline.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.butterfly.components.pipeline.InvalidPipelineException;
import org.apache.butterfly.components.pipeline.PipelineProcessingException;
import org.apache.butterfly.components.pipeline.ProcessingPipeline;
import org.apache.butterfly.environment.Environment;
import org.apache.butterfly.generation.Generator;
import org.apache.butterfly.reading.Reader;
import org.apache.butterfly.serialization.Serializer;
import org.apache.butterfly.source.SourceValidity;
import org.apache.butterfly.transformation.Transformer;
import org.apache.butterfly.xml.XMLConsumer;
import org.apache.butterfly.xml.XMLProducer;


/**
 * Implementation of the non-caching pipeline.
 * 
 * TODO: change InvalidPipelineExceptions with PipelineExceptions
 * 
 * @version CVS $Id: NonCachingProcessingPipeline.java,v 1.7 2004/07/25 21:55:20 ugo Exp $
 */
public class NonCachingProcessingPipeline implements ProcessingPipeline {
    
    /** The generator */
    protected Generator generator;
    
    /** The reader */
    protected Reader reader;
    
    /** The transformers */
    protected List transformers;
    
    /** The serializer */
    protected Serializer serializer;

    /** 
     * This is the last component in the pipeline, either the serializer
     * or a custom xmlconsumer for the cocoon: protocol etc.
     */
    protected XMLConsumer lastConsumer;
    
    /** Output Buffer Size */
    protected int  outputBufferSize;
    
    /**
     * Build a new pipeline.
     */
    public NonCachingProcessingPipeline() {
        transformers = new ArrayList();
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#setGenerator(org.apache.butterfly.generation.Generator)
     */
    public void setGenerator(Generator generator) {
        if (this.generator != null) {
            throw new InvalidPipelineException("Generator already set. Cannot set generator.");
        }
        if (this.reader != null) {
            throw new InvalidPipelineException("Reader already set. Cannot set generator.");
        }
        this.generator = generator;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#setReader(org.apache.butterfly.reading.Reader)
     */
    public void setReader(Reader reader) {
        if (this.generator != null) {
            throw new InvalidPipelineException("Generator already set. Cannot set reader.");
        }
        if (this.reader != null) {
            throw new InvalidPipelineException("Reader already set. Cannot set reader.");
        }
        this.reader = reader;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#getGenerator()
     */
    public Generator getGenerator() {
        return this.generator;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#informBranchPoint()
     */
    public void informBranchPoint() {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#addTransformer(javax.xml.transform.Transformer)
     */
    public void addTransformer(Transformer transformer) {
        if (this.generator == null) {
            throw new InvalidPipelineException("Generator not yet set. Cannot add transformer.");
        }
        if (this.reader != null) {
            throw new InvalidPipelineException("Reader already set. Cannot add transformer.");
        }
        transformers.add(transformer);
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#setSerializer(org.apache.butterfly.xml.XMLPipe)
     */
    public void setSerializer(Serializer serializer) {
        if (this.serializer != null) {
            // Should normally not happen as adding a serializer starts pipeline processing
            throw new InvalidPipelineException ("Serializer already set. Cannot set serializer.");
        }
        if (this.reader != null) {
            // Should normally never happen as setting a reader starts pipeline processing
            throw new InvalidPipelineException ("Reader already set. Cannot set serializer.");
        }
        if (this.generator == null) {
            throw new InvalidPipelineException ("Must set a generator before setting serializer");
        }
        this.serializer = serializer;
        this.lastConsumer = serializer;
    }

    /**
     * Sanity check
     * @return true if the pipeline is 'sane', false otherwise.
     */
    protected boolean checkPipeline() {
        if (this.generator == null && this.reader == null) {
            return false;
        }
        if (this.generator != null && this.serializer == null) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#process()
     */
    public boolean process(Environment environment) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#prepareInternal()
     */
    public void prepareInternal(Environment environment) {
        // TODO Auto-generated method stub

    }

    /**
     * Process the given <code>Environment</code>, but do not use the
     * serializer. Instead the sax events are streamed to the XMLConsumer.
     */
    public boolean process(Environment environment, XMLConsumer consumer) {
        this.lastConsumer = consumer;
        if (this.reader != null) {
            throw new InvalidPipelineException("Streaming of an internal pipeline is not possible with a reader.");
        } else {
            connectPipeline(environment);
            return processXMLPipeline(environment);
        }
    }
    
    /**
     * Process the SAX event pipeline
     */
    protected boolean processXMLPipeline(Environment environment) {
        if (this.serializer != this.lastConsumer) {
            // internal processing
            this.generator.generate();
        } else {
            if (this.serializer.shouldSetContentLength()) {
                // set the output stream
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                this.serializer.setOutputStream(os);

                // execute the pipeline:
                this.generator.generate();
                environment.setContentLength(os.size());
                try {
                    os.writeTo(environment.getOutputStream(0));
                } catch (IOException e) {
                    throw new PipelineProcessingException("Cannot write to output stream", e);
                }
            } else {
                // set the output stream
                this.serializer.setOutputStream(environment.getOutputStream(this.outputBufferSize));
                // execute the pipeline:
                this.generator.generate();
            }
        }
        return true;
    }

    /**
     * Connect the next component
     */
    protected void connect(Environment environment,
                           XMLProducer producer,
                           XMLConsumer consumer) {
        XMLProducer next = producer;
        // Connect next component.
        next.setConsumer(consumer);
    }

    /**
     * Connect the XML pipeline.
     */
    protected void connectPipeline(Environment environment) {
        XMLProducer prev = this.generator;

        Iterator itt = this.transformers.iterator();
        while (itt.hasNext()) {
            Transformer next = (Transformer) itt.next();
            connect(environment, prev, next);
            prev = next;
        }

        // insert the serializer
        connect(environment, prev, this.lastConsumer);
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#getValidityForEventPipeline()
     */
    public SourceValidity getValidityForEventPipeline() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#getKeyForEventPipeline()
     */
    public String getKeyForEventPipeline() {
        // TODO Auto-generated method stub
        return null;
    }

}
