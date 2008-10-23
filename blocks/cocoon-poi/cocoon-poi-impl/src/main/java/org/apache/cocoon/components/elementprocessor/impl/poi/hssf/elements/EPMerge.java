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

import org.apache.poi.hssf.util.CellRangeAddress;
import org.apache.poi.hssf.util.RangeAddress;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Merge" tag.
 * This element is a container of other elements and has several attributes.
 *
 * @version $Id$
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
     * Setup the merged cellRangeAddresses
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        RangeAddress rangeAddress = new RangeAddress(getCellRange());
        Sheet sheet = this.getSheet();

        //subtracting one since rangeaddress starts at 1,1 where rows/cols
        // start at 0,0
        int fromCol = rangeAddress.getXPosition(rangeAddress.getFromCell()) - 1;
        int fromRow = rangeAddress.getYPosition(rangeAddress.getFromCell()) - 1;
        int toCol = rangeAddress.getXPosition(rangeAddress.getToCell()) - 1;
        int toRow = rangeAddress.getYPosition(rangeAddress.getToCell()) - 1;

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Merging Range: Row (" + fromRow + ") Col ("
                    + fromCol + ")" + " to Row (" + toRow + ") Col (" + toCol
                    + ")");
        }
        CellRangeAddress cellRangeAddress = new CellRangeAddress(fromRow, toRow, fromCol, toCol);
        sheet.addMergedRegion(cellRangeAddress);
    }

} // end public class EPMerge
