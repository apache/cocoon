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

package org.apache.cocoon.components.elementprocessor.types;

import java.io.IOException;

/**
 * This interface allows a client of NumericConverter to apply more
 * restrictive rules to the number that the NumericConverter obtained.
 *
 * @version $Id$
 */
public interface Validator
{

    /**
     * Is this number valid? If so, return a null
     * IOException. Otherwise, return an IOException whose message
     * explains why the number is not valid.
     *
     * @param number the Number holding the value. Guaranteed non-null
     *
     * @return a null IOException if the value is ok, else a real
     *         IOException
     */

    IOException validate(final Number number);
}   // end public interface Validator
