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

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Solver" tag
 *
 * This element is a container with four atributes (TargetRow,
 * TargetCol, and ProblemType are integers, and Inputs is a String)
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPSolver.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
 */
public class EPSolver extends BaseElementProcessor {
    private NumericResult _target_row;
    private NumericResult _target_col;
    private NumericResult _problem_type;
    private String _inputs;
    private static final String _target_row_attribute = "TargetRow";
    private static final String _target_col_attribute = "TargetCol";
    private static final String _problem_type_attribute = "ProblemType";
    private static final String _inputs_attribute = "Inputs";

    /**
     * constructor
     */
    public EPSolver() {
        super(null);
        _target_row = null;
        _target_col = null;
        _problem_type = null;
        _inputs = null;
    }

    /**
     * @return target column
     * @exception IOException
     */
    public int getTargetCol() throws IOException {
        if (_target_col == null) {
            _target_col = NumericConverter.extractInteger(
                    getValue(_target_col_attribute));
        }
        return _target_col.intValue();
    }

    /**
     * @return target row
     * @exception IOException
     */
    public int getTargetRow() throws IOException {
        if (_target_row == null) {
            _target_row = NumericConverter.extractInteger(
                    getValue(_target_row_attribute));
        }
        return _target_row.intValue();
    }

    /**
     * @return problem type
     * @exception IOException
     */
    public int getProblemType() throws IOException {
        if (_problem_type == null) {
            _problem_type = NumericConverter.extractInteger(
                    getValue(_problem_type_attribute));
        }
        return _problem_type.intValue();
    }

    /**
     * @return inputs
     */
    public String getInputs() {
        if (_inputs == null) {
            String value = getValue(_inputs_attribute);

            _inputs = (value == null) ? "" : value.trim();
        }
        return _inputs;
    }
} // end public class EPSolver
