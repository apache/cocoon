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

import org.apache.cocoon.components.elementprocessor.types.NumericResult;

import java.io.IOException;

/**
 * Print order -- encapsulation of the strings representing the print
 * ordering, and a simpler way to deal with them.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: PrintOrder.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
 */
public class PrintOrder {
    private static final String _right_then_down = "r_then_d";
    private static final String _down_then_right = "d_then_r";
    public static final int PRINT_ORDER_RIGHT_THEN_DOWN = 0;
    public static final int PRINT_ORDER_DOWN_THEN_RIGHT = 1;
    private static final NumericResult _null_result =
        new NumericResult(new IOException("print order cannot be null"));
    private static final NumericResult _right_then_down_result =
        new NumericResult(new Integer(PRINT_ORDER_RIGHT_THEN_DOWN));
    private static final NumericResult _down_then_right_result =
        new NumericResult(new Integer(PRINT_ORDER_DOWN_THEN_RIGHT));

    private PrintOrder() {}

    /**
     * convert a string into a NumericResult
     * @param value the string describing the print order
     * @return a NumericResult containing either one of the public enumeration
     *             values, or an appropriate IOException
     */
    public static NumericResult extractPrintOrder(final String value) {
        NumericResult rval = null;
        String input = (value == null) ? null : value.trim();

        if (input == null) {
            rval = _null_result;
        } else if (input.equalsIgnoreCase(_right_then_down)) {
            rval = _right_then_down_result;
        } else if (input.equalsIgnoreCase(_down_then_right)) {
            rval = _down_then_right_result;
        } else {
            rval = new NumericResult(new IOException(
                    "\"" + input + "\" is not a valid print order"));
        }
        return rval;
    }
} // end public class PrintOrder
