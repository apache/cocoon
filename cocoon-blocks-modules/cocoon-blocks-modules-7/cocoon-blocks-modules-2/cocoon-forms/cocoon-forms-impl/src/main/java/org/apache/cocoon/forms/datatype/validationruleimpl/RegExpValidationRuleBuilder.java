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
package org.apache.cocoon.forms.datatype.validationruleimpl;

import org.apache.cocoon.forms.datatype.ValidationRule;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.MalformedPatternException;
import org.w3c.dom.Element;

/**
 * Builds {@link RegExpValidationRule}s.
 *
 * @version $Id$
 */
public class RegExpValidationRuleBuilder extends AbstractValidationRuleBuilder {

    public ValidationRule build(Element validationRuleElement) throws Exception {
        RegExpValidationRule rule = new RegExpValidationRule();

        String regexp = DomHelper.getAttribute(validationRuleElement, "pattern");
        buildFailMessage(validationRuleElement, rule);

        Perl5Compiler compiler = new Perl5Compiler();
        Pattern pattern;
        try {
            pattern = compiler.compile(regexp, Perl5Compiler.READ_ONLY_MASK);
        } catch (MalformedPatternException e) {
            throw new Exception("Invalid regular expression at " + DomHelper.getLocation(validationRuleElement) + ": " + e.getMessage());
        }
        rule.setPattern(regexp, pattern);

        buildFailMessage(validationRuleElement, rule);

        return rule;
    }

}
