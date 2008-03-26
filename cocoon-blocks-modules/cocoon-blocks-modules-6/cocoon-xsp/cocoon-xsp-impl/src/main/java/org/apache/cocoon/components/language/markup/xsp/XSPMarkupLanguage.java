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
package org.apache.cocoon.components.language.markup.xsp;

import org.apache.cocoon.components.language.markup.CocoonMarkupLanguage;
import org.apache.cocoon.components.language.markup.LogicsheetFilter;
import org.apache.cocoon.components.language.programming.ProgrammingLanguage;
import org.apache.cocoon.xml.AbstractXMLPipe;

/**
 * This class implements <code>MarkupLanguage</code> for Cocoon's
 * <a href="http://cocoon.apache.org/userdocs/xsp/">XSP</a>.
 *
 * @version $Id$
 */
public class XSPMarkupLanguage extends CocoonMarkupLanguage {

    /**
     * Returns the root element for a valid XSP page: page element!
     */
    public String getRootElement() {
        return "page";
    }

    /**
     * Return the filter to preprocess logicsheets expanding {#expr} to
     * xsp:attribute and xsp:expr elements.
     */
    protected LogicsheetFilter getLogicsheetFilter() {
        return new XSPExpressionFilter(this);
    }

    /**
     * Prepare the input source for logicsheet processing and code generation
     * with a preprocess filter.
     * The return <code>XMLFilter</code> object is the first filter on the
     * transformer chain.
     *
     * @param filename The source filename
     * @param language The target programming language
     * @return The preprocess filter
     *
     * @see XSPMarkupLanguage.PreProcessFilter
     */
    protected AbstractXMLPipe getPreprocessFilter(String filename,
                                                  AbstractXMLPipe filter,
                                                  ProgrammingLanguage language) {
        return new PreProcessFilter(filter, filename, language, this);
    }

//
//  Inner classes
//

    /**
     * <code>{@link CocoonMarkupLanguage.PreProcessFilter PreProcessFilter}</code> that replaces
     * XSP expressions.
     *
     * @see org.xml.sax.ContentHandler
     */
    protected class PreProcessFilter extends CocoonMarkupLanguage.PreProcessFilter {
        public PreProcessFilter(AbstractXMLPipe filter, String filename, ProgrammingLanguage language, XSPMarkupLanguage markup) {
            super(new XSPExpressionFilter.XMLPipeAdapter(new XSPExpressionFilter(markup), filter), filename, language);
        }
    }
}
