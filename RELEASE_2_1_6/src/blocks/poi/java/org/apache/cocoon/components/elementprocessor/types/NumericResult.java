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
 * This class holds the result of a numeric conversion. The result is
 * either a valid value, or an IOException that was created by the
 * numeric converter
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: NumericResult.java,v 1.5 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class NumericResult
{
    private Number      _value;
    private IOException _exception;

    /**
     * Constructor
     *
     * @param value the numeric value
     */

    public NumericResult(final Number value) {
        this();
        _value = value;
    }

    /**
     * Constructor
     *
     * @param exception the exception to be thrown
     */

    public NumericResult(final IOException exception) {
        this();
        _exception = exception;
    }

    private NumericResult() {
        _value     = null;
        _exception = null;
    }

    /**
     * Get the value, if possible, as an int
     *
     * @return the value, as an int
     *
     * @exception IOException if there was a problem converting the
     *            number
     */

    public int intValue() throws IOException {
        return value().intValue();
    }

    /**
     * Get the value, if possible, as a double
     *
     * @return the value, as a double
     *
     * @exception IOException if there was a problem converting the
     *            number
     */

    public double doubleValue() throws IOException {
        return value().doubleValue();
    }

    private Number value() throws IOException {
        if (_exception != null) {
            throw _exception;
        }
        return _value;
    }
}   // end public class NumericResult
