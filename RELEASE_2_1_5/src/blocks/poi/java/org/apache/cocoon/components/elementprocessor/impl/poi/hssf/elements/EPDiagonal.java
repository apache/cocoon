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
 * No-op implementation of ElementProcessor to handle the "Diagonal"
 * tag
 *
 * This element has two attributes and no content.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPDiagonal.java,v 1.5 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class EPDiagonal extends BaseElementProcessor {
    private NumericResult _style;
    private boolean _color_fetched;
    private ColorCode _color;
    private static final String _style_attribute = "Style";
    private static final String _color_attribute = "Color";
    private static final Validator _style_validator = new Validator() {
        public IOException validate(final Number number) {
            return BorderStyle.isValid(number.intValue()) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPDiagonal() {
        super(null);
        _style = null;
        _color = null;
        _color_fetched = false;
    }

    /**
     * @return style as an int from BorderStyle
     * @exception IOException
     */
    public int getStyle() throws IOException {
        if (_style == null) {
            _style = NumericConverter.extractInteger(
                    getValue(_style_attribute), _style_validator);
        }
        return _style.intValue();
    }

    /**
     * @return color
     * @exception IOException
     */
    public ColorCode getColor() throws IOException {
        if (!_color_fetched) {
            String colorString = getValue(_color_attribute);
            if (colorString != null) {
                _color = new ColorCode(colorString);
            }
            _color_fetched = true;
        }
        return _color;
    }
} // end public class EPDiagonal
