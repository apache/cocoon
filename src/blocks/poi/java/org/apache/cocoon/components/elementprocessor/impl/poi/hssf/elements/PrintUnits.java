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

package org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements;

import org.apache.cocoon.components.elementprocessor.types.NumericResult;

import java.io.IOException;

/**
 * Print units -- encapsulation of the strings representing them, and
 * a simpler way to deal with them.GTK type codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: PrintUnits.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class PrintUnits {
    private static final String _cm = "cm";
    private static final String _in = "in";
    private static final String _mm = "mm";
    private static final String _points = "points";
    public static final int PRINT_UNITS_CM = 0;
    public static final int PRINT_UNITS_IN = 1;
    public static final int PRINT_UNITS_MM = 2;
    public static final int PRINT_UNITS_POINTS = 3;
    private static final NumericResult _null_result =
        new NumericResult(new IOException("print units cannot be null"));
    private static final NumericResult _cm_result =
        new NumericResult(new Integer(PRINT_UNITS_CM));
    private static final NumericResult _in_result =
        new NumericResult(new Integer(PRINT_UNITS_IN));
    private static final NumericResult _mm_result =
        new NumericResult(new Integer(PRINT_UNITS_MM));
    private static final NumericResult _points_result =
        new NumericResult(new Integer(PRINT_UNITS_POINTS));

    private PrintUnits() {}

    /**
     * convert a string into a NumericResult
     * @param value the string describing the print units
     * @return a NumericResult containing either one of the public enumeration
     *             values, or an appropriate IOException
     */
    public static NumericResult extractPrintUnits(final String value) {
        NumericResult rval = null;
        String input = (value == null) ? null : value.trim();

        if (input == null) {
            rval = _null_result;
        } else if (input.equalsIgnoreCase(_cm)) {
            rval = _cm_result;
        } else if (input.equalsIgnoreCase(_in)) {
            rval = _in_result;
        } else if (input.equalsIgnoreCase(_mm)) {
            rval = _mm_result;
        } else if (input.equalsIgnoreCase(_points)) {
            rval = _points_result;
        } else {
            rval = new NumericResult(new IOException(
                        "\"" + input + "\" is not a valid print unit"));
        }
        return rval;
    }
} // end public class PrintUnits
