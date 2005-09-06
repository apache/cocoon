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
import org.apache.cocoon.components.validation.ValidationHandler;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.store.Store;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>TODO: ...</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public abstract class CachingValidator extends AbstractValidator {

    private boolean enableCaching = true;

    protected Store transientStore = null;

    public void initialize()
    throws Exception {
        try {
            super.initialize();
        } finally {
            this.transientStore = (Store) super.serviceManager.lookup(Store.TRANSIENT_STORE);
        }
    }
    
    public void dispose() {
        try {
            super.dispose();
        } finally {
            if (this.transientStore != null) super.serviceManager.release(this.transientStore);
        }
    }

    public ValidationHandler getValidationHandler(Source schemaSource,
                                                  ErrorHandler errorHandler)
    throws IOException, SAXException {

        String key = this.getClass().getName() + ":" + schemaSource.getURI();
        Schema schema = (Schema) this.transientStore.get(key);
        SourceValidity validity = null;

        if (schema != null) {
            validity = schema.getValidity();
            if (validity == null) {
                this.transientStore.remove(key);
                schema = null;
            } else if (validity.isValid() != SourceValidity.VALID) {
                this.transientStore.remove(key);
                schema = null;
            }
        }

        if (schema == null) {
            schema = super.schemaParser.parseSchema(schemaSource);
            validity = schema.getValidity();
            if ((validity != null) && (validity.isValid() == SourceValidity.VALID)) {
                this.transientStore.store(key, schema);
            }
        }

        return new ValidationHandlerImpl(schema.createXMLConsumer(errorHandler),
                                         schema.getValidity());
    }
}
