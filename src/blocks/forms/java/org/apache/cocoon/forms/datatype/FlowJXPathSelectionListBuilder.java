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
package org.apache.cocoon.forms.datatype;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.util.DomHelper;
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
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version $Id$
 */
public class FlowJXPathSelectionListBuilder implements SelectionListBuilder, Contextualizable {

    private Context context;

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public SelectionList build(Element selectionListElement, Datatype datatype) throws Exception {

        String listPath = DomHelper.getAttribute(selectionListElement, "list-path");
        String valuePath = DomHelper.getAttribute(selectionListElement, "value-path");
        Map nspfx = DomHelper.getInheritedNSDeclarations(selectionListElement);
        String i18nPfx = Constants.I18N_PREFIX;
        if (nspfx != null) {
            i18nPfx = (String)nspfx.get( Constants.I18N_NS );
            if (i18nPfx == null ) {
                i18nPfx = Constants.I18N_PREFIX;
            }
        }
        String labelPath = DomHelper.getAttribute(selectionListElement, "label-path", null);
        boolean labelIsI18nKey = false;
        if( labelPath == null )
        {
            labelPath = DomHelper.getAttribute(selectionListElement, i18nPfx + ":label-path");
            labelIsI18nKey = true;
        }
        String nullText = DomHelper.getAttribute(selectionListElement, "null-text", null);
        boolean nullTextIsI18nKey = false;
        if( nullText == null ) {
            nullText = DomHelper.getAttribute(selectionListElement, i18nPfx + ":null-text", null);
            if( nullText != null ) {
                nullTextIsI18nKey = true;
            }
        }
        
        String i18nCatalog = DomHelper.getAttribute(selectionListElement, "catalogue", null);


        return new FlowJXPathSelectionList(context, 
                                           listPath, 
                                           valuePath, 
                                           labelPath, 
                                           datatype, 
                                           nullText, 
                                           nullTextIsI18nKey, 
                                           i18nCatalog, 
                                           labelIsI18nKey);
    }

}
