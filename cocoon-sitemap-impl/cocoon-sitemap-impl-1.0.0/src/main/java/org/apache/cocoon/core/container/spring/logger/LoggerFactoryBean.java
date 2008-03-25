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
package org.apache.cocoon.core.container.spring.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.FactoryBean;

/**
 * Spring factory bean to setup the Commons Logging logger with specified
 * category.
 *
 * <p>If category was not set, default category 'cocoon' is used.
 *
 * @since 2.2
 * @version $Id$
 */
public class LoggerFactoryBean implements FactoryBean {

    /** The logging category. */
    private String category = "cocoon";

    /** The log instance */
    private Log logger;


    public String getCategory() {
        return this.category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }

    protected void setLogger(Log logger) {
        this.logger = logger;
    }

    public void init() {
        setLogger(LogFactory.getLog(this.category));
    }

    /**
     * @see FactoryBean#getObject()
     */
    public Object getObject() throws Exception {
        return this.logger;
    }

    /**
     * @see FactoryBean#getObjectType()
     */
    public Class getObjectType() {
        return Log.class;
    }

    /**
     * @see FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }
}
