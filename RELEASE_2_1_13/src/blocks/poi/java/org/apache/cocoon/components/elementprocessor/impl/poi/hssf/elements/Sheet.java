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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFFooter;
import org.apache.poi.hssf.usermodel.HSSFHeader;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.util.Region;
import org.apache.poi.ss.util.CellRangeAddress;

/**
 * internal representation of a Sheet
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id$
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
    int getColumnWidth(short number) {
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
        Integer objColWidth = new Integer(_sheet.getDefaultColumnWidth());
        return objColWidth.shortValue();
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
            if (region.contains(row, col)) {
                return (HSSFCellStyle)regions.get(region);
            }
        }
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

    /**
     * Flag a certain region of cells to be merged
     * @param range the cell range to create as merged
     */
    void addMergedRegion(CellRangeAddress range) {
        this._sheet.addMergedRegion(range);
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
            for (int rownum = region.getRowFrom(); rownum < region.getRowTo() + 1; rownum++) {
                HSSFRow row = _sheet.getRow(rownum);
                for (short colnum = region.getColumnFrom();
                            colnum < region.getColumnTo() + 1; colnum++) {
                    HSSFCellStyle style = (HSSFCellStyle)regions.get(region);
                    if (!isBlank(style)) {
                        //don't waste time with huge blocks of blankly styled cells
                        if (row == null) {
                            if (rownum > Short.MAX_VALUE) {
                                rownum = Short.MAX_VALUE;
                            }
                            row = _sheet.createRow(rownum);
                        }
                        HSSFCell cell = row.getCell(colnum);
                        if (cell == null) {
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

    /**
     * Set the paper size.
     * @param paperSize the paper size.
     */

    void setPaperSize(short paperSize) {
        _sheet.getPrintSetup().setPaperSize(paperSize);
    }

    /**
     * Set whether to print in landscape
     * @param ls landscape
     */

    void setOrientation(boolean ls) {
        _sheet.getPrintSetup().setLandscape(ls);
    }

    /**
     * Set whether or not the grid is printed for the worksheet
     * @param gridLines boolean to turn on or off the printing of
     * gridlines
     */

    void setPrintGridLines(boolean gridLines) {
        _sheet.setPrintGridlines(gridLines);
    }
    
    /**
     * Set whether or not the worksheet content is centered (horizontally)
     * on the page when it is printed
     */
    void setHCenter(boolean hCenter) {
        _sheet.setHorizontallyCenter(hCenter);
    }

    /**
     * Setwhether or not the worksheet content is centered (vertically)
     * on the page when it is printed
     */
    void setVCenter(boolean vCenter) {
        _sheet.setVerticallyCenter(vCenter);
    }
    
    /**
     * Setup whether or not printing is in monochrome (no color)
     */
    void setMonochrome(boolean noColor) {
        _sheet.getPrintSetup().setNoColor(noColor);
    }
    
    /**
     * Setup whether or not the worksheet is printed in draft format
     */
    void setDraft(boolean draftMode) {
        _sheet.getPrintSetup().setDraft(draftMode);
    }
    
    /**
     * Set text to be printed at the top of every page
     */
    void setHeader(String left, String middle, String right) {
        HSSFHeader header = _sheet.getHeader();
        header.setLeft(left);
        header.setCenter(middle);
        header.setRight(right);
    }
    
    /**
     * Set text to be printed at the bottom of every page
     */
    void setFooter(String left, String middle, String right) {
        HSSFFooter footer = _sheet.getFooter();
        footer.setLeft(left);
        footer.setCenter(middle);
        footer.setRight(right);
    }
    
    /**
     * Set the top margin of the page
     */
    void setTopMargin(double points) {
        _sheet.setMargin(HSSFSheet.TopMargin, points);
    }
    
    /**
     * Set the left margin of the page
     */
    void setLeftMargin(double points) {
        _sheet.setMargin(HSSFSheet.LeftMargin, points);
    }
    
    /**
     * Set the right margin of the page
     */
    void setRightMargin(double points) {
        _sheet.setMargin(HSSFSheet.RightMargin, points);
    }
    
    /**
     * Set the bottom margin of the page
     */
    void setBottomMargin(double points) {
        _sheet.setMargin(HSSFSheet.BottomMargin, points);
    }
    
    /**
     * Set the header margin of the page
     */
    void setHeaderMargin(double points) {
        _sheet.getPrintSetup().setHeaderMargin(points);
    }
    
    /**
     * Set the header margin of the page
     */
    void setFooterMargin(double points) {
        _sheet.getPrintSetup().setFooterMargin(points);
    }

} // end package scope class Sheet
