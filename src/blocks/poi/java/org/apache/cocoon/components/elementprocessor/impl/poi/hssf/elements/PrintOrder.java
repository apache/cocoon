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
 * Print order -- encapsulation of the strings representing the print
 * ordering, and a simpler way to deal with them.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: PrintOrder.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class PrintOrder {
    private static final String _right_then_down = "r_then_d";
    private static final String _down_then_right = "d_then_r";
    public static final int PRINT_ORDER_RIGHT_THEN_DOWN = 0;
    public static final int PRINT_ORDER_DOWN_THEN_RIGHT = 1;
    private static final NumericResult _null_result =
        new NumericResult(new IOException("print order cannot be null"));
    private static final NumericResult _right_then_down_result =
        new NumericResult(new Integer(PRINT_ORDER_RIGHT_THEN_DOWN));
    private static final NumericResult _down_then_right_result =
        new NumericResult(new Integer(PRINT_ORDER_DOWN_THEN_RIGHT));

    private PrintOrder() {}

    /**
     * convert a string into a NumericResult
     * @param value the string describing the print order
     * @return a NumericResult containing either one of the public enumeration
     *             values, or an appropriate IOException
     */
    public static NumericResult extractPrintOrder(final String value) {
        NumericResult rval = null;
        String input = (value == null) ? null : value.trim();

        if (input == null) {
            rval = _null_result;
        } else if (input.equalsIgnoreCase(_right_then_down)) {
            rval = _right_then_down_result;
        } else if (input.equalsIgnoreCase(_down_then_right)) {
            rval = _down_then_right_result;
        } else {
            rval = new NumericResult(new IOException(
                    "\"" + input + "\" is not a valid print order"));
        }
        return rval;
    }
} // end public class PrintOrder
