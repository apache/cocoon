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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

/**
 * internal representation of a Workbook
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id: Workbook.java,v 1.7 2004/03/05 13:02:04 bdelacretaz Exp $
 */

// package scope
class Workbook {
    private HSSFWorkbook _workbook;
    private int _sheet_index;
    private final static int REPEAT_CAPACITY = 91;
    private Map _repeat;

    /**
     * Constructor Workbook
     */
    Workbook() {
        _workbook = new HSSFWorkbook();
        _sheet_index = 0;
        _repeat = new HashMap(REPEAT_CAPACITY);
    }
    /**
     * Method createDataFormat
     * @return newly created DataFormat
     */
    HSSFDataFormat createDataFormat() {
        return _workbook.createDataFormat();
    }

    /**
     * check if the format exists
     * @param format and the value
     * @return the format index
     */
    Object getValidate(String format, short value) {
        if (_repeat.containsKey(format) == false) {
            _repeat.put(format, new Short(value));
        }
        return _repeat.get(format);
    }

    /**
     * Method getNextName
     * @return next name for a new sheet
     */
    String getNextName() {
        return "Sheet" + _sheet_index++;
    }

    /**
     * Method createSheet
     * @param name name of the sheet
     * @return newly created sheet
     */
    HSSFSheet createSheet(final String name) {
        return _workbook.createSheet(name);
    }

    /**
     * Method getPhysicalIndex
     * @param name name of the sheet
     * @return the sheet's physical index
     */
    int getPhysicalIndex(final String name) {
        return _workbook.getSheetIndex(name);
    }

    /**
     * Method renameSheet
     * @param index the sheet's physical index
     * @param name the new name for the sheet
     */
    void renameSheet(final int index, final String name) {
        _workbook.setSheetName(index, name, HSSFWorkbook.ENCODING_UTF_16);
    }

    /**
     * create a cell style in the underlying HSSF model and return the
     * reference this should match reasonably close to what is in the
     * StyleRegion element in the gnumeric ss.
     */
    HSSFCellStyle createStyle() {
        HSSFCellStyle style = _workbook.createCellStyle();
        return style;
    }

    /**
     * create a font in the underlying HSSF model and return the reference
     */
    HSSFFont createFont() {
        HSSFFont font = _workbook.createFont();
        return font;
    }

    HSSFWorkbook getWorkbook() {
        return _workbook;
    }

    /**
     * write self to a filesystem
     * @param filesystem the filesystem to be written to
     * @exception IOException
     */
    void write(final POIFSFileSystem filesystem) throws IOException {
        filesystem.createDocument(new ByteArrayInputStream(
                _workbook.getBytes()), "Workbook");
    }
} // end package scope class Workbook
