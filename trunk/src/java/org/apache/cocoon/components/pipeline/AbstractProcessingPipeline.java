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
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.logger.AbstractLogEnabled;
import org.apache.avalon.framework.parameters.ParameterException;
import org.apache.avalon.framework.parameters.Parameterizable;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ConnectionResetException;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.Processor;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Response;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.generation.Generator;
import org.apache.cocoon.reading.Reader;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.apache.cocoon.sitemap.SitemapParameters;
import org.apache.cocoon.transformation.Transformer;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLProducer;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.SAXException;

/**
 * This is the base for all implementations of a <code>ProcessingPipeline</code>.
 *
 * @since 2.1
 * @author <a href="mailto:cziegeler@apache.org">Carsten Ziegeler</a>
 * @version CVS $Id: AbstractProcessingPipeline.java,v 1.35 2004/03/11 14:48:30 cziegeler Exp $
 */
public abstract class AbstractProcessingPipeline
  extends AbstractLogEnabled
  implements ProcessingPipeline, Parameterizable, Recyclable, Serviceable {

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

    // Reader stuff
    protected Reader reader;
    protected Parameters readerParam;
    protected String readerSource;
    protected String readerMimeType;

    /** This is the last component in the pipeline, either the serializer
     *  or a custom xmlconsumer for the cocoon: protocol etc.
     */
    protected XMLConsumer lastConsumer;

    /** the service manager set with service() */
    protected ServiceManager manager;

    /** the service manager set with service() and reservice() */
    protected ServiceManager newManager;

    /** The configuration */
    protected Parameters configuration;

    /** The parameters */
    protected Parameters parameters;

    /** Expires value */
    protected long expires;

    /** Configured Expires value */
    protected long configuredExpires;

    /** Configured Output Buffer Size */
    protected int  configuredOutputBufferSize;
    
    /** Output Buffer Size */
    protected int  outputBufferSize;

    /** The current Processor */
    protected Processor processor;
    
    /* (non-Javadoc)
     * @see org.apache.avalon.framework.service.Serviceable#service(org.apache.avalon.framework.service.ServiceManager)
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        this.manager = manager;
        this.newManager = manager;
    }

    /**
     * Reserviceable Interface
     */
    public void reservice (ServiceManager manager)
    throws ServiceException {
        this.newManager = manager;
    }

    /**
     * Parameterizable Interface - Configuration
     */
    public void parameterize(Parameters params) 
    throws ParameterException {
        this.configuration = params;
        final String expiresValue = params.getParameter("expires", null);
        if (expiresValue != null) {
            this.configuredExpires = parseExpires(expiresValue);
        }
        this.configuredOutputBufferSize = params.getParameterAsInteger("outputBufferSize", -1);
    }

    /**
     * Setup this component
     */
    public void setup(Parameters params) {
        this.parameters = params;
        final String expiresValue = params.getParameter("expires", null);
        if (expiresValue != null) {
            this.expires = parseExpires(expiresValue);
        } else {
            this.expires = this.configuredExpires;
        }
        this.outputBufferSize = params.getParameterAsInteger("outputBufferSize",
                                                              this.configuredOutputBufferSize);
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
    public void informBranchPoint() {}

    /**
     * Set the generator that will be used as the initial step in the pipeline.
     * The generator role is given : the actual <code>Generator</code> is fetched
     * from the latest <code>ComponentManager</code> given by <code>compose()</code>
     * or <code>recompose()</code>.
     *
     * @param hint the generator hint in the component manager.
     * @param source the source where to produce XML from, or <code>null</code> if no
     *        source is given.
     * @param param the parameters for the generator.
     * @throws ProcessingException if the generator couldn't be obtained.
     */
    public void setGenerator (String hint, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        if (this.generator != null) {
            throw new ProcessingException ("Generator already set. Cannot set generator '" + hint +
                "' at " + getLocation(param));
        }
        if (this.reader != null) {
            throw new ProcessingException ("Reader already set. Cannot set generator '" + hint +
                "' at " + getLocation(param));
        }
        try {
            String role = hint == null ? Generator.ROLE : Generator.ROLE + "/" + hint;
            this.generator = (Generator) newManager.lookup(role);
        } catch (ServiceException ce) {
            throw new ProcessingException("Lookup of generator failed at " + getLocation(param), ce);
        }
        this.generatorSource = source;
        this.generatorParam = param;
    }

    /**
     * Add a transformer at the end of the pipeline.
     * The transformer role is given : the actual <code>Transformer</code> is fetched
     * from the latest <code>ComponentManager</code> given by <code>compose()</code>
     * or <code>recompose()</code>.
     *
     * @param hint the transformer hint in the component manager.
     * @param source the source used to setup the transformer (e.g. XSL file), or
     *        <code>null</code> if no source is given.
     * @param param the parameters for the transfomer.
     * @throws ProcessingException if the generator couldn't be obtained.
     */
    public void addTransformer (String hint, String source, Parameters param, Parameters hintParam)
    throws ProcessingException {
        if (this.reader != null) {
            // Should normally never happen as setting a reader starts pipeline processing
            throw new ProcessingException ("Reader already set. Cannot add transformer '" + hint +
                "' at " + getLocation(param));
        }
        if (this.generator == null) {
            throw new ProcessingException ("Must set a generator before adding transformer '" + hint +
                "' at " + getLocation(param));   
        }
        try {
            String role = hint == null ? Transformer.ROLE : Transformer.ROLE + "/" + hint;
            this.transformers.add(newManager.lookup(role));
        } catch (ServiceException ce) {
            throw new ProcessingException("Lookup of transformer '" + hint + "' failed at " + getLocation(param), ce);
        }
        this.transformerSources.add(source);
        this.transformerParams.add(param);
    }

    /**
     * Set the serializer for this pipeline
     * @param mimeType Can be null
     */
    public void setSerializer (String hint, String source, Parameters param, Parameters hintParam, String mimeType)
    throws ProcessingException {
        if (this.serializer != null) {
            // Should normally not happen as adding a serializer starts pipeline processing
            throw new ProcessingException ("Serializer already set. Cannot set serializer '" + hint +
                "' at " + getLocation(param));
        }
        if (this.reader != null) {
            // Should normally never happen as setting a reader starts pipeline processing
            throw new ProcessingException ("Reader already set. Cannot set serializer '" + hint +
                "' at " + getLocation(param));
        }
        if (this.generator == null) {
            throw new ProcessingException ("Must set a generator before setting serializer '" + hint +
                "' at " + getLocation(param));
        }

        try {
            String role = hint == null ? Serializer.ROLE : Serializer.ROLE + "/" + hint;
            this.serializer = (Serializer) newManager.lookup(role);
        } catch (ServiceException ce) {
            throw new ProcessingException("Lookup of serializer '" + hint + "' failed at " + getLocation(param), ce);
        }
        this.serializerSource = source;
        this.serializerParam = param;
        this.serializerMimeType = mimeType;
        this.lastConsumer = this.serializer;
    }

    /**
     * Set the reader for this pipeline
     * @param mimeType Can be null
     */
    public void setReader (String hint, String source, Parameters param, String mimeType)
    throws ProcessingException {
        if (this.reader != null) {
            // Should normally never happen as setting a reader starts pipeline processing
            throw new ProcessingException ("Reader already set. Cannot set reader '" + hint +
                "' at " + getLocation(param));
        }
        if (this.generator != null) {
            // Should normally never happen as setting a reader starts pipeline processing
            throw new ProcessingException ("Generator already set. Cannot use reader '" + hint +
                "' at " + getLocation(param));
        }

        try {
            String role = hint == null ? Reader.ROLE : Reader.ROLE + "/" + hint;
            this.reader = (Reader) newManager.lookup(role);
        } catch (ServiceException ce) {
            throw new ProcessingException("Lookup of reader '" + hint + "' failed at " + getLocation(param), ce);
        }
        this.readerSource = source;
        this.readerParam = param;
        this.readerMimeType = mimeType;
    }

    /**
     * Sanity check
     * @return true if the pipeline is 'sane', false otherwise.
     */
    protected boolean checkPipeline() {
        if ( this.generator == null && this.reader == null) {
            return false;
        }

        if ( this.generator != null && this.serializer == null ) {
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
            // setup the generator
            this.generator.setup(
                this.processor.getSourceResolver(),
                environment.getObjectModel(),
                generatorSource,
                generatorParam
            );

            Iterator transformerItt = this.transformers.iterator();
            Iterator transformerSourceItt = this.transformerSources.iterator();
            Iterator transformerParamItt = this.transformerParams.iterator();

            while (transformerItt.hasNext()) {
                Transformer trans = (Transformer)transformerItt.next();
                trans.setup(this.processor.getSourceResolver(),
                            environment.getObjectModel(),
                            (String)transformerSourceItt.next(),
                            (Parameters)transformerParamItt.next());
            }

            if (this.serializer instanceof SitemapModelComponent) {
                ((SitemapModelComponent)this.serializer).setup(
                this.processor.getSourceResolver(),
                    environment.getObjectModel(),
                    this.serializerSource,
                    this.serializerParam
                );
            }

            String mimeType = this.serializer.getMimeType();
            if (mimeType != null) {
                // we have a mimeType from the component itself
                environment.setContentType (mimeType);
            } else if (serializerMimeType != null) {
                // there was a mimeType specified in the sitemap pipeline
                environment.setContentType (serializerMimeType);
            } else {
                // No mimeType available
                String message = "Unable to determine MIME type for " +
                    environment.getURIPrefix() == null ? "" : environment.getURIPrefix()
                    + "/" + environment.getURI();
                throw new ProcessingException(message);
            }
        } catch (SAXException e) {
            throw new ProcessingException(
                "Could not setup pipeline.",
                e
            );
        } catch (IOException e) {
            throw new ProcessingException(
                "Could not setup pipeline.",
                e
            );
        }
    }

    /**
     * Connect the next component
     */
    protected void connect(Environment environment,
                           XMLProducer producer,
                           XMLConsumer consumer)
    throws ProcessingException {
        XMLProducer next = producer;
        // Connect next component.
        next.setConsumer(consumer);
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
     * Process the given <code>Environment</code>, producing the output.
     */
    public boolean process(Environment environment)
    throws ProcessingException {
        // If this is an internal request, lastConsumer was reset!
        if (null == this.lastConsumer) {
            this.lastConsumer = this.serializer;
        } else {
            this.preparePipeline(environment);
        }
        if ( this.reader != null ) {
            this.preparePipeline(environment);            
        }
        
        // See if we need to set an "Expires:" header
        if (this.expires != 0) {
            Response res = ObjectModelHelper.getResponse(environment.getObjectModel());
            res.setDateHeader("Expires", System.currentTimeMillis() + expires);
            res.setHeader("Cache-Control", "max-age=" + expires/1000 + ", public");
            if (getLogger().isDebugEnabled()) {
                getLogger().debug("Setting a new Expires object for this resource");
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
     * Prepare the pipeline 
     */
    protected void preparePipeline(Environment environment)
    throws ProcessingException {
        // TODO (CZ) Get the processor set via IoC
        this.processor = EnvironmentHelper.getCurrentProcessor();
        if ( !checkPipeline() ) {
            throw new ProcessingException("Attempted to process incomplete pipeline.");
        }

        if ( this.reader != null ) {
            this.setupReader( environment );
        } else {
            this.setupPipeline(environment);
        }
    }
    
    /**
     * Prepare an internal processing
     * @param environment          The current environment.
     * @throws ProcessingException
     */
    public void prepareInternal(Environment environment)
    throws ProcessingException {
        this.lastConsumer = null;
        this.preparePipeline(environment);
    }
    
    /**
     * Process the SAX event pipeline
     */
    protected boolean processXMLPipeline(Environment environment)
    throws ProcessingException {

        try {
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
                    byte[] data = os.toByteArray();
                    environment.setContentLength(data.length);
                    environment.getOutputStream(0).write(data);
                } else {
                    // set the output stream
                    this.serializer.setOutputStream(environment.getOutputStream(this.outputBufferSize));
                    // execute the pipeline:
                    this.generator.generate();
                }
            }
        } catch ( ProcessingException e ) {
            throw e;
        } catch ( Exception e ) {
            // TODO: Unwrap SAXException ?
            throw new ProcessingException("Failed to execute pipeline.", e);
        }
        return true;
    }

    /**
     * Setup the reader
     */
    protected void setupReader(Environment environment)
    throws ProcessingException {
        try {
            this.reader.setup(this.processor.getSourceResolver(),environment.getObjectModel(),readerSource,readerParam);
            // Set the mime-type
            // the behaviour has changed from 2.1.x to 2.2 according to bug #10277:
            // MIME type declared on the reader instance
            // MIME type declared for the reader component
            // Ask the Reader for a MIME type:
            //     A *.doc reader could peek into the file
            //     and return either text/plain or application/vnd.msword or
            //     the reader can use MIME type declared in WEB-INF/web.xml or 
            //     by the server.

            if ( this.readerMimeType != null ) {
                environment.setContentType(this.readerMimeType);                
            } else {
                String mimeType = this.reader.getMimeType();
                if ( mimeType != null ) {
                    environment.setContentType(mimeType);                    
                }
            }
        } catch (SAXException e){
            throw new ProcessingException("Failed to execute reader pipeline.", e);
        } catch (IOException e){
            throw new ProcessingException("Failed to execute reader pipeline.", e);
        }
    }

    protected boolean checkIfModified(Environment environment,
                                        long lastModified)
    throws ProcessingException {
        // has the read resource been modified?
        if(!environment.isResponseModified(lastModified)) {

            // environment supports this, so we are finished
            environment.setResponseIsNotModified();
            return true;
        }
        return false;
    }

    /**
     * Process the pipeline using a reader.
     * @throws ProcessingException if
     */
    protected boolean processReader(Environment environment)
    throws ProcessingException {
        try {
            if (this.reader.shouldSetContentLength()) {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                this.reader.setOutputStream(os);
                this.reader.generate();
                byte[] data = os.toByteArray();
                environment.setContentLength(data.length);
                environment.getOutputStream(0).write(data);
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
                throw new ProcessingException("Failed to execute reader pipeline.", se);
            }
        } catch ( ProcessingException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new ProcessingException("Error executing reader pipeline.",e);
        }
        return true;
    }

    public void recycle() {
        // release reader.
        if ( this.reader != null) {
            this.newManager.release( this.reader );
            this.reader = null;
            this.readerParam = null;
        }

        // Release generator.
        if ( this.generator != null) {
            this.newManager.release( this.generator );
            this.generator = null;
            this.generatorParam = null;
        }

        // Release transformers
        int size = this.transformers.size();
        for (int i = 0; i < size; i++) {
            newManager.release( this.transformers.get(i) );
        }
        this.transformers.clear();
        this.transformerParams.clear();
        this.transformerSources.clear();

        // release serializer
        if ( this.serializer != null ) {
            this.newManager.release( this.serializer );
            this.serializerParam = null;
        }
        this.serializer = null;
        this.parameters = null;
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
        if ( this.reader != null ) {
            throw new ProcessingException("Streaming of an internal pipeline is not possible with a reader.");
        } else {
            this.connectPipeline(environment);
            return this.processXMLPipeline(environment);
        }
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

    /**
     * Parse the expires parameter
     */
    private long parseExpires(String expire) {
        StringTokenizer tokens = new StringTokenizer(expire);

        // get <base>
        String current = tokens.nextToken();
        if (current.equals("modification")) {
            getLogger().warn("the \"modification\" keyword is not yet" +
                             " implemented. Assuming \"now\" as the base attribute");
            current = "now";
        }

        if (!current.equals("now") && !current.equals("access")) {
            getLogger().error("bad <base> attribute, Expires header will not be set");
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
                getLogger().error("state violation: a number was expected here");
                return -1;
            }

            // now get <modifier>
            try {
                current = tokens.nextToken();
            } catch (NoSuchElementException nsee) {
                getLogger().error("State violation: expecting a modifier" +
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
                getLogger().error("Bad modifier (" + current +
                                  "): ignoring expires configuration");
                return -1;
            }
            expires += number * modifier;
        }

        return expires;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.components.pipeline.ProcessingPipeline#getKeyForEventPipeline()
     */
    public String getKeyForEventPipeline() {
        return null;
    }
    
    protected String getLocation(Parameters param) {
        String value = null;
        if ( param instanceof SitemapParameters ) {
            value = ((SitemapParameters)param).getStatementLocation();
        }
        if ( value == null ) {
            value = "[unknown location]";
        }
        return value;
    }
}
