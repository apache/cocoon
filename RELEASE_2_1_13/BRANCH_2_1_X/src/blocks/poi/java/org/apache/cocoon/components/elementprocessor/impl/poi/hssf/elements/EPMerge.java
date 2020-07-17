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

import org.apache.poi.ss.util.CellRangeAddress;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Merge" tag.
 * This element is a container of other elements and has several attributes.
 *
 * @author Danny Mui (danny@muibros.com)
 * @version CVS $Id$
 */
public class EPMerge extends BaseElementProcessor {

    private String _cellRange;

    /**
     * constructor
     */
    public EPMerge() {
        super(null);
        _cellRange = null;
    }

    public String getCellRange() {
        if (this._cellRange == null) {
            //pulls in the content
            _cellRange = this.getData();
        }
        return this._cellRange;
    }

    /**
     * Setup the merged regions
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        Sheet sheet = this.getSheet();
        CellRangeAddress range = CellRangeAddress.valueOf(getCellRange());
        sheet.addMergedRegion(range);
    }

} // end public class EPMerge
