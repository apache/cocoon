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

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

import java.io.IOException;

/**
 * internal representation of a Row
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: Row.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */

// package scope

class Row {
    private HSSFRow _row;
    private Sheet _sheet;

    /**
     * Constructor Row
     * @param row
     */
    Row(final HSSFRow row, final Sheet sheet) {
        _row = row;
        _sheet = sheet;
    }

    /**
     * set a row's height
     * @param points the height, in points
     * @exception IOException if any arguments are illegal
     */
    void setHeight(final double points) throws IOException {
        if (!isValid(points)) {
            throw new IOException("points " + points + " is out of range");
        }
        _row.setHeight((short) (points * 20));
    }

    /**
     * get the row height of a specified row
     * @return row height in 1/20 of a point
     */
    short getHeight() {
        return _row.getHeight();
    }

    /**
     * create a cell in a specific column, with a specific type
     * @param column the column number for the cell
     * @param cellType the cell type, being an enum from the CellType class
     * @return a new Cell object
     * @exception IOException
     */
    Cell createCell(final int column, final int cellType) throws IOException {
        if (column < 0 || column > Short.MAX_VALUE) {
            throw new IOException("Illegal column value: " + column);
        }
        HSSFCell hssfCell = _row.createCell((short)column);
        hssfCell.setCellType(CellType.convertCellType(cellType));

        Cell cell = new Cell(hssfCell, cellType);
        cell.setStyle(_sheet.getCellStyleForRegion(_row.getRowNum(),
                (short)column));
        return cell;
    }

    private boolean isValid(double points) {
        return ((points >= 0) && (points <= (((double)Short.MAX_VALUE) / 20)));
    }
} // end package scope class Row
