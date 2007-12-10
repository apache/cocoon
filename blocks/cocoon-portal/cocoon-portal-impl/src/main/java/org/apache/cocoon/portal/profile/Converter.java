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
package org.apache.cocoon.portal.profile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;


/**
 * This is a component that converts a profile (= object tree) to a persistence
 * format.
 *
 * @version $Id$
 */
public interface Converter {

    /**
     * Load an object from the given input stream.
     *
     * @param stream      The input stream with the data.
     * @param type        The persistence type.
     * @param parameters  An optional map of parameters for the conversion.
     * @return The loaded object.
     * @throws ConverterException
     */
    Object getObject(InputStream stream,
                     PersistenceType type,
                     Map         parameters)
    throws ConverterException;

    /**
     * Save an object to a given stream.
     *
     * @param stream The output stream.
     * @param object The object to save.
     * @param type   The persistence type.
     * @param parameters  An optional map of parameters for the conversion.
     * @throws ConverterException
     */
    void storeObject(OutputStream stream,
                     Object       object,
                     PersistenceType type,
                     Map          parameters)
    throws ConverterException;
}
