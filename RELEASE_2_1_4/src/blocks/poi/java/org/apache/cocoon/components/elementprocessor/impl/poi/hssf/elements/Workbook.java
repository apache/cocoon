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
 * @version CVS $Id: Workbook.java,v 1.6 2004/01/31 08:50:39 antonio Exp $
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
