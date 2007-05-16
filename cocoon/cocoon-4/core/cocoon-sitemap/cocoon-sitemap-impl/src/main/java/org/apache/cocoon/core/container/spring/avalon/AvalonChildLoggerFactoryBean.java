/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.avalon.framework.logger.Logger;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.HierarchicalBeanFactory;

/**
 * Spring factory bean to setup a child Avalon logger.
 *
 * @since 2.2
 * @version $Id$
 */
public class AvalonChildLoggerFactoryBean
    implements FactoryBean, BeanFactoryAware {

    /** Logger (we use the same logging mechanism as Spring!) */
    protected final Log log = LogFactory.getLog(getClass());

    /** The bean factory. */
    protected BeanFactory beanFactory;

    protected Logger logger;

    /** The logging category. */
    protected String category;

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.beanFactory = factory;
    }

    protected void init()
    throws Exception {
        // get parent factory
        final BeanFactory parentFactory = ((HierarchicalBeanFactory)this.beanFactory).getParentBeanFactory();
        final Logger parentLogger = (Logger)parentFactory.getBean(AvalonUtils.LOGGER_ROLE);
        if ( this.category == null ) {
            this.logger = parentLogger;
        } else {
            this.logger = parentLogger.getChildLogger(this.category);
        }
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return this.logger;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return Logger.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
