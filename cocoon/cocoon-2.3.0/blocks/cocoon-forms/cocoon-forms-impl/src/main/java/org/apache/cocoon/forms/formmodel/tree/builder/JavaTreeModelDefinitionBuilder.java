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
package org.apache.cocoon.forms.formmodel.tree.builder;

import org.apache.cocoon.forms.FormsException;
import org.apache.cocoon.forms.formmodel.tree.JavaTreeModelDefinition;
import org.apache.cocoon.forms.formmodel.tree.TreeModel;
import org.apache.cocoon.forms.formmodel.tree.TreeModelDefinition;
import org.apache.cocoon.forms.util.DomHelper;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.w3c.dom.Element;

/**
 * Builds a {@link TreeModelDefinition} based on an Spring bean subclassing {@TreeModel}.
 *
 * @version $Id$
 */
public class JavaTreeModelDefinitionBuilder implements TreeModelDefinitionBuilder, BeanFactoryAware {

    private BeanFactory beanFactory;
    
    public void setBeanFactory( BeanFactory beanFactory )
                                                  throws BeansException
    {
        this.beanFactory = beanFactory;
    }

    public TreeModelDefinition build(Element treeModelElement) throws Exception {

        // hard way deprecation
        if (DomHelper.getAttribute(treeModelElement, "class", null) != null) {
            throw new RuntimeException("The 'class' attribute is not supported anymore at "
                                       + DomHelper.getLocationObject( treeModelElement )
                                       + ". Use a 'ref' attribute to address a Spring bean");
        }
        
        String beanRefId = DomHelper.getAttribute(treeModelElement, "ref");
        try {
            Class clazz = beanFactory.getType(beanRefId);
            if (!TreeModel.class.isAssignableFrom(clazz)) {
                throw new FormsException("Spring Bean '" + beanRefId + "' doesn't implement TreeModel.",
                                         DomHelper.getLocationObject(treeModelElement));
            }
        } catch(NoSuchBeanDefinitionException nsbde) {
            throw new FormsException("Spring Bean '" + beanRefId + "' doesn't exists.",
                                     DomHelper.getLocationObject(treeModelElement));
        }

        JavaTreeModelDefinition definition = new JavaTreeModelDefinition();
        definition.setBeanFactory( beanFactory );
        definition.setModelBeanRef( beanRefId );

        return definition;
    }

}
