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

import org.apache.cocoon.forms.event.ValueChangedEvent;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;

/**
 * The {@link WidgetDefinition} part of a BooleanField widget, see {@link BooleanField} for more information.
 * 
 * @version $Id$
 */
public class BooleanFieldDefinition extends AbstractWidgetDefinition {
    private ValueChangedListener listener;
    
    private Boolean initialValue;
    
    private String trueParamValue = "true";

    public Widget createInstance() {
        return new BooleanField(this);
    }
    
    public void setInitialValue(Boolean value) {
        checkMutable();
        this.initialValue = value;
    }
    
    public Boolean getInitialValue() {
        return this.initialValue;
    }
    
    public void setTrueParamValue(String value) {
        checkMutable();
        this.trueParamValue = value;
    }
    
    /**
     * Get the parameter value that indicates a true value. Default
     * is "<code>true</code>".
     */
    public String getTrueParamValue() {
        return this.trueParamValue;
    }
    
    public void addValueChangedListener(ValueChangedListener listener) {
        checkMutable();
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
        
    public void setRequired(boolean required) {
        checkMutable();
        throw new UnsupportedOperationException("The property 'required' is not available on widgets of type booleanfield.");
    }    
}
