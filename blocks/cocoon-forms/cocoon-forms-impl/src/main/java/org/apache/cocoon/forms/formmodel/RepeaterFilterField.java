/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.apache.cocoon.forms.formmodel;

import org.apache.cocoon.forms.binding.BindingException;
import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;

/**
 * @version $Id$
 */
public class RepeaterFilterField extends Field {

	private EnhancedRepeater repeater;
	private String field;

	public RepeaterFilterField(RepeaterFilterFieldDefinition fieldDefinition) {
		super(fieldDefinition);
		this.field = fieldDefinition.getField();
	}

    public void initialize() {
        super.initialize();
        String name = ((RepeaterFilterFieldDefinition) getDefinition()).getRepeaterName();
        Widget w = getParent().lookupWidget(name);
        if (w == null) {
            throw new IllegalArgumentException("Cannot find repeater named " + name);
        }
        if (!(w instanceof EnhancedRepeater)) {
            throw new IllegalArgumentException("The repeater named " + name + " is not an enhanced repeater");
        }
        this.repeater = (EnhancedRepeater) w;

        addValueChangedListener(new ValueChangedListener() {
            public void valueChanged(ValueChangedEvent event) {
                if (repeater.validate()) {
                    try {
                        repeater.setFilter(field, event.getNewValue());
                    } catch (BindingException e) {
                        throw new RuntimeException("Error setting filter", e);
                    }
                }
            }
        });
    }

}
