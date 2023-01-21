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

import java.util.Hashtable;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * No-op implementation of ElementProcessor to handle the "Styles" tag
 *
 * This is a container element with no attributes
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @version $Id$
 */
public class EPStyles extends BaseElementProcessor {
    private Hashtable colors;

    /**
     * constructor
     */
    public EPStyles() {
        super(null);
        colors = HSSFColor.getTripletHash();
    }

    /**
     * @return an instance of Hashtable created by HSSFColor.getTripletHash()
     * @see org.apache.poi.hssf.util.HSSFColor#getTripletHash()
     */
    public Hashtable getColorHash() {
        return colors;
    }
} // end public class EPStyles
