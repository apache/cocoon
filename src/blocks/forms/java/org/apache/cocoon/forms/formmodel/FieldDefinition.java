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


/**
 * The {@link WidgetDefinition} part of a Field widget, see {@link Field} for more information.
 * 
 * @version $Id$
 */
public class FieldDefinition extends AbstractDatatypeWidgetDefinition {
    private boolean required;

    public Widget createInstance() {
        Field field = new Field(this);
        // Set the initial selection list, if any
        field.setSelectionList(getSelectionList());
        return field;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        checkMutable();
        this.required = required;
    }
}
