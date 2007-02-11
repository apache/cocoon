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

import java.io.IOException;

/**
 * ElementProcessor that handles the "Workbook" tag
 *
 * The Workbook element includes one attribute ("gmr"), which
 * basically gives us the Gnumeric major version. At some point we
 * might care about that, but not today.
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @version CVS $Id: EPWorkbook.java,v 1.5 2004/03/05 13:02:04 bdelacretaz Exp $
 */
public class EPWorkbook extends BaseElementProcessor {
    private Workbook _workbook;

    /**
     * constructor
     */
    public EPWorkbook() {
        // the Workbook element has no implied attributes
        super(null);
        _workbook = new Workbook();
    }

    /**
     * override of getWorkbook()
     * @return the workbook
     */
    protected Workbook getWorkbook() {
        return _workbook;
    }

    /**
     * override of endProcessing()
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        _workbook.write(getFilesystem());
    }
} // end public class EPWorkbook
