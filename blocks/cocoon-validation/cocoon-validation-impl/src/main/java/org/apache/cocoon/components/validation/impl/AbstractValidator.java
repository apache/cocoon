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

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.ValidationHandler;
import org.apache.cocoon.components.validation.Validator;
import org.apache.cocoon.components.validation.ValidatorException;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;
import org.apache.excalibur.xml.sax.NOPContentHandler;
import org.apache.excalibur.xml.sax.SAXParser;
import org.apache.excalibur.xml.sax.XMLizable;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>The {@link AbstractValidator} provides a generic implementation of the methods
 * specified by the {@link Validator} interface.</p>
 * 
 * <p>Final implementations must implement three component management methods
 * {@link #lookupParserByGrammar(String)}, {@link #lookupParserByName(String)} and
 * {@link #releaseParser(SchemaParser)}.</p>
 * 
 * <p>In addition to this, they might also override the default implementation of
 * the {@link #getSchema(SchemaParser, Source, String)} method, for example when
 * caching {@link Schema} instances.</p>
 * 
 * <p>This implementation provides a simple grammar identification mechanism, which
 * can be overridden by reimplementing the {@link #detectGrammar(Source)} method
 * provided by this class.</p>
 *
 */
public abstract class AbstractValidator
implements Validator, Serviceable, Disposable, LogEnabled {

    /** <p>The configured {@link ServiceManager} instance.</p> */
    protected ServiceManager manager = null;
    /** <p>The configured {@link SourceResolver} instance.</p> */
    protected SourceResolver resolver = null;
    /** <p>The configured {@link Logger} instance.</p> */
    protected Logger logger = null;

    /**
     * <p>Create a new {@link AbstractValidator} instance.</p>
     */
    public AbstractValidator() {
        super();
    }

    /**
     * <p>Enable logging.</p>
     */
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    /**
     * <p>Specify the {@link ServiceManager} available to this instance.</p>
     */
    public void service(ServiceManager manager)
    throws ServiceException {
        this.manager = manager;
        this.resolver = (SourceResolver) manager.lookup(SourceResolver.ROLE);
    }

    /**
     * <p>Dispose of this component instance.</p>
     */
    public void dispose() {
        if (this.resolver != null) this.manager.release(this.resolver);
    }

    /* =========================================================================== */
    /* IMPLEMENTATION OF THE VALIDATOR INTERFACE                                   */
    /* =========================================================================== */

    /**
     * <p>Return a {@link ValidationHandler} validating an XML document according to
     * the schema found at the specified location.</p>
     *
     * <p>The {@link Validator} will attempt to automatically detect the grammar
     * language of the specified schema, and each error or warning occurring while
     * validating the document will trigger a {@link SAXException} to be thrown back
     * to the caller.</p> 
     *
     * @param uri the location of the schema to use to validate the document.
     * @return a <b>non null</b> {@link ValidationHandler} able to SAX events from
     *         the original XML document to validate.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws ValidatorException if the grammar language of the specified schema
     *                            could not be detected or was not supported.
     * @see SchemaParser#parseSchema(Source, String)
     * @see Schema#createValidator(ErrorHandler)
     */
    public ValidationHandler getValidationHandler(String uri)
    throws IOException, SAXException, ValidatorException {
        return this.getValidationHandler(uri, null, null);
    }

    /**
     * <p>Return a {@link ValidationHandler} validating an XML document according to
     * the schema found at the specified location.</p>
     *
     * <p>Each error or warning occurring while validating the document will trigger
     * a {@link SAXException} to be thrown back to the caller.</p> 
     *
     * @param uri the location of the schema to use to validate the document.
     * @param grammar the grammar language of the schema to parse.
     * @return a <b>non null</b> {@link ValidationHandler} able to SAX events from
     *         the original XML document to validate.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws ValidatorException if the specified grammar language wasn't supported.
     * @see SchemaParser#parseSchema(Source, String)
     * @see Schema#createValidator(ErrorHandler)
     */
    public ValidationHandler getValidationHandler(String uri, String grammar)
    throws IOException, SAXException, ValidatorException {
        return this.getValidationHandler(uri, grammar, null);
    }

    /**
     * <p>Return a {@link ValidationHandler} validating an XML document according to
     * the schema found at the specified location.</p>
     *
     * <p>The {@link Validator} will attempt to automatically detect the grammar
     * language of the specified schema, while each validation error or warning will
     * be passed to the specified {@link ErrorHandler} which will have the ability
     * to generate and throw a {@link SAXException} back to the caller.</p>
     *
     * @param uri the location of the schema to use to validate the document.
     * @param errorHandler the {@link ErrorHandler} notified of validation problems.
     * @return a <b>non null</b> {@link ValidationHandler} able to SAX events from
     *         the original XML document to validate.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws ValidatorException if the grammar language of the specified schema
     *                            could not be detected or was not supported.
     * @see SchemaParser#parseSchema(Source, String)
     * @see Schema#createValidator(ErrorHandler)
     */
    public ValidationHandler getValidationHandler(String uri, ErrorHandler errorHandler)
    throws IOException, SAXException, ValidatorException {
        return this.getValidationHandler(uri, null, errorHandler);
    }

    /**
     * <p>Return a {@link ValidationHandler} validating an XML document according to
     * the schema found at the specified location.</p>
     *
     * <p>Each validation error or warning will be passed to the specified
     * {@link ErrorHandler} which will have the ability to generate and throw a
     * {@link SAXException} back to the caller.</p>
     *
     * @param uri the location of the schema to use to validate the document.
     * @param grammar the grammar language of the schema to parse.
     * @param errorHandler the {@link ErrorHandler} notified of validation problems.
     * @return a <b>non null</b> {@link ValidationHandler} able to SAX events from
     *         the original XML document to validate.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws ValidatorException if the specified grammar language wasn't supported.
     * @see SchemaParser#parseSchema(Source, String)
     * @see Schema#createValidator(ErrorHandler)
     */
    public ValidationHandler getValidationHandler(String uri, String grammar,
                                               ErrorHandler errorHandler)
    throws IOException, SAXException, ValidatorException {
        if (uri == null) throw new IOException("Specified schema URI was null");
        Source source = null;
        try {
            source = this.resolver.resolveURI(uri);
            return this.getValidationHandler(source, grammar, errorHandler);
        } finally {
            if (source != null) this.resolver.release(source);
        }
    }

    /**
     * <p>Return a {@link ValidationHandler} validating an XML document according to
     * the schema found at the specified location.</p>
     *
     * <p>The {@link Validator} will attempt to automatically detect the grammar
     * language of the specified schema, and each error or warning occurring while
     * validating the document will trigger a {@link SAXException} to be thrown back
     * to the caller.</p> 
     *
     * @param source the {@link Source} identifying the schema to use for validation.
     * @return a <b>non null</b> {@link ValidationHandler} able to SAX events from
     *         the original XML document to validate.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws ValidatorException if the grammar language of the specified schema
     *                            could not be detected or was not supported.
     * @see SchemaParser#parseSchema(Source, String)
     * @see Schema#createValidator(ErrorHandler)
     */
    public ValidationHandler getValidationHandler(Source source)
    throws IOException, SAXException, ValidatorException {
        return this.getValidationHandler(source, null, null);
    }

    /**
     * <p>Return a {@link ValidationHandler} validating an XML document according to
     * the schema found at the specified location.</p>
     *
     * <p>Each error or warning occurring while validating the document will trigger
     * a {@link SAXException} to be thrown back to the caller.</p> 
     *
     * @param source the {@link Source} identifying the schema to use for validation.
     * @param grammar the grammar language of the schema to parse.
     * @return a <b>non null</b> {@link ValidationHandler} able to SAX events from
     *         the original XML document to validate.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws ValidatorException if the specified grammar language wasn't supported.
     * @see SchemaParser#parseSchema(Source, String)
     * @see Schema#createValidator(ErrorHandler)
     */
    public ValidationHandler getValidationHandler(Source source, String grammar)
    throws IOException, SAXException, ValidatorException {
        return this.getValidationHandler(source, grammar, null);
    }

    /**
     * <p>Return a {@link ValidationHandler} validating an XML document according to
     * the schema found at the specified location.</p>
     *
     * <p>The {@link Validator} will attempt to automatically detect the grammar
     * language of the specified schema, while each validation error or warning will
     * be passed to the specified {@link ErrorHandler} which will have the ability
     * to generate and throw a {@link SAXException} back to the caller.</p>
     *
     * @param source the {@link Source} identifying the schema to use for validation.
     * @param errorHandler the {@link ErrorHandler} notified of validation problems.
     * @return a <b>non null</b> {@link ValidationHandler} able to SAX events from
     *         the original XML document to validate.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws ValidatorException if the grammar language of the specified schema
     *                            could not be detected or was not supported.
     * @see SchemaParser#parseSchema(Source, String)
     * @see Schema#createValidator(ErrorHandler)
     */
    public ValidationHandler getValidationHandler(Source source,
                                                  ErrorHandler errorHandler)
    throws IOException, SAXException, ValidatorException {
        return this.getValidationHandler(source, null, errorHandler);
    }

    /**
     * <p>Return a {@link ValidationHandler} validating an XML document according to
     * the schema found at the specified location.</p>
     *
     * <p>Each validation error or warning will be passed to the specified
     * {@link ErrorHandler} which will have the ability to generate and throw a
     * {@link SAXException} back to the caller.</p>
     *
     * @param source the {@link Source} identifying the schema to use for validation.
     * @param grammar the grammar language of the schema to parse.
     * @param errorHandler the {@link ErrorHandler} notified of validation problems.
     * @return a <b>non null</b> {@link ValidationHandler} able to SAX events from
     *         the original XML document to validate.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws ValidatorException if the specified grammar language wasn't supported.
     * @see SchemaParser#parseSchema(Source, String)
     * @see Schema#createValidator(ErrorHandler)
     */
    public ValidationHandler getValidationHandler(Source source, String grammar,
                                               ErrorHandler errorHandler)
    throws IOException, SAXException, ValidatorException {
        if (errorHandler == null) errorHandler = DraconianErrorHandler.INSTANCE;

        SchemaParser parser = null;
        try {
            /* If no grammar was supplied, try to detect one */
            if (grammar == null) grammar = this.detectGrammar(source);

            /* Save the grammar name and try to find a schema parser */
            String language = grammar; 
            parser = this.lookupParserByGrammar(grammar);

            /*
             * If the schema parser for the language was not found, we might have to
             * look up for the form name:grammar as specified by Validator.
             */
            if (parser == null) {
                int index = grammar.indexOf(':');
                if (index > 0) {
                    String name = grammar.substring(0, index);
                    language = grammar.substring(index + 1);
                    parser = this.lookupParserByName(name);
                }
            }

            /* If still we didn't find any parser, simply die of natural death */
            if (parser == null) {
                String message = "Unsupported grammar language" + grammar;
                throw new ValidatorException(message);
            }

            /* Somehow we have a schema parser, check it supports the gramar */
            String languages[] = parser.getSupportedGrammars();
            for (int x = 0; x < languages.length; x++) {
                if (! language.equals(languages[x])) continue;
                /* Hah! language supported, go ahead and parse now */
                Schema schema = this.getSchema(parser, source, language);
                return schema.createValidator(errorHandler);
            }

            /* Something really odd going on, this should never happen */
            String message = "Schema parser " + parser.getClass().getName() +
                             " does not support grammar " + grammar;
            throw new ValidatorException(message);

        } finally {
            if (parser != null) this.releaseParser(parser);
        }
    }

    /* =========================================================================== */
    /* METHODS TO BE IMPLEMENTED BY THE EXTENDING CLASSES                          */
    /* =========================================================================== */

    /**
     * <p>Attempt to acquire a {@link SchemaParser} interface able to understand
     * the grammar language specified.</p>
     * 
     * @param grammar the grammar language that must be understood by the returned
     *                {@link SchemaParser}
     * @return a {@link SchemaParser} instance or <b>null</b> if none was found able
     *         to understand the specified grammar language.
     */
    protected abstract SchemaParser lookupParserByGrammar(String grammar);

    /**
     * <p>Attempt to acquire a {@link SchemaParser} interface associated with the
     * specified instance name.</p>
     * 
     * @param name the name associated with the {@link SchemaParser} to be returned.
     * @return a {@link SchemaParser} instance or <b>null</b> if none was found.
     */
    protected abstract SchemaParser lookupParserByName(String name);

    /**
     * <p>Release a previously acquired {@link SchemaParser} instance back to its
     * original component manager.</p>
     * 
     * <p>This method is supplied in case solid implementations of this class relied
     * on the {@link ServiceManager} to manage {@link SchemaParser}s instances.</p>
     * 
     * @param parser the {@link SchemaParser} whose instance is to be released.
     */
    protected abstract void releaseParser(SchemaParser parser);
    
    /* =========================================================================== */
    /* METHODS SPECIFIC TO ABSTRACTVALIDATOR OVERRIDABLE BY OTHER IMPLEMENTATIONS  */
    /* =========================================================================== */

    /**
     * <p>Return a {@link Schema} instance from the specified {@link SchemaParser}
     * associated with the given {@link Source} and grammar language.</p>
     * 
     * <p>This method simply implements resolution returning the {@link Schema}
     * instance acquired calling <code>parser.getSchema(source,grammar)</code>.</p>
     * 
     * @param parser the {@link SchemaParser} producing the {@link Schema}.
     * @param source the {@link Source} associated with the {@link Schema} to return.
     * @param grammar the grammar language of the schema to produce.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws IOException if an I/O error occurred parsing the schema.
     */
    protected Schema getSchema(SchemaParser parser, Source source, String grammar)
    throws IOException, SAXException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Parsing schema \"" + source.getURI() + "\" using " +
                              "grammar \"" + grammar + "\" and SourceParser " +
                              parser.getClass().getName());
        }

        try {
            return parser.parseSchema(source, grammar);
        } catch (IllegalArgumentException exception) {
            String message = "Schema parser " + parser.getClass().getName() +
                             " does not support grammar " + grammar;
            throw new ValidatorException(message, exception);
        }
    }

    /**
     * <p>Attempt to detect the grammar language used by the schema identified
     * by the specified {@link Source}.</p>
     * 
     * @param source a {@link Source} instance pointing to the schema to be analyzed.
     * @throws IOException if an I/O error occurred accessing the schema.
     * @throws SAXException if an error occurred parsing the schema.
     * @throws ValidatorException if the language of the schema could not be guessed.
     */
    protected String detectGrammar(Source source)
    throws IOException, SAXException, ValidatorException {
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Detecting grammar for \"" + source.getURI() + "\"");
        }

        SAXParser xmlParser = null;
        String grammar = null;

        try {
            DetectionHandler handler = new DetectionHandler();
            if (source instanceof XMLizable) {
                XMLizable xmlizable = (XMLizable) source;
                xmlizable.toSAX(handler);
            } else {
                xmlParser = (SAXParser) this.manager.lookup((SAXParser.ROLE)); 
                InputSource input = new InputSource();
                input.setSystemId(source.getURI());
                input.setByteStream(source.getInputStream());
                xmlParser.parse(input, handler);
            }
        } catch (ServiceException exception) {
            throw new SAXException("Unable to access XML parser", exception);
        } catch (DetectionException exception) {
            grammar = exception.grammar;
        } finally {
            if (xmlParser != null) this.manager.release(xmlParser);
        }

        if (("".equals(grammar)) || (grammar == null)) {
            String message = "Unable to detect grammar for schema at ";
            throw new ValidatorException(message + source.getURI());
        } else {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Grammar \"" + grammar + "\" detected for " +
                                  "schema \"" + source.getURI());
            }
            return grammar;
        }
    }

    /* =========================================================================== */
    /* METHODS AND INNER CLASSES FOR AUTOMATIC GRAMMAR LANGUAGE DETECTION          */
    /* =========================================================================== */

    /**
     * <p>A simple implementation of a {@link ValidationHandler} detecting schemas
     * based on the namespace of the root element.</p>
     */
    private static final class DetectionHandler extends NOPContentHandler {
        
        /**
         * <p>Detect the namespace of the root element and always throw a
         * {@link SAXException} or a {@link DetectionException}.</p>
         */
        public void startElement(String ns, String lnam, String qnam, Attributes a)
        throws SAXException {
            throw new DetectionException(ns);
        }
    }

    /**
     * <p>An exception thrown by {@link DetectionHandler} representing that
     * a grammar was successfully detected.</p>
     */
    private static final class DetectionException extends SAXException {

        private final String grammar;

        private DetectionException(String grammar) {
            super ("Grammar detected: " + grammar);
            this.grammar = grammar;
        }
    }
}
