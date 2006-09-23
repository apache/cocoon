/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.validation;

import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;

/**
 * <p>The {@link Schema} interface defines an abstraction of a schema usable to
 * validate an XML document.</p>
 *
 * <p>This interface is not tied to any specific validation grammar language
 * such as the <a href="http://www.w3.org/XML/Schema">W3C XML Shema</a> language
 * or the <a href="http://www.relaxng.org/">RELAX-NG</a/> language.</p>
 *
 * <p>Selection and use of specific schema grammar languages is performed through
 * the use of the {@link Validator} interface.</p>
 * 
 * <p>Once returned by the {@link SchemaParser}, a {@link Schema} instance must be
 * able to validate a number of XML documents: each time a document needs to be
 * validated, a new {@link ValidationHandler} can be obtained invoking the
 * {@link #createValidator(ErrorHandler)} method. While validating an XML document,
 * {@link SAXException}s should be thrown back to the caller only when the specified
 * {@link ErrorHandler} is configured to do so.</p> 
 *
 */
public interface Schema {

    /**
     * <p>Return the {@link SourceValidity} associated with this {@link Schema}.</p>
     * 
     * <p>If the schema represented by this instance was parsed from several sources
     * (through the use of inclusions or referencing to external entities, for
     * example) the {@link SourceValidity} returned by this method <b>must</b>
     * consider all of them when the {@link SourceValidity#isValid()} or the
     * {@link SourceValidity#isValid(SourceValidity)} methods are called.</p>
     * 
     * @return a {@link SourceValidity} instance or <b>null</b> if not known.
     */
    public SourceValidity getValidity();

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
    throws SAXException;

}
