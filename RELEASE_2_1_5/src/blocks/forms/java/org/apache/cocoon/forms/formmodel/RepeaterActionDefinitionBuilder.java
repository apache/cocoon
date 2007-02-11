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
import org.w3c.dom.Element;

/**
 * Builds a <code>&lt;wd:repeater-action/></code>
 * <p>
 * Two actions are defined :
 * <ul>
 * <li><code>&lt;wd:repeater-action id="add" action-command="add-row" repeater="repeater-id"/></code> :
 *   when activated, adds a row to the sibling repeater named "repeater-id".
 * </li>
 * <li><code>&lt;wd:repeater-action id="rm" action-command="delete-rows" repeater="repeater-id"
 *   select="select-id"/></code> : removes the selected rows from the sibling repeater named "repeater-id".
 *   The selected rows are identified by the boolean field "select-id" present in each row.
 * </ul>
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: RepeaterActionDefinitionBuilder.java,v 1.1 2004/03/09 10:33:49 reinhard Exp $
 */
public class RepeaterActionDefinitionBuilder extends AbstractWidgetDefinitionBuilder {
    
    
    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        String actionCommand = DomHelper.getAttribute(widgetElement, "action-command");
        RepeaterActionDefinition definition = createDefinition(widgetElement, actionCommand);
        setLocation(widgetElement, definition);
        setId(widgetElement, definition);
        setDisplayData(widgetElement, definition);
        setValidators(widgetElement, definition);

        definition.setActionCommand(actionCommand);

        Iterator iter = buildEventListeners(widgetElement, "on-activate", ActionListener.class).iterator();
        while (iter.hasNext()) {
            definition.addActionListener((ActionListener)iter.next());
        }

        return definition;
    }
    
    protected RepeaterActionDefinition createDefinition(Element element, String actionCommand) throws Exception {
        
        if ("delete-rows".equals(actionCommand)) {
            String repeater = DomHelper.getAttribute(element, "repeater");
            String select = DomHelper.getAttribute(element, "select");
            return new DeleteRowsActionDefinition(repeater, select);

        } else if ("add-row".equals(actionCommand)) {
            String repeater = DomHelper.getAttribute(element, "repeater");
            return new AddRowActionDefinition(repeater);
            
        } else {
            throw new Exception("Unknown repeater action '" + actionCommand + "' at " + DomHelper.getLineLocation(element));
        }
    }
}
