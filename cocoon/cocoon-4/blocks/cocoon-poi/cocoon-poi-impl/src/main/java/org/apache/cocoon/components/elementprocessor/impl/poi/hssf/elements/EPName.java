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

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Name" tag
 *
 * This element contains the name of the containing Sheet
 *
 * @version $Id$
 */
public class EPName extends BaseElementProcessor {
    private String _name;

    /**
     * constructor
     */
    public EPName() {
        super(null);
        _name = null;
    }

    /**
     * @return the name of the spreadsheet (may be empty)
     */
    public String getName() {
        if (_name == null) {
            _name = getData();
        }
        return _name;
    }

    /**
     * give the sheet its name
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        getSheet().renameSheet(getName());
    }
} // end public class EPName
