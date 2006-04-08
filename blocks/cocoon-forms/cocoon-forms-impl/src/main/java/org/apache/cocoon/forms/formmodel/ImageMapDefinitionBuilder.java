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

import org.apache.cocoon.forms.FormsConstants;
import org.apache.cocoon.forms.event.ActionListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.w3c.dom.Element;

/**
 * Builds {@link ImageMapDefinition}s.
 * 
 * @version $Id$
 * @since 2.1.8
 */
public class ImageMapDefinitionBuilder extends AbstractWidgetDefinitionBuilder {
    
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
    	ImageMapDefinition definition = new ImageMapDefinition();
        setupDefinition(widgetElement, definition);
        definition.makeImmutable();
        return definition;
    }
    
    protected void setupDefinition(Element widgetElement, ImageMapDefinition definition) throws Exception {
        super.setupDefinition(widgetElement, definition);

        setDisplayData(widgetElement, definition);

        // Get the "command" optional attribute
        String actionCommand = DomHelper.getAttribute(widgetElement, ImageMap.COMMAND_AT, null);
        definition.setActionCommand(actionCommand);

        Iterator iter = buildEventListeners(widgetElement, ImageMap.ONACTION_EL, ActionListener.class).iterator();
        while (iter.hasNext()) {
            definition.addActionListener((ActionListener)iter.next());
        }

        // Sets image map source
        Element imageURIEl= DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, ImageMap.VALUE_EL);
        if ( imageURIEl != null ) {
            definition.setImageURI(DomHelper.getElementText(imageURIEl));
       	} 
    }
}
