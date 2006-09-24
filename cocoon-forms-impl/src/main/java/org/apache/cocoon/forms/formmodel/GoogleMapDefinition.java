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
 * The {@link WidgetDefinition} part of a GoogleMap widget, see {@link GoogleMap} for more information.
 */
public class GoogleMapDefinition extends AbstractWidgetDefinition {
    
    private GoogleMapValue initialValue;
    

    public Widget createInstance() {
        return new GoogleMap(this);
    }
    
    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
    	super.initializeFrom(definition);
    	
    	if(definition instanceof GoogleMapDefinition) {
            GoogleMapDefinition other = (GoogleMapDefinition)definition;
    		
    		this.initialValue = other.initialValue;
    		
    	} else {
    		throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
    	}
    }
    
    public void setInitialValue(GoogleMapValue value) {
        checkMutable();
        this.initialValue = value;
    }
    
    public GoogleMapValue getInitialValue() {
        return this.initialValue;
    }
    
}
