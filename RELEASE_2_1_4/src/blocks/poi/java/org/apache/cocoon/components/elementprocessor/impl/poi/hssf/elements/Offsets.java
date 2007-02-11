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
 * @version CVS $Id: Offsets.java,v 1.5 2004/01/31 08:50:39 antonio Exp $
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
