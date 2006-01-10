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

import org.apache.cocoon.forms.event.ActionEvent;
import org.apache.cocoon.forms.event.ActionListener;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;

/**
 * The {@link WidgetDefinition} part of a Action widget, see {@link Action} for more information.
 * 
 * @version $Id$
 */
public class ActionDefinition extends AbstractWidgetDefinition {
    private String actionCommand;
    private ActionListener listener;

    public void setActionCommand(String actionCommand) {
        this.actionCommand = actionCommand;
    }
    
    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
    	super.initializeFrom(definition);
    	
    	if(definition instanceof ActionDefinition) {
    		ActionDefinition other = (ActionDefinition)definition;
    		
    		this.actionCommand = other.actionCommand;
    		this.listener = other.listener;
    		
    	} else {
    		throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
    	}
    }

    public String getActionCommand() {
        return actionCommand;
    }

    public Widget createInstance() {
        return new Action(this);
    }

    public void addActionListener(ActionListener listener) {
        checkMutable();
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
