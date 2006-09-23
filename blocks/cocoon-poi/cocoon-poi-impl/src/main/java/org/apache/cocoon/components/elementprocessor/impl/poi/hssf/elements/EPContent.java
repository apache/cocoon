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

import java.io.IOException;

/**
 * No-op implementation of ElementProcessor to handle the "Content"
 * tag
 *
 * This element has no attributes and holds its parent element's
 * content.
 *
 * @version $Id$
 */
public class EPContent extends BaseElementProcessor {
    private String _content;

    /**
     * constructor
     */
    public EPContent() {
        super(null);
        _content = null;
    }

    /**
     * @return content
     */
    public String getContent() {
        if (_content == null) {
            try {
                _content = getData();
            } catch (NullPointerException ignored) {
                //ignore npe
            }
        }
        return _content;
    }

    /**
     * end processing -- pass their content up to their cell
     * @exception IOException
     */
    public void endProcessing() throws IOException {
        String thecontent = getContent();
        if (thecontent != null && !thecontent.trim().equals(""))
            getCell().setContent(getContent());
    }
} // end public class EPContent
