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

import org.apache.cocoon.forms.event.ActionEvent;
import org.apache.cocoon.forms.event.ActionListener;

/**
 * 
 * @version $Id$
 */
public class RowActionDefinition extends ActionDefinition {
    
    public Widget createInstance() {
        return new RowAction(this);
    }
    
    /**
     * Deletes the row containing this action. Action listeners, if any, are called <em>before</em>
     * the row is deleted.
     */
    public static class DeleteRowDefinition extends RowActionDefinition {
        
        public boolean hasActionListeners() {
            // We always want to be notified
            return true;
        }
        
        public void fireActionEvent(ActionEvent event) {
            // Call event listeners, if any (the row still exists)
            super.fireActionEvent(event);

            // and delete the row
            Repeater.RepeaterRow row = Repeater.getParentRow(event.getSourceWidget());
            Repeater repeater = (Repeater)row.getParent();
            repeater.removeRow(repeater.indexOf(row));
        }
    }
    
    /**
     * Moves up the row containing this action. Action listeners, if any, are called <em>after</em>
     * the row has been moved.
     */
    public static class MoveUpDefinition extends RowActionDefinition {
        public MoveUpDefinition() {
            super.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    Repeater.RepeaterRow row = Repeater.getParentRow(event.getSourceWidget());
                    Repeater repeater = (Repeater)row.getParent();
                    // Rotation: up in a table is left in a list!
                    repeater.moveRowLeft(repeater.indexOf(row));
                }
            });
        }
    }
    
    /**
     * Moves up the row containing this action. Action listeners, if any, are called <em>after</em>
     * the row has been moved.
     */
    public static class MoveDownDefinition extends RowActionDefinition {
        public MoveDownDefinition() {
            super.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    Repeater.RepeaterRow row = Repeater.getParentRow(event.getSourceWidget());
                    Repeater repeater = (Repeater)row.getParent();
                    // Rotation : down in a table is right in a list!
                    repeater.moveRowRight(repeater.indexOf(row));
                }
            });
        }
    }
    
    /**
     * Adds a row after the one containing this action. Action listeners, if any, are called <em>after</em>
     * the new row has been created.
     */
    public static class AddAfterDefinition extends RowActionDefinition {
        public AddAfterDefinition() {
            super.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    Repeater.RepeaterRow row = Repeater.getParentRow(event.getSourceWidget());
                    Repeater repeater = (Repeater)row.getParent();
                    repeater.addRow(repeater.indexOf(row)+1);
                }
            });
        }
    }
}
