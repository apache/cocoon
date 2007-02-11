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
 * Style shading codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: StyleShading.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class StyleShading {
    public static final int STYLE_SHADING_NONE = 0;
    public static final int STYLE_SHADING_SOLID = 1;
    public static final int STYLE_SHADING_PERCENT75 = 2;
    public static final int STYLE_SHADING_PERCENT50 = 3;
    public static final int STYLE_SHADING_PERCENT25 = 4;
    public static final int STYLE_SHADING_PERCENT12PT5 = 5;
    public static final int STYLE_SHADING_PERCENT6PT25 = 6;
    public static final int STYLE_SHADING_HORIZONTAL_STRIPE = 7;
    public static final int STYLE_SHADING_VERTICAL_STRIPE = 8;
    public static final int STYLE_SHADING_REVERSE_DIAGONAL_STRIPE = 9;
    public static final int STYLE_SHADING_DIAGONAL_STRIPE = 10;
    public static final int STYLE_SHADING_DIAGONAL_CROSSHATCH = 11;
    public static final int STYLE_SHADING_THICK_DIAGONAL_CROSSHATCH = 12;
    public static final int STYLE_SHADING_THIN_HORIZONTAL_STRIPE = 13;
    public static final int STYLE_SHADING_THIN_VERTICAL_STRIPE = 14;
    public static final int STYLE_SHADING_THIN_REVERSE_DIAGONAL_STRIPE = 15;
    public static final int STYLE_SHADING_THIN_DIAGONAL_STRIPE = 16;
    public static final int STYLE_SHADING_THIN_CROSSHATCH = 17;
    public static final int STYLE_SHADING_THIN_DIAGONAL_CROSSHATCH = 18;
    public static final int STYLE_SHADING_APPLIX_SMALL_CIRCLE = 19;
    public static final int STYLE_SHADING_APPLIX_SEMICIRCLE = 20;
    public static final int STYLE_SHADING_APPLIX_SMALL_THATCH = 21;
    public static final int STYLE_SHADING_APPLIX_ROUND_THATCH = 22;
    public static final int STYLE_SHADING_APPLIX_BRICK = 23;
    public static final int STYLE_SHADING_PERCENT100 = 24;
    public static final int STYLE_SHADING_PERCENT87PT5 = 25;

    private StyleShading() {}

    /**
     * Is this a valid style shading?
     * @param val value to be checked
     * @return true if valid, false otherwise
     */
    public static boolean isValid(int val) {
        return (val >= STYLE_SHADING_NONE && val <= STYLE_SHADING_PERCENT87PT5);
    }
} // end public class StyleShading
