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
package org.apache.cocoon.forms.validation.impl;

import org.apache.cocoon.forms.datatype.validationruleimpl.Mod10ValidationRuleBuilder;
import org.apache.cocoon.forms.formmodel.WidgetDefinition;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.cocoon.forms.validation.WidgetValidatorBuilder;
import org.w3c.dom.Element;

/**
 * Adapter for {@link org.apache.cocoon.woody.datatype.validationruleimpl.Mod10ValidationRuleBuilder}
 * 
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: Mod10ValidatorBuilder.java,v 1.1 2004/03/09 10:33:57 reinhard Exp $
 */
public class Mod10ValidatorBuilder extends Mod10ValidationRuleBuilder implements WidgetValidatorBuilder {

    public WidgetValidator build(Element validationRuleElement, WidgetDefinition definition) throws Exception {
        return new ValidationRuleValidator(super.build(validationRuleElement));
    }
}
