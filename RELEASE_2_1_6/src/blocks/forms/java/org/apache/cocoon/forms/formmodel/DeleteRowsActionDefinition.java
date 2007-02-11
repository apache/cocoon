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

/**
 * The definition for a repeater action that deletes the selected rows of a sibling repeater.
 * <p>
 * The action listeners attached to this action, if any, are called <em>before</em> the rows
 * are actually removed
 *
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public class DeleteRowsActionDefinition extends RepeaterActionDefinition {

    private String selectName;

    public DeleteRowsActionDefinition(String repeaterName, String selectName) {
        super(repeaterName);
        this.selectName = selectName;
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
            if (Boolean.TRUE.equals(row.getChild(DeleteRowsActionDefinition.this.selectName).getValue())) {
                repeater.removeRow(i);
            }
        }
    }
}
