/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements;

/**
 * Style shading codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: StyleShading.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
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
