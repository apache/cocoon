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

import org.apache.cocoon.components.elementprocessor.types.Attribute;
import org.apache.cocoon.components.elementprocessor.types.BooleanConverter;
import org.apache.cocoon.components.elementprocessor.types.BooleanResult;
import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "ColInfo"
 * tag
 *
 * This element has several attributes and has no content
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPColInfo.java,v 1.5 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class EPColInfo extends BaseElementProcessor {

    // column number
    private NumericResult _no;

    // size, in points
    private NumericResult _unit;

    // top margin, in points
    private NumericResult _margin_a;

    // bottom margin, in points
    private NumericResult _margin_b;

    // true if size is explicitly set
    private BooleanResult _hard_size;

    // true if column is hidden
    private BooleanResult _hidden;

    // true if column is collapsed
    private BooleanResult _collapsed;

    // outline level
    private NumericResult _outline_level;

    // rle count
    private NumericResult _count;
    private static final String _no_attribute = "No";
    private static final String _unit_attribute = "Unit";
    private static final String _margin_a_attribute = "MarginA";
    private static final String _margin_b_attribute = "MarginB";
    private static final String _hard_size_attribute = "HardSize";
    private static final String _hidden_attribute = "Hidden";
    private static final String _collapsed_attribute = "Collapsed";
    private static final String _outline_level_attribute = "OutlineLevel";
    private static final String _count_attribute = "Count";
    private static final Validator _margin_validator = new Validator() {
        public IOException validate(final Number number) {
            int val = number.intValue();

            return (val >= 0 && val <= 7) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    private static final Attribute[] _implied_attributes =
        {
            new Attribute(_hard_size_attribute, "0"),
            new Attribute(_hidden_attribute, "0"),
            new Attribute(_collapsed_attribute, "0"),
            new Attribute(_outline_level_attribute, "0"),
            new Attribute(_count_attribute, "1")
        };

    /**
     * constructor
     */
    public EPColInfo() {
        super(_implied_attributes);
        _no = null;
        _unit = null;
        _margin_a = null;
        _margin_b = null;
        _hard_size = null;
        _hidden = null;
        _collapsed = null;
        _outline_level = null;
        _count = null;
    }

    /**
     * @return column number
     * @exception IOException
     */
    public int getColumnNo() throws IOException {
        if (_no == null) {
            _no = NumericConverter.extractNonNegativeInteger(
                    getValue(_no_attribute));
        }
        return _no.intValue();
    }

    /**
     * @return column size in points
     * @exception IOException
     */
    public double getPoints() throws IOException {
        if (_unit == null) {
            _unit = NumericConverter.extractDouble(getValue(_unit_attribute));
        }
        return _unit.doubleValue();
    }

    /**
     * @return top margin
     * @exception IOException
     */
    public int getTopMargin() throws IOException {
        if (_margin_a == null) {
            _margin_a = NumericConverter.extractInteger(
                    getValue(_margin_a_attribute), _margin_validator);
        }
        return _margin_a.intValue();
    }

    /**
     * @return bottom margin
     * @exception IOException
     */
    public int getBottomMargin() throws IOException {
        if (_margin_b == null) {
            _margin_b = NumericConverter.extractInteger(
                    getValue(_margin_b_attribute), _margin_validator);
        }
        return _margin_b.intValue();
    }

    /**
     * @return hard size
     * @exception IOException
     */
    public boolean getHardSize() throws IOException {
        if (_hard_size == null) {
            _hard_size =
                BooleanConverter.extractBoolean(getValue(_hard_size_attribute));
        }
        return _hard_size.booleanValue();
    }

    /**
     * @return hidden state
     * @exception IOException
     */
    public boolean getHidden() throws IOException {
        if (_hidden == null) {
            _hidden =
                BooleanConverter.extractBoolean(getValue(_hidden_attribute));
        }
        return _hidden.booleanValue();
    }

    /**
     * @return collapsed state
     * @exception IOException
     */
    public boolean getCollapsed() throws IOException {
        if (_collapsed == null) {
            _collapsed =
                BooleanConverter.extractBoolean(getValue(_collapsed_attribute));
        }
        return _collapsed.booleanValue();
    }

    /**
     * @return outline level
     * @exception IOException
     */
    public int getOutlineLevel() throws IOException {
        if (_outline_level == null) {
            _outline_level =
                NumericConverter.extractInteger(
                    getValue(_outline_level_attribute));
        }
        return _outline_level.intValue();
    }

    /**
     * @return rle count
     * @exception IOException
     */
    public int getRLECount() throws IOException {
        if (_count == null) {
            _count =
                NumericConverter.extractInteger(getValue(_count_attribute));
        }
        return _count.intValue();
    }

    /**
     * Set this column's width
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        getSheet().setColumnWidth(getColumnNo(), getPoints());
    }
} // end public class EPColInfo
