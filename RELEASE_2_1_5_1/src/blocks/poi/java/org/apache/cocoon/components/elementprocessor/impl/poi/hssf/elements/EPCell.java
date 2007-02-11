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
import java.util.Locale;

import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.LocaleAware;
import org.apache.cocoon.components.elementprocessor.types.Attribute;
import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

/**
 * Implementation of ElementProcessor to handle the "Cell" tag.
 * This element has several attributes and may contain other elements.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPCell.java,v 1.5 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class EPCell extends BaseElementProcessor implements LocaleAware {

    private Cell _cell;
    private NumericResult _col;
    private NumericResult _row;
    private NumericResult _expr_id;
    private NumericResult _cols;
    private NumericResult _rows;
    private NumericResult _value_type;
    private String _value_format;
    private boolean _expr_id_fetched;
    private boolean _cols_fetched;
    private boolean _rows_fetched;
    private boolean _value_type_fetched;
    private boolean _value_format_fetched;
    private static final String _col_attribute = "Col";
    private static final String _row_attribute = "Row";
    private static final String _expr_id_attribute = "ExprID";
    private static final String _cols_attribute = "Cols";
    private static final String _rows_attribute = "Rows";
    private static final String _value_type_attribute = "ValueType";
    private static final String _value_format_attribute = "ValueFormat";
    private String locale; // the locale for this EPCell
    private static final Validator _cell_type_validator = new Validator() {
        public IOException validate(final Number number) {
            return CellType.isValid(number.intValue()) ? null : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPCell() {
        super(null);
        _cell = null;
        _col = null;
        _row = null;
        _expr_id = null;
        _cols = null;
        _rows = null;
        _value_type = null;
        _value_format = null;
        _expr_id_fetched = false;
        _cols_fetched = false;
        _rows_fetched = false;
        _value_type_fetched = false;
        _value_format_fetched = false;
    }

    /**
     * @return column
     *
     * @exception IOException
     */
    public int getColumn() throws IOException {
        if (_col == null) {
            _col = NumericConverter.extractNonNegativeInteger(getValue(_col_attribute));
        }
        return _col.intValue();
    }

    /**
     * @return row
     *
     * @exception IOException
     */
    public int getRow() throws IOException {
        if (_row == null) {
            _row = NumericConverter.extractNonNegativeInteger(getValue(_row_attribute));
        }
        return _row.intValue();
    }

    /**
     * @return expression id
     *
     * @exception IOException
     * @exception NullPointerException
     */
    public int getExpressionId() throws IOException, NullPointerException {
        if (!_expr_id_fetched) {
            String valueString = getValue(_expr_id_attribute);
            if (valueString != null) {
                _expr_id = NumericConverter.extractPositiveInteger(valueString);
            }
            _expr_id_fetched = true;
        }
        return _expr_id.intValue();
    }

    /**
     * @return columns
     *
     * @exception IOException
     * @exception NullPointerException
     */
    public int getColumns() throws IOException, NullPointerException {
        if (!_cols_fetched) {
            String valueString = getValue(_cols_attribute);
            if (valueString != null) {
                _cols = NumericConverter.extractPositiveInteger(valueString);
            }
            _cols_fetched = true;
        }
        return _cols.intValue();
    }

    /**
     * @return rows
     *
     * @exception IOException
     * @exception NullPointerException
     */
    public int getRows() throws IOException, NullPointerException {
        if (!_rows_fetched) {
            String valueString = getValue(_rows_attribute);
            if (valueString != null) {
                _rows = NumericConverter.extractPositiveInteger(valueString);
            }
            _rows_fetched = true;
        }
        return _rows.intValue();
    }

    /**
     * @return cell type as a public member of CellType
     *
     * @exception IOException
     * @exception NullPointerException
     */
    public int getCellType() throws IOException, NullPointerException {
        if (!_value_type_fetched) {
            String valueString = getValue(_value_type_attribute);
            if (valueString != null) {
                _value_type = NumericConverter.extractInteger(valueString, _cell_type_validator);
            }
            _value_type_fetched = true;
        }
        return _value_type.intValue();
    }

    /**
     * @return format string; null if no such attribute
     *
     * @exception IOException
     */
    public String getFormat() throws IOException {
        if (!_value_format_fetched) {
            _value_format = getValue(_value_format_attribute);
            _value_format_fetched = true;
        }
        return _value_format;
    }

    /**
     * Override of initialize() implementation
     *
     * @param attributes the array of Attribute instances; may be
     *                   empty, will never be null
     * @param parent the parent ElementProcessor; may be null
     *
     * @exception IOException if anything is wrong
     */
    public void initialize(final Attribute[] attributes, final ElementProcessor parent) throws IOException {
        super.initialize(attributes, parent);
        // default value (when <gmr:Cell> has no ValueType attribute)
        int cellType = CellType.CELL_TYPE_FORMULA;
        try {
            cellType = getCellType();
        } catch (NullPointerException ignored) {
        }
        _cell = getSheet().getRow(getRow()).createCell(getColumn(), cellType);
    }

    public String getContent() {
        String content = getData();
        return content;
    }

    /**
     * end processing -- apply content to the cell.
     *
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        String content = getContent();
        if (content != null && locale != null) {
            // if there is a locale then set it (otherwise the default locale
            // will be used)
            getCell().setLocale(new Locale(locale, locale.toUpperCase()));
        }
        if (content != null && !content.trim().equals("")) {
            getCell().setContent(getContent());
        }
    }

    /**
     * override of getCell()
     *
     * @return the cell
     */
    protected Cell getCell() {
        return _cell;
    }

    // from LocaleAware - set the locale for a cell
    public void setLocale(String locale) {
        this.locale = locale;
    }

}   // end public class EPCell
