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
 * No-op implementation of ElementProcessor to handle the
 * "SheetObjectFilled" tag
 *
 * This element has a small number of Attributes and no content.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPSheetObjectFilled.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EPSheetObjectFilled extends BaseElementProcessor {
    private String _object_bound;
    private Offsets _object_offset;
    private Anchors _object_anchor_type;
    private NumericResult _direction;
    private ColorCode _outline_color;
    private ColorCode _fill_color;
    private NumericResult _type;
    private NumericResult _width;
    private NumericResult _arrow_shape_a;
    private NumericResult _arrow_shape_b;
    private NumericResult _arrow_shape_c;
    private boolean _arrow_shape_a_fetched;
    private boolean _arrow_shape_b_fetched;
    private boolean _arrow_shape_c_fetched;
    private static final String _object_bound_attribute = "ObjectBound";
    private static final String _object_offset_attribute = "ObjectOffset";
    private static final String _object_anchor_type_attribute =
        "ObjectAnchorType";
    private static final String _direction_attribute = "Direction";
    private static final String _outline_color_attribute = "OutlineColor";
    private static final String _fill_color_attribute = "FillColor";
    private static final String _type_attribute = "Type";
    private static final String _width_attribute = "Width";
    private static final String _arrow_shape_a_attribute = "ArrowShapeA";
    private static final String _arrow_shape_b_attribute = "ArrowShapeB";
    private static final String _arrow_shape_c_attribute = "ArrowShapeC";
    private static final Validator _direction_validator = new Validator() {
        public IOException validate(final Number number) {
            return Direction.isValid(number.intValue()) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };
    private static final Validator _object_fill_validator = new Validator() {
        public IOException validate(final Number number) {
            return ObjectFill.isValid(number.intValue()) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPSheetObjectFilled() {
        super(null);
        _object_bound = null;
        _object_offset = null;
        _object_anchor_type = null;
        _direction = null;
        _outline_color = null;
        _fill_color = null;
        _type = null;
        _width = null;
        _arrow_shape_a = null;
        _arrow_shape_b = null;
        _arrow_shape_c = null;
        _arrow_shape_a_fetched = false;
        _arrow_shape_b_fetched = false;
        _arrow_shape_c_fetched = false;
    }

    /**
     * @return object_bound
     * @exception IOException
     */
    public String getObjectBound() throws IOException {
        if (_object_bound == null) {
            _object_bound = getValue(_object_bound_attribute);
            if (_object_bound == null) {
                throw new IOException(
                    "missing " + _object_bound_attribute + " attribute");
            }
        }
        return _object_bound;
    }

    /**
     * @return offsets
     * @exception IOException
     */
    public Offsets getOffsets() throws IOException {
        if (_object_offset == null) {
            _object_offset = new Offsets(getValue(_object_offset_attribute));
        }
        return _object_offset;
    }

    /**
     * @return anchors
     * @exception IOException
     */
    public Anchors getAnchors() throws IOException {
        if (_object_anchor_type == null) {
            _object_anchor_type =
                new Anchors(getValue(_object_anchor_type_attribute));
        }
        return _object_anchor_type;
    }

    /**
     * @return direction as a public member of Direction
     * @exception IOException
     */
    public int getDirection() throws IOException {
        if (_direction == null) {
            _direction = NumericConverter.extractInteger(
                    getValue(_direction_attribute), _direction_validator);
        }
        return _direction.intValue();
    }

    /**
     * @return outline color
     * @exception IOException
     */
    public ColorCode getOutlineColor() throws IOException {
        if (_outline_color == null) {
            _outline_color = new ColorCode(getValue(_outline_color_attribute));
        }
        return _outline_color;
    }

    /**
     * @return fill color
     * @exception IOException
     */
    public ColorCode getFillColor() throws IOException {
        if (_fill_color == null) {
            _fill_color = new ColorCode(getValue(_fill_color_attribute));
        }
        return _fill_color;
    }

    /**
     * @return type as a public member of ObjectFill
     * @exception IOException
     */
    public int getType() throws IOException {
        if (_type == null) {
            _type = NumericConverter.extractInteger(
                    getValue(_type_attribute), _object_fill_validator);
        }
        return _type.intValue();
    }

    /**
     * @return width
     * @exception IOException
     */
    public int getWidth() throws IOException {
        if (_width == null) {
            _width = NumericConverter.extractPositiveInteger(
                    getValue(_width_attribute));
        }
        return _width.intValue();
    }

    /**
     * @return arrow shape a
     * @exception IOException
     * @exception NullPointerException
     */
    public double getArrowShapeA() throws IOException, NullPointerException {
        if (!_arrow_shape_a_fetched) {
            _arrow_shape_a_fetched = true;
            String arrowShapeString = getValue(_arrow_shape_a_attribute);
            if (arrowShapeString != null) {
                _arrow_shape_a =
                    NumericConverter.extractDouble(arrowShapeString);
            } else {
                throw new NullPointerException(
                    "attribute " + _arrow_shape_a_attribute + " is absent");
            }
        }
        return _arrow_shape_a.doubleValue();
    }

    /**
     * @return arrow shape b
     * @exception IOException, NullPointerException
     * @exception NullPointerException
     */
    public double getArrowShapeB() throws IOException, NullPointerException {
        if (!_arrow_shape_b_fetched) {
            _arrow_shape_b_fetched = true;
            String arrowShapeString = getValue(_arrow_shape_b_attribute);
            if (arrowShapeString != null) {
                _arrow_shape_b =
                    NumericConverter.extractDouble(arrowShapeString);
            } else {
                throw new NullPointerException(
                    "attribute " + _arrow_shape_b_attribute + " is absent");
            }
        }
        return _arrow_shape_b.doubleValue();
    }

    /**
     * @return arrow shape c
     * @exception IOException, NullPointerException
     * @exception NullPointerException
     */
    public double getArrowShapeC() throws IOException, NullPointerException {
        if (!_arrow_shape_c_fetched) {
            _arrow_shape_c_fetched = true;
            String arrowShapeString = getValue(_arrow_shape_c_attribute);
            if (arrowShapeString != null) {
                _arrow_shape_c =
                    NumericConverter.extractDouble(arrowShapeString);
            } else {
                throw new NullPointerException(
                    "attribute " + _arrow_shape_c_attribute + " is absent");
            }
        }
        return _arrow_shape_c.doubleValue();
    }
} // end public class EPSheetObjectFilled
