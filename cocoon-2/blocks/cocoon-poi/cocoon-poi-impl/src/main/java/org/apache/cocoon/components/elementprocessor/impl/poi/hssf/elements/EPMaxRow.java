/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
 * No-op implementation of ElementProcessor to handle the "MaxRow" tag
 *
 * This element contains the maximum number of rows in the containing
 * sheet. The value is contained in the data.
 *
 * @version $Id$
 */
public class EPMaxRow extends BaseElementProcessor {
    private NumericResult _max_row;

    /**
     * constructor
     */
    public EPMaxRow() {
        super(null);
        _max_row = null;
    }

    /**
     * get the maximum row for the containing sheet
     * @return maximum row number
     * @exception IOException if the data is malformed
     */
    public int getMaxRow() throws IOException {
        if (_max_row == null) {
            _max_row = NumericConverter.extractPositiveInteger(getData());
        }
        return _max_row.intValue();
    }
} // end public class EPMaxRow
