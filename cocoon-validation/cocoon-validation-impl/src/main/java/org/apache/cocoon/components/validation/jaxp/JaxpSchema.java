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
package org.apache.cocoon.components.validation.jaxp;

import javax.xml.validation.Schema;
import javax.xml.validation.ValidatorHandler;

import org.apache.cocoon.components.validation.ValidationHandler;
import org.apache.cocoon.components.validation.impl.AbstractSchema;
import org.apache.cocoon.components.validation.impl.DefaultValidationHandler;
import org.apache.cocoon.components.validation.impl.DraconianErrorHandler;
import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>An extension of the {@link AbstractSchema} class specific to the
 * {@link JaxpSchemaParser} implementation.</p>
 *
 */
public class JaxpSchema extends AbstractSchema {

    /** <p>The wrapped JAXP {@link Schema} instance.</p> */
    private final Schema schema;

    /**
     * <p>Create a new {@link JaxpSchema} instance.</p>
     *
     * @param schema the {@link Schema} instance to wrap.
     * @param validity the {@link SourceValidity} associated with the schema.
     */
    public JaxpSchema(Schema schema, SourceValidity validity) {
        super(validity);
        this.schema = schema;
    }

    /**
     * <p>Return a new {@link ValidationHandler} instance that can be used to
     * validate an XML document by sending SAX events to it.</p>
     *
     * <p>The specified {@link ErrorHandler} will be notified of all warnings or
     * errors encountered validating the SAX events sent to the returned
     * {@link ValidationHandler}, and <b>must not</b> be <b>null</b>.</p>
     *
     * <p>The returned {@link ValidationHandler} can be used to validate <b>only
     * one</b> XML document. To validate more than one document, this method should
     * be called once for each document to validate.</p>
     *
     * @param handler an {@link ErrorHandler} to notify of validation errors.
     * @return a <b>non-null</b> {@link ValidationHandler} instance.
     * @throws SAXException if an error occurred creating the validation handler.
     */
    public ValidationHandler createValidator(ErrorHandler handler)
    throws SAXException {
        if (handler == null) handler = DraconianErrorHandler.INSTANCE;
        ValidatorHandler validator = this.schema.newValidatorHandler();
        validator.setErrorHandler(handler);
        return new DefaultValidationHandler(this.getValidity(), validator);
    }
}
