/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "paper" tag
 *
 * This element contains no other elements and has no attributes. Its
 * content describes the paper to be used (e.g., A4)
 *
 *
 * @author Marc Johnson (marc_johnson27591@hotmail.com)
 * @author Victor Skladov (skladov@his.de)
 * @version CVS $Id$
 */
public class EP_Paper extends BaseElementProcessor {

    private String _paper;

    /**
     * constructor
     */
    public EP_Paper() {
        super(null);
    }

    public String getPaper(){
        if (_paper == null) {
            _paper = StringUtils.strip(this.getData());
        }
        return this._paper;
    }

    /**
     * Setup the paper size (Letter, Legal, Executive, A4, A5, A6, ).
     * Default Letter
     * @exception IOException
     */

    public void endProcessing() throws IOException{
        _paper = getPaper();
        Sheet sheet = this.getSheet();
        short paperSize = HSSFPrintSetup.LETTER_PAPERSIZE;
        if (StringUtils.isNotEmpty(_paper)) {
            if ("A4".equalsIgnoreCase(_paper)) {
                paperSize = HSSFPrintSetup.A4_PAPERSIZE;
            } else if ("A5".equalsIgnoreCase(_paper)) {
                paperSize = HSSFPrintSetup.A5_PAPERSIZE;
            } else if ("Legal".equalsIgnoreCase(_paper)) {
                paperSize = HSSFPrintSetup.LEGAL_PAPERSIZE;
            } else if ("Executive".equalsIgnoreCase(_paper)) {
                paperSize = HSSFPrintSetup.EXECUTIVE_PAPERSIZE;
            }
        }
        sheet.setPaperSize(paperSize);
    }

}   // end public class EP_Paper
