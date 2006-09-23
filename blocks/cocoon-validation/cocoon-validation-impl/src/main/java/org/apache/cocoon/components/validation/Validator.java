/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.validation;

import java.io.IOException;

import org.apache.excalibur.source.Source;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>The {@link Validator} interface provides the abstraction of a component able
 * to validate XML documents using schemas defined in different languages.</p>
 * 
 * <p>This is basically the main entry point of the validation API, allowing users
 * to transparently access validators (in the form of {@link ValidationHandler}s
 * receiving SAX events for the documents to be validated), in different grammar
 * languages, using different implementations.</p>
 * 
 * <p>As more than one {@link SchemaParser} might be able to parse and create
 * {@link Schema} instances for a particular grammar language, this interface
 * defines a unique lookup method to allow selection of a particular
 * {@link SchemaParser} implementation.</p>
 * 
 * <p>Assuming that two different {@link SchemaParser}s called <code>first</code>
 * and <code>second</code> are both able to understand the
 * {@link #GRAMMAR_RELAX_NG RELAX NG} grammar (identified by the
 * <code>http://relaxng.org/ns/structure/1.0</code> identifier) one could select
 * between the two implementation using the following two strings:</p>
 * 
 * <ul>
 *   <li><code>first:http://relaxng.org/ns/structure/1.0</code></li>
 *   <li><code>second:http://relaxng.org/ns/structure/1.0</code></li>
 * </ul>
 * 
 * <p>As a rule (unless when this is impossible) the grammar identifier is
 * equivalent to the namespace of the root element of a schema.</p>
 *
 */
public interface Validator {

    /** <p>Avalon Role name of {@link Validator} components.</p> */
    public static final String ROLE = Validator.class.getName();

    /** <p>The <a href="http://www.schematron.com/">ISO Schematron</a/> grammar identifer.</p> */
    public static final String GRAMMAR_ISO_SCHEMATRON = "http://purl.oclc.org/dsdl/schematron";
    /** <p>The <a href="http://www.relaxng.org/">RELAX NG</a/> grammar identifer.</p> */
    public static final String GRAMMAR_RELAX_NG = "http://relaxng.org/ns/structure/1.0";
    /** <p>The <a href="http://www.xml.gr.jp/relax">RELAX Core</a/> grammar identifer.</p> */
    public static final String GRAMMAR_RELAX_CORE = "http://www.xml.gr.jp/xmlns/relaxCore";
    /** <p>The <a href="http://www.xml.gr.jp/relax">RELAX Namespace</a/> grammar identifer.</p> */
    public static final String GRAMMAR_RELAX_NS = "http://www.xml.gr.jp/xmlns/relaxNamespace";
    /** <p>The <a href="http://xml.ascc.net/schematron/">Schematron</a/> grammar identifer.</p> */
    public static final String GRAMMAR_SCHEMATRON = "http://www.ascc.net/xml/schematron";
    /** <p>The <a href="http://www.thaiopensource.com/trex/">Trex</a/> grammar identifer.</p> */
    public static final String GRAMMAR_TREX = "http://www.thaiopensource.com/trex";
    /** <p>The <a href="http://www.w3.org/XML/Schema">XML Schema</a/> grammar identifer.</p> */
    public static final String GRAMMAR_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    /** <p>The <a href="http://www.w3.org/TR/REC-xml">XML DTD</a/> grammar identifer.</p> */
    public static final String GRAMMAR_XML_DTD = "http://www.w3.org/TR/REC-xml";

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
    throws IOException, SAXException, ValidatorException;

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
    throws IOException, SAXException, ValidatorException;

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
    throws IOException, SAXException, ValidatorException;

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
    throws IOException, SAXException, ValidatorException;

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
    throws IOException, SAXException, ValidatorException;

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
    throws IOException, SAXException, ValidatorException;

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
    throws IOException, SAXException, ValidatorException;

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
    throws IOException, SAXException, ValidatorException;

}
