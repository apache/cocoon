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

import org.apache.cocoon.forms.datatype.SelectionList;

/**
 * The {@link WidgetDefinition} part of a Field widget, see {@link Field} for more information.
 * 
 * @version $Id$
 */
public class FieldDefinition extends AbstractDatatypeWidgetDefinition {
    private boolean required;
    private SelectionList suggestionList;

    public Widget createInstance() {
        Field field = new Field(this);
        return field;
    }
    
    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
        if(!(definition instanceof FieldDefinition)) {
            throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
        }
        super.initializeFrom(definition);
        
        FieldDefinition other = (FieldDefinition)definition;
        
        this.required = other.required;
        
        if (suggestionList == null) {
            suggestionList = other.getSuggestionList();
        }
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        checkMutable();
        this.required = required;
    }

    public SelectionList getSuggestionList() {
        return this.suggestionList;
    }

    public void setSuggestionList(SelectionList list) {
        this.suggestionList = list;
    }
}
