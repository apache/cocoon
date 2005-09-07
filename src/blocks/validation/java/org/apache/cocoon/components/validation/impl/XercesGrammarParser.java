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
import org.apache.excalibur.source.SourceValidity;
import org.apache.xerces.impl.Constants;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.grammars.XMLGrammarLoader;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLComponent;
import org.xml.sax.SAXException;

/**
 * <p>The implementation of the {@link SchemaParser} interface using the internals
 * of <a href="http://xml.apache.org/xerces2-j/">Xerces</a>.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public abstract class XercesGrammarParser extends CachingSchemaParser 
implements SchemaParser {

    private static final String P_XML_GRAMMAR_POOL =
            Constants.XERCES_PROPERTY_PREFIX + Constants.XMLGRAMMAR_POOL_PROPERTY;

    /**
     * <p>Create a new {@link XercesGrammarParser} instance.</p>
     */
    public XercesGrammarParser() {
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
    protected final Schema parseSchema(String uri)
    throws IOException, SAXException {
        /* Create a Xerces Grammar Pool and Entity Resolver */
        XMLGrammarPool pool = new XMLGrammarPoolImpl();
        XercesEntityResolver resolver = new XercesEntityResolver();

        /* Create a Xerces component manager contextualizing the loader */
        XercesComponentManager manager = new XercesComponentManager(pool, resolver);

        /* Create a new XML Schema Loader and set the pool into it */
        XMLGrammarLoader loader = this.newGrammarLoader();
        ((XMLComponent) loader).reset(manager);

        /* Load (parse and interpret) the grammar */
        loader.loadGrammar(resolver.resolveEntity(uri));
        
        /* Return a new Schema instance */
        SourceValidity validity = resolver.getSourceValidity();
        Class validator = this.getValidationHandler();
        return new XercesSchema(pool, validity, resolver, validator);
    }

    /**
     * <p>Return a {@link XMLGrammarLoader} instance able to read the grammar
     * handled by this {@link SchemaParser}.</p>
     */
    protected abstract XMLGrammarLoader newGrammarLoader(); 

    /**
     * <p>Return the {@link Class} that will implement the validator handler.</p>
     */
    protected abstract Class getValidationHandler(); 

}
