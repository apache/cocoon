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

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * A basic Spring test case.
 */
public abstract class BaseSpringTest extends TestCase {

    /**
     * Spring {@link BeanFactory}.
     */
    protected BeanFactory factory;

    /**
     * Returns the Spring config file name.
     *
     * @return Config file name.
     */
    protected abstract String getSpringConfigFile();

    /**
     * Bring up Spring IoC container.
     */
    protected void setUp() throws Exception {
        super.setUp();
        ApplicationContext context = new ClassPathXmlApplicationContext(
                getClass().getResource(getSpringConfigFile()).toExternalForm());
        factory = context;
    }

    /**
     * Tear down disposes Spring container.
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        factory = null;
    }

}