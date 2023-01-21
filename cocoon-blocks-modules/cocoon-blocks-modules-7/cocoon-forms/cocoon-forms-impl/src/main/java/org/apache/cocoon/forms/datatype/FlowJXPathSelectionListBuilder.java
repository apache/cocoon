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

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * Builds a selection list that will take its values from the flow page data.
 * The items list and, for each item, its value and label, are fetched using
 * JXPath expressions.
 *
 * <p>If an item has no label, its value is used as the label.
 *
 * <p>Example:
 * <pre>
 *   &lt;fd:selection-list type="flow-jxpath"
 *       list-path="selectList" value-path="value" label-path="label"/gt;
 * </pre>
 * Flow script:
 * <pre>
 *   var data = {
 *      selectList: [{value:3, label:"three"}, {value:4}]
 *   };
 *   form.showForm("form.html", data);
 * </pre>
 *
 * @see org.apache.cocoon.forms.datatype.FlowJXPathSelectionList
 * @version $Id$
 */
public class FlowJXPathSelectionListBuilder implements SelectionListBuilder {

    private ProcessInfoProvider processInfoProvider;

    public SelectionList build(Element selectionListElement, Datatype datatype) throws Exception {

        String listPath = DomHelper.getAttribute(selectionListElement, "list-path");
        String valuePath = DomHelper.getAttribute(selectionListElement, "value-path");

        Map nspfx = DomHelper.getInheritedNSDeclarations(selectionListElement);
        String i18nPfx = FormsConstants.I18N_PREFIX;
        if (nspfx != null) {
            i18nPfx = (String)nspfx.get( FormsConstants.I18N_NS );
            if (i18nPfx == null ) {
                i18nPfx = FormsConstants.I18N_PREFIX;
            }
        }

        String labelPath = DomHelper.getAttribute(selectionListElement, "label-path", null);
        boolean labelIsI18nKey = false;
        if (labelPath == null) {
            labelPath = DomHelper.getAttribute(selectionListElement, i18nPfx + ":label-path");
            labelIsI18nKey = true;
        }

        String nullText = DomHelper.getAttribute(selectionListElement, "null-text", null);
        boolean nullTextIsI18nKey = false;
        if (nullText == null) {
            nullText = DomHelper.getAttribute(selectionListElement, i18nPfx + ":null-text", null);
            if (nullText != null) {
                nullTextIsI18nKey = true;
            }
        }

        String i18nCatalog = DomHelper.getAttribute(selectionListElement, "catalogue", null);

        return new FlowJXPathSelectionList(processInfoProvider,
                                           listPath,
                                           valuePath,
                                           labelPath,
                                           datatype,
                                           nullText,
                                           nullTextIsI18nKey,
                                           i18nCatalog,
                                           labelIsI18nKey);
    }

    public void setProcessInfoProvider( ProcessInfoProvider processInfoProvider )
    {
        this.processInfoProvider = processInfoProvider;
    }

}
