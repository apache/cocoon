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
 * No-op implementation of ElementProcessor to handle the
 * "SheetObjectFilled" tag
 *
 * This element has a small number of Attributes and no content.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPSheetObjectFilled.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
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
