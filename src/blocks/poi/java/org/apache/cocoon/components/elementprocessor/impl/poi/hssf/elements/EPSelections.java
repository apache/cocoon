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
 * No-op implementation of ElementProcessor to handle the "Selections"
 * tag
 *
 * This element is a container element with two attributes: CursorCol
 * and CursorRow, which presumable show where the cursor should be.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPSelections.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EPSelections extends BaseElementProcessor {
    private static final String _cursor_col_attribute = "CursorCol";
    private static final String _cursor_row_attribute = "CursorRow";
    private NumericResult _cursor_col;
    private NumericResult _cursor_row;

    /**
     * constructor
     */
    public EPSelections() {
        super(null);
        _cursor_col = null;
        _cursor_row = null;
    }

    /**
     * @return cursor column
     * @exception IOException
     */
    public int getCursorCol() throws IOException {
        if (_cursor_col == null) {
            _cursor_col = NumericConverter.extractNonNegativeInteger(
                    getValue(_cursor_col_attribute));
        }
        return _cursor_col.intValue();
    }

    /**
     * @return cursor row
     * @exception IOException
     */
    public int getCursorRow() throws IOException {
        if (_cursor_row == null) {
            _cursor_row = NumericConverter.extractNonNegativeInteger(
                    getValue(_cursor_row_attribute));
        }
        return _cursor_row.intValue();
    }
} // end public class EPSelections
