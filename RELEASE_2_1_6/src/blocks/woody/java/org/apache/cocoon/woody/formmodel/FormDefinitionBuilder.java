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

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link FormDefinition}s.
 * 
 * @version $Id: FormDefinitionBuilder.java,v 1.11 2004/03/09 13:53:56 reinhard Exp $
 */
public class FormDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element formElement) throws Exception {
        FormDefinition formDefinition = new FormDefinition();
        setLocation(formElement, formDefinition);
        formDefinition.setId("");
        setDisplayData(formElement, formDefinition);
        setValidators(formElement, formDefinition);

        Element widgetsElement = DomHelper.getChildElement(formElement, Constants.WD_NS, "widgets", true);
        // All child elements of the widgets element are widgets
        Element[] widgetElements = DomHelper.getChildElements(widgetsElement, Constants.WD_NS);
        for (int i = 0; i < widgetElements.length; i++) {
            Element widgetElement = widgetElements[i];
            WidgetDefinition widgetDefinition = buildAnotherWidgetDefinition(widgetElement);
            formDefinition.addWidgetDefinition(widgetDefinition);
        }

        formDefinition.resolve();

        return formDefinition;
    }
}
