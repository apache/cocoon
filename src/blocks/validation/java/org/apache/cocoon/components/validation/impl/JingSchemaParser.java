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

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.Validator;
import org.apache.excalibur.source.Source;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.thaiopensource.validate.IncorrectSchemaException;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.rng.SAXSchemaReader;

/**
 * <p>A {@link SchemaParser} implementation for the RELAX NG grammar using the
 * <a href="http://www.thaiopensource.com/relaxng/jing.html">JING</a> validation
 * engine.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class JingSchemaParser extends AbstractSchemaParser implements ThreadSafe {

    /**
     * <p>Create a new {@link JingSchemaParser} instance.</p>
     */
    public JingSchemaParser() {
        super();
    }

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
    public Schema getSchema(Source source, String grammar)
    throws SAXException, IOException {
        if (! Validator.GRAMMAR_RELAX_NG.equals(grammar)) {
            throw new IllegalArgumentException("Unsupported grammar " + grammar);
        }

        SchemaReader schemaReader = SAXSchemaReader.getInstance();
        JingContext context = new JingContext(sourceResolver, entityResolver);
        InputSource input = new InputSource();
        input.setByteStream(source.getInputStream());
        input.setSystemId(source.getURI());
        context.pushInputSource(input);

        try {
            final com.thaiopensource.validate.Schema schema;
            schema = schemaReader.createSchema(input, context.getProperties());
            return new JingSchema(schema, context.getValidity());
        } catch (IncorrectSchemaException exception) {
            String message = "Incorrect schema \"" + source.getURI() + "\"";
            throw new SAXException(message, exception);
        }
    }

    /**
     * <p>Return an array of {@link String}s containing all schema grammars
     * supported by this {@link SchemaParser}.</p>
     * 
     * <p>The {@link JingSchemaParser} supports only the
     * {@link Validator#GRAMMAR_RELAX_NG RELAX NG} grammar.</p>
     */
    public String[] getSupportedGrammars() {
        return new String[] { Validator.GRAMMAR_RELAX_NG };
    }
}
