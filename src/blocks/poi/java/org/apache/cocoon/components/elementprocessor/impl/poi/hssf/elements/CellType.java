
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



import org.apache.poi.hssf.usermodel.HSSFCell;

/**
 * Cell type codes
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: CellType.java,v 1.2 2003/03/11 19:05:01 vgritsenko Exp $
 */
public class CellType
{
    public static final int CELL_TYPE_EMPTY     = 10;
    public static final int CELL_TYPE_BOOLEAN   = 20;
    public static final int CELL_TYPE_INTEGER   = 30;
    public static final int CELL_TYPE_FLOAT     = 40;
    public static final int CELL_TYPE_ERROR     = 50;
    public static final int CELL_TYPE_STRING    = 60;
    public static final int CELL_TYPE_CELLRANGE = 70;
    public static final int CELL_TYPE_ARRAY     = 80;

    private CellType()
    {
    }

    /**
     * Is this a valid cell type?
     *
     * @param val value to be checked
     *
     * @return true if valid, false otherwise
     */

    public static boolean isValid(final int val)
    {
        switch (val)
        {

            case CELL_TYPE_EMPTY :
            case CELL_TYPE_BOOLEAN :
            case CELL_TYPE_INTEGER :
            case CELL_TYPE_FLOAT :
            case CELL_TYPE_ERROR :
            case CELL_TYPE_STRING :
            case CELL_TYPE_CELLRANGE :
            case CELL_TYPE_ARRAY :
                return true;

            default :
                return false;
        }
    }

    /**
     * Convert a CellType enum into an HSSFCell enum
     *
     * @param val the value to be converted
     *
     * @return the converted value
     */

    static int convertCellType(final int val)
    {
        switch (val)
        {

            case CELL_TYPE_INTEGER :
            case CELL_TYPE_FLOAT :
                return HSSFCell.CELL_TYPE_NUMERIC;

            case CELL_TYPE_STRING :
                return HSSFCell.CELL_TYPE_STRING;

            default :
                return HSSFCell.CELL_TYPE_BLANK;
        }
    }
}   // end public class CellType
