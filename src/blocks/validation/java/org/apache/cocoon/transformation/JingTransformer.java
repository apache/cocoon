/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
package org.apache.cocoon.transformation;

import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.caching.CacheableProcessingComponent;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.xml.ContentHandlerWrapper;
import org.apache.cocoon.xml.XMLConsumer;
import org.apache.cocoon.xml.XMLMulticaster;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.apache.excalibur.store.Store;
import org.apache.excalibur.xml.EntityResolver;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.Schema;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import com.thaiopensource.xml.sax.XMLReaderCreator;

/**
 * <p>The {@link JingTransformer} provides a very simple {@link Transformer}
 * validating documents using <a href="http://relax-ng.org/">RELAX-NG</a> grammar
 * through the <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a>
 * validation engine.</p>
 * 
 * <p>The only defined (but not required) configuration for this component is
 * <code>&lt;enable-caching&gt;<i>true|false</i>&lt;/enable-caching&gt;</code>
 * indicating whether compiled schema files must be cached in Cocoon's
 * transient store.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class JingTransformer extends AbstractTransformer
implements Configurable, Serviceable, Disposable, XMLReaderCreator,
           CacheableProcessingComponent, EntityResolver {

    /** <p>The {@link SchemaReader} to use for reading RNG schemas.</p> */
    private final SchemaReader schemaReader = SAXSchemaReader.getInstance();
    /** <p>The current {@link Stack} of {@link InputSource}s being parsed. </p> */
    private final Stack parsedSourceStack = new Stack();

    /* VARIABLES CREATED BY SERVICE() OR CONFIGURE() AND DESTROYED BY DISPOSE() */

    /** <p>The {@link ServiceManager} associated with this instance.</p> */
    private ServiceManager serviceManager = null;
    /** <p>Cocoon's global {@link EntityResolver} for catalog resolution.</p> */
    private EntityResolver entityResolver = null;
    /** <p>Cocoon's global temporary cache {@link Store} for parsed schemas.</p> */
    private Store transientStore = null;
    /** <p>The {@link PropertyMap} to use with JING's factories.</p> */
    private PropertyMap validatorProperties = null;
    /** <p>A {@link String} for unique IDs stored into caches.</p> */
    private String uniqueIdentifier = null;
    /** <p>A flag indicating whether caching is enabled or not.</p> */
    private boolean cachingEnabled = true;

    /* TEMPORARY VARIABLES SET BY SETUP() AND RESET BY RECYCLE() */

    /** <p>The {@link InputSource} associated with the current schema.</p> */
    private InputSource inputSource = null;
    /** <p>The {@link SourceValidity} associated with the schema.</p> */
    private AggregatedValidity sourceValidity = null;
    /** <p>The {@link SourceResolver} to use for resolving URIs.</p> */
    private SourceResolver sourceResolver = null;
    /** <p>The {@link XMLConsumer} of the validator.</p> */
    private XMLConsumer validationHandler = null;

    
    /* =========================================================================== */
    /* COMPONENT LIFECYCLE METHODS: CONFIGURE(), SERVICE() AND DISPOSE()           */
    /* =========================================================================== */

    /**
     * <p>Contextualize this component instance specifying its associated
     * {@link ServiceManager} instance.</p>
     * 
     * @param manager the {@link ServiceManager} to associate with this component.
     * @throws ServiceException if a dependancy of this could not be resolved.
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        this.serviceManager = manager;
        this.transientStore = (Store) manager.lookup(Store.TRANSIENT_STORE);
        this.entityResolver = (EntityResolver) manager.lookup(EntityResolver.ROLE);

        PropertyMapBuilder builder = new PropertyMapBuilder();
        ValidateProperty.ENTITY_RESOLVER.put(builder, this);
        ValidateProperty.ERROR_HANDLER.put(builder, new DraconianErrorHandler());
        ValidateProperty.XML_READER_CREATOR.put(builder, this);
        this.validatorProperties = builder.toPropertyMap();
    }

    /**
     * <p>Configure this component instance.</p>
     * 
     * <p>The only defined (but not required) configuration for this component is
     * <code>&lt;enable-caching&gt;<i>true|false</i>&lt;/enable-caching&gt;</code>
     * indicating whether compiled schema files must be cached in Cocoon's
     * transient store.</p>
     *
     * @param configuration a{@link Configuration} instance for this component.
     * @throws ConfigurationException never thrown.
     */
    public void configure(Configuration configuration)
    throws ConfigurationException {
        configuration = configuration.getChild("enable-caching");
        this.cachingEnabled = configuration.getValueAsBoolean(true);
    }

    /**
     * <p>Dispose of this component instance releasing all previously acquired
     * required instances back to the {@link ServiceManager}.</p>
     */
    public void dispose() {
        this.serviceManager.release(this.entityResolver);
        this.serviceManager.release(this.transientStore);
        this.serviceManager = null;
        this.validatorProperties = null;
        this.cachingEnabled = true;
    }


    /* =========================================================================== */
    /* REQUEST LIFECYCLE METHODS: SETUP() AND RECYCLE()                            */
    /* =========================================================================== */

    /**
     * <p>Contextualize this component in the scope of a pipeline when a request
     * is processed.</p>
     * 
     * @param resolver the {@link SourceResolver} contextualized in this request.
     * @param objectModel unused.
     * @param source the source URI of the schema to validate against.
     * @param parameters unused.
     */
    public void setup(SourceResolver resolver, Map objectModel, String source,
                      Parameters parameters)
    throws ProcessingException, SAXException, IOException {
        this.parsedSourceStack.clear();
        this.parsedSourceStack.push(null);
        this.sourceValidity = new AggregatedValidity();
        this.sourceResolver = resolver;
        this.inputSource = this.resolveEntity(null, source);
        this.parsedSourceStack.push(this.inputSource);

        ContentHandler handler = this.newValidator().getContentHandler();
        this.validationHandler = new ContentHandlerWrapper(handler);
    }

    /**
     * <p>Specify the {@link XMLConsumer} receiving SAX events emitted by this
     * {@link Transformer} instance in the scope of a request.</p>
     *
     * @param consumer the {@link XMLConsumer} to send SAX events to.
     */
    public void setConsumer(XMLConsumer consumer) {
        super.setConsumer(new XMLMulticaster(this.validationHandler, consumer));
    }

    /**
     * <p>Return the unique key to associated with the schema being processed in
     * the scope of the request being processed for caching.</p>
     *
     * @return a non null {@link String} representing the unique key for the schema.
     */
    public Serializable getKey() {
        if (this.uniqueIdentifier == null) {
            StringBuffer buffer = new StringBuffer();
            buffer.append(this.getClass().getName());
            buffer.append(':');
            buffer.append(this.inputSource.getSystemId());
            this.uniqueIdentifier = buffer.toString();
        }
        return this.uniqueIdentifier;
    }

    /**
     * <p>Return the {@link SourceValidity} associated with the schema currently
     * being processed in the scope of the request being processed.</p>
     *
     * @return a non null {@link SourceValidity} instance.
     */
    public SourceValidity getValidity() {
        return this.sourceValidity;
    }

    /**
     * <p>Recycle this component instance at the end of request processing.</p>
     */
    public void recycle() {
        this.parsedSourceStack.clear();
        this.validationHandler = null;
        this.sourceResolver = null;
        this.sourceValidity = null;
        this.inputSource = null;

        super.recycle();
    }

    /* =========================================================================== */
    /* SAX2 ENTITY RESOLVER INTERFACE IMPLEMENTATION                               */
    /* =========================================================================== */

    /**
     * <p>Resolve an {@link InputSource} from a public ID and/or a system ID.</p>
     * 
     * <p>This method can be called only while a request is being processed (after
     * the {@link #setup(SourceResolver, Map, String, Parameters)} metod was called
     * but before the {@link #recycle()} method was called) and will resolve URIs
     * against a dynamic {@link Stack} of {@link InputSource}s.</p>
     * 
     * <p>Since <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a>
     * doesn't offer a complete URI resolution contract, a {@link Stack} is kept
     * for all the sources parsed while reading a schema. Keeping in mind that the
     * last {@link InputSource} pushed in the {@link Stack} can be considered to be
     * the "base URI" for the current resolution, full relative resolution of system
     * IDs can be achieved this way.<p>
     * 
     * <p>Note that this method of resolving URIs by keeping a {@link Stack} of
     * processed URIs is a <i>sort of</i> a hack, but mimics the internal state of
     * <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a> itself:
     * if URI resolution fails, the {@link Stack} analysis is the first part to
     * look at.</p>
     * 
     * <p>Resolution will use Cocoon's configured {@link EntityResolver} for
     * resolving public IDs, and Cocoon's {@link SourceResolver} to resolve the
     * system IDs and accessing the underlying byte streams of the entities.</p>
     * 
     * @param publicId the public ID of the entity to resolve.
     * @param systemId the system ID of the entity to resolve.
     * @return a <b>non-null</b> {@link InputSource} instance.
     * @throws IOException if an I/O error occurred resolving the entity.
     * @throws SAXException if an XML error occurred resolving the entity.
     */
    public InputSource resolveEntity(String publicId, String systemId)
    throws SAXException, IOException {
        if (this.sourceResolver == null) throw new IllegalStateException();
        System.err.println("RESOLVING: " + systemId);

        /* Try to resolve the public id if we don't have a system id */
        if (systemId == null) {
            InputSource source = this.entityResolver.resolveEntity(publicId, null);
            if ((source == null) || (source.getSystemId() == null)) {
                throw new IOException("Can't resolve \"" + publicId + "\"");
            } else {
                systemId = source.getSystemId();
            }
        }

        /* Use Cocoon's SourceResolver to resolve the system id */
        InputSource parsing = (InputSource) this.parsedSourceStack.peek();
        String base = parsing != null? parsing.getSystemId(): null;
        System.err.println("BASE URI:  " + base);
        Source source = this.sourceResolver.resolveURI(systemId, base, null);
        System.err.println("RESOLVED:  " + source.getURI());
        try {
            this.sourceValidity.add(source.getValidity());
            InputSource inputSource = new InputSource();
            inputSource.setSystemId(source.getURI());
            inputSource.setPublicId(publicId);
            inputSource.setByteStream(source.getInputStream());
            return inputSource;
        } finally {
            this.sourceResolver.release(source);
        }
    }


    /* =========================================================================== */
    /* CALL JARV TO ACCESS A CACHED OR FRESHLY PARSED SCHEMA INSTANCE              */
    /* =========================================================================== */

    /**
     * <p>Create an {@link XMLReader} instance that can be used by
     * <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a> for
     * parsing schemas.</p>
     * 
     * <p>The returned {@link XMLReader} will keep track of populating/clearing the
     * {@link Stack} of {@link InputSource}s kept for URI resolution as explained
     * in the description of the {@link #resolveEntity(String, String)} method.</p>
     * 
     * @return a <b>non-null</b> {@link XMLReader} instance.
     * @throws SAXException if an error occurrent creating the {@link XMLReader}.
     */
    public XMLReader createXMLReader()
    throws SAXException {
        return new Reader(this);
    }


    /* =========================================================================== */
    /* CALL JARV TO ACCESS A CACHED OR FRESHLY PARSED SCHEMA INSTANCE              */
    /* =========================================================================== */

    /**
     * <p>Return a new {@link Validator} instance either parsing a schema or
     * retrieving it from Cocoon's transient store (if caching is enabled).</p>
     */
    private Validator newValidator()
    throws SAXException, IOException, ProcessingException {
        if (this.cachingEnabled) {
            Entry entry = (Entry) this.transientStore.get(this.getKey());
            if ((entry == null) || (entry.expired())) {
                Schema schema = this.newSchema();
                entry = new Entry(schema, this.sourceValidity);
                this.transientStore.store(this.getKey(), entry);
            }
            this.sourceValidity = entry.validity;
            return entry.schema.createValidator(this.validatorProperties);
        }
        return this.newSchema().createValidator(this.validatorProperties);
    }

    /**
     * <p>Return a new {@link Schema} instance parsing it from the source specified
     * in the {@link #setup(SourceResolver, Map, String, Parameters)} method.</p>
     */
    private Schema newSchema()
    throws SAXException, IOException, ProcessingException {
        try {
            return this.schemaReader.createSchema(this.inputSource,
                                                  this.validatorProperties);
        } catch (IncorrectSchemaException exception) {
            String message = "Incorrect schema " + this.inputSource.getSystemId();
            throw new ProcessingException(message, exception);
        }
    }

    /* =========================================================================== */
    /* INNER CLASSES USED TO WRAP XML READER AND JARV SCHEMA/VALIDITY IN CACHES    */
    /* =========================================================================== */

    /**
     * <p>A trivial {@link XMLReader} implementation populating and clearing the
     * {@link Stack} of {@link InputSource}s for URI resolution.</p>
     */
    private static final class Reader implements XMLReader {
        
        private final XMLReader reader;
        private final JingTransformer transformer;

        private Reader(JingTransformer transformer)
        throws SAXException {
            /*
             * We have to look up the XMLReader using JAXP or SAX, as the SAXParser
             * supplied by Avalon/Excalibur does not seem to work with JING.
             */
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setNamespaceAware(true);
                factory.setValidating(false);
                this.reader = factory.newSAXParser().getXMLReader();
                this.reader.setEntityResolver(transformer);
                this.reader.setErrorHandler(new DraconianErrorHandler());
                this.transformer = transformer;
            } catch (ParserConfigurationException exception) {
                throw new SAXException("Can't create XML reader instance", exception);
            }
        }

        public boolean getFeature(String feature)
        throws SAXNotRecognizedException, SAXNotSupportedException {
            return this.reader.getFeature(feature);
        }

        public void setFeature(String feature, boolean value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
            this.reader.setFeature(feature, value);
        }

        public Object getProperty(String property)
        throws SAXNotRecognizedException, SAXNotSupportedException {
            return this.reader.getProperty(property);
        }

        public void setProperty(String property, Object value)
        throws SAXNotRecognizedException, SAXNotSupportedException {
            this.reader.setProperty(property, value);
        }

        public void setEntityResolver(org.xml.sax.EntityResolver resolver) {
            this.reader.setEntityResolver(resolver);
        }

        public org.xml.sax.EntityResolver getEntityResolver() {
            return this.reader.getEntityResolver();
        }

        public void setDTDHandler(DTDHandler handler) {
            this.reader.setDTDHandler(handler);
        }

        public DTDHandler getDTDHandler() {
            return this.reader.getDTDHandler();
        }

        public void setContentHandler(ContentHandler handler) {
            this.reader.setContentHandler(handler);
        }

        public ContentHandler getContentHandler() {
            return this.reader.getContentHandler();
        }

        public void setErrorHandler(ErrorHandler handler) {
            this.reader.setErrorHandler(handler);
        }

        public ErrorHandler getErrorHandler() {
            return this.reader.getErrorHandler();
        }

        public void parse(InputSource source)
        throws IOException, SAXException {
            this.transformer.parsedSourceStack.push(source);
            this.reader.parse(source);
            this.transformer.parsedSourceStack.pop();
        }

        public void parse(String source)
        throws IOException, SAXException {
            this.parse(this.getEntityResolver().resolveEntity(null, source));
        }
    }

    /**
     * <p>An extremely simple bean associating a {@link Schema} with its
     * {@link AggregatedValidity}.</p>
     */
    private static final class Entry {

        private final Schema schema;
        private final AggregatedValidity validity;
        
        private Entry(Schema schema, AggregatedValidity validity) {
            this.validity = validity;
            this.schema = schema;
        }
        
        private boolean expired() {
            return (this.validity.isValid() != SourceValidity.VALID);
        }
    }
}
