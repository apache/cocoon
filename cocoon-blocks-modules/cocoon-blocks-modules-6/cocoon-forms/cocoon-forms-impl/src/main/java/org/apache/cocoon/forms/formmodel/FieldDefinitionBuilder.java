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

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.datatype.SelectionList;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link FieldDefinition}s.
 *
 * @version $Id$
 */
public class FieldDefinitionBuilder extends AbstractDatatypeWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement, WidgetDefinitionBuilderContext context)
    throws Exception {
        FieldDefinition definition = new FieldDefinition();
        setupDefinition(widgetElement, definition, context);

        definition.makeImmutable();
        return definition;
    }

    protected void setupDefinition(Element widgetElement, FieldDefinition definition, WidgetDefinitionBuilderContext context)
    throws Exception {
        super.setupDefinition(widgetElement, definition, context);

        // parse "@required"
        if (widgetElement.hasAttribute("required")) {
            definition.setRequired(DomHelper.getAttributeAsBoolean(widgetElement, "required", false));
        }

        // parse "@whitespace"
        Whitespace whitespace = Whitespace.getEnum(DomHelper.getAttribute(widgetElement, "whitespace", "trim"));
        if (whitespace == null) {
            throw new FormsException("Unknown value for 'whitespace' attribute; valid values are 'trim', 'trim-start', 'trim-end', and 'preserve'.",
                                     DomHelper.getLocationObject(widgetElement));
        }
        definition.setWhitespaceTrim(whitespace);

        SelectionList list = buildSelectionList(widgetElement, definition, "suggestion-list");
        if (list != null) {
            definition.setSuggestionList(list);
        }
    }
}
