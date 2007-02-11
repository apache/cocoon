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

import java.io.IOException;

import java.util.StringTokenizer;

/**
 * Color codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id: ColorCode.java,v 1.5 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class ColorCode {
    private static final int _red             = 0;
    private static final int _green           = 1;
    private static final int _blue            = 2;
    private static final int _component_count = 3;
    private int[]            _components      = new int[ _component_count ];
    private String           rgbstring        = null;

    /**
     * construct the ColorCode object
     *
     * @param value the string containing the colors
     *
     * @exception IOException if the string is badly formed
     */

    public ColorCode(final String value) throws IOException {
        rgbstring = value;
        if (value == null) {
            throw new IOException("cannot process a null color code");
        }
        StringTokenizer tokenizer = new StringTokenizer(value.trim(), ":");

        if (tokenizer.countTokens() != _component_count) {
            throw new IOException("color code must have exactly "
                      + _component_count + " components, no more, no less");
        }
        for (int j = 0; j < _component_count; j++) {
            try {
                _components[j] = Integer.parseInt(tokenizer.nextToken(), 16);
            } catch (Exception e) {
                throw new IOException("cannot parse component #" + j + " ("
                                      + e.getMessage() + ")");
            }
            if (_components[j] < 0 || _components[j] > 65535) {
                throw new IOException("Component #" + j + " is out of range");
            }
        }
    }

    /**
     * @return red component
     */

    public int getRed() {
        return _components[_red];
    }

    /**
     * @return green component
     */

    public int getGreen() {
        return _components[_green];
    }

    /**
     * @return blue component
     */

    public int getBlue() {
        return _components[_blue];
    }
    
    public String toString() {
        return rgbstring;
    }
}   // end public class ColorCode
