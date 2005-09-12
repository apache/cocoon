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

import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.validation.SchemaParser;
import org.apache.cocoon.components.validation.Validator;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.impl.xs.XMLSchemaValidator;
import org.apache.xerces.xni.grammars.XMLGrammarLoader;

/**
 * <p>The implementation of the {@link SchemaParser} interface for the XML Schema
 * grammar using <a href="http://xml.apache.org/xerces2-j/">Xerces</a>.</p>
 *
 * <p>Most of this code has been derived from the Xerces JAXP Validation interface
 * available in the <code>org.xml.xerces.jaxp.validation</code> package.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesSchemaParser extends XercesGrammarParser
implements SchemaParser, ThreadSafe {

    /**
     * <p>Create a new {@link XercesSchemaParser} instance.</p>
     */
    public XercesSchemaParser() {
        super();
    }

    /**
     * <p>Return an array of {@link String}s containing all schema grammars
     * supported by this {@link SchemaParser}.</p>
     * 
     * <p>The {@link XercesSchemaParser} supports only the
     * {@link Validator#GRAMMAR_XML_SCHEMA} grammar.</p>
     */
    public String[] getSupportedGrammars() {
        return new String[] { Validator.GRAMMAR_XML_SCHEMA };
    }

    /**
     * <p>Return a {@link XMLGrammarLoader} for the XML Schema grammar.</p>
     */
    protected XMLGrammarLoader newGrammarLoader() {
        return new XMLSchemaLoader();
    }

    /**
     * <p>Return a {@link Class} implementing the final validation handler.</p>
     */
    protected Class getValidationHandlerClass() {
        return XMLSchemaValidator.class;
    }
}
