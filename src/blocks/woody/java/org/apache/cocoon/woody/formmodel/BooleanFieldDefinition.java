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

import org.apache.cocoon.woody.event.WidgetEventMulticaster;
import org.apache.cocoon.woody.event.ValueChangedEvent;
import org.apache.cocoon.woody.event.ValueChangedListener;

/**
 * The {@link WidgetDefinition} part of a BooleanField widget, see {@link BooleanField} for more information.
 * 
 * @version $Id: BooleanFieldDefinition.java,v 1.6 2004/03/09 13:53:56 reinhard Exp $
 */
public class BooleanFieldDefinition extends AbstractWidgetDefinition {
    private ValueChangedListener listener;

    public Widget createInstance() {
        return new BooleanField(this);
    }
    
    public void addValueChangedListener(ValueChangedListener listener) {
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }
    
    public void fireValueChangedEvent(ValueChangedEvent event) {
        if (this.listener != null) {
            this.listener.valueChanged(event);
        }
    }

    public boolean hasValueChangedListeners() {
        return listener != null;
    }
}
