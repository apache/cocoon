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
import org.apache.cocoon.util.Deprecation;
import org.w3c.dom.Element;

/**
 * Builds a <code>&lt;fd:repeater-action/&gt;</code>
 *
 * <p>Three actions are defined:
 * <ul>
 * <li>
 *   <code>&lt;fd:repeater-action id="add" command="add-row"
 *   repeater="repeater-id"/&gt;</code>: when activated, adds a row to the
 *   sibling repeater named "repeater-id".
 * </li>
 * <li>
 *   <code>&lt;fd:repeater-action id="rm" command="delete-rows"
 *   repeater="repeater-id" select="select-id"/&gt;</code>: removes the
 *   selected rows from the sibling repeater named "repeater-id". The
 *   selected rows are identified by the boolean field "select-id" present
 *   in each row.
 * </li>
 * <li>
 *   <code>&lt;fd:repeater-action id="insert" command="insert-rows"
 *   repeater="repeater-id" select="select-id"/&gt;</code>: inserts rows before
 *   the selected rows from the sibling repeater named "repeater-id". The
 *   selected rows are identified by the boolean field "select-id" present
 *   in each row.
 * </li>
 * </ul>
 *
 * @version $Id$
 */
public class RepeaterActionDefinitionBuilder extends AbstractWidgetDefinitionBuilder {

    public WidgetDefinition buildWidgetDefinition(Element widgetElement) throws Exception {
        // Get the "command" attribute
        String actionCommand = DomHelper.getAttribute(widgetElement, "command", null);

        // If unspecified, check the deprecated "action-command" deprecated attribute
        if (actionCommand == null) {
            actionCommand = DomHelper.getAttribute(widgetElement, "action-command", null);
            if (actionCommand != null) {
                Deprecation.logger.info("The 'action-command' attribute is deprecated and replaced by 'command', at " +
                                        DomHelper.getLocation(widgetElement));
            }
        }
        if (actionCommand == null) {
            throw new Exception("Missing attribute 'command' at " + DomHelper.getLocation(widgetElement));
        }


        RepeaterActionDefinition definition = createDefinition(widgetElement, actionCommand);
        super.setupDefinition(widgetElement, definition);
        setDisplayData(widgetElement, definition);

        definition.setActionCommand(actionCommand);

        // Warn of the mis-named 'on-action' that existed initially
        Element buggyOnActivate = DomHelper.getChildElement(widgetElement, FormsConstants.DEFINITION_NS, "on-activate", false);
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

    protected RepeaterActionDefinition createDefinition(Element element, String actionCommand) throws Exception {

        String repeater = DomHelper.getAttribute(element, "repeater");
        if ("delete-rows".equals(actionCommand)) {
            String select = DomHelper.getAttribute(element, "select");
            return new RepeaterActionDefinition.DeleteRowsActionDefinition(repeater, select);

        } else if ("add-row".equals(actionCommand)) {
            int insertRows = DomHelper.getAttributeAsInteger(element,"number-of-rows",1);
            return new RepeaterActionDefinition.AddRowActionDefinition(repeater,insertRows);

        } else if ("insert-rows".equals(actionCommand)) {
            String select = DomHelper.getAttribute(element, "select");
            return new RepeaterActionDefinition.InsertRowsActionDefinition(repeater, select);

        } else if ("page-first".equals(actionCommand)) {
            return new RepeaterActionDefinition.ChangePageActionDefinition(repeater, RepeaterActionDefinition.ChangePageActionDefinition.FIRST);

        } else if ("page-prev".equals(actionCommand)) {
            return new RepeaterActionDefinition.ChangePageActionDefinition(repeater, RepeaterActionDefinition.ChangePageActionDefinition.PREV);

        } else if ("page-next".equals(actionCommand)) {
            return new RepeaterActionDefinition.ChangePageActionDefinition(repeater, RepeaterActionDefinition.ChangePageActionDefinition.NEXT);

        } else if ("page-last".equals(actionCommand)) {
            return new RepeaterActionDefinition.ChangePageActionDefinition(repeater, RepeaterActionDefinition.ChangePageActionDefinition.LAST);

        } else if ("page-custom".equals(actionCommand)) {
            return new RepeaterActionDefinition.ChangePageActionDefinition(repeater, RepeaterActionDefinition.ChangePageActionDefinition.CUSTOM);

        } else {
            throw new Exception("Unknown repeater action '" + actionCommand + "' at " + DomHelper.getLineLocation(element));
        }
    }
}
