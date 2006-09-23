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
 * No-op implementation of ElementProcessor to handle the "UIData" tag
 *
 * This element contains no data and has an attribute, SelectedTab,
 * that indicates which sheet is currently selected
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @version $Id$
 */
public class EPUIData extends BaseElementProcessor {
    private NumericResult _selected_tab;
    private static final String _selected_tab_attribute = "SelectedTab";

    /**
     * constructor
     */
    public EPUIData() {
        super(null);
        _selected_tab = null;
    }

    /**
     * Get the selected tab
     * @return the number of the currently selected sheet (tab)
     * @exception IOException if the value is missing or incorrectly written
     */
    public int getSelectedTab() throws IOException {
        if (_selected_tab == null) {
            _selected_tab = NumericConverter.extractNonNegativeInteger(
                    getValue(_selected_tab_attribute));
        }
        return _selected_tab.intValue();
    }
} // end public class EPUIData
