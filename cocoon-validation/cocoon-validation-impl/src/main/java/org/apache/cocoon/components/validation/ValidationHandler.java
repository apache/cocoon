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
import org.xml.sax.ContentHandler;
import org.xml.sax.ext.LexicalHandler;

/**
 * <p>A {@link ValidationHandler} represents a {@link ContentHandler} validating
 * SAX events according to a validation schema.</p>
 * 
 * <p>As the handler might be tied to one (or more) resources from where the original
 * schema was read from, the {@link #getValidity()} method provides a way to verify
 * whether the validation instruction are still valid or not.</p>
 *
 */
public interface ValidationHandler extends ContentHandler, LexicalHandler {

    /**
     * <p>Return a {@link SourceValidity} instance associated with the original
     * resources of the schema describing the validation instructions.</p>
     *
     * <p>As the handler might be tied to one (or more) resources from where the
     * original schema was read from, the {@link #getValidity()} method provides a
     * way to verify whether the validation instruction are still valid or not.</p>
     */
    public SourceValidity getValidity();

}
