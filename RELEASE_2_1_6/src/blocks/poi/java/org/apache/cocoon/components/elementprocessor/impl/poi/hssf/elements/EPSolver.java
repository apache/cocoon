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
 * @version CVS $Id: EPSolver.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
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
