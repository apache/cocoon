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
import org.apache.cocoon.CascadingIOException;
import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.types.BooleanConverter;
import org.apache.cocoon.components.elementprocessor.types.BooleanResult;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Sheet" tag
 *
 * This element contains other elements and has the following boolean
 * attributes:
 * <ul>
 * <li>DisplayFormulas
 * <li>HideZero
 * <li>HideGrid
 * <li>HideColHeader
 * <li>HideRowHeader
 * <li>DisplayOutlines
 * <li>OutlineSymbolsBelow
 * <li>OutlineSymbolsRight
 * </ul>
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com) 
 * @author Andrew C. Oliver (acoliver2@users.sourceforge.net)
 * @version CVS $Id: EPSheet.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EPSheet extends BaseElementProcessor {
    private Sheet _sheet;
    private BooleanResult _display_formulas;
    private BooleanResult _hide_zero;
    private BooleanResult _hide_grid;
    private BooleanResult _hide_col_header;
    private BooleanResult _hide_row_header;
    private BooleanResult _display_outlines;
    private BooleanResult _outline_symbols_below;
    private BooleanResult _outline_symbols_right;
    private static final String _display_formulas_attribute = "DisplayFormulas";
    private static final String _hide_zero_attribute = "HideZero";
    private static final String _hide_grid_attribute = "HideGrid";
    private static final String _hide_col_header_attribute = "HideColHeader";
    private static final String _hide_row_header_attribute = "HideRowHeader";
    private static final String _display_outlines_attribute = "DisplayOutlines";
    private static final String _outline_symbols_below_attribute =
        "OutlineSymbolsBelow";
    private static final String _outline_symbols_right_attribute =
        "OutlineSymbolsRight";

    /**
     * constructor
     */

    public EPSheet() {
        super(null);
        _display_formulas = null;
        _hide_zero = null;
        _hide_grid = null;
        _hide_col_header = null;
        _hide_row_header = null;
        _display_outlines = null;
        _outline_symbols_below = null;
        _outline_symbols_right = null;
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
        try {
            _sheet = new Sheet(getWorkbook());
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new CascadingIOException(e.getMessage(), e);
        }
    }

    /**
     * override of endProcessing(). Reponsible for fixing style regions with
     * blank cells.
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        _sheet.assignBlanksToRegions();
    }

    /**
     * override of getSheet()
     * @return the sheet
     */
    protected Sheet getSheet() {
        return _sheet;
    }

    /**
     * @return true if formulas should be displayed
     * @exception IOException if attribute is missing or malformed
     */
    public boolean getDisplayFormulas() throws IOException {
        if (_display_formulas == null) {
            _display_formulas = BooleanConverter.extractBoolean(
                    getValue(_display_formulas_attribute));
        }
        return _display_formulas.booleanValue();
    }

    /**
     * @return true if zeroes should be suppressed
     * @exception IOException if attribute is missing or malformed
     */
    public boolean getHideZero() throws IOException {
        if (_hide_zero == null) {
            _hide_zero =
                BooleanConverter.extractBoolean(getValue(_hide_zero_attribute));
        }
        return _hide_zero.booleanValue();
    }

    /**
     * @return true if grid should be hidden
     * @exception IOException if attribute is missing or malformed
     */
    public boolean getHideGrid() throws IOException {
        if (_hide_grid == null) {
            _hide_grid =
                BooleanConverter.extractBoolean(getValue(_hide_grid_attribute));
        }
        return _hide_grid.booleanValue();
    }

    /**
     * @return true if column headers should be hidden
     * @exception IOException if attribute is missing or malformed
     */
    public boolean getHideColHeader() throws IOException {
        if (_hide_col_header == null) {
            _hide_col_header = BooleanConverter.extractBoolean(
                    getValue(_hide_col_header_attribute));
        }
        return _hide_col_header.booleanValue();
    }

    /**
     * @return true if row headers should be hidden
     * @exception IOException if attribute is missing or malformed
     */
    public boolean getHideRowHeader() throws IOException {
        if (_hide_row_header == null) {
            _hide_row_header = BooleanConverter.extractBoolean(
                    getValue(_hide_row_header_attribute));
        }
        return _hide_row_header.booleanValue();
    }

    /**
     * @return true if outlines should be displayed
     * @exception IOException if attribute is missing or malformed
     */
    public boolean getDisplayOutlines() throws IOException {
        if (_display_outlines == null) {
            _display_outlines = BooleanConverter.extractBoolean(
                    getValue(_display_outlines_attribute));
        }
        return _display_outlines.booleanValue();
    }

    /**
     * @return true if outline symbols are below
     * @exception IOException if attribute is missing or malformed
     */
    public boolean getOutlineSymbolsBelow() throws IOException {
        if (_outline_symbols_below == null) {
            _outline_symbols_below = BooleanConverter.extractBoolean(
                    getValue(_outline_symbols_below_attribute));
        }
        return _outline_symbols_below.booleanValue();
    }

    /**
     * @return true if outline symbols are on the right
     * @exception IOException if attribute is missing or malformed
     */
    public boolean getOutlineSymbolsRight() throws IOException {
        if (_outline_symbols_right == null) {
            _outline_symbols_right = BooleanConverter.extractBoolean(
                    getValue(_outline_symbols_right_attribute));
        }
        return _outline_symbols_right.booleanValue();
    }
} // end public class EPSheet
