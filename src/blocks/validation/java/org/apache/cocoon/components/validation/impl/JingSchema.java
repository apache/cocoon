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

import org.apache.cocoon.components.validation.Schema;
import org.apache.excalibur.source.SourceValidity;
import org.apache.excalibur.xml.sax.NOPLexicalHandler;
import org.apache.excalibur.xml.sax.XMLConsumer;
import org.apache.excalibur.xml.sax.XMLConsumerProxy;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;
import org.xml.sax.ext.LexicalHandler;

import com.thaiopensource.util.PropertyMap;
import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.Validator;
import com.thaiopensource.xml.sax.DraconianErrorHandler;

/**
 * <p>The implementation of the {@link Schema} implementation from the
 * {@link JingSchemaParser} component.</p>
 */
public class JingSchema extends AbstractSchema {

    /** <p>The original schema instance to wrap.</p> */
    private final com.thaiopensource.validate.Schema schema;

    /**
     * <p>Create a new {@link JingSchema} instance.</p>
     *
     * @param schema the JING original schema to wrap.
     * @param validity the {@link SourceValidity} associated with the schema.
     */
    protected JingSchema(com.thaiopensource.validate.Schema schema,
                         SourceValidity validity) {
        super(validity);
        this.schema = schema;
    }

    /**
     * <p>Return a new {@link XMLConsumer} instance that can be used to send SAX
     * events to for proper validation.</p>
     * 
     * <p>The specified {@link ErrorHandler} will be notified of all warnings or
     * errors encountered validating the SAX events sent to the returned
     * {@link XMLConsumer}.</p>
     * 
     * <p>Once used, the returned {@link XMLConsumer} <b>can not</b> be reused.</p> 
     * 
     * @param errorHandler an {@link ErrorHandler} to notify of validation errors.
     * @return a <b>non-null</b> {@link XMLConsumer} instance.
     */
    public XMLConsumer newValidator(ErrorHandler errorHandler) {
        if (errorHandler == null) errorHandler = new DraconianErrorHandler();
        final PropertyMapBuilder builder = new PropertyMapBuilder();
        ValidateProperty.ERROR_HANDLER.put(builder, errorHandler);
        final PropertyMap properties = builder.toPropertyMap();
        final Validator validator = this.schema.createValidator(properties);
        final ContentHandler contentHandler = validator.getContentHandler();
        final LexicalHandler lexicalHandler = new NOPLexicalHandler();
        return new XMLConsumerProxy(contentHandler, lexicalHandler);
    }
}