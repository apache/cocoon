/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

package org.apache.cocoon.components.elementprocessor.types;

import java.io.IOException;

/**
 * This interface allows a client of NumericConverter to apply more
 * restrictive rules to the number that the NumericConverter obtained.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: Validator.java,v 1.4 2004/03/05 13:02:07 bdelacretaz Exp $
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

    public IOException validate(final Number number);
}   // end public interface Validator
