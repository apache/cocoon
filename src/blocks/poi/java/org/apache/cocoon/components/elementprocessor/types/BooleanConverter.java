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
 * This class knows how to convert strings into numbers, and also
 * knows how to check the results against certain criteria
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: BooleanConverter.java,v 1.4 2004/03/05 13:02:07 bdelacretaz Exp $
 */
public class BooleanConverter
{
    private static final String        _true_values[]  =
    {
        "1", "true"
    };
    private static final String        _false_values[] =
    {
        "0", "false"
    };
    private static final BooleanResult _true_result    =
        new BooleanResult(true);
    private static final BooleanResult _false_result   =
        new BooleanResult(false);

    private BooleanConverter() {
    }

    /**
     * Given a string that is expected to hold a boolean, get the
     * boolean value.
     *
     * @param value the string holding the boolean
     *
     * @return a BooleanResult object containing either the boolean
     *         value or an exception generated if there was a problem
     *         with the value;
     */

    public static BooleanResult extractBoolean(final String value) {
        String input  = (value == null) ? "" : value.trim();
        BooleanResult result = null;

        for (int k = 0; k < _true_values.length; k++) {
            if (_true_values[k].equalsIgnoreCase(input)) {
                result = _true_result;
                break;
            }
        }
        if (result == null) {
            for (int k = 0; k < _false_values.length; k++) {
                if (_false_values[k].equalsIgnoreCase(input)) {
                    result = _false_result;
                    break;
                }
            }
        }
        if (result == null) {
            result = new BooleanResult(
                new IOException("\"" + input + "\" is not a boolean value"));
        }
        return result;
    }
}   // end public class BooleanConverter
