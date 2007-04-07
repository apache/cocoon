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
package org.apache.cocoon.forms.validation.impl;

import java.util.Map;

import org.apache.avalon.framework.CascadingRuntimeException;
import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.excalibur.xml.sax.XMLizable;
import org.mozilla.javascript.Function;

/**
 * A {@link org.apache.cocoon.forms.validation.WidgetValidator} implemented as a JavaScript snippet.
 * <p>
 * This snippet must return a value which can be of different types. The only way to indicate
 * successfull validation is to return a boolean <code>true</code> value.
 * <p>
 * To indicate validation error, a number of result types are possible:
 * <ul>
 * <li>A boolean <code>false</code>: the validator <strong>must</strong> then have
 *     set a validation error on the validated widget or one of its children.</li>
 * <li>A {@link ValidationError}: this error is then set on the validated widget.</li>
 * <li>A <code>String</code>: a validation error using that string as a non-i18nized message is
 *     then set on the validated widget</li>
 * <li>An <code>XMLizable</code> such as {@link org.apache.cocoon.forms.util.I18nMessage}: this
 *     xmlizable is used to build a validation error that is set on the validated widget.</li>
 * </ul>
 * <p>
 * The JavaScript snippet has the "this" and "widget" variables set to the validated widget, and, if the form
 * is used in a flowscript, can use the flow's global values and fonctions and the <code>cocoon</code> object.
 * 
 * @version $Id$
 */
public class JavaScriptValidator implements WidgetValidator {
    
    private final Function function;
    private final Context avalonContext;
    
    public JavaScriptValidator(Context context, Function function) {
        this.function = function;
        this.avalonContext = context;
    }

    public final boolean validate(Widget widget) {

        Map objectModel = ContextHelper.getObjectModel(this.avalonContext);

        Object result;
            
        try {
            result = JavaScriptHelper.callFunction(this.function, widget, new Object[] {widget}, objectModel);
        } catch(RuntimeException re) {
            throw re; // rethrow
        } catch(Exception e) {
            throw new CascadingRuntimeException("Error invoking JavaScript event handler", e);
        }

        if (result == null) {
            throw new RuntimeException("Validation script did not return a value");
        }

        if (result instanceof Boolean) {
            return ((Boolean)result).booleanValue();
        }
        
        if (result instanceof ValidationError) {
            // Set the validation error on the widget
            ((ValidationErrorAware)widget).setValidationError((ValidationError)result);
            return false;
        }

        if (result instanceof String) {
            // Set a non-i18n error on the current widget
            ((ValidationErrorAware)widget).setValidationError(new ValidationError((String)result, false));
            return false;
        }

        if (result instanceof XMLizable) {
            // Set a xmlizable error (e.g. I18nMessage) on the current widget
            ((ValidationErrorAware)widget).setValidationError(new ValidationError((XMLizable)result));
            return false;
        }

        throw new RuntimeException("Validation script returned an unexpected value of type " + result.getClass());
    }
}
