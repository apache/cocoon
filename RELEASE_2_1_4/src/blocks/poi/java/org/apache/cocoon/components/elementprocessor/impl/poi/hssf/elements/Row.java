/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/

package org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;

import java.io.IOException;

/**
 * internal representation of a Row
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: Row.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
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
