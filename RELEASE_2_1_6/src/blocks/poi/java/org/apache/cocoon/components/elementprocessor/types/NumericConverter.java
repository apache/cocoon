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

import org.apache.cocoon.CascadingIOException;

import java.io.IOException;

/**
 * This class knows how to convert strings into numbers, and also
 * knows how to check the results against certain criteria
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: NumericConverter.java,v 1.6 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class NumericConverter
{
    private static final Validator _non_negative_validator = new Validator()
    {
        public IOException validate(final Number number) {
            IOException e = null;

            if (number.intValue() < 0) {
                e = new IOException("\"" + number.intValue()
                                    + "\" is not a non-negative integer");
            }
            return e;
        }
    };
    private static final Validator _positive_validator = new Validator()
    {
        public IOException validate(final Number number) {
            IOException e = null;

            if (number.intValue() < 1) {
                e = new IOException("\"" + number.intValue()
                                    + "\" is not a positive integer");
            }
            return e;
        }
    };

    private NumericConverter() {
    }

    /**
     * Shortcut for extractDouble without a Validator
     *
     * @param value the string holding the double
     * @return a NumericResult object containing either the double
     *         value or an exception generated if there was a problem
     *         with the value;
     */

    public static NumericResult extractDouble(final String value) {
        return extractDouble(value, null);
    }

    /**
     * Given a string that is expected to hold a double, get the double value.
     *
     * @param value the string holding the double
     * @param validator a Validator object; if null, no additional
     *                  validation will be performed
     *
     * @return a NumericResult object containing either the double
     *         value or an exception generated if there was a problem
     *         with the value;
     */

    public static NumericResult extractDouble(final String value,
                final Validator validator) {
        String input  = (value == null) ? "" : value.trim();
        NumericResult result = null;

        try {
            Number number = new Double(input);
            IOException exception = null;

            if (validator != null) {
                exception = validator.validate(number);
            }
            if (exception == null) {
                result = new NumericResult(number);
            } else {
                result = new NumericResult(exception);
            }
        } catch (NumberFormatException ignored) {
            result = new NumericResult(
                new CascadingIOException(
                    "\"" + input + "\" does not represent a double value", ignored));
        }
        return result;
    }

    /**
     * Shortcut for extractInteger without a Validator
     *
     * @param value the string holding the integer
     * @return a NumericResult object containing either the integer
     *         value or an exception generated if there was a problem
     *         with the value;
     */

    public static NumericResult extractInteger(final String value) {
        return extractInteger(value, null);
    }

    /**
     * Given a string that is expected to hold a integer, get the integer value.
     *
     * @param value the string holding the integer
     * @param validator a Validator object; if null, no additional
     *                  validation will be performed
     *
     * @return a NumericResult object containing either the integer
     *         value or an exception generated if there was a problem
     *         with the value;
     */

    public static NumericResult extractInteger(final String value,
            final Validator validator) {
        String input  = (value == null) ? "" : value.trim();
        NumericResult result = null;

        try {
            Number      number    = new Integer(input);
            IOException exception = null;

            if (validator != null) {
                exception = validator.validate(number);
            }
            if (exception == null) {
                result = new NumericResult(number);
            } else {
                result = new NumericResult(exception);
            }
        } catch (NumberFormatException ignored) {
            result = new NumericResult(
                new CascadingIOException(
                    "\"" + input + "\" does not represent an integer value", ignored));
        }
        return result;
    }

    /**
     * extract a positive integer (i.e., an integer with a range of 1
     * ... MAX_VALUE)
     *
     * @param value the string holding the value
     *
     * @return a NumericResult object containing either the integer
     *         value or an exception generated if there was a problem
     *         with the value;
     */

    public static NumericResult extractPositiveInteger(final String value) {
        return extractInteger(value, _positive_validator);
    }

    /**
     * extract a non-negative integer (i.e., an integer with a range
     * of 1 ... MAX_VALUE)
     *
     * @param value the string holding the value
     *
     * @return a NumericResult object containing either the integer
     *         value or an exception generated if there was a problem
     *         with the value;
     */

    public static NumericResult extractNonNegativeInteger(final String value) {
        return extractInteger(value, _non_negative_validator);
    }
}   // end public class NumericConverter
