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

/**
 * No-op implementation of ElementProcessor to handle the "Header" tag
 *
 * This element has three attributes: Left, Middle, and Top, and no
 * contents.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPHeader.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
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
} // end public class EPHeader
