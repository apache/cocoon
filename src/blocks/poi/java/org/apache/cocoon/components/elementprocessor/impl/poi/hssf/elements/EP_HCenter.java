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

import org.apache.cocoon.components.elementprocessor.types.BooleanConverter;
import org.apache.cocoon.components.elementprocessor.types.BooleanResult;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "hcenter"
 * tag
 *
 * This element has a single attribute, value, which is boolean.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EP_HCenter.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EP_HCenter extends BaseElementProcessor {
    private static final String _value_attribute = "value";
    private BooleanResult       _value;

    /**
     * constructor
     */

    public EP_HCenter() {
        super(null);
        _value = null;
    }

    /**
     * @return value
     *
     * @exception IOException if the value is malformed or missing
     */

    public boolean getValue() throws IOException {
        if (_value == null) {
            _value =
                BooleanConverter.extractBoolean(getValue(_value_attribute));
        }
        return _value.booleanValue();
    }
}   // end public class EP_HCenter
