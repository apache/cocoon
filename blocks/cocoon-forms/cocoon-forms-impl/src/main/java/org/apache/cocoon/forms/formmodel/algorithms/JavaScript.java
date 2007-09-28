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
package org.apache.cocoon.forms.formmodel.algorithms;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.cocoon.forms.datatype.Datatype;
import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm;
import org.apache.cocoon.forms.formmodel.Form;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.mozilla.javascript.Function;

/**
 * Javascript calculated field algorithm.
 * @see org.apache.cocoon.forms.formmodel.algorithms.JavaScriptBuilder
 * @version $Id$
 */
public class JavaScript implements CalculatedFieldAlgorithm {

    private Function jsfunction = null;
    private Set triggerWidgets = new HashSet();
    

    public Object calculate(Form form, Widget parent, Datatype datatype) {
        try {
            // FIXME we could make it convert to the correct datatype automatically, 
            // at least between different numbers types and eventually between numbers and strings.
            return JavaScriptHelper.callFunction(this.jsfunction, null, new Object[]{form, parent}, null);
        } catch (Exception e) {
            throw new RuntimeException("Error invoking JavaScript server side calculation", e);
        }        
    }

    public boolean isSuitableFor(Datatype dataType) {
        return true;
    }

    /**
     * @return Returns the jsfunction.
     */
    public Function getJsfunction() {
        return jsfunction;
    }
    /**
     * @param jsfunction The jsfunction to set.
     */
    public void setJsfunction(Function jsfunction) {
        this.jsfunction = jsfunction;
    }

    public void addTriggerWidget(String widgetname) {
        this.triggerWidgets.add(widgetname);
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm#getTriggerWidgets()
     */
    public Iterator getTriggerWidgets() {
        return this.triggerWidgets.iterator();
    }    
}
