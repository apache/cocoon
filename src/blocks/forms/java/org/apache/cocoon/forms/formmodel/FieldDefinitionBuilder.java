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

import java.util.Iterator;

import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {FieldDefinition}s.
 *
 * @version $Id: FieldDefinitionBuilder.java,v 1.4 2004/06/15 07:33:44 sylvain Exp $
 */
public class FieldDefinitionBuilder extends AbstractDatatypeWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        FieldDefinition fieldDefinition = new FieldDefinition();
        buildWidgetDefinition(fieldDefinition, widgetElement);
        return fieldDefinition;
    }

    protected void buildWidgetDefinition(FieldDefinition fieldDefinition, Element widgetElement) throws Exception {
        setLocation(widgetElement, fieldDefinition);
        setId(widgetElement, fieldDefinition);

        Element datatypeElement = DomHelper.getChildElement(widgetElement, Constants.DEFINITION_NS, "datatype");
        if (datatypeElement == null) {
            throw new Exception("A nested datatype element is required for the widget " 
                                + widgetElement.getTagName() + " with id \"" + fieldDefinition.getId()
                                + "\" at " + DomHelper.getLocation(widgetElement));
        }

        Datatype datatype = datatypeManager.createDatatype(datatypeElement, false);
        fieldDefinition.setDatatype(datatype);

        buildSelectionList(widgetElement, fieldDefinition);

        Iterator iter = buildEventListeners(widgetElement, "on-value-changed", ValueChangedListener.class).iterator();
        while (iter.hasNext()) {
            fieldDefinition.addValueChangedListener((ValueChangedListener)iter.next());
        }

        setDisplayData(widgetElement, fieldDefinition);
        setValidators(widgetElement, fieldDefinition);
        setCreateListeners(widgetElement, fieldDefinition);

        boolean required = DomHelper.getAttributeAsBoolean(widgetElement, "required", false);
        fieldDefinition.setRequired(required);
    }
}
