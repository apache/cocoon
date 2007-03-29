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
package org.apache.cocoon;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.cocoon.configuration.MutableSettings;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.environment.mock.MockRequestAttributes;
import org.apache.cocoon.environment.mock.MockResponse;
import org.apache.cocoon.spring.configurator.impl.ServletContextFactoryBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * This class sets up all necessary environment information to implement
 * own test cases.
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class AbstractTestCase extends TestCase {

    private MockRequest request;
    private MockResponse response;
    private MockContext context;
    private Map objectmodel;

    private MockRequestAttributes requestAttributes;

    /** The bean factory. */
    private DefaultListableBeanFactory beanFactory;

    public final MockRequest getRequest() {
        return this.request;
    }

    public final MockResponse getResponse() {
        return this.response;
    }

    public final MockContext getContext() {
        return this.context;
    }

    public final Map getObjectModel() {
        return this.objectmodel;
    }

    /** Return the bean factory. */
    protected ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        // setup object model
        this.setUpObjectModel();

        // create bean factory
        this.createBeanFactory();
        // initialize bean factory
        this.initBeanFactory();

        // setup request attributes
        this.requestAttributes = new MockRequestAttributes(this.getRequest());
        RequestContextHolder.setRequestAttributes(this.requestAttributes);

        // setting up an webapplicationcontext is neccesarry to make spring believe
        // it runs in a servlet container. we initialize it with our current
        // bean factory to get consistent bean resolution behaviour
        this.setUpRootApplicationContext();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        if ( this.requestAttributes != null ) {
            this.requestAttributes.requestCompleted();
            this.requestAttributes = null;
        }
        RequestContextHolder.resetRequestAttributes();

        if( this.beanFactory != null ) {
            this.beanFactory.destroySingletons();
            this.beanFactory = null;
        }
        super.tearDown();
    }

    /**
     * Set up the object model.
     */
    protected void setUpObjectModel() {
        this.request = this.createRequest();
        this.response = this.createResponse();
        this.context = this.createContext();
        this.objectmodel = this.createObjectModel();

        this.objectmodel.put(ObjectModelHelper.REQUEST_OBJECT, this.getRequest());
        this.objectmodel.put(ObjectModelHelper.RESPONSE_OBJECT, this.getResponse());
        this.objectmodel.put(ObjectModelHelper.CONTEXT_OBJECT, this.getContext());        
    }

    protected void setUpRootApplicationContext() {
        // set up servlet context access first
        final ServletContextFactoryBean scfb = new ServletContextFactoryBean();
        scfb.setServletContext(this.getContext());

        WebApplicationContext staticWebApplicationContext = new MockWebApplicationContext(this.getBeanFactory(), this.getContext());
        this.getContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, staticWebApplicationContext);
    }

    protected void createBeanFactory() throws Exception {
        ClassPathResource cpr = new ClassPathResource(getClass().getName().replace('.', '/') + ".spring.xml");
        if(cpr.exists()) {
            this.beanFactory = new XmlBeanFactory(cpr);                
        } else {
            this.beanFactory = new DefaultListableBeanFactory();
        }
        this.addSettings();
    }

    protected void initBeanFactory() {
        this.beanFactory.preInstantiateSingletons();        
    }

    protected void addSettings() {
        RootBeanDefinition def = new RootBeanDefinition();
        def.setBeanClass(MutableSettings.class);
        def.setSingleton(true);
        def.setLazyInit(false);
        def.getConstructorArgumentValues().addIndexedArgumentValue(0, "test");
        def.getPropertyValues().addPropertyValue("workDirectory", System.getProperty("java.io.tmpdir"));
        def.getPropertyValues().addPropertyValue("cacheDirectory", System.getProperty("java.io.tmpdir"));
        BeanDefinitionHolder holder = new BeanDefinitionHolder(def, Settings.ROLE);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, this.beanFactory);
    }

    protected MockRequest createRequest() {
        return new MockRequest();
    }

    protected MockResponse createResponse() {
        return new MockResponse();
    }

    protected Map createObjectModel() {
        return new HashMap();
    }

    protected MockContext createContext() {
        return new MockContext();
    }
}
