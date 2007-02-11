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
package org.apache.cocoon.woody.validation.impl;

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.woody.formmodel.WidgetDefinition;
import org.apache.cocoon.woody.util.JavaScriptHelper;
import org.apache.cocoon.woody.validation.WidgetValidator;
import org.apache.cocoon.woody.validation.WidgetValidatorBuilder;
import org.mozilla.javascript.Function;
import org.w3c.dom.Element;

/**
 * Builds a JavaScript validator.
 * 
 * @see org.apache.cocoon.woody.validation.impl.JavaScriptValidator
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: JavaScriptValidatorBuilder.java,v 1.2 2004/03/05 13:02:35 bdelacretaz Exp $
 */
public class JavaScriptValidatorBuilder implements WidgetValidatorBuilder, Contextualizable, ThreadSafe {
    
    private Context avalonContext;
    
    private static final String[] ARG_NAMES = {"widget"};

    /* (non-Javadoc)
     * @see org.apache.avalon.framework.context.Contextualizable#contextualize(org.apache.avalon.framework.context.Context)
     */
    public void contextualize(Context context) throws ContextException {
        this.avalonContext = context;
    }

    /* (non-Javadoc)
     * @see org.apache.cocoon.woody.validation.ValidatorBuilder#build(org.apache.cocoon.woody.formmodel.WidgetDefinition, org.w3c.dom.Element)
     */
    public WidgetValidator build(Element element, WidgetDefinition definition) throws Exception {
            Function function = JavaScriptHelper.buildFunction(element, ARG_NAMES);

            return new JavaScriptValidator(this.avalonContext, function);
    }
}
