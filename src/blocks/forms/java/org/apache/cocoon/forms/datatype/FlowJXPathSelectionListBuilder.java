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
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds a selection list that will take its values from the flow page data. The items list and,
 * for each item, its value and label, are fetched using JXPath expressions.
 * <p>
 * If an item has no label, its value is used as the label.
 * <p>
 * Example:
 * <pre>
 *   &lt;wd:selection-list type="flow-jxpath"
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
 * @see org.apache.cocoon.woody.datatype.FlowJXPathSelectionList
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: FlowJXPathSelectionListBuilder.java,v 1.1 2004/03/09 10:34:00 reinhard Exp $
 */
public class FlowJXPathSelectionListBuilder implements SelectionListBuilder, Contextualizable {

    private Context context;
    
    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public SelectionList build(Element selectionListElement, Datatype datatype) throws Exception {
        
        String listPath = DomHelper.getAttribute(selectionListElement, "list-path");
        String keyPath = DomHelper.getAttribute(selectionListElement, "value-path");
        String valuePath = DomHelper.getAttribute(selectionListElement, "label-path");
        
        return new FlowJXPathSelectionList(context, listPath, keyPath, valuePath, datatype);
    }

}
