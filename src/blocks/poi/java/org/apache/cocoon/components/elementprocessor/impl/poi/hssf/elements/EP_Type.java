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
 * No-op implementation of ElementProcessor to handle the "type" tag
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * This element has no attributes, but has string content, which is
 * numeric.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EP_Type.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EP_Type extends BaseElementProcessor {
    private NumericResult _type;
    private static final Validator _validator = new Validator()
    {
        public IOException validate(final Number number) {
            return GTKTypes.isValid(number.intValue()) ? null :
                new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EP_Type() {
        super(null);
        _type = null;
    }

    /**
     * @return the type
     *
     * @exception IOException if the type is not numeric
     */
    int getType() throws IOException {
        if (_type == null) {
            _type = NumericConverter.extractInteger(getData(), _validator);
        }
        return _type.intValue();
    }
}   // end public class EP_Type
