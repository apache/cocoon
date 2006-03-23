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

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.avalon.framework.thread.ThreadSafe;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.forms.formmodel.WidgetDefinition;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.cocoon.forms.validation.WidgetValidatorBuilder;
import org.apache.cocoon.util.ClassUtils;
import org.w3c.dom.Element;

/**
 * A {@link org.apache.cocoon.forms.validation.WidgetValidatorBuilder} that creates java classes.
 * <p>
 * The syntax for this validator is as follows :<br/>
 * <pre>
 *   &lt;java class="com.my.SuperValidator"/&gt;
 * </pre>
 *
 * @version $Id$
 */
public class JavaClassValidatorBuilder implements WidgetValidatorBuilder, ThreadSafe, Serviceable, LogEnabled, Contextualizable  {

    private ServiceManager manager;
    private Logger logger = null;
    private Context context = null;
    

    public void service(ServiceManager manager) throws ServiceException {
        this.manager = manager;
    }

    public WidgetValidator build(Element validationRuleElement, WidgetDefinition definition) throws Exception {
        String name = DomHelper.getAttribute(validationRuleElement, "class");

        Object validator = ClassUtils.newInstance(name);
        if (validator instanceof WidgetValidator) {
            LifecycleHelper.setupComponent(validator, logger, context, manager, null);
            return (WidgetValidator)validator;
        } else {
            throw new Exception("Class " + validator.getClass() + " is not a " + WidgetValidator.class.getName());
        }
    }
    
    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

}
