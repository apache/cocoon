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
import org.apache.cocoon.components.elementprocessor.ElementProcessor;

import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;

import java.io.IOException;
import java.util.Hashtable;

/**
 * No-op implementation of ElementProcessor to handle the "Right" tag
 *
 * This element has two attributes and no content.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id: EPRight.java,v 1.5 2004/01/31 08:50:39 antonio Exp $
 */
public class EPRight extends BaseElementProcessor {
    private NumericResult _style;
    private ColorCode _color;
    private boolean _color_fetched;
    private static final String _style_attribute = "Style";
    private static final String _color_attribute = "Color";
    private static final Validator _style_validator = new Validator() {
        public IOException validate(final Number number) {
            return BorderStyle.isValid(number.intValue()) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPRight() {
        super(null);
        _style = null;
        _color = null;
        _color_fetched = false;
    }

    /**
     * Override of Initialize() implementation
     * @param attributes the array of Attribute instances; may be empty, will
     *                  never be null
     * @param parent the parent ElementProcessor; may be null
     * @exception IOException if anything is wrong
     */
    public void initialize(final Attribute[] attributes,
                final ElementProcessor parent) throws IOException {
        super.initialize(attributes, parent);
        EPStyle pstyle = (EPStyle)getAncestor(EPStyle.class);
        if (pstyle != null && pstyle.isValid()) {
            Hashtable colorhash = pstyle.getColorHash();
            HSSFColor color = null;

            HSSFCellStyle style = pstyle.getStyle(); //oops
                                                                       // a
                                                                       // little
                                                                       // confusing
            //below is the style attribute
            //this is an HSSFCellStyle
            //associated with EPStyle
            style.setBorderRight((short)getStyle());

            ColorCode colorCode = getColor();
            if (colorCode != null) {
                color = (HSSFColor)colorhash.get(colorCode.toString());
            }
            if (color == null) {
                color = new HSSFColor.BLACK();
            }
            style.setRightBorderColor(color.getIndex());
        }

    }

    /**
     * @return style as an int from BorderStyle
     * @exception IOException
     */
    public int getStyle() throws IOException {
        if (_style == null) {
            _style = NumericConverter.extractInteger(
                    getValue(_style_attribute), _style_validator);
        }
        return _style.intValue();
    }

    /**
     * @return color
     * @exception IOException
     */
    public ColorCode getColor() throws IOException {
        if (!_color_fetched) {
            String colorString = getValue(_color_attribute);
            if (colorString != null) {
                _color = new ColorCode(colorString);
            }
            _color_fetched = true;
        }
        return _color;
    }
} // end public class EPRight
