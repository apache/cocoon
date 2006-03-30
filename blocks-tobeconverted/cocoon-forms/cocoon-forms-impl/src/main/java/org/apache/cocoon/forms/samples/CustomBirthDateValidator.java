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
package org.apache.cocoon.forms.samples;

import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.forms.validation.WidgetValidator;

/**
 * Example of a custom validator.  Check that the given date is a valid birth date, i.e. 
 * is at least 5 years before current date and no more than 100 years old.
 */
public class CustomBirthDateValidator implements WidgetValidator, LogEnabled, Contextualizable {

    private Logger logger = null;
    private Context context = null;
    
    
    public boolean validate(Widget widget) {
        Date birthDate = (Date) widget.getValue();
        if (logger.isDebugEnabled()) logger.debug("Validating date " + birthDate);
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(GregorianCalendar.YEAR, -5);
        Date maxDate = cal.getTime();
        cal.add(GregorianCalendar.YEAR, -95);
        Date minDate = cal.getTime();
        if (birthDate.after(maxDate) || birthDate.before(minDate)) {
            if (widget instanceof ValidationErrorAware) {
                ((ValidationErrorAware)widget).setValidationError(new ValidationError("Invalid birth date", false));
            }
            return false;
        }
        return true;
    }

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

}
