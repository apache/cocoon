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

import org.apache.cocoon.components.elementprocessor.types.Attribute;
import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Cols" tag
 *
 * This element has an attribute (DefaultSizePts) and is a container
 * element
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPCols.java,v 1.5 2004/01/31 08:50:39 antonio Exp $
 */
public class EPCols extends BaseElementProcessor {
    private NumericResult _default_size_pts;
    private boolean _default_size_pts_fetched;
    private static final String _default_size_pts_attribute = "DefaultSizePts";

    // package scope so test code can access
    static final String DEFAULT_SIZE_PTS = "40.0";

    /**
     * constructor
     */
    public EPCols() {
        super(null);
        _default_size_pts = null;
        _default_size_pts_fetched = false;
    }

    /**
     * get the default size of columns, in points
     * @return size in points
     * @exception IOException if the attribute is malformed
     * @exception NullPointerException if the attribute is missing
     */
    public double getDefaultSizePts()
        throws IOException, NullPointerException {
        if (!_default_size_pts_fetched) {
            String value = getValue(_default_size_pts_attribute);

            if (value == null || value.trim().equals("")) {
                value = DEFAULT_SIZE_PTS;
            }
            _default_size_pts = NumericConverter.extractDouble(value);
            _default_size_pts_fetched = true;
        }
        return _default_size_pts.doubleValue();
    }

    /**
     * Override of Initialize() implementation
     * @param attributes the array of Attribute instances; may be empty, will
     *                  never be null
     * @param parent the parent ElementProcessor; may be null
     * @exception IOException if anything is wrong
     */
    public void initialize(
        final Attribute[] attributes,
        final ElementProcessor parent)
        throws IOException {
        super.initialize(attributes, parent);
        getSheet().setDefaultColumnWidth(getDefaultSizePts());
    }
} // end public class EPCols
