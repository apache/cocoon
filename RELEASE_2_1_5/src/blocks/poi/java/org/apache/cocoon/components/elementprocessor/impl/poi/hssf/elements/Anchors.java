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
 * Anchors. This particular object is represented in gnumeric's XML as four
 * integers, space separated. Presumably, each represents an anchor for a
 * particular direction -- top, bottom, left, right -- but what the reference
 * is for each anchor is not known, nor is it known which one is top, bottom,
 * left, or right, or even whether that's the correct interpretation of the
 * numbers. This is an area of the gnumeric XML that is not terribly well
 * documented even in their code, and I don't think the features that use
 * anchors are terribly mature yet.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: Anchors.java,v 1.6 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class Anchors
{
    private static final int       _component_count                  = 4;
    private int[]                  _components                       =
        new int[ _component_count ];

    // Each element of an anchor has to be one of these values:
    public static final int        ANCHOR_UNKNOWN                    = 0;
    public static final int        ANCHOR_PERCENTAGE_FROM_COLROW_ST  = 16;
    public static final int        ANCHOR_PERCENTAGE_FROM_COLROW_END = 17;
    public static final int        ANCHOR_PTS_FROM_COLROW_START      = 32;
    public static final int        ANCHOR_PTS_FROM_COLROW_END        = 33;
    public static final int        ANCHOR_PTS_ABSOLUTE               = 48;
    private static final Validator _validator                        =
        new Validator()
    {
        public IOException validate(final Number number) {
            switch (number.intValue()) {
                case ANCHOR_UNKNOWN :
                case ANCHOR_PERCENTAGE_FROM_COLROW_ST :
                case ANCHOR_PERCENTAGE_FROM_COLROW_END :
                case ANCHOR_PTS_FROM_COLROW_START :
                case ANCHOR_PTS_FROM_COLROW_END :
                case ANCHOR_PTS_ABSOLUTE :
                    return null;

                default :
                    return new IOException("\"" + number
                                           + "\" is not a legal value");
            }
        }
    };

    /**
     * construct the Anchors object
     *
     * @param value the string containing the anchor values
     *
     * @exception IOException if the string is badly formed
     */

    public Anchors(final String value) throws IOException {
        if (value == null) {
            throw new IOException("cannot process a null anchors string");
        }
        char[]   input   = value.trim().toCharArray();
        int      index   = 0;

        for (int j = 0; j < _component_count; j++) {
            while (index < input.length
                    && Character.isWhitespace(input[index])) {
                ++index;
            }
            if (index == input.length) {
                throw new IOException("insufficient anchors in string");
            }
            int tailIndex = index;

            while (tailIndex < input.length
                    && !Character.isWhitespace(input[tailIndex])) {
                ++tailIndex;
            }
            _components[j] = NumericConverter.extractInteger(new String(input,
                    index, tailIndex - index), _validator).intValue();
            index = tailIndex;
        }
        if (new String(input, index, input.length - index).trim().length() != 0) {
            throw new IOException("Too much data in string for "
                                  + _component_count + " anchors");
        }
    }

    /**
     * @return components
     */

    public int [] getComponents() {
        return _components;
    }
}   // end public class Anchors
