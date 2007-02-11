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

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.cocoon.CascadingIOException;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;

/**
 * internal representation of a Cell
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: Cell.java,v 1.8 2004/03/05 13:02:03 bdelacretaz Exp $
 */
// package scope

class Cell {
    private HSSFCell _cell;

    // original CellType value
    private int _celltype;
    private Locale locale;

    /**
     * Constructor Cell.
     * Only a hack as long as the POI stuff is not maintained in the POI CVS:
     * Setting the encoding to UTF-16 for internationalization
     * (<a href="http://jakarta.apache.org/poi/javadocs/org/apache/poi/hssf/usermodel/HSSFCell.html#getEncoding()">POI API</a>).
     *
     * @param cell
     */
    Cell(final HSSFCell cell, final int cellType) {
        _cell = cell;
        _celltype = cellType;
        _cell.setEncoding(HSSFCell.ENCODING_UTF_16);
    }

    /**
     * if there is a locale that can be used for validation it is
     * set here.  Cell expects a fully constructed locale.  It must
     * be passed in before SetContent can be called.
     */
    void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * set content
     *
     * @param content the value of the cell, as a string
     *
     * @exception IOException
     */
    void setContent(final String content) throws IOException {
        if (_cell.getCellType() == HSSFCell.CELL_TYPE_NUMERIC) {
            try {
                if (_celltype == CellType.CELL_TYPE_FLOAT) {
                    // if there is a locale set then we'll use it to parse the
                    // string form of the number... otherwise we'll use the default.
                    NumberFormat form = null;
                    if (locale == null) {
                        form = NumberFormat.getInstance();
                    } else {
                        form = NumberFormat.getInstance(locale);
                    }
                    _cell.setCellValue(form.parse(content).doubleValue());
                } else {
                    _cell.setCellValue(Integer.parseInt(content));
                }
            } catch (NumberFormatException e) {
                throw new CascadingIOException("Invalid value for a numeric cell: " + content, e);
            } catch (ParseException e) {
                throw new CascadingIOException("Invalid value for a numeric cell: " + content, e);
            }
        } else if (_cell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
            _cell.setCellValue(content);
        } else if (_cell.getCellType() == HSSFCell.CELL_TYPE_FORMULA) {
            _cell.setCellFormula(content.toUpperCase().substring(1));
        }
    }

    void setStyle(HSSFCellStyle style) {
        if (style != null) {
            _cell.setCellStyle(style);
        }
    }

    /**
     * @return cell type
     */
    int getCellType() {
        return _cell.getCellType();
    }

    /**
     * @return string value
     */
    String getStringValue() {
        return _cell.getStringCellValue();
    }

    /**
     * @return numeric value
     */
    double getNumericValue() {
        return _cell.getNumericCellValue();
    }
}   // end package scope class Cell
