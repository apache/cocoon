/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.components.elementprocessor.types;

import org.apache.cocoon.CascadingIOException;

import java.io.IOException;

/**
 * This class knows how to convert strings into numbers, and also
 * knows how to check the results against certain criteria
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: NumericConverter.java,v 1.5 2004/01/31 08:50:43 antonio Exp $
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
