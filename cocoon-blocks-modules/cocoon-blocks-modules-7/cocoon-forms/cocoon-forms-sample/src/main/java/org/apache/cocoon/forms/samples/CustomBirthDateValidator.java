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
package org.apache.cocoon.forms.samples;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Example of a custom validator.  Check that the given date is a valid birth date, i.e. 
 * is at least 5 years before current date and no more than 100 years old.
 */
public class CustomBirthDateValidator
    implements WidgetValidator {

    private static Log LOG = LogFactory.getLog( CustomBirthDateValidator.class );
    
    /**
     * @see org.apache.cocoon.forms.validation.WidgetValidator#validate(org.apache.cocoon.forms.formmodel.Widget)
     */
    public boolean validate(Widget widget) {
        Date birthDate = (Date) widget.getValue();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Validating date " + birthDate);
        }
        GregorianCalendar cal = new GregorianCalendar();
        cal.add(Calendar.YEAR, -5);
        Date maxDate = cal.getTime();
        cal.add(Calendar.YEAR, -95);
        Date minDate = cal.getTime();
        if (birthDate.after(maxDate) || birthDate.before(minDate)) {
            if (widget instanceof ValidationErrorAware) {
                ((ValidationErrorAware)widget).setValidationError(new ValidationError("Invalid birth date", false));
            }
            return false;
        }
        return true;
    }
}
