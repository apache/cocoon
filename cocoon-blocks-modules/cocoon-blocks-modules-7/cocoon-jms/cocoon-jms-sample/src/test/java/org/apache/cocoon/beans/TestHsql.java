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
package org.apache.cocoon.beans;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

import junit.framework.TestCase;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This test verifies correct startup of various components (Spring beans).
 */
public class TestHsql extends TestCase {

    /**
     * Spring config file.
     */
    private static final String SPRING_CONFIG = "spring-hsql.xml";

    private static final String DATA_SOURCE = "dataSource";

    private static final String SELECT_FROM_USER = "SELECT * FROM USER";

    /**
     * Spring {@link ApplicationContext}.
     */
    protected BeanFactory factory;

    /**
     * Constructor initializing Spring.
     */
    public TestHsql() {
        ApplicationContext context = new ClassPathXmlApplicationContext(this.getClass().getResource(SPRING_CONFIG)
                .toExternalForm());
        this.factory = context;
    }

    /**
     * Place a simple HSQLDB query to ensure, DB is up.
     * 
     * @throws Exception If DB access fails.
     */
    public void testHsql() throws Exception {
        DataSource ds = (DataSource) this.factory.getBean(DATA_SOURCE);
        Connection connection = ds.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet result = stmt.executeQuery(SELECT_FROM_USER);
        assertEquals(1, result.getFetchSize());
    }
}
