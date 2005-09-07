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

import org.apache.cocoon.components.validation.Schema;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.Validator;
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
public class JingSchemaParser extends CachingSchemaParser {

    /** <p>The list of grammars supported by the {@link JingSchemaParser}.</p> */
    private static final String[] GRAMMARS =
                                        new String[] { Validator.GRAMMAR_RELAX_NG };

    /**
     * <p>Create a new {@link JingSchemaParser} instance.</p>
     */
    public JingSchemaParser() {
        super();
    }

    /**
     * <p>Parse the specified URI and return a {@link Schema}.</p>
     *
     * @param uri the URI of the {@link Schema} to return.
     * @return a <b>non-null</b> {@link Schema} instance.
     * @throws SAXException if an error occurred parsing the schema.
     * @throws IOException if an I/O error occurred parsing the schema.
     */
    public Schema parseSchema(String uri)
    throws SAXException, IOException {
        SchemaReader schemaReader = SAXSchemaReader.getInstance();
        JingContext context = new JingContext(sourceResolver, entityResolver);
        InputSource source = context.resolveEntity(null, uri);

        try {
            final com.thaiopensource.validate.Schema schema;
            schema = schemaReader.createSchema(source, context.getProperties());
            return new JingSchema(schema, context.getValidity());
        } catch (IncorrectSchemaException exception) {
            String message = "Incorrect schema \"" + uri + "\"";
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
        return GRAMMARS;
    }
}
