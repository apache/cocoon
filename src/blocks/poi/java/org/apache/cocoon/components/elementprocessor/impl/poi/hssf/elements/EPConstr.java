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

import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Constr" tag
 *
 * This element contains several attributes and no content or other
 * elements.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPConstr.java,v 1.5 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class EPConstr extends BaseElementProcessor {
    private NumericResult _lcol;
    private NumericResult _lrow;
    private NumericResult _rcol;
    private NumericResult _rrow;
    private NumericResult _cols;
    private NumericResult _rows;
    private NumericResult _type;
    private static final String _lcol_attribute = "Lcol";
    private static final String _lrow_attribute = "Lrow";
    private static final String _rcol_attribute = "Rcol";
    private static final String _rrow_attribute = "Rrow";
    private static final String _cols_attribute = "Cols";
    private static final String _rows_attribute = "Rows";
    private static final String _type_attribute = "Type";
    private static final Validator _type_validator = new Validator() {
        public IOException validate(final Number number) {
            return ConstraintType.isValid(number.intValue()) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPConstr() {
        super(null);
        _lcol = null;
        _lrow = null;
        _rcol = null;
        _rrow = null;
        _cols = null;
        _rows = null;
        _type = null;
    }

    /**
     * @return lcol
     * @exception IOException
     */
    public int getLcol() throws IOException {
        if (_lcol == null) {
            _lcol = NumericConverter.extractNonNegativeInteger(
                    getValue(_lcol_attribute));
        }
        return _lcol.intValue();
    }

    /**
     * @return lrow
     * @exception IOException
     */
    public int getLrow() throws IOException {
        if (_lrow == null) {
            _lrow = NumericConverter.extractNonNegativeInteger(
                    getValue(_lrow_attribute));
        }
        return _lrow.intValue();
    }

    /**
     * @return rcol
     * @exception IOException
     */
    public int getRcol() throws IOException {
        if (_rcol == null) {
            _rcol = NumericConverter.extractNonNegativeInteger(
                    getValue(_rcol_attribute));
        }
        return _rcol.intValue();
    }

    /**
     * @return rrow
     * @exception IOException
     */
    public int getRrow() throws IOException {
        if (_rrow == null) {
            _rrow = NumericConverter.extractNonNegativeInteger(
                    getValue(_rrow_attribute));
        }
        return _rrow.intValue();
    }

    /**
     * @return cols
     * @exception IOException
     */
    public int getCols() throws IOException {
        if (_cols == null) {
            _cols = NumericConverter.extractPositiveInteger(
                    getValue(_cols_attribute));
        }
        return _cols.intValue();
    }

    /**
     * @return rows
     * @exception IOException
     */
    public int getRows() throws IOException {
        if (_rows == null) {
            _rows = NumericConverter.extractPositiveInteger(
                    getValue(_rows_attribute));
        }
        return _rows.intValue();
    }

    /**
     * @return type
     * @exception IOException
     */
    public int getType() throws IOException {
        if (_type == null) {
            _type = NumericConverter.extractInteger(
                    getValue(_type_attribute), _type_validator);
        }
        return _type.intValue();
    }
} // end public class EPConstr
