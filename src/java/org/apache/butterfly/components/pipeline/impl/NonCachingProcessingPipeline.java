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
package org.apache.butterfly.components.pipeline.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.butterfly.components.pipeline.ConnectionResetException;
import org.apache.butterfly.components.pipeline.InvalidPipelineException;
import org.apache.butterfly.components.pipeline.PipelineProcessingException;
import org.apache.butterfly.components.pipeline.ProcessingPipeline;
import org.apache.butterfly.environment.Environment;
import org.apache.butterfly.environment.ObjectModelHelper;
import org.apache.butterfly.environment.Response;
import org.apache.butterfly.generation.Generator;
import org.apache.butterfly.reading.Reader;
import org.apache.butterfly.serialization.Serializer;
import org.apache.butterfly.source.SourceValidity;
import org.apache.butterfly.transformation.Transformer;
import org.apache.butterfly.xml.XMLConsumer;
import org.apache.butterfly.xml.XMLProducer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Implementation of the non-caching pipeline.
 * 
 * @version CVS $Id$
 */
public class NonCachingProcessingPipeline implements ProcessingPipeline {

    protected static final Log logger = LogFactory.getLog(NonCachingProcessingPipeline.class);
    
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

    /** Expires value */
    protected long expires;
    
    /** Output Buffer Size */
    protected int  outputBufferSize;

    /**
     * Build a new pipeline.
     */
    public NonCachingProcessingPipeline() {
        transformers = new ArrayList();
    }
    
    /**
     * @param expires The expires to set.
     */
    public void setExpires(String expires) {
        if (expires != null) {
            // FIXME: use a BeanPropertyEditor?
            this.expires = parseExpires(expires);
        }
    }
    
    public long getExpires() {
        return expires;
    }

    /**
     * @param outputBufferSize The outputBufferSize to set.
     */
    public void setOutputBufferSize(int outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
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

    /**
     * Informs pipeline we have come across a branch point
     * Default Behaviour is do nothing
     */
    public void informBranchPoint() {}

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
        // If this is an internal request, lastConsumer was reset!
        if (null == this.lastConsumer) {
            this.lastConsumer = this.serializer;
        } else {
            preparePipeline(environment);
        }
        if (this.reader != null) {
            preparePipeline(environment);   
            reader.setObjectModel(environment.getObjectModel());
        }
        
        // See if we need to set an "Expires:" header
        if (this.expires != 0) {
            Response res = ObjectModelHelper.getResponse(environment.getObjectModel());
            res.setDateHeader("Expires", System.currentTimeMillis() + expires);
            res.setHeader("Cache-Control", "max-age=" + expires/1000 + ", public");
            if (logger.isDebugEnabled()) {
                logger.debug("Setting a new Expires object for this resource");
            }
            environment.getObjectModel().put(ObjectModelHelper.EXPIRES_OBJECT,
                                             new Long(expires + System.currentTimeMillis()));
        }
        
        if (this.reader != null) {
            if (checkIfModified(environment, this.reader.getLastModified())) {
                return true;
            }
            return this.processReader(environment);
        } else {
            this.connectPipeline(environment);
            return this.processXMLPipeline(environment);
        }
    }

    /**
     * Prepare an internal processing
     * @param environment          The current environment.
     * @throws ProcessingException
     */
    public void prepareInternal(Environment environment) {
        this.lastConsumer = null;
        this.preparePipeline(environment);
    }

    /**
     * Prepare the pipeline 
     */
    protected void preparePipeline(Environment environment) {
        if (! checkPipeline()) {
            throw new PipelineProcessingException("Attempted to process incomplete pipeline.");
        }
        if (this.reader != null) {
            setupReader(environment);
        } else {
            setupPipeline(environment);
        }
    }

    /**
     * Setup pipeline components.
     */
    protected void setupPipeline(Environment environment) {
        if (this.lastConsumer == null) {
            // internal processing: text/xml
            environment.setContentType("text/xml");
        } else {
            environment.setContentType(this.serializer.getMimeType());
        }
    }

    /**
     * Setup the reader
     */
    protected void setupReader(Environment environment) {
        final String mimeType = this.reader.getMimeType();
        if (mimeType != null) {
            environment.setContentType(mimeType);                    
        }
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

    /**
     * Return valid validity objects for the event pipeline
     * If the "event pipeline" (= the complete pipeline without the
     * serializer) is cacheable and valid, return all validity objects.
     * Otherwise return <code>null</code>
     */
    public SourceValidity getValidityForEventPipeline() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.apache.butterfly.components.pipeline.ProcessingPipeline#getKeyForEventPipeline()
     */
    public String getKeyForEventPipeline() {
        return null;
    }

    protected boolean checkIfModified(Environment environment,
                                        long lastModified) {
        // has the read resource been modified?
        if(! environment.isResponseModified(lastModified)) {
            // environment supports this, so we are finished
            environment.setResponseIsNotModified();
            return true;
        }
        return false;
    }

    /**
     * Process the pipeline using a reader.
     */
    protected boolean processReader(Environment environment) {
        try {
            if (this.reader.shouldSetContentLength()) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                this.reader.setOutputStream(os);
                this.reader.generate();
                environment.setContentLength(os.size());
                os.writeTo(environment.getOutputStream(0));
            } else {
                this.reader.setOutputStream(environment.getOutputStream(this.outputBufferSize));
                this.reader.generate();
            }
        } catch ( SocketException se ) {
            if (se.getMessage().indexOf("reset") > 0
                    || se.getMessage().indexOf("aborted") > 0
                    || se.getMessage().indexOf("connection abort") > 0) {
                throw new ConnectionResetException("Connection reset by peer", se);
            } else {
                throw new PipelineProcessingException("Failed to execute reader pipeline.", se);
            }
        } catch ( Exception e ) {
            throw new PipelineProcessingException("Error executing reader pipeline.",e);
        }
        return true;
    }

    /**
     * Parse the expires parameter
     */
    private long parseExpires(String expire) {
        StringTokenizer tokens = new StringTokenizer(expire);

        // get <base>
        String current = tokens.nextToken();
        if (current.equals("modification")) {
            logger.warn("the \"modification\" keyword is not yet" +
                             " implemented. Assuming \"now\" as the base attribute");
            current = "now";
        }

        if (! current.equals("now") && ! current.equals("access")) {
            logger.error("bad <base> attribute, Expires header will not be set");
            return -1;
        }

        long number = 0;
        long modifier = 0;
        long expires = 0;

        while (tokens.hasMoreTokens()) {
            current = tokens.nextToken();

            // get rid of the optional <plus> keyword
            if (current.equals("plus")) {
                current = tokens.nextToken();
            }

            // We're expecting a sequence of <number> and <modification> here
            // get <number> first
            try {
                number = Long.parseLong(current);
            } catch (NumberFormatException nfe) {
                logger.error("state violation: a number was expected here");
                return -1;
            }

            // now get <modifier>
            try {
                current = tokens.nextToken();
            } catch (NoSuchElementException nsee) {
                logger.error("State violation: expecting a modifier" +
                                  " but no one found: Expires header will not be set");
            }
            if (current.equals("years")) {
                modifier = 365L * 24L * 60L * 60L * 1000L;
            } else if (current.equals("months")) {
                modifier = 30L * 24L * 60L * 60L * 1000L;
            } else if (current.equals("weeks")) {
                modifier = 7L * 24L * 60L * 60L * 1000L;
            } else if (current.equals("days")) {
                modifier = 24L * 60L * 60L * 1000L;
            } else if (current.equals("hours")) {
                modifier = 60L * 60L * 1000L;
            } else if (current.equals("minutes")) {
                modifier = 60L * 1000L;
            } else if (current.equals("seconds")) {
                modifier = 1000L;
            } else {
                logger.error("Bad modifier (" + current +
                                  "): ignoring expires configuration");
                return -1;
            }
            expires += number * modifier;
        }

        return expires;
    }

}
