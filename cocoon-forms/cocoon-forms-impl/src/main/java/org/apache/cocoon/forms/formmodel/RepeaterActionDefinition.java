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

/**
 * Abstract repeater action. Subclasses will typically just self-add an
 * event handler that will act on the repeater.
 * 
 * @see RepeaterActionDefinitionBuilder
 * @version $Id$
 */
public abstract class RepeaterActionDefinition extends ActionDefinition {

    private String name = null;
    
    /**
     * Builds an action whose target repeater is the parent of this widget
     */
    public RepeaterActionDefinition() {
    }
    
    /**
     * Builds an action whose target is a sibling of this widget
     * @param repeaterName the name of the repeater
     */
    public RepeaterActionDefinition(String repeaterName) {
        this.name = repeaterName;
    }
    
    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
    	super.initializeFrom(definition);
    	
    	if(definition instanceof RepeaterActionDefinition) {
    		RepeaterActionDefinition other = (RepeaterActionDefinition)definition;
    		
    		this.name = other.name;
    		
    	} else {
    		throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
    	}
    }

    public Widget createInstance() {
        return new RepeaterAction(this);
    }
    
    /**
     * Get the name of the repeater on which to act. If <code>null</code>, the repeater
     * is the parent of the current widget (i.e. actions are in repeater rows). Otherwise,
     * the repeater is a sibling of the current widget.
     * 
     * @return the repeater name (can be <code>null</code>).
     */
    public String getRepeaterName() {
        return this.name;
    }
    
    //---------------------------------------------------------------------------------------------

    /**
     * The definition of a repeater action that deletes the selected rows of a sibling repeater.
     * <p>
     * The action listeners attached to this action, if any, are called <em>before</em> the rows
     * are actually removed
     */
    public static class DeleteRowsActionDefinition extends RepeaterActionDefinition {

        private String selectName;

        public DeleteRowsActionDefinition(String repeaterName, String selectName) {
            super(repeaterName);
            this.selectName = selectName;
        }

        /**
         * initialize this definition with the other, sort of like a copy constructor
         */
        public void initializeFrom(WidgetDefinition definition) throws Exception {
        	super.initializeFrom(definition);
        	
        	if(definition instanceof DeleteRowsActionDefinition) {
        		DeleteRowsActionDefinition other = (DeleteRowsActionDefinition)definition;
        		
        		this.selectName = other.selectName;
        		
        	} else {
        		throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
        	}
        }
        
        public boolean hasActionListeners() {
            // we always want to be notified
            return true;
        }

        public void fireActionEvent(ActionEvent event) {
            // Call action listeners, if any
            super.fireActionEvent(event);

            // and actually delete the rows
            Repeater repeater = ((RepeaterAction)event.getSource()).getRepeater();
            for (int i = repeater.getSize() - 1; i >= 0; i--) {
                Repeater.RepeaterRow row = repeater.getRow(i);
                if (Boolean.TRUE.equals(row.getChild(this.selectName).getValue())) {
                    repeater.removeRow(i);
                }
            }
        }
    }

    //---------------------------------------------------------------------------------------------

    /**
     * The definition of a repeater action that adds a row to a sibling repeater.
     */
    public static class AddRowActionDefinition extends RepeaterActionDefinition {
        
        public AddRowActionDefinition(String repeaterName) {
            super(repeaterName);
            
            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Repeater repeater = ((RepeaterAction)event.getSource()).getRepeater();
                    repeater.addRow();
                }
            });
        }
    }

    //---------------------------------------------------------------------------------------------

    /**
     * The definition of a repeater action that insert rows before the selected rows in a sibling repeater,
     * or at the end of the repeater if no row is selected.
     */
    public static class InsertRowsActionDefinition extends RepeaterActionDefinition {

        protected String selectName;

        /**
         * initialize this definition with the other, sort of like a copy constructor
         */
        public void initializeFrom(WidgetDefinition definition) throws Exception {
        	super.initializeFrom(definition);

        	if(definition instanceof InsertRowsActionDefinition) {
        		InsertRowsActionDefinition other = (InsertRowsActionDefinition)definition;

        		this.selectName = other.selectName;

        	} else {
        		throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
        	}
        }

        public InsertRowsActionDefinition(String repeaterName, String selectWidgetName) {
            super(repeaterName);
            this.selectName = selectWidgetName;

            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Repeater repeater = ((RepeaterAction)event.getSource()).getRepeater();
                    boolean foundSelection = false;
                    for (int i = repeater.getSize() - 1; i >= 0; i--) {
                        Repeater.RepeaterRow row = repeater.getRow(i);
                        Widget selectWidget = row.getChild(selectName);
                        if (Boolean.TRUE.equals(selectWidget.getValue())) {
                            // Add a row
                            repeater.addRow(i);
                            foundSelection = true;
                        }
                    }

                    if (!foundSelection) {
                        // Add a row at the end
                        repeater.addRow();
                    }
                }
            });
        }
    }
}
