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

import org.apache.cocoon.forms.event.ActionListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.log.DeprecationLogger;
import org.w3c.dom.Element;

/**
 * Builds {@link ActionDefinition}s.
 * 
 * @version $Id$
 */
public class ActionDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        ActionDefinition definition = new ActionDefinition();
        setupDefinition(widgetElement, definition);
        definition.makeImmutable();
        return definition;
    }
    
    protected void setupDefinition(Element widgetElement, ActionDefinition definition) throws Exception {
        super.setupDefinition(widgetElement, definition);

        setDisplayData(widgetElement, definition);

        // Get the "command" optional attribute
        String actionCommand = DomHelper.getAttribute(widgetElement, "command", null);
        
        // If unspecified, check the deprecated "action-command" deprecated attribute
        if (actionCommand == null) {
            actionCommand = DomHelper.getAttribute(widgetElement, "action-command", null);
            if (actionCommand != null) {
                DeprecationLogger.log("The 'action-command' attribute is deprecated and replaced by 'command', at " +
                    DomHelper.getLocation(widgetElement));
            }
        }
        
        definition.setActionCommand(actionCommand);

        Iterator iter = buildEventListeners(widgetElement, "on-action", ActionListener.class).iterator();
        while (iter.hasNext()) {
            definition.addActionListener((ActionListener)iter.next());
        }
    }
}
