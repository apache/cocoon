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
import org.apache.cocoon.components.elementprocessor.ElementProcessor;

import org.apache.cocoon.components.elementprocessor.types.BooleanConverter;
import org.apache.cocoon.components.elementprocessor.types.BooleanResult;
import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;

import java.io.IOException;
import java.util.Hashtable;

/**
 * No-op implementation of ElementProcessor to handle the "Font" tag
 *
 * This element has five attributes and also holds the name of the
 * font as element content.
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id: EPFont.java,v 1.6 2004/03/05 13:02:03 bdelacretaz Exp $
 */
public class EPFont extends BaseElementProcessor {
    private NumericResult _unit;
    private BooleanResult _bold;
    private BooleanResult _italic;
    private NumericResult _underline;
    private BooleanResult _strike_through;
    private String _font;
    private HSSFFont hssfFont;
    private static final String _unit_attribute = "Unit";
    private static final String _bold_attribute = "Bold";
    private static final String _italic_attribute = "Italic";
    private static final String _underline_attribute = "Underline";
    private static final String _strike_through_attribute = "StrikeThrough";
    private static final Validator _underline_type_validator =
        new Validator() {
            public IOException validate(final Number number) {
                return UnderlineType.isValid(number.intValue()) ? null
                  : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPFont() {
        super(null);
        _unit = null;
        _bold = null;
        _italic = null;
        _underline = null;
        _strike_through = null;
        _font = null;
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
        EPStyle pstyle = (EPStyle)parent;
        if (pstyle.isValid()) {
            Hashtable colorhash = pstyle.getColorHash();
            HSSFColor color = null;

            HSSFCellStyle style = pstyle.getStyle();
            //style.setFillForegroundColor(
            Workbook workbook = getWorkbook();
            HSSFFont font = workbook.createFont();
            style.setFont(font);
            font.setFontHeightInPoints((short)getUnit());
            //font.setFontName(getFont());
            font.setItalic(getItalic());
            if (getBold()) {
                font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            } else {
                font.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
            }
            font.setUnderline((byte)getUnderline());
            font.setStrikeout(getStrikeThrough());

            color = (HSSFColor)colorhash.get(
                    pstyle.getForegroundColor().toString());
            //System.out.println(pstyle.getForegroundColor().toString());
            if (color == null)
                color = new HSSFColor.BLACK();
            font.setColor(color.getIndex());
            hssfFont = font;
        }
    }

    /**
     * @return unit
     * @exception IOException
     */
    public double getUnit() throws IOException {
        if (_unit == null) {
            _unit = NumericConverter.extractDouble(getValue(_unit_attribute));
        }
        return _unit.doubleValue();
    }

    /**
     * @return bold
     * @exception IOException
     */
    public boolean getBold() throws IOException {
        if (_bold == null) {
            _bold = BooleanConverter.extractBoolean(getValue(_bold_attribute));
        }
        return _bold.booleanValue();
    }

    /**
     * @return italic
     * @exception IOException
     */
    public boolean getItalic() throws IOException {
        if (_italic == null) {
            _italic =
                BooleanConverter.extractBoolean(getValue(_italic_attribute));
        }
        return _italic.booleanValue();
    }

    /**
     * @return underline as one of the public variables in UnderlineType
     * @exception IOException
     */
    public int getUnderline() throws IOException {
        if (_underline == null) {
            _underline = NumericConverter.extractInteger(
                    getValue(_underline_attribute), _underline_type_validator);
        }
        return _underline.intValue();
    }

    /**
     * @return strikeThrough
     * @exception IOException
     */

    public boolean getStrikeThrough() throws IOException {
        if (_strike_through == null) {
            _strike_through = BooleanConverter.extractBoolean(
                    getValue(_strike_through_attribute));
        }
        return _strike_through.booleanValue();
    }

    /**
     * @return name of the font
     */

    public String getFont() {
        if (_font == null) {
            try {
                _font = getData();
            } catch (NullPointerException ignored) { /* VOID */}
        }

        return _font;
    }

    private HSSFFont getHSSFFont() {
        return hssfFont;
    }

    /**
     * push the data into the font
     * @exception IOException
     */

    public void endProcessing() throws IOException {
        String thefont = getFont();
        if (thefont != null && !thefont.trim().equals("")
                && getHSSFFont() != null) {
            getHSSFFont().setFontName(thefont);
        } else if (getHSSFFont() != null) {
            getHSSFFont().setFontName("Arial"); //default excel font
        }
    }
} // end public class EPFont
