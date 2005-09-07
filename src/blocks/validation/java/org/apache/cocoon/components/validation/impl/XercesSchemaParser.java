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
import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.SAXException;

/**
 * <p>The implementation of the {@link SchemaParser} interface for the XML Schema
 * grammar using <a href="http://xml.apache.org/xerces2-j/">Xerces</a>.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesSchemaParser extends CachingSchemaParser  implements SchemaParser {

    private static final String F_SCHEMA_FULL_CHECK = 
            Constants.XERCES_FEATURE_PREFIX + Constants.SCHEMA_FULL_CHECKING;
    private static final String P_XML_GRAMMAR_POOL =
            Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;
    private static final String[] GRAMMARS =
            new String[] { Validator.GRAMMAR_XML_SCHEMA };

    /**
     * <p>Create a new {@link XercesSchemaParser} instance.</p>
     */
    public XercesSchemaParser() {
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
    protected Schema parseSchema(String uri)
    throws IOException, SAXException {
        /* Create a Xerces Grammar Pool */
        XMLGrammarPool pool = new XMLGrammarPoolImpl();
        
        /* Create a new XML Schema Loader and set the pool into it */
        XMLSchemaLoader loader = new XMLSchemaLoader();
        loader.setFeature(F_SCHEMA_FULL_CHECK, true);
        loader.setProperty(P_XML_GRAMMAR_POOL, pool);

        /* Set up the entity resolver (from Cocoon) used to resolve URIs */
        XercesEntityResolver resolver = new XercesEntityResolver();
        loader.setEntityResolver(resolver);

        /* Default Error Handler: fail always! */
        loader.setErrorHandler(new XMLErrorHandler() {
            public void warning(String domain, String key, XMLParseException e)
            throws XNIException {
                throw e;
            }
            public void error(String domain, String key, XMLParseException e)
            throws XNIException {
                throw e;
            }
            public void fatalError(String domain, String key, XMLParseException e)
            throws XNIException {
                throw e;
            }
        });

        /* Load (parse and interpret) the grammar */
        loader.loadGrammar(resolver.resolveEntity(uri));
        
        /* Return a new Schema instance */
        return new XercesSchema(pool, resolver.getSourceValidity());
    }

    /**
     * <p>Return an array of {@link String}s containing all schema grammars
     * supported by this {@link SchemaParser}.</p>
     * 
     * <p>The {@link XercesSchemaParser} supports only the
     * {@link Validator#GRAMMAR_XML_SCHEMA} grammar.</p>
     */
    public String[] getSupportedGrammars() {
        return GRAMMARS;
    }
}
