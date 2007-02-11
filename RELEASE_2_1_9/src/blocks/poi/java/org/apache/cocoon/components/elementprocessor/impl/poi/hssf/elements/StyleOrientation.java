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

import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import java.io.IOException;

/**
 * Style orientation is written as an integer, and each bit in the
 * integer specifies a particular boolean attribute. This class deals
 * with all that information in an easily digested form.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: StyleOrientation.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class StyleOrientation {
    private int _alignment;
    private static final int _horiz = 1;
    private static final int _vert_horiz_text = 2;
    private static final int _vert_vert_text = 4;
    private static final int _vert_vert_text2 = 8;
    private static final Validator _validator = new Validator() {
        public IOException validate(final Number number) {
            int value = number.intValue();

            return (value >= 0 && value <= 15) ? null
                : new IOException("\"" + number + "\" is out of range");
        }
    };

    /**
     * Create a StyleOrientation object
     * @param value the string containing the style orientation data
     * @exception IOException if the data is malformed
     */
    public StyleOrientation(final String value) throws IOException {
        _alignment =
            NumericConverter.extractInteger(value, _validator).intValue();
    }

    /**
     * @return true if horiz bit is set
     */
    public boolean isHoriz() {
        return (_alignment & _horiz) == _horiz;
    }

    /**
     * @return true if vert horiz text bit is set
     */
    public boolean isVertHorizText() {
        return (_alignment & _vert_horiz_text) == _vert_horiz_text;
    }

    /**
     * @return true if vert vert text bit is set
     */
    public boolean isVertVertText() {
        return (_alignment & _vert_vert_text) == _vert_vert_text;
    }

    /**
     * @return true if vert vert text2 bit is set
     */
    public boolean isVertVertText2() {
        return (_alignment & _vert_vert_text2) == _vert_vert_text2;
    }
} // end public class StyleOrientation
