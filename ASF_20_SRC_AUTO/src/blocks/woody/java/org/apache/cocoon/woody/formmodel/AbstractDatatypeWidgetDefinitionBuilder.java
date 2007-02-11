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
package org.apache.cocoon.woody.formmodel;

import org.apache.avalon.framework.service.ServiceSelector;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.SelectionList;
import org.apache.cocoon.woody.datatype.SelectionListBuilder;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Abstract base class for WidgetDefinitionBuilders that build widgets that have datatypes/selection lists.
 * 
 * @version $Id: AbstractDatatypeWidgetDefinitionBuilder.java,v 1.7 2004/03/05 13:02:31 bdelacretaz Exp $
 */
public abstract class AbstractDatatypeWidgetDefinitionBuilder extends AbstractWidgetDefinitionBuilder {
    
    /**
     * @return true if a selectionlist has actually been build.
     */
    protected boolean buildSelectionList(Element widgetElement, AbstractDatatypeWidgetDefinition widget) throws Exception {
        // FIXME: pass the manager to the definition as a side effect. Should be removed
        // when definition are managed like components.
        widget.service(this.serviceManager);

        Element selectionListElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "selection-list");
        if (selectionListElement != null) {
            // Get an appropriate list builder
            ServiceSelector builderSelector = (ServiceSelector)this.serviceManager.lookup(SelectionListBuilder.ROLE + "Selector");
            SelectionListBuilder builder = null;
            try {
                // listType can be null, meaning we will use the default selection list
                String listType = selectionListElement.getAttribute("type");
                if ("".equals(listType)) {
                    listType = null;
                }

                builder = (SelectionListBuilder)builderSelector.select(listType);
                SelectionList list = builder.build(selectionListElement, widget.getDatatype());
                widget.setSelectionList(list);
            } finally {
                if (builder != null) {
                    builderSelector.release(builder);
                }
                this.serviceManager.release(builderSelector);
            }

            return true;
        } else {
            return false;
        }
    }
}
