/*
 * Copyright 1999-2002,2004 The Apache Software Foundation.
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

import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.hssf.util.Region;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Merge" tag.
 * This element is a container of other elements and has several attributes.
 *
 * @author Danny Mui (danny@muibros.com)
 * @version CVS $Id: EPMerge.java,v 1.6 2004/03/05 13:02:04 bdelacretaz Exp $
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
        RangeAddress rangeAddress = new RangeAddress(getCellRange());
        Sheet sheet = this.getSheet();

        //subtracting one since rangeaddress starts at 1,1 where rows/cols
        // start at 0,0
        short fromCol =
            (short) (rangeAddress.getXPosition(rangeAddress.getFromCell()) - 1);
        int fromRow = rangeAddress.getYPosition(rangeAddress.getFromCell()) - 1;
        short toCol =
            (short) (rangeAddress.getXPosition(rangeAddress.getToCell()) - 1);
        int toRow = rangeAddress.getYPosition(rangeAddress.getToCell()) - 1;

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Merging Range: Row (" + fromRow + ") Col ("
                    + fromCol + ")" + " to Row (" + toRow + ") Col (" + toCol
                    + ")");
        }
        Region region = new Region(fromRow, fromCol, toRow, toCol);
        sheet.addMergedRegion(region);
    }

} // end public class EPMerge
