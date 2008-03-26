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

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.formmodel.WidgetDefinition;
import org.apache.cocoon.forms.util.DomHelper;
import org.apache.cocoon.forms.validation.ConfigurableWidgetValidator;
import org.apache.cocoon.forms.validation.WidgetValidator;
import org.apache.cocoon.forms.validation.WidgetValidatorBuilder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.w3c.dom.Element;

/**
 * A {@link org.apache.cocoon.forms.validation.WidgetValidatorBuilder} that uses Spring beans.
 * <p>
 * The syntax for this validator is as follows :<br/>
 * <pre>
 *   &lt;java ref="spring-bean-id"/&gt;
 * </pre>
 *
 * @version $Id$
 */
public class JavaClassValidatorBuilder
    implements WidgetValidatorBuilder, BeanFactoryAware {
    
    private BeanFactory beanFactory;
    
    public void setBeanFactory( BeanFactory beanFactory )
                                                  throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    /**
     * @see org.apache.cocoon.forms.validation.WidgetValidatorBuilder#build(org.w3c.dom.Element, org.apache.cocoon.forms.formmodel.WidgetDefinition)
     */
    public WidgetValidator build(Element validationRuleElement, WidgetDefinition definition) 
    throws Exception {
        
        // hard way deprecation
        if (DomHelper.getAttribute(validationRuleElement, "class", null) != null) {
            throw new RuntimeException("The 'class' attribute is not supported anymore at "
                                       + DomHelper.getLocationObject( validationRuleElement )
                                       + ". Use a 'ref' attribute to address a Spring bean");
        }

        String name = DomHelper.getAttribute(validationRuleElement, "ref");

        try {
            Object validator = beanFactory.getBean( name );
            if (validator instanceof WidgetValidator) {
                if (validator instanceof ConfigurableWidgetValidator) {
                    ((ConfigurableWidgetValidator)validator).setConfiguration( validationRuleElement );
                }
                return (WidgetValidator)validator;
            } else {
                throw new FormsException("Spring bean " + name + " is not a " 
                                         + WidgetValidator.class.getName(), 
                                         DomHelper.getLocationObject( validationRuleElement ));
            }
        } catch (BeansException be) {
            throw new FormsException("Spring bean " + name + " does not exist in Spring context", 
                                     DomHelper.getLocationObject( validationRuleElement ));
        }
    }
}
