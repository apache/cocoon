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

import org.apache.cocoon.xml.AttributesImpl;

/**
 * A submit is an action that exits of the current form.
 * 
 * @see SubmitDefinitionBuilder
 * @version $Id$
 */
public class Submit extends Action {

    private boolean validateForm;
    
    public Submit(ActionDefinition definition, boolean validateForm) {
        super(definition);
        this.validateForm = validateForm;
    }
    
    /**
     * @see org.apache.cocoon.forms.formmodel.AbstractWidget#getXMLElementAttributes()
     */
    public AttributesImpl getXMLElementAttributes() {
        AttributesImpl attrs = super.getXMLElementAttributes();
        attrs.addCDATAAttribute("validate", String.valueOf(validateForm));
        return attrs;
    }

    protected void handleActivate() {
        performAction();
        if (!validateForm) {
            // End the form processing now and don't redisplay the form.
            getForm().endProcessing(false);
        }
        // Otherwise let the normal processing flow continue.
    }
}
