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
 * @version CVS $Id: Cell.java,v 1.7 2004/01/31 08:50:39 antonio Exp $
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
