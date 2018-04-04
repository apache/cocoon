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

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextHolder;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.environment.ObjectModelHelper;
import org.apache.cocoon.environment.mock.MockContext;
import org.apache.cocoon.environment.mock.MockRequest;
import org.apache.cocoon.environment.mock.MockRequestAttributes;
import org.apache.cocoon.environment.mock.MockResponse;
import org.apache.cocoon.processing.ProcessInfoProvider;
import org.apache.cocoon.processing.impl.MockProcessInfoProvider;
import org.apache.cocoon.spring.configurator.impl.ServletContextFactoryBean;
import org.apache.cocoon.spring.configurator.impl.SettingsBeanFactoryPostProcessor;

import junit.framework.TestCase;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;

/**
 * This class sets up all necessary environment information to implement own test cases.
 *
 * @version $Id$
 * @since 2.2
 */
public abstract class AbstractTestCase extends TestCase {

    private MockRequest request;

    private MockResponse response;

    private MockContext context;

    private Map<String, Object> objectmodel;

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

    public final Map<String, Object> getObjectModel() {
        return this.objectmodel;
    }

    /** Return the bean factory. */
    protected ConfigurableListableBeanFactory getBeanFactory() {
        return this.beanFactory;
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // setup object model
        setUpObjectModel();

        // create bean factory
        createBeanFactory();

        // initialize bean factory
        initBeanFactory();

        // setup request attributes
        this.requestAttributes = new MockRequestAttributes(this.getRequest());
        RequestContextHolder.setRequestAttributes(this.requestAttributes);

        // setting up an webapplicationcontext is neccesarry to make spring believe
        // it runs in a servlet container. we initialize it with our current
        // bean factory to get consistent bean resolution behaviour
        setUpRootApplicationContext();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception {
        if (this.requestAttributes != null) {
            this.requestAttributes.requestCompleted();
            this.requestAttributes = null;
        }

        RequestContextHolder.resetRequestAttributes();

        if (this.beanFactory != null) {
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

        this.objectmodel.put(ObjectModelHelper.REQUEST_OBJECT, getRequest());
        this.objectmodel.put(ObjectModelHelper.RESPONSE_OBJECT, getResponse());
        this.objectmodel.put(ObjectModelHelper.CONTEXT_OBJECT, getContext());
    }

    protected void setUpRootApplicationContext() {
        // set up servlet context access first
        final ServletContextFactoryBean scfb = new ServletContextFactoryBean();
        scfb.setServletContext(getContext());

        MockWebApplicationContext ctx = new MockWebApplicationContext(this.beanFactory, getContext());
        getContext().setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ctx);
        
        // COCOON-2374
        // needed to avoid problem with 
        // java.lang.IllegalStateException: org.apache.cocoon.MockWebApplicationContext@7d898981 has not been refreshed yet
        ctx.refresh();
    }

    protected void createBeanFactory() throws Exception {
        this.beanFactory = new DefaultListableBeanFactory();

        ClassPathResource cpr = new ClassPathResource(getClass().getName().replace('.', '/') + ".spring.xml");
        if (cpr.exists()) {
            XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this.beanFactory);
            reader.loadBeanDefinitions(cpr);
        }

        File base = new File("target");
        if (!base.exists()) {
            base = new File(System.getProperty("java.io.tmpdir"));
        }
        File workDir = new File(base, "cocoon-files");
        System.setProperty("org.apache.cocoon.work.directory", workDir.getAbsolutePath());
        System.setProperty("org.apache.cocoon.cache.directory", new File(workDir, "cache-dir").getAbsolutePath());

        addSettings();
        addProcessingInfoProvider();
        configureBeans();
    }

    protected void initBeanFactory() {
        this.beanFactory.preInstantiateSingletons();
    }

    protected void addSettings() {
        RootBeanDefinition def = new RootBeanDefinition();
        def.setBeanClass(SettingsBeanFactoryPostProcessor.class);
        def.setScope(BeanDefinition.SCOPE_SINGLETON);
        def.setLazyInit(false);
        def.setInitMethodName("init");
        BeanDefinitionHolder holder = new BeanDefinitionHolder(def, Settings.ROLE);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, this.beanFactory);
    }

    protected void addProcessingInfoProvider() {
        RootBeanDefinition def = new RootBeanDefinition();
        def.setBeanClass(MockProcessInfoProvider.class);
        def.setScope(BeanDefinition.SCOPE_SINGLETON);
        def.setLazyInit(false);
        def.getPropertyValues().addPropertyValue("objectModel", getObjectModel());
        def.getPropertyValues().addPropertyValue("request", new MockProcessInfoProvider.StubRequest(getRequest()));
        def.getPropertyValues().addPropertyValue("response", new MockProcessInfoProvider.StubResponse(getResponse()));
        def.getPropertyValues().addPropertyValue("servletContext", getContext());
        BeanDefinitionHolder holder = new BeanDefinitionHolder(def, ProcessInfoProvider.ROLE);
        BeanDefinitionReaderUtils.registerBeanDefinition(holder, this.beanFactory);
    }

    protected void configureBeans() {
        SettingsBeanFactoryPostProcessor processor =
                (SettingsBeanFactoryPostProcessor) this.beanFactory.getBean("&" + Settings.ROLE);
        processor.postProcessBeanFactory(beanFactory);
    }

    protected MockRequest createRequest() {
        return new MockRequest();
    }

    protected MockResponse createResponse() {
        return new MockResponse();
    }

    protected Map<String, Object> createObjectModel() {
        return new HashMap<String, Object>();
    }

    protected MockContext createContext() {
        return new MockContext();
    }
}
