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

import java.util.Iterator;

import org.w3c.dom.Element;
import org.apache.cocoon.woody.util.DomHelper;
import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.woody.event.ValueChangedListener;

/**
 * Builds {@link MultiValueFieldDefinition}s.
 * 
 * @version $Id: MultiValueFieldDefinitionBuilder.java,v 1.11 2004/03/09 13:53:56 reinhard Exp $
 */
public class MultiValueFieldDefinitionBuilder extends AbstractDatatypeWidgetDefinitionBuilder {
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        MultiValueFieldDefinition definition = new MultiValueFieldDefinition();
        setLocation(widgetElement, definition);
        setId(widgetElement, definition);
        setDisplayData(widgetElement, definition);
        setValidators(widgetElement, definition);

        Element datatypeElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "datatype");
        if (datatypeElement == null)
            throw new Exception("A nested datatype element is required for the widget specified at " + DomHelper.getLocation(widgetElement));

        Datatype datatype = datatypeManager.createDatatype(datatypeElement, true);
        definition.setDatatype(datatype);

        boolean hasSelectionList = buildSelectionList(widgetElement, definition);
        if (!hasSelectionList)
            throw new Exception("Error: multivaluefields always require a selectionlist at " + DomHelper.getLocation(widgetElement));

        boolean required = DomHelper.getAttributeAsBoolean(widgetElement, "required", false);
        definition.setRequired(required);

        Iterator iter = buildEventListeners(widgetElement, "on-value-changed", ValueChangedListener.class).iterator();
        while (iter.hasNext()) {
            definition.addValueChangedListener((ValueChangedListener)iter.next());
        }
        return definition;
    }
}
