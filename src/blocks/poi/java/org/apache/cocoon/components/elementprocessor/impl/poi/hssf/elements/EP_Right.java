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
 * No-op implementation of ElementProcessor to handle the "right" tag
 *
 * This element has two attributes: Points and PrefUnit
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EP_Right.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EP_Right extends BaseElementProcessor {
    private static final String _points_attribute    = "Points";
    private static final String _pref_unit_attribute = "PrefUnit";
    private NumericResult       _points;
    private NumericResult       _pref_unit;

    /**
     * constructor
     */
    public EP_Right() {
        super(null);
        _points    = null;
        _pref_unit = null;
    }

    /**
     * @return points
     *
     * @exception IOException
     */

    double getPoints() throws IOException {
        if (_points == null) {
            _points =
                NumericConverter.extractDouble(getValue(_points_attribute));
        }
        return _points.doubleValue();
    }

    /**
     * @return print unit (always one of the values in PrintUnits)
     *
     * @exception IOException
     */

    int getPrefUnit() throws IOException {
        if (_pref_unit == null) {
            _pref_unit =
                PrintUnits.extractPrintUnits(getValue(_pref_unit_attribute));
        }
        return _pref_unit.intValue();
    }
}   // end public class EP_Right
