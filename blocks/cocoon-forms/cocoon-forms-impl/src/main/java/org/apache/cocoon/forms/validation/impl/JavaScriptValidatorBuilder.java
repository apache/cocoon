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

import org.apache.cocoon.forms.formmodel.WidgetDefinition;
import org.apache.cocoon.forms.util.JavaScriptHelper;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.cocoon.forms.validation.WidgetValidatorBuilder;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.mozilla.javascript.Function;
import org.w3c.dom.Element;

/**
 * Builds a JavaScript validator.
 * 
 * @see org.apache.cocoon.forms.validation.impl.JavaScriptValidator
 * @version $Id$
 */
public class JavaScriptValidatorBuilder 
    implements WidgetValidatorBuilder {

    private static final String[] ARG_NAMES = {"widget"};

    private ProcessInfoProvider processInfoProvider;
    /* (non-Javadoc)
     * @see org.apache.cocoon.forms.validation.ValidatorBuilder#build(org.apache.cocoon.forms.formmodel.WidgetDefinition, org.w3c.dom.Element)
     */
    public WidgetValidator build(Element element, WidgetDefinition definition) throws Exception {
            Function function = JavaScriptHelper.buildFunction(element, "validate", ARG_NAMES);

            return new JavaScriptValidator(this.processInfoProvider, function);
    }
    public void setProcessInfoProvider( ProcessInfoProvider processInfoProvider )
    {
        this.processInfoProvider = processInfoProvider;
    }
}
