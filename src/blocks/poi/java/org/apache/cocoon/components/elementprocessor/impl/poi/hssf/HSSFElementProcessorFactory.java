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
package org.apache.cocoon.components.elementprocessor.impl.poi.hssf;

import org.apache.cocoon.components.elementprocessor.CannotCreateElementProcessorException;
import org.apache.cocoon.components.elementprocessor.ElementProcessor;
import org.apache.cocoon.components.elementprocessor.LocaleAware;
import org.apache.cocoon.components.elementprocessor.impl.AbstractElementProcessorFactory;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPAttribute;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPAttributes;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPBottom;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPButton;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPCell;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPCellComment;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPCells;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPCheckbox;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPColInfo;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPCols;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPConstr;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPContent;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPDiagonal;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPFont;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPFooter;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPFrame;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPGeometry;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPHeader;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPItem;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPLabel;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPLeft;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPMargins;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPMaxCol;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPMaxRow;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPMerge;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPMergedRegions;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPName;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPNames;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPObjects;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPPrintInformation;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPRev_Diagonal;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPRight;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPRowInfo;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPRows;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSelection;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSelections;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSheet;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSheetName;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSheetNameIndex;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSheetObjectBonobo;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSheetObjectFilled;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSheets;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSolver;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPStyle;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPStyleBorder;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPStyleRegion;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPStyles;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPSummary;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPTop;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPUIData;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPWorkbook;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EPZoom;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Bottom;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Default_;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Draft;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_EvenIfOnlyStyles;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Footer;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Grid;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_HCenter;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Header;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Left;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Monochrome;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Name;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Order;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Orientation;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Paper;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_RepeatLeft;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_RepeatTop;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Right;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Titles;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Top;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Type;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_VCenter;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_ValString;
import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.EP_Value;

/**
 * a simple extension of ElementProcessorFactory that maps the HSSF
 * XML element names to HSSF-specific ElementProcessor progenitor
 * objects.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: HSSFElementProcessorFactory.java,v 1.5 2004/01/31 08:50:44 antonio Exp $
 */
public class HSSFElementProcessorFactory
    extends AbstractElementProcessorFactory
{
    //holds the locale configuration
    String locale;
    /**
     * default constructor
     */

    public HSSFElementProcessorFactory(String locale)
    {
        super();
        this.locale=locale;
        addElementProcessorProgenitor("Attribute", EPAttribute.class);
        addElementProcessorProgenitor("Attributes", EPAttributes.class);
        addElementProcessorProgenitor("Bottom", EPBottom.class);
        addElementProcessorProgenitor("bottom", EP_Bottom.class);
        addElementProcessorProgenitor("Button", EPButton.class);
        addElementProcessorProgenitor("Cell", EPCell.class);
        addElementProcessorProgenitor("CellComment", EPCellComment.class);
        addElementProcessorProgenitor("Cells", EPCells.class);
        addElementProcessorProgenitor("Checkbox", EPCheckbox.class);
        addElementProcessorProgenitor("ColInfo", EPColInfo.class);
        addElementProcessorProgenitor("Cols", EPCols.class);
        addElementProcessorProgenitor("Constr", EPConstr.class);
        addElementProcessorProgenitor("Content", EPContent.class);
        addElementProcessorProgenitor("Diagonal", EPDiagonal.class);
        addElementProcessorProgenitor("draft", EP_Draft.class);
        addElementProcessorProgenitor("even_if_only_styles",
                                      EP_EvenIfOnlyStyles.class);
        addElementProcessorProgenitor("Font", EPFont.class);
        addElementProcessorProgenitor("Footer", EPFooter.class);
        addElementProcessorProgenitor("footer", EP_Footer.class);
        addElementProcessorProgenitor("Frame", EPFrame.class);
        addElementProcessorProgenitor("Geometry", EPGeometry.class);
        addElementProcessorProgenitor("grid", EP_Grid.class);
        addElementProcessorProgenitor("hcenter", EP_HCenter.class);
        addElementProcessorProgenitor("Header", EPHeader.class);
        addElementProcessorProgenitor("header", EP_Header.class);
        addElementProcessorProgenitor("Item", EPItem.class);
        addElementProcessorProgenitor("Label", EPLabel.class);
        addElementProcessorProgenitor("Left", EPLeft.class);
        addElementProcessorProgenitor("left", EP_Left.class);
        addElementProcessorProgenitor("Margins", EPMargins.class);
        addElementProcessorProgenitor("MaxCol", EPMaxCol.class);
        addElementProcessorProgenitor("MaxRow", EPMaxRow.class);
        addElementProcessorProgenitor("Merge", EPMerge.class);
        addElementProcessorProgenitor("MergedRegions", EPMergedRegions.class);
        addElementProcessorProgenitor("monochrome", EP_Monochrome.class);
        addElementProcessorProgenitor("Name", EPName.class);
        addElementProcessorProgenitor("name", EP_Name.class);
        addElementProcessorProgenitor("Names", EPNames.class);
        addElementProcessorProgenitor("Objects", EPObjects.class);
        addElementProcessorProgenitor("order", EP_Order.class);
        addElementProcessorProgenitor("orientation", EP_Orientation.class);
        addElementProcessorProgenitor("paper", EP_Paper.class);
        addElementProcessorProgenitor("PrintInformation",
                                      EPPrintInformation.class);
        addElementProcessorProgenitor("repeat_left", EP_RepeatLeft.class);
        addElementProcessorProgenitor("repeat_top", EP_RepeatTop.class);
        addElementProcessorProgenitor("Rev-Diagonal", EPRev_Diagonal.class);
        addElementProcessorProgenitor("Right", EPRight.class);
        addElementProcessorProgenitor("right", EP_Right.class);
        addElementProcessorProgenitor("RowInfo", EPRowInfo.class);
        addElementProcessorProgenitor("Rows", EPRows.class);
        addElementProcessorProgenitor("Selection", EPSelection.class);
        addElementProcessorProgenitor("Sheet", EPSheet.class);
        addElementProcessorProgenitor("SheetName", EPSheetName.class);
        addElementProcessorProgenitor("SheetNameIndex",
                                      EPSheetNameIndex.class);
        addElementProcessorProgenitor("SheetObjectBonobo",
                                      EPSheetObjectBonobo.class);
        addElementProcessorProgenitor("SheetObjectFilled",
                                      EPSheetObjectFilled.class);
        addElementProcessorProgenitor("Sheets", EPSheets.class);
        addElementProcessorProgenitor("Selections", EPSelections.class);
        addElementProcessorProgenitor("Solver", EPSolver.class);
        addElementProcessorProgenitor("Style", EPStyle.class);
        addElementProcessorProgenitor("StyleBorder", EPStyleBorder.class);
        addElementProcessorProgenitor("StyleRegion", EPStyleRegion.class);
        addElementProcessorProgenitor("Styles", EPStyles.class);
        addElementProcessorProgenitor("Summary", EPSummary.class);
        addElementProcessorProgenitor("titles", EP_Titles.class);
        addElementProcessorProgenitor("Top", EPTop.class);
        addElementProcessorProgenitor("top", EP_Top.class);
        addElementProcessorProgenitor("type", EP_Type.class);
        addElementProcessorProgenitor("UIData", EPUIData.class);
        addElementProcessorProgenitor("val-string", EP_ValString.class);
        addElementProcessorProgenitor("value", EP_Value.class);
        addElementProcessorProgenitor("vcenter", EP_VCenter.class);
        addElementProcessorProgenitor("Workbook", EPWorkbook.class);
        addElementProcessorProgenitor("Zoom", EPZoom.class);
        addElementProcessorProgenitor("*", EP_Default_.class);
    }

    /**
     * create an ElementProcessor
     *
     * @param progenitor the Object from which the ElementProcessor
     * will be created
     *
     * @return the new ElementProcessor
     *
     * @exception CannotCreateElementProcessorException
     */

    protected ElementProcessor doCreateElementProcessor(
            final Object progenitor)
            throws CannotCreateElementProcessorException {
        ElementProcessor rval = null;

        try {
            rval = createNewElementProcessorInstance((Class)progenitor);

            //every locale aware element processor is passed the locale string
            if (rval instanceof LocaleAware) {
                   ((LocaleAware)rval).setLocale(locale);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
            throw new CannotCreateElementProcessorException(
                "Progenitor is not an instance of Class");
        }
        return rval;
    }
}   // end public class HSSFElementProcessorFactory
