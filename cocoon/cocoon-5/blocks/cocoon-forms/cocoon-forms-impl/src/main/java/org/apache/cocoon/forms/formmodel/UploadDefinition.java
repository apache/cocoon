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

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.event.ValueChangedListener;
import org.apache.cocoon.forms.event.WidgetEventMulticaster;

/**
 * The definition of an upload widget.
 *
 * @version $Id$
 */
public class UploadDefinition extends AbstractWidgetDefinition {
    private ValueChangedListener listener;
    private boolean required;
    private String mimeTypes;

    public UploadDefinition() {
        this.mimeTypes = null;
        this.required = false;
    }

    public UploadDefinition(boolean required, String mimeTypes) {
        this.required = required;
        this.mimeTypes = mimeTypes;
    }

    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
        super.initializeFrom(definition);

        if (!(definition instanceof UploadDefinition)) {
            throw new FormsException("Ancestor definition " + definition.getClass().getName() + " is not an UploadDefinition.",
                                     getLocation());
        }

        UploadDefinition other = (UploadDefinition) definition;

        this.required = other.required;
        this.mimeTypes = other.mimeTypes;
        this.listener = WidgetEventMulticaster.add(this.listener, other.listener);
    }

    public void addMimeTypes(String types) {
        if(types != null) {
            if(mimeTypes == null)
                mimeTypes = types;
            else {
                if(mimeTypes.length()>0)
                    mimeTypes += ", ";
                mimeTypes += types;
            }
        }
    }

    public void setRequired(boolean required) {
        checkMutable();
        this.required = required;
    }

    public Widget createInstance() {
        return new Upload(this);
    }

    public boolean isRequired() {
        return required;
    }

    public String getMimeTypes() {
        return this.mimeTypes;
    }

    public void addValueChangedListener(ValueChangedListener listener) {
        checkMutable();
        this.listener = WidgetEventMulticaster.add(this.listener, listener);
    }

    public ValueChangedListener getValueChangedListener() {
        return this.listener;
    }

    public boolean hasValueChangedListeners() {
        return listener != null;
    }
}
