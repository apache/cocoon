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
package org.apache.cocoon.forms.formmodel;

import org.apache.avalon.framework.context.Context;

/**
 * A {@link FieldDefinition} for {@link CaptchaField}s.
 * 
 * @see http://www.captcha.net/
 * @version CVS $Id$
 */
public class CaptchaFieldDefinition extends FieldDefinition {
    
    private Context avalonContext;

    public CaptchaFieldDefinition(Context avalonContext) {
        this.avalonContext = avalonContext;
    }

    public Widget createInstance() {
        CaptchaField field = new CaptchaField(this, avalonContext);
        return field;
    }

}
