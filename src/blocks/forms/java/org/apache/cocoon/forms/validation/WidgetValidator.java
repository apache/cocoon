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
package org.apache.cocoon.forms.validation;

import org.apache.cocoon.forms.FormContext;
import org.apache.cocoon.forms.formmodel.Widget;

/**
 * Validates a widget. Validation can mean lots of different things depending on the
 * actual widget and validator type, e.g. :
 * <li>
 * <ul>on fields, a validator will validate the field's value,</ul>
 * <ul>on repeaters, a validator can perform inter-row validation</ul>
 * </li>
 * <p>
 * A validator returns a boolean result indicating if validation was successful or not.
 * If not successful, the validator <code>must<code> set a {@link org.apache.cocoon.forms.validation.ValidationError}
 * on the validated widget or one of its children.
 * <p>
 * <em>Note:</em> It is important (although it cannot be explicitely forbidden) that a validator
 * does not consider widgets that are not the validated widgets itself or its children, as this
 * may lead to inconsistencies in the form model because of the way form validation occurs (depth-first
 * traversal of the widget tree).
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: WidgetValidator.java,v 1.2 2004/03/09 11:31:10 joerg Exp $
 */
public interface WidgetValidator {
    
    /**
     * Validate a widget.
     * 
     * @param widget the widget to validate
     * @param context the form context
     * @return <code>true</code> if validation was successful. If not, the validator must have set
     *         a {@link ValidationError} on the widget or one of its children.
     */
    boolean validate(Widget widget, FormContext context);
}
