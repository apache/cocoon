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

/**
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: RowActionDefinition.java,v 1.2 2004/03/05 13:02:32 bdelacretaz Exp $
 */
public class RowActionDefinition extends ActionDefinition {
    
    public Widget createInstance() {
        return new RowAction(this);
    }
    
    public static class DeleteRowDefinition extends RowActionDefinition {
        public DeleteRowDefinition() {
            super.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent event) {
                    Repeater.RepeaterRow row = Repeater.getParentRow(event.getSourceWidget());
                    Repeater repeater = (Repeater)row.getParent();
                    repeater.removeRow(repeater.indexOf(row));
                }
            });
        }
    }
    
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
