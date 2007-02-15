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
 * No-op implementation of ElementProcessor to handle the "Zoom" tag
 *
 * This element contains the most recently zoom factor
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @version $Id$
 */
public class EPZoom extends BaseElementProcessor {
    private NumericResult _zoom;

    /**
     * constructor
     */
    public EPZoom() {
        super(null);
        _zoom = null;
    }

    /**
     * get the most recently used zoom factor
     * @return zoom factor
     * @exception IOException if the data is malformed
     */
    public double getZoom() throws IOException {
        if (_zoom == null) {
            _zoom = NumericConverter.extractDouble(getData());
        }
        return _zoom.doubleValue();
    }
} // end public class EPZoom
