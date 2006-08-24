/*
 * Copyright 2006 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core.container.spring.avalon;

import java.util.Map;

import org.apache.cocoon.components.treeprocessor.ProcessorComponentInfo;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.HierarchicalBeanFactory;

/**
 * This spring factory bean adds the processor component info to the bean factory.
 *
 * @since 2.2
 * @version $Id$
 */
public class ProcessorComponentInfoFactoryBean
    implements FactoryBean, BeanFactoryAware {

    /** The bean factory. */
    protected BeanFactory beanFactory;

    protected Map data;

    protected ProcessorComponentInfo info;

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.beanFactory = factory;
    }

    protected void init()
    throws Exception {
        ProcessorComponentInfo parent = null;
        if ( this.beanFactory instanceof HierarchicalBeanFactory ) {
            BeanFactory parentFactory = ((HierarchicalBeanFactory)this.beanFactory).getParentBeanFactory();
            if ( parentFactory != null && parentFactory.containsBean(ProcessorComponentInfo.ROLE) ) {
                parent = (ProcessorComponentInfo)parentFactory.getBean(ProcessorComponentInfo.ROLE);
            }
        }
        this.info = new ProcessorComponentInfo(parent);
        if ( this.data != null ) {
            info.setData(data);
            info.lock();
        }
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return this.info;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return ProcessorComponentInfo.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    public Map getData() {
        return data;
    }

    public void setData(Map data) {
        this.data = data;
    }
}
