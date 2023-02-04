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

import java.util.Hashtable;

import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.types.Attribute;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.CellRangeAddress;


import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the
 * "StyleRegion" tag
 *
 * This element is a container of other elements and has four
 * attributes that define the boundaries of the region.
 *
 * @version $Id$
 */
public class EPStyleRegion extends BaseElementProcessor {
    private static final String _start_col_attribute = "startCol";
    private static final String _start_row_attribute = "startRow";
    private static final String _end_col_attribute = "endCol";
    private static final String _end_row_attribute = "endRow";
    private NumericResult _start_col;
    private NumericResult _start_row;
    private NumericResult _end_col;
    private NumericResult _end_row;

    private HSSFCellStyle _style;
    private Hashtable colorhash;

    private boolean invalid;

    //kludge constant to fix gnumeric's love of declaring large stlye regions
    //for the blank sections of the sheet w/no apparent purpose that we can
    private int MAX_AREA = 65537;  // Use max row (2^16 + 1), see: http://markmail.org/message/j5svscx3dxo4zyej

    /**
     * constructor
     */
    public EPStyleRegion() {
        super(null);
        _start_col = null;
        _start_row = null;
        _end_col = null;
        _end_row = null;
    }

    /**
     * Override of Initialize() implementation
     * @param attributes the array of Attribute instances; may be empty, will
     *                  never be null
     * @param parent the parent ElementProcessor; may be null
     * @exception IOException if anything is wrong
     */
    public void initialize(final Attribute[] attributes,
                final ElementProcessor parent) throws IOException {
        super.initialize(attributes, parent);

        CellRangeAddress cellRangeAddress = new CellRangeAddress(getStartRow(), getEndRow(),
                getStartCol(), getEndCol());

        getLogger().debug("region area is " + getArea(cellRangeAddress));
        if (getArea(cellRangeAddress) < MAX_AREA) {
            //protect against stupid mega regions
            //of generally NOTHING and no real
            //purpose created by gnumeric
            getLogger().debug("region added");
            _style = getSheet().addStyleRegion(cellRangeAddress); //test
        } else {
            getLogger().debug("Region NOT added!. Reason: getArea(cellRangeAddress) = " 
                    + getArea(cellRangeAddress) + " > " + MAX_AREA);
            invalid = true;
        }
        colorhash = ((EPStyles)parent).getColorHash();
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

    /**
     * @return HSSFCellStyle associated with this style region.
     */
    public HSSFCellStyle getStyle() {
        return _style;
    }

    /**
     * @return instance created in the EPStyles instance from
     *             HSSFColor.getTripletHash();
     * @see org.apache.poi.hssf.util.HSSFColor#getTripletHash()
     */
    public Hashtable getColorHash() {
        return colorhash;
    }

    /**
     * @return validity (used to determine whether this is a big wasteful
     *             region with no purpose (gnumeric does this
     */
    public boolean isValid() {
        return (!invalid);
    }
    
    private int getArea(CellRangeAddress cellRangeAddress) {
        return ((1 + (cellRangeAddress.getLastRow() - cellRangeAddress.getFirstRow()))
                * (1 + (cellRangeAddress.getLastColumn() - cellRangeAddress.getFirstColumn())));
    }

} // end public class EPStyleRegion
