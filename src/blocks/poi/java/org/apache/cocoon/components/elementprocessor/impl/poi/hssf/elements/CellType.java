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

/**
 * Cell type codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: CellType.java,v 1.5 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class CellType {


    public static final int CELL_TYPE_FORMULA   = -1;
    public static final int CELL_TYPE_EMPTY     = 10;
    public static final int CELL_TYPE_BOOLEAN   = 20;
    public static final int CELL_TYPE_INTEGER   = 30;
    public static final int CELL_TYPE_FLOAT     = 40;
    public static final int CELL_TYPE_ERROR     = 50;
    public static final int CELL_TYPE_STRING    = 60;
    public static final int CELL_TYPE_CELLRANGE = 70;
    public static final int CELL_TYPE_ARRAY     = 80;

    private CellType() {
    }

    /**
     * Is this a valid cell type?
     *
     * @param val value to be checked
     * @return true if valid, false otherwise
     */
    public static boolean isValid(final int val) {
        switch (val) {
            case CELL_TYPE_EMPTY :
            case CELL_TYPE_BOOLEAN :
            case CELL_TYPE_INTEGER :
            case CELL_TYPE_FLOAT :
            case CELL_TYPE_ERROR :
            case CELL_TYPE_STRING :
            case CELL_TYPE_FORMULA :
            case CELL_TYPE_CELLRANGE :
            case CELL_TYPE_ARRAY :
                return true;
            default :
                return false;
        }
    }

    /**
     * Convert a CellType enum into an HSSFCell enum
     *
     * @param val the value to be converted
     * @return the converted value
     */
    static int convertCellType(final int val) {
        switch (val) {
            case CELL_TYPE_INTEGER :
            case CELL_TYPE_FLOAT :
                return HSSFCell.CELL_TYPE_NUMERIC;
            case CELL_TYPE_STRING :
                return HSSFCell.CELL_TYPE_STRING;
            case CELL_TYPE_FORMULA :
                return HSSFCell.CELL_TYPE_FORMULA;
            default :
                return HSSFCell.CELL_TYPE_BLANK;
        }
    }
}   // end public class CellType
