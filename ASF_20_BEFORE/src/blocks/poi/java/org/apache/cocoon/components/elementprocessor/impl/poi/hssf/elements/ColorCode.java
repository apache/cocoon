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

import java.io.IOException;

import java.util.StringTokenizer;

/**
 * Color codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id: ColorCode.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
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
