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
 * @version CVS $Id: EPConstr.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
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
