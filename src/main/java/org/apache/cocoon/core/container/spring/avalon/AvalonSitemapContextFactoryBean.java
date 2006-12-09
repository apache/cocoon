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

import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.cocoon.Constants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.HierarchicalBeanFactory;

/**
 * This factory bean creates a context for a sitemap.
 * It is part of the Spring bridge for Avalon integration.
 *
 * @since 2.2
 * @version $Id$
 */
public class AvalonSitemapContextFactoryBean
    implements FactoryBean, BeanFactoryAware {

    /** The Avalon context. */
    protected Context context;

    /** The bean factory. */
    protected BeanFactory beanFactory;

    /** Environment uri prefix. */
    protected String uriPrefix;

    public void setUriPrefix(String uriPrefix) {
        this.uriPrefix = uriPrefix;
    }

    /**
     * @see org.springframework.beans.factory.BeanFactoryAware#setBeanFactory(org.springframework.beans.factory.BeanFactory)
     */
    public void setBeanFactory(BeanFactory factory) throws BeansException {
        this.beanFactory = factory;
    }

    /**
     * Create the Avalon context object.
     * @throws Exception
     */
    protected void init()
    throws Exception {
        final BeanFactory parentBeanFactory = ((HierarchicalBeanFactory)this.beanFactory).getParentBeanFactory();
        // create new Avalon context
        final DefaultContext appContext = new DefaultContext((Context)parentBeanFactory.getBean(AvalonUtils.CONTEXT_ROLE));
        appContext.put(Constants.CONTEXT_ENV_PREFIX, this.uriPrefix);

        this.context = appContext;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return this.context;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return Context.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }
}
