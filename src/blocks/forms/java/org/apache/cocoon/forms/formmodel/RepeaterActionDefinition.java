/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.binding.BindingException;
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

        if (!(definition instanceof RepeaterActionDefinition)) {
            throw new FormsException("Ancestor definition " + definition.getClass().getName() + " is not a RepeaterActionDefinition.",
                                     getLocation());
        }

        RepeaterActionDefinition other = (RepeaterActionDefinition) definition;

        this.name = other.name;
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

            if (!(definition instanceof DeleteRowsActionDefinition)) {
                throw new FormsException("Ancestor definition " + definition.getClass().getName() + " is not a DeleteRowsActionDefinition.",
                                         getLocation());
            }

            DeleteRowsActionDefinition other = (DeleteRowsActionDefinition) definition;

            this.selectName = other.selectName;
        }

        public boolean hasActionListeners() {
            // we always want to be notified
            return true;
        }

        public void fireActionEvent(ActionEvent event) {
            // Call action listeners, if any
            super.fireActionEvent(event);

            Repeater repeater = ((RepeaterAction) event.getSource()).getRepeater();

            // and actually delete the rows
            for (int i = repeater.getSize() - 1; i >= 0; i--) {
                Repeater.RepeaterRow row = repeater.getRow(i);
                if (Boolean.TRUE.equals(row.getChild(this.selectName).getValue())) {
                    repeater.removeRow(i);
                }
            }

            if (repeater instanceof EnhancedRepeater) {
                try {
                    ((EnhancedRepeater) repeater).refreshPage();
                } catch (BindingException e) {
                    throw new CascadingRuntimeException("Error refreshing repeater page", e);
                }
            }

        }
    }

    //---------------------------------------------------------------------------------------------

    /**
     * The definition of a repeater action that adds a row to a sibling repeater.
     */
    public static class AddRowActionDefinition extends RepeaterActionDefinition {
        private int insertRows;

        public AddRowActionDefinition(String repeaterName, int insertRows) {
            super(repeaterName);
            this.insertRows = insertRows;

            addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Repeater repeater = ((RepeaterAction)event.getSource()).getRepeater();
                    if (repeater instanceof EnhancedRepeater) {
                    	try {
                            ((EnhancedRepeater) repeater).goToPage(((EnhancedRepeater) repeater).getMaxPage());
                        } catch (BindingException e) {
							throw new CascadingRuntimeException("Error switching page", e);
						}
                    }
                    for (int i = 0; i < AddRowActionDefinition.this.insertRows; i++) {
                        repeater.addRow();
                    }
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

            if (!(definition instanceof InsertRowsActionDefinition)) {
                throw new FormsException("Ancestor definition " + definition.getClass().getName() + " is not an InsertRowsActionDefinition.",
                                         getLocation());
            }

            InsertRowsActionDefinition other = (InsertRowsActionDefinition) definition;

            this.selectName = other.selectName;
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
    
    public static class SortActionDefinition extends RepeaterActionDefinition {
    	protected String field = null;
    	
        public SortActionDefinition(String repeaterName, String field) {
            super(repeaterName);
            this.field = field;
            
            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Repeater repeater = ((RepeaterAction)event.getSource()).getRepeater();
                    if (repeater instanceof EnhancedRepeater) {
                    	EnhancedRepeater erep = (EnhancedRepeater) repeater;
                        try {
                            if (repeater.validate()) {
                            	erep.sortBy(SortActionDefinition.this.field);
                            }
                        } catch (Exception e) {
                            throw new CascadingRuntimeException("Error switching page", e);
                        }
                    }
                }
            });
            
        }
    }

  
    public static class ChangePageActionDefinition extends RepeaterActionDefinition {

       protected int method;
       
       public static final int FIRST = 0; 
       public static final int PREV = 1;
       public static final int NEXT = 2;
       public static final int LAST = 3;
       public static final int CUSTOM = 4;

        /**
         * initialize this definition with the other, sort of like a copy constructor
         */
        public void initializeFrom(WidgetDefinition definition) throws Exception {
            super.initializeFrom(definition);
            if(definition instanceof ChangePageActionDefinition) {
                ChangePageActionDefinition other = (ChangePageActionDefinition)definition;
                this.method = other.method;
            } else {
                throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
            }
        }

        public ChangePageActionDefinition(String repeaterName, int m) {
            super(repeaterName);
            
            this.method = m;
            
            this.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    Repeater repeater = ((RepeaterAction)event.getSource()).getRepeater();
                    if (repeater instanceof EnhancedRepeater) {
                    	EnhancedRepeater erep = (EnhancedRepeater) repeater;
                        int page = erep.getCurrentPage();
                        if (method == FIRST) {
                            page = 0;
                        } else if (method == PREV && page > 0) {
                            page = erep.getCurrentPage() - 1;
                        } else if (method == NEXT && page < erep.getMaxPage()) {
                            page = erep.getCurrentPage() + 1;
                        } else if (method == LAST) {
                            page = erep.getMaxPage();
                        } else if (method == CUSTOM) {
                            page = erep.getCustomPageWidgetValue();
                        } else {
                            return;
                        }
                        try {
                            if (repeater.validate()) {
                            	erep.goToPage(page);
                            }
                        } catch (Exception e) {
                            throw new CascadingRuntimeException("Error switching page", e);
                        }
                    } 
                }
            });
        }
    }
    
}
