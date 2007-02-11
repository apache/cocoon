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
 * Print orientation -- encapsulation of the strings representing the
 * print orientation, and a simpler way to deal with them.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: PrintOrientation.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class PrintOrientation {
    private static final String _landscape = "landscape";
    private static final String _portrait = "portrait";
    public static final int PRINT_ORIENTATION_LANDSCAPE = 0;
    public static final int PRINT_ORIENTATION_PORTRAIT = 1;
    private static final NumericResult _null_result =
        new NumericResult(new IOException("print orientation cannot be null"));
    private static final NumericResult _landscape_result =
        new NumericResult(new Integer(PRINT_ORIENTATION_LANDSCAPE));
    private static final NumericResult _portrait_result =
        new NumericResult(new Integer(PRINT_ORIENTATION_PORTRAIT));

    private PrintOrientation() {}

    /**
     * convert a string into a NumericResult
     * @param value the string describing the print orientation
     * @return a NumericResult containing either one of the public enumeration
     *             values, or an appropriate IOException
     */
    public static NumericResult extractPrintOrientation(final String value) {
        NumericResult rval = null;
        String input = (value == null) ? null : value.trim();

        if (input == null) {
            rval = _null_result;
        } else if (input.equalsIgnoreCase(_landscape)) {
            rval = _landscape_result;
        } else if (input.equalsIgnoreCase(_portrait)) {
            rval = _portrait_result;
        } else {
            rval = new NumericResult( new IOException(
                        "\"" + input + "\" is not a valid print orientation"));
        }
        return rval;
    }
} // end public class PrintOrientation
