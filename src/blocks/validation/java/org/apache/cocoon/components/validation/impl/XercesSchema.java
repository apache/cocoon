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

import org.apache.excalibur.source.SourceValidity;
import org.apache.xerces.xni.XMLDocumentHandler;
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>A wrapper around a schema parsed by Xerces.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesSchema extends AbstractSchema {
    
    private final XMLGrammarPool grammarPool;
    private final Class validatorClass;

    /**
     * <p>Create a new {@link XercesSchema} instance.</p>
     */
    public XercesSchema(XMLGrammarPool grammarPool, SourceValidity sourceValidity,
                        Class validatorClass) {
        super(sourceValidity);
        grammarPool.lockPool();
        this.validatorClass = validatorClass;
        this.grammarPool = grammarPool;
    }

    /**
     * <p>Return a {@link ContentHandler} able to receive SAX events and performing
     * validation according to the schema wrapped by this instance.</p>
     *
     * @param errorHandler {@link ErrorHandler} to be notified of validation errors.
     * @return a <b>non-null</b> {@link ContentHandler} instance.
     * @throws SAXException if an error occurred creating the {@link ContentHandler}.
     */
    public ContentHandler newValidator(ErrorHandler errorHandler)
    throws SAXException {
        XercesContext context = new XercesContext(this.grammarPool,
                                                  new XercesEntityResolver(),
                                                  errorHandler);
        try {
            Object instance = context.initialize(this.validatorClass.newInstance());
            return new XercesContentHandler((XMLDocumentHandler) instance);
        } catch (Exception exception) {
            throw new SAXException("Unable to access validator", exception);
        }
    }
}
