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

import org.apache.cocoon.forms.FormContext;

/**
 * An action that acts on a repeater.
 * 
 * @see RepeaterActionDefinitionBuilder
 * @version $Id$
 */
public class RepeaterAction extends Action {
    
    private Repeater repeater;
    

    public RepeaterAction(RepeaterActionDefinition definition) {
        super(definition);
    }
    
    /**
     * Get the repeater on which this action acts.
     */
    public Repeater getRepeater() {
        if (this.repeater == null) {
            String name = ((RepeaterActionDefinition)getDefinition()).getRepeaterName();
            Widget widget;
            if (name != null) {
                // Get the corresponding sibling
                widget = ((ContainerWidget)getParent()).getChild(name);
            } else {
                // Get the grand-parent (parent is the repeater row).
                widget = getParent().getParent();
            }
         
            if (widget == null || !(widget instanceof Repeater)) {
                throw new RuntimeException(name != null ?
                    "Cannot find sibling repeater named '" + name + "'." :
                    "Parent widget is not a repeater");
            }
            
            this.repeater = (Repeater)widget;
        }
        
        return this.repeater;
    }
    
    public static class Move extends RepeaterAction {
        private int from;
        private int to;
        
        public Move(RepeaterActionDefinition definition) {
            super(definition);
        }
        
        public void readFromRequest(FormContext formContext) {
            String fullName = getFullName();
            String fromStr = formContext.getRequest().getParameter(fullName + ".from");
            if (fromStr != null) {
                from = Integer.parseInt(fromStr);
                to = Integer.parseInt(formContext.getRequest().getParameter(fullName + ".to"));
            } else {
                from = -1;
                to = -1;
            }
            super.readFromRequest(formContext);
        }
        
        public int getFrom() {
            return from;
        }
        
        public int getTo() {
            return to;
        }
    }
}
