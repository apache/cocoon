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

/**
 * A submit is an action that exits of the current form.
 * 
 * @see SubmitDefinitionBuilder
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: Submit.java,v 1.4 2004/03/05 13:02:32 bdelacretaz Exp $
 */
public class Submit extends Action {

    private boolean validateForm;
    
    public Submit(ActionDefinition definition, boolean validateForm) {
        super(definition);
        setLocation(definition.getLocation());
        this.validateForm = validateForm;
    }
    
    protected void handleActivate() {
        if (!validateForm) {
            // End the form processing now and don't redisplay the form.
            getForm().endProcessing(false);
        }
        // Otherwise let the normal processing flow continue.
    }
}
