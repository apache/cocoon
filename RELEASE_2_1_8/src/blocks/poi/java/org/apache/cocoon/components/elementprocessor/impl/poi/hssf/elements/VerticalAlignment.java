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
 * Vertical alignment is written as an integer, and each bit in the
 * integer specifies a particular boolean attribute. This class deals
 * with all that information in an easily digested form.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: VerticalAlignment.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class VerticalAlignment {
    private int _alignment;
    private static final int _top = 1;
    private static final int _bottom = 2;
    private static final int _center = 4;
    private static final int _justify = 8;
    private static final Validator _validator = new Validator() {
        public IOException validate(final Number number) {
            int value = number.intValue();

            return (value >= 0 && value <= 15) ? null
                : new IOException("\"" + number + "\" is out of range");
        }
    };

    /**
     * Create a VerticalAlignment object
     * @param value the string containing the vertical alignment data
     * @exception IOException if the data is malformed
     */
    public VerticalAlignment(final String value) throws IOException {
        _alignment =
            NumericConverter.extractInteger(value, _validator).intValue();
    }

    /**
     * @return true if top bit is set
     */
    public boolean isTop() {
        return (_alignment & _top) == _top;
    }

    /**
     * @return true if bottom bit is set
     */
    public boolean isBottom() {
        return (_alignment & _bottom) == _bottom;
    }

    /**
     * @return true if center bit is set
     */
    public boolean isCenter() {
        return (_alignment & _center) == _center;
    }

    /**
     * @return true if justify bit is set
     */
    public boolean isJustify() {
        return (_alignment & _justify) == _justify;
    }

    public short getCode() {
        return (short)_alignment;
    }
} // end public class VerticalAlignment
