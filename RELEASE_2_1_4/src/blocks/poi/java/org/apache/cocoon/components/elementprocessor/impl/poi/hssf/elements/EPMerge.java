/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2002 The Apache Software Foundation. All rights reserved.

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

import org.apache.poi.hssf.util.RangeAddress;
import org.apache.poi.hssf.util.Region;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Merge" tag.
 * This element is a container of other elements and has several attributes.
 *
 * @author Danny Mui (danny@muibros.com)
 * @version CVS $Id: EPMerge.java,v 1.5 2004/01/31 08:50:39 antonio Exp $
 */
public class EPMerge extends BaseElementProcessor {

    private String _cellRange;

    /**
     * constructor
     */
    public EPMerge() {
        super(null);
        _cellRange = null;
    }

    public String getCellRange() {
        if (this._cellRange == null) {
            //pulls in the content
            _cellRange = this.getData();
        }
        return this._cellRange;
    }

    /**
     * Setup the merged regions
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        RangeAddress rangeAddress = new RangeAddress(getCellRange());
        Sheet sheet = this.getSheet();

        //subtracting one since rangeaddress starts at 1,1 where rows/cols
        // start at 0,0
        short fromCol =
            (short) (rangeAddress.getXPosition(rangeAddress.getFromCell()) - 1);
        int fromRow = rangeAddress.getYPosition(rangeAddress.getFromCell()) - 1;
        short toCol =
            (short) (rangeAddress.getXPosition(rangeAddress.getToCell()) - 1);
        int toRow = rangeAddress.getYPosition(rangeAddress.getToCell()) - 1;

        if (getLogger().isDebugEnabled()) {
            getLogger().debug("Merging Range: Row (" + fromRow + ") Col ("
                    + fromCol + ")" + " to Row (" + toRow + ") Col (" + toCol
                    + ")");
        }
        Region region = new Region(fromRow, fromCol, toRow, toCol);
        sheet.addMergedRegion(region);
    }

} // end public class EPMerge
