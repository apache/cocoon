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
package org.apache.cocoon.woody.validation.impl;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.Request;
import org.apache.cocoon.woody.FormContext;
import org.apache.cocoon.woody.formmodel.Widget;
import org.apache.cocoon.woody.util.JavaScriptHelper;
import org.apache.cocoon.woody.validation.WidgetValidator;
import org.mozilla.javascript.Function;

/**
 * A {@link org.apache.cocoon.woody.validation.WidgetValidator} implemented as a JavaScript snippet.
 * This snippet must return a boolean value. If it returns <code>false</code>, it <strong>must</strong> have
 * set a validation error on the validated widget or one of its children.
 * <p>
 * The JavaScript snippet has the "this" variable set to the validated widget, and, if the form is used in a
 * flowscript, can use the flow's global values and fonctions and the <code>cocoon</code> object.
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptValidator.java,v 1.2 2004/03/05 13:02:35 bdelacretaz Exp $
 */
public class JavaScriptValidator implements WidgetValidator {
    
    private final Function function;
    private final Context avalonContext;
    
    public JavaScriptValidator(Context context, Function function) {
        this.function = function;
        this.avalonContext = context;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.validation.Validator#validate(org.apache.cocoon.woody.formmodel.Widget, org.apache.cocoon.woody.FormContext)
     */
    public final boolean validate(Widget widget, FormContext context) {

        Request request = ContextHelper.getRequest(this.avalonContext);

        Object result;
            
        try {
            result = JavaScriptHelper.callFunction(this.function, widget, new Object[] {widget}, request);
        } catch(RuntimeException re) {
            throw re; // rethrow
        } catch(Exception e) {
            throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
        }

        if (result == null) {
            throw new RuntimeException("Validation script did not return a value");
            
        } else if (result instanceof Boolean) {
            return ((Boolean)result).booleanValue();
            
        } else {
            throw new RuntimeException("Validation script returned an unexpected value of type " + result.getClass());
        }
    }
}
