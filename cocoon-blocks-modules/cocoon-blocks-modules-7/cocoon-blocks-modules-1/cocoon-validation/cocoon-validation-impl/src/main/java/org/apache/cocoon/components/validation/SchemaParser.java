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
package org.apache.cocoon.components.validation;

import java.io.IOException;

import org.apache.excalibur.source.Source;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>The {@link SchemaParser} interface defines the abstraction of a component able
 * to parse sources and produce {@link Schema} instances suitable for validation of
 * XML documents.</p>
 * 
 * <p>A {@link SchemaParser} might be able to understand more than one grammar
 * language at the same time. The list of all supported grammar languages must be
 * returned by the {@link #getSupportedGrammars()} method.</p>
 * 
 */
public interface SchemaParser {

    /** <p>Avalon Role name of {@link SchemaParser} components.</p> */
    public static final String ROLE = SchemaParser.class.getName();

    /**
     * <p>Parse the specified {@link Source} and return a new {@link Schema}.</p>
     * 
     * <p>The returned {@link Schema} must be able to validate multiple documents
     * via multiple invocations of {@link Schema#createValidator(ErrorHandler)}.</p> 
     *
     * @param source the {@link Source} associated with the {@link Schema} to return.
     * @return a <b>non-null</b> {@link Schema} instance.
     * @throws SAXException if a grammar error occurred parsing the schema.
     * @throws IOException if an I/O error occurred parsing the schema.
     * @throws IllegalArgumentException if the specified grammar type is not one
     *                                  of the grammar types returned by the
     *                                  {@link #getSupportedGrammars()} method.  
     */
    public Schema parseSchema(Source source, String grammar)
    throws SAXException, IOException, IllegalArgumentException;

    /**
     * <p>Return an array of {@link String}s containing all the grammar languages
     * supported by this {@link SchemaParser}.</p>
     *
     * @return a <b>non-null</b> array of {@link String}s.
     */
    public String[] getSupportedGrammars();

}
