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

import org.apache.cocoon.woody.Constants;
import org.apache.cocoon.woody.datatype.Datatype;
import org.apache.cocoon.woody.event.ValueChangedListener;
import org.apache.cocoon.woody.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {FieldDefinition}s.
 *
 * @version $Id: FieldDefinitionBuilder.java,v 1.10 2004/03/05 13:02:31 bdelacretaz Exp $
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

        Element datatypeElement = DomHelper.getChildElement(widgetElement, Constants.WD_NS, "datatype");
        if (datatypeElement == null) {
            throw new Exception("A nested datatype element is required for the widget at " +
                                DomHelper.getLocation(widgetElement));
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

        boolean required = DomHelper.getAttributeAsBoolean(widgetElement, "required", false);
        fieldDefinition.setRequired(required);
    }
}
