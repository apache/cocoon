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
package org.apache.cocoon.forms.validation.impl;

import java.util.Map;

import org.apache.avalon.framework.context.Context;
import org.apache.cocoon.components.ContextHelper;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.Session;
import org.apache.cocoon.forms.formmodel.CaptchaField;
import org.apache.cocoon.forms.formmodel.Widget;
import org.apache.cocoon.forms.validation.ValidationError;
import org.apache.cocoon.forms.validation.ValidationErrorAware;
import org.apache.cocoon.forms.validation.WidgetValidator;


/**
 * A {@link org.apache.cocoon.forms.validation.WidgetValidator} that relies on a CAPTCHA
 * test.
 * 
 * @see http://www.captcha.net/
 * @version $Id$
 */
public class CaptchaValidator implements WidgetValidator {

    private final Context avalonContext;

    public CaptchaValidator(Context avalonContext) {
        this.avalonContext = avalonContext;
    }

    public boolean validate(Widget widget) {
        if (! (widget instanceof ValidationErrorAware)) {
            // Invalid widget type
            throw new IllegalArgumentException("Widget '" + widget.getRequestParameterName() + "' is not ValidationErrorAware");
        }
        Map objectModel = ContextHelper.getObjectModel(this.avalonContext);
        Session session = ObjectModelHelper.getRequest(objectModel).getSession(false);
        if (session == null) {
            throw new RuntimeException("No session associated with request.");
        }
        if (session.getAttribute(CaptchaField.SESSION_ATTR_PREFIX + widget.getId()) == null) {
            throw new RuntimeException("No CAPTCHA attribute associated with widget " + widget.getId());
        }
        boolean result = widget.getValue() != null && widget.getValue().equals(session.getAttribute(CaptchaField.SESSION_ATTR_PREFIX + widget.getId()));
        if (! result) {
            ((ValidationErrorAware) widget).setValidationError(new ValidationError("general.captcha-mismatch"));
        }
        return result;
    }
}
