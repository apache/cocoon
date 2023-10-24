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
package org.apache.cocoon.forms.formmodel.algorithms;

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm;
import org.apache.cocoon.forms.util.DomHelper;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.w3c.dom.Element;


/**
 * Builder for user custom {@link org.apache.cocoon.forms.formmodel.CalculatedFieldAlgorithm}s.
 * If the specified class is a {@link org.apache.cocoon.forms.formmodel.algorithms.AbstractBaseAlgorithm}
 * subclass, the build process will be delegated to
 *  {@link org.apache.cocoon.forms.formmodel.algorithms.AbstractBaseAlgorithmBuilder}.
 *
 * @version $Id$
 */
public class JavaAlgorithmBuilder extends AbstractBaseAlgorithmBuilder implements BeanFactoryAware{

    private BeanFactory beanFactory;
    
    public void setBeanFactory(BeanFactory beanFactory)
                                                  throws BeansException
    {
        this.beanFactory = beanFactory;        
    }

    public CalculatedFieldAlgorithm build(Element algorithmElement) throws Exception {

        // hard way deprecation
        if (DomHelper.getAttribute(algorithmElement, "class", null) != null) {
            throw new RuntimeException("The 'class' attribute is not supported anymore at "
                                       + DomHelper.getLocationObject( algorithmElement )
                                       + ". Use a 'ref' attribute to address a Spring bean");
        }
        
        String name = DomHelper.getAttribute(algorithmElement, "ref");
        try {
            Class clazz = beanFactory.getType(name);
            if (AbstractBaseAlgorithm.class.isAssignableFrom(clazz)) {
                AbstractBaseAlgorithm algorithm = (AbstractBaseAlgorithm)beanFactory.getBean( name );
                super.setup(algorithmElement, algorithm);
                return algorithm;
            } if (CalculatedFieldAlgorithm.class.isAssignableFrom(clazz)) {
                CalculatedFieldAlgorithm algorithm = (CalculatedFieldAlgorithm)beanFactory.getBean( name );
                return algorithm;
            } else {
                throw new FormsException("Spring bean " + name + " is not a " + CalculatedFieldAlgorithm.class.getName(), DomHelper.getLocationObject( algorithmElement ));
            }
        } catch(NoSuchBeanDefinitionException nsbde) {
            throw new FormsException("Spring bean " + name + " does not exist in Spring context", DomHelper.getLocationObject( algorithmElement ));
        } catch(BeansException be) {
            throw new FormsException("Spring bean " + name + " cannot be retrieved/instantiated", DomHelper.getLocationObject( algorithmElement ));
        }
    }

}
