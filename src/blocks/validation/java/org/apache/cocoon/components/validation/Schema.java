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
package org.apache.cocoon.components.validation;

import org.apache.excalibur.source.SourceValidity;
import org.xml.sax.ContentHandler;
import org.xml.sax.ErrorHandler;

/**
 * <p>An interface defining a schema used for validation of XML documents.</p>
 * 
 * <p>A schema, by itself, simply provide access to its {@link SourceValidity}
 * (if any, for caching), and is able to create instances of {@link ContentHandler}s
 * that will receive SAX Events and validate them.</p>
 *
 * @author <a href="mailto:pier@betaversion.org">Pier Fumagalli</a>
 */
public interface Schema {

    /**
     * <p>Return the {@link SourceValidity} associated with this {@link Schema}.</p>
     * 
     * <p>If the current schema grammar allow inclusion of sub-schemas, the
     * {@link SourceValidity} returned by this method <b>must</b> validate both the
     * original schema URI <b>and</b> all its sub-schemas.</p>
     * 
     * @return a {@link SourceValidity} instance or <b>null</b> if not known.
     */
    public SourceValidity getValidity();

    /**
     * <p>Return a new {@link ContentHandler} instance that can be used to send SAX
     * events to for proper validation.</p>
     *
     * <p>By default, this method will create a {@link ContentHandler} failing on the
     * first occurrence of an warning, error or fatal error . If this behavior is
     * not suitable, use the {@link #newValidator(ErrorHandler)} method instead and
     * specify an {@link ErrorHandler} suitable to your needs.</p>
     *
     * <p>Once used, the returned {@link ContentHandler} <b>can't</b> be reused.</p> 
     * 
     * @return a <b>non-null</b> {@link ContentHandler} instance.
     */
    public ContentHandler newValidator();

    /**
     * <p>Return a new {@link ContentHandler} instance that can be used to send SAX
     * events to for proper validation.</p>
     * 
     * <p>The specified {@link ErrorHandler} will be notified of all warnings or
     * errors encountered validating the SAX events sent to the returned
     * {@link ContentHandler}.</p>
     * 
     * <p>Once used, the returned {@link ContentHandler} <b>can not</b> be reused.</p> 
     * 
     * @param handler an {@link ErrorHandler} to notify of validation errors.
     * @return a <b>non-null</b> {@link ContentHandler} instance.
     */
    public ContentHandler newValidator(ErrorHandler handler);

}
