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
 * Print units -- encapsulation of the strings representing them, and
 * a simpler way to deal with them.GTK type codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: PrintUnits.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
 */
public class PrintUnits {
    private static final String _cm = "cm";
    private static final String _in = "in";
    private static final String _mm = "mm";
    private static final String _points = "points";
    public static final int PRINT_UNITS_CM = 0;
    public static final int PRINT_UNITS_IN = 1;
    public static final int PRINT_UNITS_MM = 2;
    public static final int PRINT_UNITS_POINTS = 3;
    private static final NumericResult _null_result =
        new NumericResult(new IOException("print units cannot be null"));
    private static final NumericResult _cm_result =
        new NumericResult(new Integer(PRINT_UNITS_CM));
    private static final NumericResult _in_result =
        new NumericResult(new Integer(PRINT_UNITS_IN));
    private static final NumericResult _mm_result =
        new NumericResult(new Integer(PRINT_UNITS_MM));
    private static final NumericResult _points_result =
        new NumericResult(new Integer(PRINT_UNITS_POINTS));

    private PrintUnits() {}

    /**
     * convert a string into a NumericResult
     * @param value the string describing the print units
     * @return a NumericResult containing either one of the public enumeration
     *             values, or an appropriate IOException
     */
    public static NumericResult extractPrintUnits(final String value) {
        NumericResult rval = null;
        String input = (value == null) ? null : value.trim();

        if (input == null) {
            rval = _null_result;
        } else if (input.equalsIgnoreCase(_cm)) {
            rval = _cm_result;
        } else if (input.equalsIgnoreCase(_in)) {
            rval = _in_result;
        } else if (input.equalsIgnoreCase(_mm)) {
            rval = _mm_result;
        } else if (input.equalsIgnoreCase(_points)) {
            rval = _points_result;
        } else {
            rval = new NumericResult(new IOException(
                        "\"" + input + "\" is not a valid print unit"));
        }
        return rval;
    }
} // end public class PrintUnits
