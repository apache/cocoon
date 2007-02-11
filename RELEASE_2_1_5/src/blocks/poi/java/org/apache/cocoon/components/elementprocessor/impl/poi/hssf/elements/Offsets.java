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

import java.io.IOException;

/**
 * Offsets. This particular object is represented in gnumeric's XML as
 * four doubles, space separated. Presumably, each represents an
 * offset in a particular direction -- top, bottom, left, right -- but
 * what the reference is for each offset is not known, nor is it known
 * which one is top, bottom, left, or right, or even whether that's
 * the correct interpretation of the numbers. This is an area of the
 * gnumeric XML that is not terribly well documented even in their
 * code, and I don't think the features that use offsets are terribly
 * mature yet.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: Offsets.java,v 1.6 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class Offsets {
    private static final int _component_count = 4;
    private double[] _components = new double[_component_count];

    /**
     * construct the Offsets object
     * @param value the string containing the offset values
     * @exception IOException if the string is badly formed
     */
    public Offsets(final String value) throws IOException {
        if (value == null) {
            throw new IOException("cannot process a null offsets string");
        }
        char[] input = value.trim().toCharArray();
        int index = 0;

        for (int j = 0; j < _component_count; j++) {
            while (index < input.length
                && Character.isWhitespace(input[index])) {
                ++index;
            }
            if (index == input.length) {
                throw new IOException("insufficient offsets in string");
            }
            int tailIndex = index;

            while (tailIndex < input.length
                && !Character.isWhitespace(input[tailIndex])) {
                ++tailIndex;
            }
            _components[j] = NumericConverter
                    .extractDouble(new String(input, index, tailIndex - index))
                    .doubleValue();
            index = tailIndex;
        }
        if (new String(input, index, input.length - index).trim().length()
            != 0) {
            throw new IOException(
                "Too much data in string for " + _component_count + " offsets");
        }
    }

    /**
     * @return components
     */
    public double[] getComponents() {
        return _components;
    }
} // end public class Offsets
