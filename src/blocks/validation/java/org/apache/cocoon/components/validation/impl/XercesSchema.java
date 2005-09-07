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
import org.apache.xerces.xni.grammars.XMLGrammarPool;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public class XercesSchema extends AbstractSchema {
    
    private final XMLGrammarPool grammarPool;

    public XercesSchema(XMLGrammarPool grammarPool, SourceValidity validity) {
        super(validity);
        this.grammarPool = grammarPool;
        grammarPool.lockPool();
    }

    public ContentHandler newValidator(ErrorHandler handler) {
        return new XercesValidationHandler(this.grammarPool);
    }
}
