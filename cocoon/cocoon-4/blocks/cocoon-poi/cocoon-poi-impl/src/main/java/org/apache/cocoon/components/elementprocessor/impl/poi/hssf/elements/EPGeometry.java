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

import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Geometry" tag
 *
 * This element has two attributes: Width and Height
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @version $Id$
 */
public class EPGeometry extends BaseElementProcessor {
    private static final String _width_attribute = "Width";
    private static final String _height_attribute = "Height";
    private NumericResult _width;
    private NumericResult _height;

    /**
     * constructor
     */
    public EPGeometry() {
        super(null);
        _width = null;
        _height = null;
    }

    /**
     * @return height
     * @exception IOException
     */
    int getHeight() throws IOException {
        if (_height == null) {
            _height = NumericConverter.extractPositiveInteger(
                    getValue(_height_attribute));
        }
        return _height.intValue();
    }

    /**
     * @return width
     * @exception IOException
     */
    int getWidth() throws IOException {
        if (_width == null) {
            _width = NumericConverter.extractPositiveInteger(
                    getValue(_width_attribute));
        }
        return _width.intValue();
    }
} // end public class EPGeometry
