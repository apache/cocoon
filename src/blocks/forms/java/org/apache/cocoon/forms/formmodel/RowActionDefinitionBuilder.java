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
import org.apache.cocoon.forms.event.ActionListener;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.util.Deprecation;
import org.w3c.dom.Element;

/**
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version $Id$
 */
public class RowActionDefinitionBuilder extends AbstractWidgetDefinitionBuilder {
    
    
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        // Get the "command" attribute
        String actionCommand = DomHelper.getAttribute(widgetElement, "command", null);
        
        // If unspecified, check the deprecated "action-command" deprecated attribute
        if (actionCommand == null) {
            actionCommand = DomHelper.getAttribute(widgetElement, "action-command", null);
            if (actionCommand != null) {
                Deprecation.logger.warn("The 'action-command' attribute is deprecated and replaced by 'command', at " +
                    DomHelper.getLocation(widgetElement));
            }
        }
        if (actionCommand == null) {
            throw new Exception("Missing attribute 'command' at " + DomHelper.getLocation(widgetElement));
        }

        RowActionDefinition definition = createDefinition(widgetElement, actionCommand);
        super.setupDefinition(widgetElement, definition);
        setDisplayData(widgetElement, definition);

        definition.setActionCommand(actionCommand);

        // Warn of the mis-named 'on-action' that existed initially
        Element buggyOnActivate = DomHelper.getChildElement(widgetElement, Constants.DEFINITION_NS, "on-activate", false);
        if (buggyOnActivate != null) {
            throw new Exception("Use 'on-action' instead of 'on-activate' on row-action at " +
                DomHelper.getLocation(buggyOnActivate));
        }

        Iterator iter = buildEventListeners(widgetElement, "on-action", ActionListener.class).iterator();
        while (iter.hasNext()) {
            definition.addActionListener((ActionListener)iter.next());
        }

        definition.makeImmutable();
        return definition;
    }
    
    protected RowActionDefinition createDefinition(Element element, String actionCommand) throws Exception {

        if ("delete".equals(actionCommand)) {
            return new RowActionDefinition.DeleteRowDefinition();

        } else if ("add-after".equals(actionCommand)) {
            return new RowActionDefinition.AddAfterDefinition();

        } else if ("move-up".equals(actionCommand)) {
            return new RowActionDefinition.MoveUpDefinition();

        } else if ("move-down".equals(actionCommand)) {
            return new RowActionDefinition.MoveDownDefinition();

        } else {
            throw new Exception("Unknown repeater row action '" + actionCommand + "' at " + DomHelper.getLineLocation(element));
        }
    }
}

