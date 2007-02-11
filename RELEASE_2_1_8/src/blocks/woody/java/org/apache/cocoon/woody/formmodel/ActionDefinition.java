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

import org.apache.cocoon.woody.event.ActionEvent;
import org.apache.cocoon.woody.event.ActionListener;
import org.apache.cocoon.woody.event.WidgetEventMulticaster;

/**
 * The {@link WidgetDefinition} part of a Action widget, see {@link Action} for more information.
 * 
 * @version $Id: ActionDefinition.java,v 1.6 2004/03/09 13:53:56 reinhard Exp $
 */
public class ActionDefinition extends AbstractWidgetDefinition {
    private String actionCommand;
    private ActionListener listener;

    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public Widget createInstance() {
        return new Action(this);
    }

    public void addActionListener(ActionListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }
    
    public void fireActionEvent(ActionEvent event) {
        if (this.listener != null) {
            this.listener.actionPerformed(event);
        }
    }

    public boolean hasActionListeners() {
        return this.listener != null;
    }
}
