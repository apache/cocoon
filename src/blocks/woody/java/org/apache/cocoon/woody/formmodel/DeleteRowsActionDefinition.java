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
package org.apache.cocoon.woody.formmodel;

import org.apache.cocoon.woody.event.ActionEvent;
import org.apache.cocoon.woody.event.ActionListener;

/**
 * The definition for a repeater action that deletes the selected rows of a sibling repeater.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id$
 */
public class DeleteRowsActionDefinition extends RepeaterActionDefinition {
    
    private String selectName;
    
    public DeleteRowsActionDefinition(String repeaterName, String selectName) {
        super(repeaterName);
        this.selectName = selectName;
        
        this.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Repeater repeater = ((RepeaterAction)event.getSource()).getRepeater();
                for (int i = repeater.getSize() - 1; i >= 0; i--) {
                    Repeater.RepeaterRow row = repeater.getRow(i);
                    if (Boolean.TRUE.equals(row.getWidget(DeleteRowsActionDefinition.this.selectName).getValue())) {
                        repeater.removeRow(i);
                    }
                }
            }
        });
    }
}
