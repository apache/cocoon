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

import org.apache.cocoon.components.elementprocessor.types.Attribute;
import org.apache.cocoon.components.elementprocessor.types.BooleanConverter;
import org.apache.cocoon.components.elementprocessor.types.BooleanResult;
import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "RowInfo"
 * tag
 *
 * This element has several attributes and has no content
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPRowInfo.java,v 1.4 2004/01/31 08:50:39 antonio Exp $
 */
public class EPRowInfo extends BaseElementProcessor {

    // row number
    private NumericResult _no;

    // size, in points
    private NumericResult _unit;

    // left margin, in points
    private NumericResult _margin_a;

    // right margin, in points
    private NumericResult _margin_b;

    // true if size is explicitly set
    private BooleanResult _hard_size;

    // true if row is hidden
    private BooleanResult _hidden;

    // true if row is collapsed
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
            new Attribute(_count_attribute, "1")};

    /**
     * constructor
     */
    public EPRowInfo() {
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
     * @return row number
     * @exception IOException
     */
    public int getRowNo() throws IOException {
        if (_no == null) {
            _no =
                NumericConverter.extractNonNegativeInteger(
                    getValue(_no_attribute));
        }
        return _no.intValue();
    }

    /**
     * @return row size in points
     * @exception IOException
     */
    public double getPoints() throws IOException {
        if (_unit == null) {
            _unit = NumericConverter.extractDouble(getValue(_unit_attribute));
        }
        return _unit.doubleValue();
    }

    /**
     * @return left margin
     * @exception IOException
     */
    public int getLeftMargin() throws IOException {
        if (_margin_a == null) {
            _margin_a =
                NumericConverter.extractInteger(
                    getValue(_margin_a_attribute),
                    _margin_validator);
        }
        return _margin_a.intValue();
    }

    /**
     * @return right margin
     * @exception IOException
     */
    public int getRightMargin() throws IOException {
        if (_margin_b == null) {
            _margin_b =
                NumericConverter.extractInteger(
                    getValue(_margin_b_attribute),
                    _margin_validator);
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
            _outline_level = NumericConverter.extractInteger(
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
     * Set this row's height
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        int row = getRowNo();

        if (row > Short.MAX_VALUE) {
            throw new IOException("Illegal row value: " + row);
        }
        getSheet().getRow(row).setHeight(getPoints());
    }
} // end public class EPRowInfo
