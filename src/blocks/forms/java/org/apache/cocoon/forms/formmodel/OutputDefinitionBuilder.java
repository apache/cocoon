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

import org.w3c.dom.Element;
import org.apache.cocoon.forms.Constants;
import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.util.DomHelper;

/**
 * Builds {@link OutputDefinition}s.
 * 
 * @version $Id: OutputDefinitionBuilder.java,v 1.2 2004/03/09 13:08:45 cziegeler Exp $
 */
public class OutputDefinitionBuilder extends AbstractDatatypeWidgetDefinitionBuilder {
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        OutputDefinition definition = new OutputDefinition();
        setLocation(widgetElement, definition);
        setId(widgetElement, definition);

        Element datatypeElement = DomHelper.getChildElement(widgetElement, Constants.DEFINITION_NS, "datatype");
        if (datatypeElement == null)
            throw new Exception("A nested datatype element is required for the widget specified at " + DomHelper.getLocation(widgetElement));

        Datatype datatype = datatypeManager.createDatatype(datatypeElement, false);
        definition.setDatatype(datatype);

        setDisplayData(widgetElement, definition);

        return definition;
    }
}
