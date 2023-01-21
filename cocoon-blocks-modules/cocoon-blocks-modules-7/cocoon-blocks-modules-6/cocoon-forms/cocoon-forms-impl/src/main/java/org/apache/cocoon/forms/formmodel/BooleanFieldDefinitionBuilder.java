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

import java.util.Iterator;

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link BooleanFieldDefinition}s.
 * 
 * @version $Id$
 */
public final class BooleanFieldDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement, WidgetDefinitionBuilderContext context)
    throws Exception {
        BooleanFieldDefinition definition = new BooleanFieldDefinition();
        setupDefinition(widgetElement, definition, context);
        setDisplayData(widgetElement, definition);

        Iterator i = buildEventListeners(widgetElement, "on-value-changed", ValueChangedListener.class).iterator();
        while (i.hasNext()) {
            definition.addValueChangedListener((ValueChangedListener) i.next());
        }

        // Initial value
        Element initialValueElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "initial-value", false);
        if (initialValueElement != null) {
            Boolean initialValue = Boolean.valueOf(DomHelper.getElementText(initialValueElement));
            definition.setInitialValue(initialValue);
        }
        
        // Parameter value for true
        Element trueParamElement = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "true-param-value", false);
        if (trueParamElement != null) {
            definition.setTrueParamValue(DomHelper.getElementText(trueParamElement));
        }

        definition.makeImmutable();
        return definition;
    }
}
