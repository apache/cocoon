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

/**
 * Border style codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: BorderStyle.java,v 1.5 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class BorderStyle
{
    public static final int BORDER_STYLE_NONE                = 0;    
    public static final int BORDER_STYLE_THIN                = 1;
    public static final int BORDER_STYLE_MEDIUM              = 2;
    public static final int BORDER_STYLE_DASHED              = 3;
    public static final int BORDER_STYLE_DOTTED              = 4;
    public static final int BORDER_STYLE_THICK               = 5;
    public static final int BORDER_STYLE_DOUBLE              = 6;
    public static final int BORDER_STYLE_HAIR                = 7;
    public static final int BORDER_STYLE_MEDIUM_DASH         = 8;
    public static final int BORDER_STYLE_DASH_DOT            = 9;
    public static final int BORDER_STYLE_MEDIUM_DASH_DOT     = 10;
    public static final int BORDER_STYLE_DASH_DOT_DOT        = 11;
    public static final int BORDER_STYLE_MEDIUM_DASH_DOT_DOT = 12;
    public static final int BORDER_STYLE_SLANTED_DASH_DOT    = 13;

    private BorderStyle() {
    }

    /**
     * Is this a valid border style?
     *
     * @param val value to be checked
     *
     * @return true if valid, false otherwise
     */

    public static boolean isValid(int val) {
        return ((val >= BORDER_STYLE_NONE)
                && (val <= BORDER_STYLE_SLANTED_DASH_DOT));
    }
}   // end public class BorderStyle
