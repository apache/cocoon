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
 * Horizontal alignment is written as an integer, and each bit in the
 * integer specifies a particular boolean attribute. This class deals
 * with all that information in an easily digested form.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: HorizontalAlignment.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class HorizontalAlignment {
    private int _alignment;
    private static final int _general = 1;
    private static final int _left = 2;
    private static final int _right = 4;
    private static final int _center = 8;
    private static final int _fill = 16;
    private static final int _justify = 32;
    private static final int _center_across_selection = 64;
    private static final Validator _validator = new Validator() {
        public IOException validate(final Number number) {
            int value = number.intValue();

            return ((value >= 0) && (value <= 127)) ? null
                : new IOException("\"" + number + "\" is out of range");
        }
    };

    /**
     * Create an HorizontalAlignment object
     * @param value the string containing the horizontal alignment data
     * @exception IOException if the data is malformed
     */
    public HorizontalAlignment(final String value) throws IOException {
        _alignment =
            NumericConverter.extractInteger(value, _validator).intValue();
    }

    /**
     * @return true if general bit is set
     */
    public boolean isGeneral() {
        return (_alignment & _general) == _general;
    }

    /**
     * @return true if left bit is set
     */
    public boolean isLeft() {
        return (_alignment & _left) == _left;
    }

    /**
     * @return true if right bit is set
     */
    public boolean isRight() {
        return (_alignment & _right) == _right;
    }

    /**
     * @return true if center bit is set
     */
    public boolean isCenter() {
        return (_alignment & _center) == _center;
    }

    /**
     * @return true if fill bit is set
     */
    public boolean isFill() {
        return (_alignment & _fill) == _fill;
    }

    /**
     * @return true if justify bit is set
     */
    public boolean isJustify() {
        return (_alignment & _justify) == _justify;
    }

    /**
     * @return true if center across selection bit is set
     */
    public boolean isCenterAcrossSelection() {
        return (_alignment & _center_across_selection)
            == _center_across_selection;
    }

    short getCode() {
        return (short)_alignment;
    }
} // end public class HorizontalAlignment
