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
package org.apache.cocoon.components.validation.impl;

import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.source.impl.validity.AggregatedValidity;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.apache.excalibur.xml.EntityResolver;
import org.apache.excalibur.xml.sax.NOPLexicalHandler;
import org.apache.excalibur.xml.sax.XMLConsumer;
import org.apache.excalibur.xml.sax.XMLConsumerProxy;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.validate.rng.SAXSchemaReader;
import com.thaiopensource.xml.sax.DraconianErrorHandler;
import com.thaiopensource.xml.sax.XMLReaderCreator;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class JingSchemaParser implements EntityResolver, XMLReaderCreator,
SchemaParser, Serviceable, Disposable, Initializable, Recyclable {

    /** <p>The {@link SchemaReader} to use for reading RNG schemas.</p> */
    private final SchemaReader schemaReader = SAXSchemaReader.getInstance();
    /** <p>The current {@link Stack} of {@link InputSource}s being parsed. </p> */
    private final Stack parsedSourceStack = new Stack();
    /** <p>The {@link PropertyMap} to use with JING's factories.</p> */
    private final PropertyMap validatorProperties;
    
    /** <p>The {@link ServiceManager} to resolve other components.</p> */
    private ServiceManager serviceManager;
    /** <p>Cocoon's global {@link EntityResolver} for catalog resolution.</p> */
    private EntityResolver entityResolver;
    /** <p>The {@link SourceResolver} to use for resolving URIs.</p> */
    private SourceResolver sourceResolver;

    /** <p>The {@link SourceValidity} associated with the schema.</p> */
    private AggregatedValidity sourceValidity = null;

    public JingSchemaParser() {
        PropertyMapBuilder builder = new PropertyMapBuilder();
        ValidateProperty.ENTITY_RESOLVER.put(builder, this);
        ValidateProperty.ERROR_HANDLER.put(builder, new DraconianErrorHandler());
        ValidateProperty.XML_READER_CREATOR.put(builder, this);
        this.validatorProperties = builder.toPropertyMap();
    }

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
    }

    public void initialize()
    throws Exception {
        this.entityResolver = (EntityResolver) this.serviceManager.lookup(EntityResolver.ROLE);
        this.sourceResolver = (SourceResolver) this.serviceManager.lookup(SourceResolver.ROLE);
    }

    public void dispose() {
        if (this.sourceResolver != null)  this.serviceManager.release(this.sourceResolver);
        if (this.entityResolver != null)  this.serviceManager.release(this.entityResolver);
    }

    public Schema parseSchema(Source source)
    throws SAXException, IOException {
        this.sourceValidity = new AggregatedValidity();
        this.sourceValidity.add(source.getValidity());
        try {
            final InputSource input = this.prepareInputSource(source, null);
            this.parsedSourceStack.push(input);
            final com.thaiopensource.validate.Schema schema;
            schema = this.schemaReader.createSchema(input, this.validatorProperties);
            return null; // TODO
        } catch (IncorrectSchemaException exception) {
            String message = "Incorrect schema \"" + source.getURI() + "\"";
            throw new SAXException(message, exception);
        }
    }
    
    public void recycle() {
        this.sourceValidity = null;
        this.parsedSourceStack.clear();
    }
    
    private InputSource prepareInputSource(Source source, String publicId)
    throws IOException {
        InputSource inputSource = new InputSource();
        inputSource.setSystemId(source.getURI());
        inputSource.setPublicId(publicId);
        inputSource.setByteStream(source.getInputStream());
        return inputSource;
    }

    /* =========================================================================== */
    /* SAX2 ENTITY RESOLVER INTERFACE IMPLEMENTATION                               */
    /* =========================================================================== */

    /**
     * <p>Resolve an {@link InputSource} from a public ID and/or a system ID.</p>
     * 
     * <p>This method can be called only while a schema is being parsed and will
     * resolve URIs against a dynamic {@link Stack} of {@link InputSource}s.</p>
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
        if (this.sourceValidity == null) throw new IllegalStateException();

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
        Source source = this.sourceResolver.resolveURI(systemId, base, null);
        try {
            this.sourceValidity.add(source.getValidity());
            return this.prepareInputSource(source, publicId);
        } finally {
            this.sourceResolver.release(source);
        }
    }


    /* =========================================================================== */
    /* CALL JING TO ACCESS A CACHED OR FRESHLY PARSED SCHEMA INSTANCE              */
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
    /* INNER CLASSES USED TO WRAP JING'S SCHEMA INTO A COCOON VALIDATION ONE       */
    /* =========================================================================== */
    
    /**
     * <p>The implementation of the {@link Schema} implementation from the
     * {@link JingSchemaParser} component.</p>
     */
    private static final class JingSchema implements Schema {

        private final com.thaiopensource.validate.Schema schema;
        private final SourceValidity validity;
        
        private JingSchema(com.thaiopensource.validate.Schema schema,
                           SourceValidity validity) {
            this.validity = validity;
            this.schema = schema;
        }

        public SourceValidity getValidity() {
            return validity;
        }

        public XMLConsumer createXMLConsumer(ErrorHandler errorHandler) {
            if (errorHandler == null) errorHandler = new DraconianErrorHandler();
            final PropertyMapBuilder builder = new PropertyMapBuilder();
            ValidateProperty.ERROR_HANDLER.put(builder, errorHandler);
            final PropertyMap properties = builder.toPropertyMap();
            final Validator validator = this.schema.createValidator(properties);
            final ContentHandler contentHandler = validator.getContentHandler();
            final LexicalHandler lexicalHandler = new NOPLexicalHandler();
            return new XMLConsumerProxy(contentHandler, lexicalHandler);
        }
    }

    /* =========================================================================== */
    /* INNER CLASSES USED TO WRAP XML READER TO USE WITH JING'S RESOLUTION STACK   */
    /* =========================================================================== */

    /**
     * <p>A trivial {@link XMLReader} implementation populating and clearing the
     * {@link Stack} of {@link InputSource}s for URI resolution.</p>
     */
    private static final class Reader implements XMLReader {
        
        private final XMLReader reader;
        private final JingSchemaParser parser;

        private Reader(JingSchemaParser parser)
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
                this.reader.setEntityResolver(parser);
                this.reader.setErrorHandler(new DraconianErrorHandler());
                this.parser = parser;
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
            this.parser.parsedSourceStack.push(source);
            this.reader.parse(source);
            this.parser.parsedSourceStack.pop();
        }

        public void parse(String source)
        throws IOException, SAXException {
            this.parse(this.getEntityResolver().resolveEntity(null, source));
        }
    }
}
