/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.cocoon.components.elementprocessor.types.Attribute;
import org.apache.cocoon.components.elementprocessor.ElementProcessor;

import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.io.IOException;
import java.util.Hashtable;

/**
 * No-op implementation of ElementProcessor to handle the "Right" tag
 *
 * This element has two attributes and no content.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @version $Id$
 */
public class EPRight extends BaseElementProcessor {
    private NumericResult _style;
    private ColorCode _color;
    private boolean _color_fetched;
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
    public EPRight() {
        super(null);
        _style = null;
        _color = null;
        _color_fetched = false;
    }

    /**
     * Override of Initialize() implementation
     * @param attributes the array of Attribute instances; may be empty, will
     *                  never be null
     * @param parent the parent ElementProcessor; may be null
     * @exception IOException if anything is wrong
     */
    public void initialize(final Attribute[] attributes,
                final ElementProcessor parent) throws IOException {
        super.initialize(attributes, parent);
        EPStyle pstyle = (EPStyle)getAncestor(EPStyle.class);
        if (pstyle != null && pstyle.isValid()) {
            Hashtable colorhash = pstyle.getColorHash();
            HSSFColor color = null;

            HSSFCellStyle style = pstyle.getStyle(); //oops
                                                                       // a
                                                                       // little
                                                                       // confusing
            //below is the style attribute
            //this is an HSSFCellStyle
            //associated with EPStyle
            style.setBorderRight((short)getStyle());

            ColorCode colorCode = getColor();
            if (colorCode != null) {
                color = (HSSFColor)colorhash.get(colorCode.toString());
            }
            if (color == null) {
                color = new HSSFColor.BLACK();
            }
            style.setRightBorderColor(color.getIndex());
        }

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
} // end public class EPRight
