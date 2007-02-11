
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

import org.apache.cocoon.components.elementprocessor.impl.poi.hssf.elements.*;
import org.apache.cocoon.components.elementprocessor.*;
import org.apache.cocoon.components.elementprocessor.impl.AbstractElementProcessorFactory;

/**
 * a simple extension of ElementProcessorFactory that maps the HSSF
 * XML element names to HSSF-specific ElementProcessor progenitor
 * objects.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: HSSFElementProcessorFactory.java,v 1.2 2003/03/11 19:05:00 vgritsenko Exp $
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
        throws CannotCreateElementProcessorException
    {
        ElementProcessor rval = null;

        try
        {
            rval = createNewElementProcessorInstance(( Class ) progenitor);

            //every locale aware element processor is passed the locale string
            if (rval instanceof LocaleAware) {
                   ((LocaleAware)rval).setLocale(locale);
            }
        }
        catch (ClassCastException e)
        {
            e.printStackTrace();
            throw new CannotCreateElementProcessorException(
                "Progenitor is not an instance of Class");
        }
        return rval;
    }
}   // end public class HSSFElementProcessorFactory
