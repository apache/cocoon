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
 * This class holds the result of a boolean conversion. The result is
 * either a valid value, or an IOException that was created by the
 * boolean converter
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: BooleanResult.java,v 1.4 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class BooleanResult
{
    private boolean     _value;
    private IOException _exception;

    /**
     * Constructor; package scope, as only a boolean converter should
     * generate one of these
     *
     * @param value the boolean value
     */

    BooleanResult(final boolean value) {
        this();
        _value = value;
    }

    /**
     * Constructor; package scope, as only a boolean converter should
     * generate one of these
     *
     * @param exception the exception to be thrown
     */

    BooleanResult(final IOException exception) {
        this();
        _exception = exception;
    }

    private BooleanResult() {
        _value     = false;
        _exception = null;
    }

    /**
     * Get the value as a boolean
     *
     * @return the value as a boolean
     *
     * @exception IOException if there was a problem converting the
     *            value
     */

    public boolean booleanValue() throws IOException {
        if (_exception != null) {
            throw _exception;
        }
        return _value;
    }
}   // end public class BooleanResult
