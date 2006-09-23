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
package org.apache.cocoon.components.validation.impl;

import org.apache.cocoon.components.validation.Schema;
import org.apache.excalibur.source.SourceValidity;

/**
 * <p>A simple implementation of the {@link Schema} interface.</p>
 *
 */
public abstract class AbstractSchema implements Schema {
    
    /** <p>The {@link SourceValidity} of this {@link Schema} instance.</p> */
    private final SourceValidity validity;

    /**
     * <p>Create a new {@link AbstractSchema} instance.</p>
     */
    public AbstractSchema(SourceValidity validity) {
        this.validity = validity;
    }

    /**
     * <p>Return the {@link SourceValidity} associated with this {@link Schema}.</p>
     * 
     * @return a {@link SourceValidity} instance or <b>null</b> if not known.
     */
    public SourceValidity getValidity() {
        return this.validity;
    }
}
