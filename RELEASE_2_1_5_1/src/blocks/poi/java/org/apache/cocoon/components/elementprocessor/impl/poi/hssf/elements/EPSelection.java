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

import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the
 * "Selection" tag
 *
 * This element is a container of other elements and has four
 * attributes that define the boundaries of the region.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPSelection.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EPSelection extends BaseElementProcessor {
    private static final String _start_col_attribute = "startCol";
    private static final String _start_row_attribute = "startRow";
    private static final String _end_col_attribute = "endCol";
    private static final String _end_row_attribute = "endRow";
    private NumericResult _start_col;
    private NumericResult _start_row;
    private NumericResult _end_col;
    private NumericResult _end_row;

    /**
     * constructor
     */
    public EPSelection() {
        super(null);
        _start_col = null;
        _start_row = null;
        _end_col = null;
        _end_row = null;
    }

    /**
     * @return start row
     * @exception IOException
     */
    public int getStartRow() throws IOException {
        if (_start_row == null) {
            _start_row = NumericConverter.extractNonNegativeInteger(
                    getValue(_start_row_attribute));
        }
        return _start_row.intValue();
    }

    /**
     * @return start column
     * @exception IOException
     */
    public int getStartCol() throws IOException {
        if (_start_col == null) {
            _start_col = NumericConverter.extractNonNegativeInteger(
                    getValue(_start_col_attribute));
        }
        return _start_col.intValue();
    }

    /**
     * @return end row
     * @exception IOException
     */
    public int getEndRow() throws IOException {
        if (_end_row == null) {
            _end_row = NumericConverter.extractNonNegativeInteger(
                    getValue(_end_row_attribute));
        }
        return _end_row.intValue();
    }

    /**
     * @return end column
     * @exception IOException
     */
    public int getEndCol() throws IOException {
        if (_end_col == null) {
            _end_col = NumericConverter.extractNonNegativeInteger(
                    getValue(_end_col_attribute));
        }
        return _end_col.intValue();
    }
} // end public class EPSelection
