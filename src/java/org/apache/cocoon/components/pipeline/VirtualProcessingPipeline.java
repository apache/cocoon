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

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.context.Context;

import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.Constants;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.sitemap.SitemapParameters;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;

import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Pipeline used by virtual pipeline components
 *
 * @version CVS $Id$
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

    /**
     * True when pipeline has been prepared.
     */
    private boolean prepared;

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


    public VirtualProcessingPipeline(Context context) throws Exception {
        this.resolver = (EnvironmentHelper) context.get(Constants.CONTEXT_ENV_HELPER);
    }

    public void service (ServiceManager manager)
    throws ServiceException {
        this.manager = manager;
        this.newManager = manager;
    }

    /**
     * Set the processor's service manager
     */
    public void setProcessorManager (ServiceManager manager) {
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
    public void setGenerator (String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        if (this.generator != null) {
            throw new ProcessingException ("Generator already set. Cannot set generator '" + role +
                                           "' at " + getLocation(param));
        }

        try {
            this.generator = (Generator) this.newManager.lookup(Generator.ROLE + '/' + role);
        } catch (ServiceException ce) {
            throw new ProcessingException("Lookup of generator '" + role + "' failed at " + getLocation(param), ce);
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
     * @throws org.apache.cocoon.ProcessingException if the generator couldn't be obtained.
     */
    public void addTransformer (String role, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        if (this.generator == null) {
            throw new ProcessingException ("Must set a generator before adding transformer '" + role +
                                           "' at " + getLocation(param));
        }

        try {
            this.transformers.add(this.newManager.lookup(Transformer.ROLE + '/' + role));
        } catch (ServiceException ce) {
            throw new ProcessingException("Lookup of transformer '"+role+"' failed at " + getLocation(param), ce);
        }
        this.transformerSources.add(source);
        this.transformerParams.add(param);
    }

    /**
     * Set the serializer for this pipeline
     * @param mimeType Can be null
     */
    public void setSerializer (String role, String source, Parameters param, Parameters hintParam, String mimeType)
    throws ProcessingException {
        if (this.serializer != null) {
            // Should normally not happen as adding a serializer starts pipeline processing
            throw new ProcessingException ("Serializer already set. Cannot set serializer '" + role +
                                           "' at " + getLocation(param));
        }

        if (this.generator == null) {
            throw new ProcessingException ("Must set a generator before setting serializer '" + role +
                                           "' at " + getLocation(param));
        }

        try {
            this.serializer = (Serializer)this.newManager.lookup(Serializer.ROLE + '/' + role);
        } catch (ServiceException ce) {
            throw new ProcessingException("Lookup of serializer '" + role + "' failed at " + getLocation(param), ce);
        }
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
        this.lastConsumer = this.serializer;
    }

    /**
     * Sanity check
     * @return true if the pipeline is 'sane', false otherwise.
     */
    protected boolean checkPipeline() {
        if (this.generator == null) {
            return false;
        }

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
            this.generator.setup(
                resolver,
                environment.getObjectModel(),
                generatorSource,
                generatorParam
            );

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

            if (this.serializer instanceof SitemapModelComponent) {
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
        while (itt.hasNext()) {
            Transformer next = (Transformer) itt.next();
            connect(environment, prev, next);
            prev = next;
        }

        // insert the serializer
        connect(environment, prev, this.lastConsumer);
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
        preparePipeline(environment);
    }

    public void setReader(String role, String source, Parameters param, String mimeType) throws ProcessingException {
        throw new UnsupportedOperationException();
    }

    public boolean process(Environment environment) throws ProcessingException {
        throw new UnsupportedOperationException();
    }

    /**
     * Process the SAX event pipeline
     */
    protected boolean processXMLPipeline(Environment environment)
    throws ProcessingException {

        try {
            this.generator.generate();
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
    }

    /**
     * Process the given <code>Environment</code>, but do not use the
     * serializer. Instead the sax events are streamed to the XMLConsumer.
     */
    public boolean process(Environment environment, XMLConsumer consumer)
    throws ProcessingException {
        this.lastConsumer = consumer;
        connectPipeline(environment);
        return processXMLPipeline(environment);
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

    protected String getLocation(Parameters param) {
        String value = null;
        if (param instanceof SitemapParameters) {
            value = ((SitemapParameters) param).getStatementLocation();
        }
        if (value == null) {
            value = "[unknown location]";
        }
        return value;
    }
}
