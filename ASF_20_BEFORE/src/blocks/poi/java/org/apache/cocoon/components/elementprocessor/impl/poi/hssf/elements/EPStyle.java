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

import java.io.IOException;
import java.util.Hashtable;

import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.types.Attribute;
import org.apache.cocoon.components.elementprocessor.types.BooleanConverter;
import org.apache.cocoon.components.elementprocessor.types.BooleanResult;
import org.apache.cocoon.components.elementprocessor.types.NumericConverter;
import org.apache.cocoon.components.elementprocessor.types.NumericResult;
import org.apache.cocoon.components.elementprocessor.types.Validator;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.util.HSSFColor;

/**
 * No-op implementation of ElementProcessor to handle the "Style" tag
 *
 * This element is a container of other elements and has several
 * attributes
 *
 * This element is not used in HSSFSerializer 1.0
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id: EPStyle.java,v 1.6 2004/01/31 08:50:39 antonio Exp $
 */
public class EPStyle extends BaseElementProcessor {
    private static final String _general_format = "General";
    private HorizontalAlignment _h_align;
    private VerticalAlignment _v_align;
    private BooleanResult _wrap_text;
    private StyleOrientation _orient;
    private NumericResult _shade;
    private NumericResult _indent;
    private ColorCode _fore;
    private ColorCode _back;
    private ColorCode _pattern_color;
    private String _format;
    private static final String _h_align_attribute = "HAlign";
    private static final String _v_align_attribute = "VAlign";
    private static final String _wrap_text_attribute = "WrapText";
    private static final String _orient_attribute = "Orient";
    private static final String _shade_attribute = "Shade";
    private static final String _indent_attribute = "Indent";
    private static final String _fore_attribute = "Fore";
    private static final String _back_attribute = "Back";
    private static final String _pattern_color_attribute = "PatternColor";
    private static final String _format_attribute = "Format";

    private boolean invalid;

    private static final Validator _shade_validator = new Validator() {
        public IOException validate(final Number number) {
            return StyleShading.isValid(number.intValue()) ? null
                : new IOException("\"" + number + "\" is not a legal value");
        }
    };

    /**
     * constructor
     */
    public EPStyle() {
        super(null);
        _h_align = null;
        _v_align = null;
        _wrap_text = null;
        _orient = null;
        _shade = null;
        _indent = null;
        _fore = null;
        _back = null;
        _pattern_color = null;
        _format = null;
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

        EPStyleRegion sregion = (EPStyleRegion)parent;

        if (sregion.isValid()) {
            Hashtable colorhash = sregion.getColorHash();

            HSSFCellStyle style = sregion.getStyle();
            short cnvhalign =
                convertAlignment(getHorizontalAlignment().getCode());
            style.setAlignment(cnvhalign);
            short cnvvalign =
                convertVAlignment(getVerticalAlignment().getCode());
            style.setVerticalAlignment(cnvvalign);
            style.setFillPattern((short)getShade());

            Workbook workbook = getWorkbook();
            HSSFDataFormat dataformat = workbook.createDataFormat();
            if (getShade() == 1) {
                // TODO: change to constant when upgrade to new HSSF
                // solid w/foreground, bg doesn't matter
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("shade = 1");
                }
                HSSFColor color =
                    (HSSFColor)colorhash.get(getBackgroundColor().toString());
                if (color == null) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("s1 BG couldn't find color for "
                                + getBackgroundColor().toString());
                    }
                    color = new HSSFColor.WHITE();
                }
                style.setFillForegroundColor(color.getIndex());
                color = (HSSFColor)colorhash.get(getPatternColor().toString());
                if (color == null) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("s1 PC couldn't find color for "
                                + getPatternColor().toString());
                    }
                    color = new HSSFColor.BLACK();
                }
                style.setFillBackgroundColor(color.getIndex());
            } else {
                HSSFColor color =
                    (HSSFColor)colorhash.get(getBackgroundColor().toString());
                if (color == null) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug(
                            "BG couldn't find color for "
                                + getBackgroundColor().toString());
                    }
                    color = new HSSFColor.BLACK();
                }
                style.setFillBackgroundColor(color.getIndex());
                color = (HSSFColor)colorhash.get(getPatternColor().toString());
                if (color == null) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("PC couldn't find color for "
                                + getPatternColor().toString());
                    }
                    color = new HSSFColor.WHITE();
                }
                style.setFillForegroundColor(color.getIndex());
            }
            style.setWrapText(getWrapText());
            style.setLocked(true);

            String format = null;
            try {
                format = getFormat();
            } catch (NullPointerException e) {
                format = _general_format;
            }

            if (!format.equals(_general_format)) {
                short valuenumber;
                format = kludgeForGnumericMisformats(format);
                format = kludgeForGnumericDateDivergence(format);
                if (getLogger().isDebugEnabled()) {
                    getLogger().debug("setting format to " + format);
                }
                Object o =
                    workbook.getValidate(format, dataformat.getFormat(format));
                Short sh = null;
                sh = (Short)o;
                valuenumber = sh.shortValue();
                style.setDataFormat(valuenumber);
            }
        } else {
            invalid = true;
        }
    }

    /**
     * @return true if horizontal alignment general bit is set
     * @exception IOException
     */
    public boolean isHorizontalGeneral() throws IOException {
        return getHorizontalAlignment().isGeneral();
    }

    /**
     * @return true if horizontal alignment left bit is set
     * @exception IOException
     */
    public boolean isHorizontalLeft() throws IOException {
        return getHorizontalAlignment().isLeft();
    }

    /**
     * @return true if horizontal alignment right bit is set
     * @exception IOException
     */
    public boolean isHorizontalRight() throws IOException {
        return getHorizontalAlignment().isRight();
    }

    /**
     * @return true if horizontal alignment center bit is set
     * @exception IOException
     */
    public boolean isHorizontalCenter() throws IOException {
        return getHorizontalAlignment().isCenter();
    }

    /**
     * @return true if horizontal alignment fill bit is set
     * @exception IOException
     */
    public boolean isHorizontalFill() throws IOException {
        return getHorizontalAlignment().isFill();
    }

    /**
     * @return true if horizontal alignment justify bit is set
     * @exception IOException
     */
    public boolean isHorizontalJustify() throws IOException {
        return getHorizontalAlignment().isJustify();
    }

    /**
     * @return true if horizontal alignment center across selection bit is set
     * @exception IOException
     */
    public boolean isHorizontalCenterAcrossSelection() throws IOException {
        return getHorizontalAlignment().isCenterAcrossSelection();
    }

    /**
     * @return true if vertical alignment top bit is set
     * @exception IOException
     */
    public boolean isVerticalTop() throws IOException {
        return getVerticalAlignment().isTop();
    }

    /**
     * @return true if vertical alignment bottom bit is set
     * @exception IOException
     */
    public boolean isVerticalBottom() throws IOException {
        return getVerticalAlignment().isBottom();
    }

    /**
     * @return true if vertical alignment center bit is set
     * @exception IOException
     */
    public boolean isVerticalCenter() throws IOException {
        return getVerticalAlignment().isCenter();
    }

    /**
     * @return true if vertical alignment justify bit is set
     * @exception IOException
     */
    public boolean isVerticalJustify() throws IOException {
        return getVerticalAlignment().isJustify();
    }

    /**
     * @return true if wrap text is enabled
     * @exception IOException
     */
    public boolean getWrapText() throws IOException {
        if (_wrap_text == null) {
            _wrap_text =
                BooleanConverter.extractBoolean(getValue(_wrap_text_attribute));
        }
        return _wrap_text.booleanValue();
    }

    /**
     * @return true if style orientation horiz bit is set
     * @exception IOException
     */
    public boolean isStyleOrientationHoriz() throws IOException {
        return getStyleOrientation().isHoriz();
    }

    /**
     * @return true if style orientation vert horiz text bit is set
     * @exception IOException
     */
    public boolean isStyleOrientationVertHorizText() throws IOException {
        return getStyleOrientation().isVertHorizText();
    }

    /**
     * @return true if style orientation vert vert text bit is set
     * @exception IOException
     */
    public boolean isStyleOrientationVertVertText() throws IOException {
        return getStyleOrientation().isVertVertText();
    }

    /**
     * @return true if style orientation vert vert text2 bit is set
     * @exception IOException
     */
    public boolean isStyleOrientationVertVertText2() throws IOException {
        return getStyleOrientation().isVertVertText2();
    }

    /**
     * @return shade as one of the public variables in StyleShading
     * @exception IOException
     */
    public int getShade() throws IOException {
        if (_shade == null) {
            _shade = NumericConverter.extractInteger(
                    getValue(_shade_attribute), _shade_validator);
        }
        return _shade.intValue();
    }

    /**
     * @return indent
     * @exception IOException
     */
    public int getIndent() throws IOException {
        if (_indent == null) {
            _indent =
                NumericConverter.extractInteger(getValue(_indent_attribute));
        }
        return _indent.intValue();
    }

    /**
     * @return foreground color
     * @exception IOException
     */
    public ColorCode getForegroundColor() throws IOException {
        if (_fore == null) {
            _fore = new ColorCode(getValue(_fore_attribute));
        }
        return _fore;
    }

    /**
     * @return background color
     * @exception IOException
     */
    public ColorCode getBackgroundColor() throws IOException {
        if (_back == null) {
            _back = new ColorCode(getValue(_back_attribute));
        }
        return _back;
    }

    /**
     * @return pattern color
     * @exception IOException
     */
    public ColorCode getPatternColor() throws IOException {
        if (_pattern_color == null) {
            _pattern_color = new ColorCode(getValue(_pattern_color_attribute));
        }
        return _pattern_color;
    }

    /**
     * @return format string
     * @exception IOException
     */
    public String getFormat() throws IOException {
        if (_format == null) {
            _format = getValue(_format_attribute);
            /*
             * if (_format == null) { throw new IOException("missing " +
             * _format_attribute + " attribute");
             */
        }
        return _format;
    }

    private HorizontalAlignment getHorizontalAlignment() throws IOException {
        if (_h_align == null) {
            _h_align = new HorizontalAlignment(getValue(_h_align_attribute));
        }
        return _h_align;
    }

    private VerticalAlignment getVerticalAlignment() throws IOException {
        if (_v_align == null) {
            _v_align = new VerticalAlignment(getValue(_v_align_attribute));
        }
        return _v_align;
    }

    private StyleOrientation getStyleOrientation() throws IOException {
        if (_orient == null) {
            _orient = new StyleOrientation(getValue(_orient_attribute));
        }
        return _orient;
    }

    /**
     * @return instance created in the EPStyles instance from
     *             HSSFColor.getTripletHash();
     * @see org.apache.poi.hssf.util.HSSFColor#getTripletHash()
     */
    Hashtable getColorHash() {
        return ((EPStyleRegion)getAncestor(EPStyleRegion.class)).getColorHash();
    }

    /**
     * @return the HSSFCellStyle object associated with this style region.
     */
    HSSFCellStyle getStyle() {
        return ((EPStyleRegion)getAncestor(EPStyleRegion.class)).getStyle();
    }

    /**
     * @return validity (used to determine whether this is a big wasteful
     *             region with no purpose (gnumeric does this)
     */
    public boolean isValid() {
        return (!invalid);
    }

    /**
     * deal with mismatch between gnumeric align and Excel
     */
    private short convertAlignment(short alignment) {
        short retval = HSSFCellStyle.ALIGN_GENERAL; // its 0

        switch (alignment) {
            case 1 :
                retval = HSSFCellStyle.ALIGN_GENERAL;
                break;
            case 2 :
                retval = HSSFCellStyle.ALIGN_LEFT;
                break;
            case 4 :
                retval = HSSFCellStyle.ALIGN_RIGHT;
                break;
            case 8 :
                retval = HSSFCellStyle.ALIGN_CENTER;
                break;
            case 16 :
                retval = HSSFCellStyle.ALIGN_FILL;
                break;
            case 32 :
                retval = HSSFCellStyle.ALIGN_JUSTIFY;
                break;
            case 64 :
                retval = HSSFCellStyle.ALIGN_CENTER_SELECTION;
                break;
            default :
                retval = HSSFCellStyle.ALIGN_GENERAL;
        }
        return retval;
    }

    /**
     * deal with mismatch between gnumeric valign and Excel
     */
    private short convertVAlignment(short alignment) {
        short retval = HSSFCellStyle.VERTICAL_TOP; // its 0

        switch (alignment) {
            case 1 :
                retval = HSSFCellStyle.VERTICAL_TOP;
                break;
            case 2 :
                retval = HSSFCellStyle.VERTICAL_BOTTOM;
                break;
            case 4 :
                retval = HSSFCellStyle.VERTICAL_CENTER;
                break;
            case 8 :
                retval = HSSFCellStyle.VERTICAL_JUSTIFY;
                break;
            default :
                retval = HSSFCellStyle.VERTICAL_TOP;
        }
        return retval;
    }

    /**
     * Takes in a Gnumeric format string and applies some rules to it. Some
     * versions of Gnumeric seem to leave off the first parenthesis which
     * causes them not to match the Excel-style format string. (which of course
     * makes it a little hard to match)
     */
    private String kludgeForGnumericMisformats(String format) {
        String retval = format;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("going out of the format kludger " + retval);
            getLogger().debug("first )=" + format.indexOf(')'));
            getLogger().debug("first (=" + format.indexOf('('));
        }
        if (format.indexOf(')') < format.indexOf('(')
            && (format.indexOf(')') != -1)) {
            retval = "(" + format;
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug("going out of the format kludger " + retval);
        }
        return retval;
    }

    private String kludgeForGnumericDateDivergence(String format) {
        String retval = format;
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                "going into the format kludgeForGnumericDateDivergence"
                    + retval);
        }

        if (retval.equals("mm/dd/yy")) {
            retval = "m/d/yy";
        } else if (retval.equals("dd-mmm-yy")) {
            retval = "d-mmm-yy";
        } else if (retval.equals("dd-mmm")) {
            retval = "d-mmm";
        }
        if (getLogger().isDebugEnabled()) {
            getLogger().debug(
                "going out of the format kludgeForGnumericDateDivergence"
                    + retval);
        }
        return retval;
    }

} // end public class EPStyle
