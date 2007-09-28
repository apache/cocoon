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
package org.apache.cocoon.forms.datatype;

import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.mozilla.javascript.Function;
import org.w3c.dom.Element;

/**
 * Builds a selection list that will take its values from a JavaScript snippet.
 * The snippet runs in scope where the list filter (if any) and the view data are
 * available as <code>filter</code> and <code>viewData</code> respectively, along
 * with all flowscript functions. It must return a list of (value, label) pairs.
 * <p>
 * If an item has no label, its value is used as the label. If the declaration has
 * a <code>catalogue</code> attribute, the labels are interpreted as i18n keys in
 * that catalogue.
 * <p>
 * Example:
 * <pre>
 *   &lt;fd:selection-list type="flow-function"&gt;
 *     return [ {value: 3, label: "three"}, {value:4} ];
 *   &lt;/fd:selection-list&gt;
 * </pre>
 *
 * @see org.apache.cocoon.forms.datatype.JavaScriptSelectionList
 * @since 2.1.9
 * @version $Id$
 */
public class JavaScriptSelectionListBuilder implements SelectionListBuilder {

    private ProcessInfoProvider processInfoProvider;
    
    public SelectionList build(Element selectionListElement, Datatype datatype) throws Exception {

        String i18nCatalog = DomHelper.getAttribute(selectionListElement, "catalogue", null);
        
        Function function = JavaScriptHelper.buildFunction(selectionListElement, "buildSelectionList", new String[] { "filter" });

        return new JavaScriptSelectionList(processInfoProvider, datatype, function, i18nCatalog, DomHelper.getLocationObject(selectionListElement));
    }

    public void setProcessInfoProvider( ProcessInfoProvider processInfoProvider )
    {
        this.processInfoProvider = processInfoProvider;
    }
}
