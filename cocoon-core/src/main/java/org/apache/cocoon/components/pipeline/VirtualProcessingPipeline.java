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
package org.apache.cocoon.components.pipeline;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.SitemapErrorHandler;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.util.location.Locatable;
import org.apache.cocoon.util.location.Location;
import org.apache.cocoon.xml.SaxBuffer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * Pipeline used by virtual pipeline components
 *
 * @version $Id$
 */
public class VirtualProcessingPipeline extends AbstractLogEnabled
                                       implements ProcessingPipeline, Recyclable, Serviceable {

    // Resolver of the defining sitemap
    private SourceResolver resolver;

    // Generator stuff
    protected Generator generator;
    protected Parameters generatorParam;
    protected String generatorSource;

    // Transformer stuff
    protected ArrayList transformers = new ArrayList();
    protected ArrayList transformerParams = new ArrayList();
    protected ArrayList transformerSources = new ArrayList();

    // Serializer stuff
    protected Serializer serializer;
    protected Parameters serializerParam;
    protected String serializerSource;
    protected String serializerMimeType;

    // Error handler stuff
    private SitemapErrorHandler errorHandler;
    private Processor.InternalPipelineDescription errorPipeline;

    /**
     * True when pipeline has been prepared.
     */
    private boolean prepared;

    /**
     * This is the first consumer component in the pipeline, either
     * the first transformer or the serializer.
     */
    protected XMLConsumer firstConsumer;

    /**
     * This is the last component in the pipeline, either the serializer
     * or a custom xmlconsumer for the cocoon: protocol etc.
     */
    protected XMLConsumer lastConsumer;

    /** The component manager set with compose() */
    protected ServiceManager manager;

    /** The component manager set with compose() and recompose() */
    protected ServiceManager newManager;

    /** The current Processor */
    protected Processor processor;


    public VirtualProcessingPipeline() {
    }

    public void service(ServiceManager manager)
    throws ServiceException {
        this.manager = manager;
        this.newManager = manager;
        this.resolver = (SourceResolver)this.manager.lookup(SourceResolver.ROLE + "/Local");
    }

    /**
     * Set the processor's service manager
     */
    public void setProcessorManager(ServiceManager manager) {
        this.newManager = manager;
    }

    /**
     * Setup this component
     */
    public void setup(Parameters params) {
    }

    /**
     * Get the generator - used for content aggregation
     */
    public Generator getGenerator() {
        return this.generator;
    }

    /**
     * Informs pipeline we have come across a branch point
     * Default Behaviour is do nothing
     */
    public void informBranchPoint() {
        // this can be overwritten in subclasses
    }

    /**
     * Set the generator that will be used as the initial step in the pipeline.
     * The generator role is given : the actual <code>Generator</code> is fetched
     * from the latest <code>ServiceManager</code>.
     *
     * @param role the generator role in the component manager.
     * @param source the source where to produce XML from, or <code>null</code> if no
     *        source is given.
     * @param param the parameters for the generator.
     * @throws org.apache.cocoon.ProcessingException if the generator couldn't be obtained.
     */
    public void setGenerator(String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        if (this.generator != null) {
            throw new ProcessingException ("Generator already set. Cannot set generator '" + role + "'",
                getLocation(param));
        }

        try {
            this.generator = (Generator) this.newManager.lookup(Generator.ROLE + '/' + role);
        } catch (ServiceException ce) {
            throw ProcessingException.throwLocated("Lookup of generator '" + role + "' failed", ce, getLocation(param));
        }

        this.generatorSource = source;
        this.generatorParam = param;
    }

    /**
     * Add a transformer at the end of the pipeline.
     * The transformer role is given : the actual <code>Transformer</code> is fetched
     * from the latest <code>ServiceManager</code>.
     *
     * @param role the transformer role in the component manager.
     * @param source the source used to setup the transformer (e.g. XSL file), or
     *        <code>null</code> if no source is given.
     * @param param the parameters for the transfomer.
     */
    public void addTransformer(String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        try {
            this.transformers.add(this.newManager.lookup(Transformer.ROLE + '/' + role));
        } catch (ServiceException ce) {
            throw ProcessingException.throwLocated("Lookup of transformer '"+role+"' failed", ce, getLocation(param));
        }
        this.transformerSources.add(source);
        this.transformerParams.add(param);
    }

    /**
     * Set the serializer for this pipeline
     * @param mimeType Can be null
     */
    public void setSerializer(String role, String source, Parameters param, Parameters hintParam, String mimeType)
    throws ProcessingException {
        if (this.serializer != null) {
            // Should normally not happen as adding a serializer starts pipeline processing
            throw new ProcessingException ("Serializer already set. Cannot set serializer '" + role + "'",
                    getLocation(param));
        }

        try {
            this.serializer = (Serializer)this.newManager.lookup(Serializer.ROLE + '/' + role);
        } catch (ServiceException ce) {
            throw ProcessingException.throwLocated("Lookup of serializer '" + role + "' failed", ce, getLocation(param));
        }
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
        this.lastConsumer = this.serializer;
    }

    /**
     * Sets error handler for this pipeline.
     * Used for handling errors in the internal pipelines.
     * @param errorHandler error handler
     */
    public void setErrorHandler(SitemapErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    /**
     * Sanity check
     * @return true if the pipeline is 'sane', for VPCs all pipelines are sane
     */
    protected boolean checkPipeline() {
        return true;
    }

    /**
     * Setup pipeline components.
     */
    protected void setupPipeline(Environment environment)
    throws ProcessingException {
        try {
            // SourceResolver resolver = this.processor.getSourceResolver();

            // setup the generator
            if (this.generator != null) {
                this.generator.setup(resolver,
                                     environment.getObjectModel(),
                                     generatorSource,
                                     generatorParam);
            }

            Iterator transformerItt = this.transformers.iterator();
            Iterator transformerSourceItt = this.transformerSources.iterator();
            Iterator transformerParamItt = this.transformerParams.iterator();

            while (transformerItt.hasNext()) {
                Transformer trans = (Transformer)transformerItt.next();
                trans.setup(resolver,
                            environment.getObjectModel(),
                            (String)transformerSourceItt.next(),
                            (Parameters)transformerParamItt.next()
                );
            }

            if (this.serializer != null && this.serializer instanceof SitemapModelComponent) {
                ((SitemapModelComponent)this.serializer).setup(
                    resolver,
                    environment.getObjectModel(),
                    this.serializerSource,
                    this.serializerParam
                );
            }

        } catch (SAXException e) {
            throw new ProcessingException("Could not setup pipeline.", e);
        } catch (IOException e) {
            throw new ProcessingException("Could not setup pipeline.", e);
        }
    }

    /**
     * Connect the next component
     */
    protected void connect(Environment environment,
                           XMLProducer producer,
                           XMLConsumer consumer)
    throws ProcessingException {
        // Connect next component.
        producer.setConsumer(consumer);
    }

    /**
     * Connect the XML pipeline.
     */
    protected void connectPipeline(Environment environment)
    throws ProcessingException {
        XMLProducer prev = this.generator;

        Iterator itt = this.transformers.iterator();

        // No generator for VPC transformer and serializer 
        if (this.generator == null && itt.hasNext()) {
            this.firstConsumer = (XMLConsumer)(prev = (XMLProducer)itt.next());
        }

        while (itt.hasNext()) {
            Transformer next = (Transformer) itt.next();
            connect(environment, prev, next);
            prev = next;
        }
        
        // insert the serializer
        if (prev != null) {
            connect(environment, prev, this.lastConsumer);
        } else {
            this.firstConsumer = this.lastConsumer;
        }
    }

    /**
     * Prepare the pipeline
     */
    protected void preparePipeline(Environment environment)
    throws ProcessingException {
        // TODO (CZ) Get the processor set via IoC
        this.processor = EnvironmentHelper.getCurrentProcessor();
        if (!checkPipeline()) {
            throw new ProcessingException("Attempted to process incomplete pipeline.");
        }

        if (this.prepared) {
            throw new ProcessingException("Duplicate preparePipeline call caught.");
        }

        setupPipeline(environment);
        this.prepared = true;
    }

    /**
     * Prepare an internal processing
     * @param environment          The current environment.
     * @throws org.apache.cocoon.ProcessingException
     */
    public void prepareInternal(Environment environment)
    throws ProcessingException {
        this.lastConsumer = null;
        try {
            preparePipeline(environment);
        } catch (ProcessingException e) {
            prepareInternalErrorHandler(environment, e);
        }
    }

    /**
     * If prepareInternal fails, prepare internal error handler.
     */
    protected void prepareInternalErrorHandler(Environment environment, ProcessingException ex)
    throws ProcessingException {
        if (this.errorHandler != null) {
            try {
                this.errorPipeline = this.errorHandler.prepareErrorPipeline(ex);
                if (this.errorPipeline != null) {
                    this.errorPipeline.processingPipeline.prepareInternal(environment);
                    return;
                }
            } catch (ProcessingException e) {
                throw e;
            } catch (Exception e) {
                throw new ProcessingException("Failed to handle exception <" + ex + ">", e);
            }
        }
    }

    /**
     * @return true if error happened during internal pipeline prepare call.
     */
    protected boolean isInternalError() {
        return this.errorPipeline != null;
    }

    public void setReader(String role, String source, Parameters param, String mimeType) throws ProcessingException {
        throw new UnsupportedOperationException();
    }

    public boolean process(Environment environment) throws ProcessingException {
        if (!this.prepared) {
            preparePipeline(environment);
        }

        // If this is an internal request, lastConsumer was reset!
        if (this.lastConsumer == null) {
            this.lastConsumer = this.serializer;
        }
        
        connectPipeline(environment);
        return processXMLPipeline(environment);
    }

    /**
     * Process the given <code>Environment</code>, but do not use the
     * serializer. Instead the sax events are streamed to the XMLConsumer.
     */
    public boolean process(Environment environment, XMLConsumer consumer)
    throws ProcessingException {
        // Exception happened during setup and was handled
        if (this.errorPipeline != null) {
            return this.errorPipeline.processingPipeline.process(environment, consumer);
        }

        // Have to buffer events if error handler is specified.
        SaxBuffer buffer = null;
        this.lastConsumer = this.errorHandler == null? consumer: (buffer = new SaxBuffer());
        try {
            connectPipeline(environment);
            return processXMLPipeline(environment);
        } catch (ProcessingException e) {
            buffer = null;
            return processErrorHandler(environment, e, consumer);
        } finally {
            if (buffer != null) {
                try {
                    buffer.toSAX(consumer);
                } catch (SAXException e) {
                    throw new ProcessingException("Failed to execute pipeline.", e);
                }
            }
        }
    }

    /**
     * Get the first consumer - used for VPC transformers
     */
    public XMLConsumer getXMLConsumer(Environment environment, XMLConsumer consumer)
        throws ProcessingException {
        if (!this.prepared) {
            preparePipeline(environment);
        }

        this.lastConsumer = consumer;
        connectPipeline(environment);

        if (this.firstConsumer == null)
            throw new ProcessingException("A VPC transformer pipeline should not contain a generator.");

        return this.firstConsumer;
    }

    /**
     * Get the first consumer - used for VPC serializers
     */
    public XMLConsumer getXMLConsumer(Environment environment) throws ProcessingException {
        if (!this.prepared) {
            preparePipeline(environment);
        }

        // If this is an internal request, lastConsumer was reset!
        if (this.lastConsumer == null) {
            this.lastConsumer = this.serializer;
        }
        
        connectPipeline(environment);

        if (this.serializer == null) {
            throw new ProcessingException("A VPC serializer pipeline must contain a serializer.");
        }

        try {
            this.serializer.setOutputStream(environment.getOutputStream(0));
        } catch (Exception e) {
            throw new ProcessingException("Couldn't set output stream ", e);
        }
            
        if (this.firstConsumer == null)
            throw new ProcessingException("A VPC serializer pipeline should not contain a generator.");

        return this.firstConsumer;
    }

    /**
     * Get the mime-type for the serializer
     */
    public String getMimeType() {
        if (this.lastConsumer == null) {
            // internal processing: text/xml
            return "text/xml";
        } else {
            // Get the mime-type
            if (serializerMimeType != null) {
                // there was a serializer defined in the sitemap
                return serializerMimeType;
            } else {
                // ask to the component itself
                return this.serializer.getMimeType();
            }
        }
    }

    /**
     * Test if the serializer wants to set the content length
     */
    public boolean shouldSetContentLength() {
        return this.serializer.shouldSetContentLength();
    }

    /**
     * Process the SAX event pipeline
     */
    protected boolean processXMLPipeline(Environment environment)
    throws ProcessingException {

        try {
            if (this.lastConsumer == null || this.serializer == null) {
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
                    os.writeTo(environment.getOutputStream(0));
                } else {
                    // set the output stream
                    this.serializer.setOutputStream(environment.getOutputStream(0));
                    // execute the pipeline:
                    this.generator.generate();
                }
            }
        } catch (ProcessingException e) {
            throw e;
        } catch (Exception e) {
            // TODO: Unwrap SAXException ?
            throw new ProcessingException("Failed to execute pipeline.", e);
        }
        return true;
    }

    public void recycle() {
        this.prepared = false;

        if (this.generator != null) {
            // Release generator.
            this.newManager.release(this.generator);
            this.generator = null;
            this.generatorParam = null;
        }

        // Release transformers
        int size = this.transformers.size();
        for (int i = 0; i < size; i++) {
            this.newManager.release(this.transformers.get(i));
        }
        this.transformers.clear();
        this.transformerParams.clear();
        this.transformerSources.clear();

        // Release serializer
        if (this.serializer != null) {
            this.newManager.release(this.serializer);
            this.serializerParam = null;
        }
        this.serializer = null;
        this.processor = null;
        this.lastConsumer = null;
        this.firstConsumer = null;

        // Release error handler
        this.errorHandler = null;
        if (this.errorPipeline != null) {
            this.errorPipeline.release();
            this.errorPipeline = null;
        }
    }

    protected boolean processErrorHandler(Environment environment, ProcessingException e, XMLConsumer consumer)
    throws ProcessingException {
        if (this.errorHandler != null) {
            try {
                this.errorPipeline = this.errorHandler.prepareErrorPipeline(e);
                if (this.errorPipeline != null) {
                    this.errorPipeline.processingPipeline.prepareInternal(environment);
                    return this.errorPipeline.processingPipeline.process(environment, consumer);
                }
            } catch (Exception ignored) {
                getLogger().debug("Exception in error handler", ignored);
            }
        }

        throw e;
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

    public String getKeyForEventPipeline() {
        return null;
    }

    protected Location getLocation(Parameters param) {
        Location value = null;
        if (param instanceof Locatable) {
            value = ((Locatable) param).getLocation();
        }
        if (value == null) {
            value = Location.UNKNOWN;
        }
        return value;
    }
}
