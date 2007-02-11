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

import org.apache.cocoon.components.elementprocessor.types.NumericResult;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the
 * "orientation" tag
 *
 * This element has no attributes and contains a string describing the
 * print orientation (landscape or portrait)
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EP_Orientation.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EP_Orientation extends BaseElementProcessor {
    private NumericResult _print_orientation;

    /**
     * constructor
     */
    public EP_Orientation() {
        super(null);
        _print_orientation = null;
    }

    /**
     * @return print orientation (always one of the values in
     *         PrintOrientation)
     *
     * @exception IOException
     */
    int getPrintOrientation() throws IOException {
        if (_print_orientation == null) {
            _print_orientation =
                PrintOrientation.extractPrintOrientation(getData());
        }
        return _print_orientation.intValue();
    }
}   // end public class EP_Orientation
