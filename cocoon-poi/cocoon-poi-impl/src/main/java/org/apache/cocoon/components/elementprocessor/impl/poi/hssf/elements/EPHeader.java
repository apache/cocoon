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
 * No-op implementation of ElementProcessor to handle the "Header" tag
 *
 * This element has three attributes: Left, Middle, and Top, and no
 * contents.
 *
 * @version $Id$
 */
public class EPHeader extends BaseElementProcessor {
    private String _left;
    private String _middle;
    private String _right;
    private static final String _left_attribute = "Left";
    private static final String _middle_attribute = "Middle";
    private static final String _right_attribute = "Right";

    /**
     * constructor
     */
    public EPHeader() {
        super(null);
        _left = null;
        _middle = null;
        _right = null;
    }

    /**
     * @return the left string
     */
    public String getLeft() {
        if (_left == null) {
            _left = getValue(_left_attribute);
            if (_left == null) {
                _left = "";
            }
        }
        return _left;
    }

    /**
     * @return the middle string
     */
    public String getMiddle() {
        if (_middle == null) {
            _middle = getValue(_middle_attribute);
            if (_middle == null) {
                _middle = "";
            }
        }
        return _middle;
    }

    /**
     * @return the right string
     */
    public String getRight() {
        if (_right == null) {
            _right = getValue(_right_attribute);
            if (_right == null) {
                _right = "";
            }
        }
        return _right;
    }
    
    /**
     * Setup the text to be printed at the top of every page
     * @exception IOException
     */
    public void endProcessing() throws IOException{
        this.getSheet().setHeader(getLeft(), getMiddle(), getRight());
    }
} // end public class EPHeader
