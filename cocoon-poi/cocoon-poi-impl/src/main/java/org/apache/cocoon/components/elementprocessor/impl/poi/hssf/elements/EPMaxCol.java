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
 * No-op implementation of ElementProcessor to handle the "MaxCol" tag
 *
 * This element contains the maximum number of columns in the
 * containing sheet. The value is contained in the data.
 *
 * @version $Id$
 */
public class EPMaxCol extends BaseElementProcessor {
    private NumericResult _max_col;

    /**
     * constructor
     */
    public EPMaxCol() {
        super(null);
        _max_col = null;
    }

    /**
     * get the maximum column for the containing sheet
     * @return maximum column number
     * @exception IOException if the data is malformed
     */
    public int getMaxCol() throws IOException {
        if (_max_col == null) {
            _max_col = NumericConverter.extractPositiveInteger(getData());
        }
        return _max_col.intValue();
    }
} // end public class EPMaxCol
