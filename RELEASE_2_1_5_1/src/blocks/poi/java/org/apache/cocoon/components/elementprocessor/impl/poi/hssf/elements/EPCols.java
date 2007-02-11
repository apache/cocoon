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
 * @version CVS $Id: EPCols.java,v 1.6 2004/03/05 13:02:03 bdelacretaz Exp $
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
