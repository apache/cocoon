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
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.xerces.util.XMLGrammarPoolImpl;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.XMLGrammarLoader;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.apache.xerces.xni.parser.XMLParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>The implementation of the {@link SchemaParser} interface using the internals
 * of <a href="http://xml.apache.org/xerces2-j/">Xerces</a>.</p>
 *
 * <p>Most of this code has been derived from the Xerces JAXP Validation interface
 * available in the <code>org.xml.xerces.jaxp.validation</code> package.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public abstract class XercesGrammarParser extends AbstractSchemaParser 
implements SchemaParser {

    /**
     * <p>Create a new {@link XercesGrammarParser} instance.</p>
     */
    public XercesGrammarParser() {
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
    public Schema parseSchema(Source source, String grammar)
    throws IOException, SAXException {
        try {
            /* Create a Xerces Grammar Pool and Entity Resolver */
            XMLGrammarPool pool = new XMLGrammarPoolImpl();
            XercesEntityResolver r = new XercesEntityResolver(super.sourceResolver,
                                                              super.entityResolver);

            /* Create a Xerces component manager contextualizing the loader */
            XercesContext context = new XercesContext(pool, r);

            /* Create a new XML Schema Loader and set the pool into it */
            XMLGrammarLoader loader = this.newGrammarLoader();
            context.initialize(loader);

            /* Load (parse and interpret) the grammar */
            this.getLogger().debug("Loading grammar from " + source.getURI());
            loader.loadGrammar(r.resolveSource(source));
            this.getLogger().debug("Grammar loaded from " + source.getURI());

            /* Return a new Schema instance */
            SourceValidity validity = r.getSourceValidity();
            Class validator = this.getValidationHandlerClass();
            return new XercesSchema(pool, validity, validator);
        } catch (XMLParseException exception) {
            throw new XercesParseException(exception);
        } catch (XNIException exception) {
            throw new SAXException("Unable to parse schema", exception);
        }
    }

    /**
     * <p>Return a {@link XMLGrammarLoader} instance able to read the grammar
     * handled by this {@link SchemaParser}.</p>
     */
    protected abstract XMLGrammarLoader newGrammarLoader(); 

    /**
     * <p>Return the {@link Class} that will implement the validator handler.</p>
     */
    protected abstract Class getValidationHandlerClass(); 

}
