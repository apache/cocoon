/*
 * Copyright 1999-2005 The Apache Software Foundation.
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

import org.apache.avalon.framework.context.Context;

/**
 * A {@link FieldDefinition} for {@link CaptchaField}s.
 * 
 * @see http://www.captcha.net/
 * @version $Id$
 */
public class CaptchaFieldDefinition extends FieldDefinition {
    
    private Context avalonContext;
    private int length;

    public CaptchaFieldDefinition(Context avalonContext) {
        this.avalonContext = avalonContext;
    }
    
    /**
     * initialize this definition with the other, sort of like a copy constructor
     */
    public void initializeFrom(WidgetDefinition definition) throws Exception {
    	super.initializeFrom(definition);
    	
    	if(definition instanceof CaptchaFieldDefinition) {
    		CaptchaFieldDefinition other = (CaptchaFieldDefinition)definition;
    		
    		this.length = other.length;
    		
    	} else {
    		throw new Exception("Definition to inherit from is not of the right type! (at "+getLocation()+")");
    	}
    }

    public Widget createInstance() {
        CaptchaField field = new CaptchaField(this, avalonContext);
        return field;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        checkMutable();
        this.length = length;
    }

}
