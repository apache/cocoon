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
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Abstract base class for container widget builders.
 *
 * @version $Id$
 */
public abstract class AbstractContainerDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    protected void setupContainer(Element element,
                                  String widgetsElementName,
                                  AbstractContainerDefinition containerDefinition,
                                  WidgetDefinitionBuilderContext containerContext)
    throws Exception {

        Element widgetsElement = DomHelper.getChildElement(element, FormsConstants.DEFINITION_NS, widgetsElementName, false);
        // If its not there, ignore it. Just means that there are no new widgets
        if (widgetsElement == null) {
            return;
        }

        // All child elements of the widgets element are widgets
        Element[] widgetElements = DomHelper.getChildElements(widgetsElement, FormsConstants.DEFINITION_NS);

        for (int i = 0; i < widgetElements.length; i++) {
            Element widgetElement = widgetElements[i];

            WidgetDefinitionBuilderContext context = new WidgetDefinitionBuilderContext(containerContext);
            context.setSuperDefinition(null);

            String newId = DomHelper.getAttribute(widgetElement, "extends", null);
            if (newId != null) {
                WidgetDefinition def;
                if ((def = context.getLocalLibrary().getDefinition(newId)) != null) {
                    context.setSuperDefinition(def);
                } else if ((def = containerDefinition.getWidgetDefinition(newId)) != null) {
                    context.setSuperDefinition(def);
                }
                // throw new FormsException("Widget to inherit from (" + newId + ") not found!",
                //                          DomHelper.getLocationObject(element));
            }

            WidgetDefinition definition = buildAnotherWidgetDefinition(widgetElement, context);
            if (definition != null) {
                containerDefinition.addWidgetDefinition(definition);
            }
        }
    }
}
