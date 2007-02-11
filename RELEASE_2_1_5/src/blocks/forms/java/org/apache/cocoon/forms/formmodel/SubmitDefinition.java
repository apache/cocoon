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
 * Definition for a {@link Submit}.
 * 
 * @see SubmitDefinitionBuilder
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: SubmitDefinition.java,v 1.1 2004/03/09 10:33:50 reinhard Exp $
 */
public class SubmitDefinition extends ActionDefinition {
    
    private boolean validateForm;
    
    public void setValidateForm(boolean validateForm) {
        this.validateForm = validateForm;
    }
    
    public Widget createInstance() {
        return new Submit(this, validateForm);
    }
}
