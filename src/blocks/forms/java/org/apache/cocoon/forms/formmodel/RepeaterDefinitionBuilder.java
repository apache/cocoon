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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link RepeaterDefinition}s.
 * 
 * @version $Id: RepeaterDefinitionBuilder.java,v 1.1 2004/03/09 10:33:50 reinhard Exp $
 */
public class RepeaterDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element repeaterElement) throws Exception {
        
        int initialSize = DomHelper.getAttributeAsInteger(repeaterElement, "initial-size", 0);
        
        RepeaterDefinition repeaterDefinition = new RepeaterDefinition(initialSize);
        setLocation(repeaterElement, repeaterDefinition);
        setId(repeaterElement, repeaterDefinition);
        setDisplayData(repeaterElement, repeaterDefinition);
        setValidators(repeaterElement, repeaterDefinition);

        Element widgetsElement = DomHelper.getChildElement(repeaterElement, Constants.FD_NS, "widgets", true);
        // All child elements of the widgets element are widgets
        Element[] widgetElements = DomHelper.getChildElements(widgetsElement, Constants.FD_NS);
        for (int i = 0; i < widgetElements.length; i++) {
            WidgetDefinition widgetDefinition = buildAnotherWidgetDefinition(widgetElements[i]);
            repeaterDefinition.addWidgetDefinition(widgetDefinition);
        }

        return repeaterDefinition;
    }
}
