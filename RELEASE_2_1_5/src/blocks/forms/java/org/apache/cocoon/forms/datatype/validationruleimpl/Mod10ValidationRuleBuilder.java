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
package org.apache.cocoon.forms.datatype.validationruleimpl;

import org.apache.cocoon.forms.datatype.ValidationRule;
import org.w3c.dom.Element;

/**
 * Builds {@link Mod10ValidationRule}s.
 * 
 * @version $Id: Mod10ValidationRuleBuilder.java,v 1.1 2004/03/09 10:34:10 reinhard Exp $
 */
public class Mod10ValidationRuleBuilder extends AbstractValidationRuleBuilder {
    public ValidationRule build(Element validationRuleElement) throws Exception {
        Mod10ValidationRule rule = new Mod10ValidationRule();
        buildFailMessage(validationRuleElement, rule);
        return rule;
    }
}
