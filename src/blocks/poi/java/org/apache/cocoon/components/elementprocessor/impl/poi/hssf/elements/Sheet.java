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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;

/**
 * internal representation of a Sheet
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id: Sheet.java,v 1.7 2004/01/31 08:50:39 antonio Exp $
 */

// package scope

class Sheet extends AbstractLogEnabled {

    private HSSFSheet _sheet;
    private String _name;
    private int _physical_index;
    private Workbook _workbook;

    // keys are Shorts (row numbers), values are Row instances
    private Map _rows;

    private Map regions;

    //optimization constant
    private final static int ROWS_CAPACITY = 200;

    //optimization constant
    private final static int REGION_CAPACITY = 20;

    /**
     * Constructor Sheet
     * @param workbook
     */
    Sheet(final Workbook workbook) {
        _workbook = workbook;
        _name = _workbook.getNextName();
        _sheet = _workbook.createSheet(_name);
        _physical_index = _workbook.getPhysicalIndex(_name);
        _rows = new HashMap(ROWS_CAPACITY);
        regions = new HashMap(REGION_CAPACITY);
    }

    /**
     * renameSheet
     * @param new_name
     */
    void renameSheet(final String new_name) {
        if (!_name.equals(new_name)) {
            _workbook.renameSheet(_physical_index, new_name);
            _name = new_name;
        }
    }

    /**
     * set a column's width
     * @param number the column number
     * @param points
     * @exception IOException if any arguments are illegal
     */
    void setColumnWidth(final int number, final double points)
        throws IOException {
        if (number < 0 || number > Short.MAX_VALUE) {
            throw new IOException("column number " + number + " is too large");
        }
        if (!isValidColumnPoints(points)) {
            throw new IOException("points " + points + " is out of range");
        }
        _sheet.setColumnWidth((short)number, (short) ((points * 48) + .5));
    }

    /**
     * get the column width of a specified column
     * @param number the column number
     * @return column width in characters
     */
    short getColumnWidth(short number) {
        return _sheet.getColumnWidth(number);
    }

    /**
     * set default column width
     * @param width width, in points
     * @exception IOException
     */
    void setDefaultColumnWidth(double width) throws IOException {
        if (width < 0 || (width >= (4.8 * (0.5 + Short.MAX_VALUE)))) {
            throw new IOException("Invalid width (" + width + ")");
        } // 12 is being used as a "guessed" points for the font
        _sheet.setDefaultColumnWidth((short) ((width / 4.8) + 0.5));
    }

    /**
     * @return default column width (in 1/256ths of a character width)
     */
    short getDefaultColumnWidth() {
        return _sheet.getDefaultColumnWidth();
    }

    /**
     * set default row height
     * @param height height, in points
     * @exception IOException
     */
    void setDefaultRowHeight(double height) throws IOException {
        if (!isValidPoints(height)) {
            throw new IOException("Invalid height (" + height + ")");
        }
        _sheet.setDefaultRowHeight((short) ((height * 20) + .5));
    }

    /**
     * @return default row height
     */
    short getDefaultRowHeight() {
        return _sheet.getDefaultRowHeight();
    }

    /**
     * @return name
     */
    String getName() {
        return _name;
    }

    /**
     * @return index
     */
    int getIndex() {
        return _physical_index;
    }

    /**
     * get a specified row
     * @param rowNo the row number
     * @return a Row object
     * @exception IOException if rowNo is out of range
     */
    Row getRow(int rowNo) throws IOException {
        if (rowNo < 0) {
            throw new IOException("Illegal row number: " + rowNo);
        }
        Short key = new Short((short)rowNo);
        Object o = _rows.get(key);
        Row rval = null;

        if (o == null) {
            rval = createRow(rowNo);
            _rows.put(key, rval);
        } else {
            rval = (Row)o;
        }
        return rval;
    }

    HSSFCellStyle addStyleRegion(Region region) {
        HSSFCellStyle style = _workbook.createStyle();
        /*
         * getLogger().debug("region = "+ region.getRowFrom() +
         * ","+region.getColumnFrom()+
         * ","+region.getRowTo()+","+region.getColumnTo());
         */
        regions.put(region, style);
        return style;
    }

    /**
     * returns the HSSFCellStyle for a cell if defined by region if there is
     * not a definition it returns null. If you don't expect that then your
     * code dies a horrible death.
     * @return HSSFCellStyle
     */
    HSSFCellStyle getCellStyleForRegion(int row, short col) {
        Iterator iregions = regions.keySet().iterator();
        while (iregions.hasNext()) {
            Region region = ((Region)iregions.next());
            //            if (col == 1)
            //                getLogger().debug("breakpoint support");
            if (region.contains(row, col)) {
                //getLogger().debug("Returning style for " + row +"," + col);
                return (HSSFCellStyle)regions.get(region);
            }
        }
        //getLogger().debug("returning null for "+row+","+col);
        return null;
    }

    private Row createRow(final int rowNo) {
        return new Row(_sheet.createRow(rowNo), this);
    }

    private boolean isValidPoints(double points) {
        return (points >= 0 && points <= ((Short.MAX_VALUE + 0.5) / 20));
    }

    private boolean isValidColumnPoints(double points) {
        return (points >= 0 && points <= ((Short.MAX_VALUE + 0.5) / 48));
    }

    /*
     * this method doesn't appear to be used private boolean
     * isValidCharacters(double characters) { return ((characters >= 0) &&
     * (characters <= ((Short.MAX_VALUE + 0.5) / 256)));
     */

    /**
     * Flag a certain region of cells to be merged
     * @param region the region to create as merged
     */
    void addMergedRegion(Region region) {
        this._sheet.addMergedRegion(region);
    }

    /**
     * assigns blank cells to regions where no cell is currently allocated.
     * Meaning if there is a sheet with a cell defined at 1,1 and a style
     * region from 0,0-1,1 then cells 0,0;0,1;1,0 will be defined as blank
     * cells pointing to the style defined by the style region. If there is not
     * a defined cell and no styleregion encompases the area, then no cell is
     * defined.
     */
    public void assignBlanksToRegions() {
        Iterator iregions = regions.keySet().iterator();
        while (iregions.hasNext()) {
            Region region = ((Region)iregions.next());
            //getLogger().debug("fixing region
            // "+region.getRowFrom()+","+region.getColumnFrom()+"-"+
            //          region.getRowTo()+","+region.getColumnTo());
            for (int rownum = region.getRowFrom();
                        rownum < region.getRowTo() + 1; rownum++) {
                HSSFRow row = _sheet.getRow(rownum);
                for (short colnum = region.getColumnFrom();
                            colnum < region.getColumnTo() + 1; colnum++) {
                    HSSFCellStyle style = (HSSFCellStyle)regions.get(region);
                    if (!isBlank(style)) {
                        //don't waste time with huge blocks of blankly styled
                        // cells
                        if (row == null) {
                            if (rownum > Short.MAX_VALUE) {
                                rownum = Short.MAX_VALUE;
                            }
                            row = _sheet.createRow(rownum);
                        }
                        HSSFCell cell = row.getCell(colnum);
                        if (cell == null) {
                            //getLogger().debug("creating blank cell at
                            // "+rownum + "," +colnum);
                            cell = row.createCell(colnum);
                            cell.setCellType(HSSFCell.CELL_TYPE_BLANK);
                            cell.setCellStyle(
                                (HSSFCellStyle)regions.get(region));
                        }
                    }
                }
            }
        }
    }

    private boolean isBlank(HSSFCellStyle style) {
        HSSFFont font = null;
        if (style.getFontIndex() > 0) {
            font = (_workbook.getWorkbook().getFontAt(style.getFontIndex()));
        }
        if (style.getBorderBottom() == 0 && style.getBorderTop() == 0
            && style.getBorderRight() == 0 && style.getBorderLeft() == 0
            && style.getFillBackgroundColor() == HSSFColor.WHITE.index
            && style.getFillPattern() == 0 && (style.getFontIndex() == 0
                || ((font.getFontName().equals("Arial")
                    || font.getFontName().equals("Helvetica"))
                    && font.getFontHeightInPoints() > 8
                    && font.getFontHeightInPoints() < 12))) {
            return true;
        }
        return false;
    }

} // end package scope class Sheet
