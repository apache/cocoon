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
 * "SheetObjectBonobo" tag
 *
 * This element has a small number of Attributes and no content.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPSheetObjectBonobo.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EPSheetObjectBonobo extends BaseElementProcessor {
    private String _object_bound;
    private Offsets _object_offset;
    private Anchors _object_anchor_type;
    private NumericResult _direction;
    private static final String _object_bound_attribute = "ObjectBound";
    private static final String _object_offset_attribute = "ObjectOffset";
    private static final String _object_anchor_type_attribute =
        "ObjectAnchorType";
    private static final String _direction_attribute = "Direction";
    private static final Validator _direction_validator = new Validator() {
        public IOException validate(final Number number) {
            return Direction.isValid(number.intValue()) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPSheetObjectBonobo() {
        super(null);
        _object_bound = null;
        _object_offset = null;
        _object_anchor_type = null;
        _direction = null;
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
} // end public class EPSheetObjectBonobo
