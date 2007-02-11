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

import org.springframework.beans.factory.FactoryBean;

/**
 * This spring factory bean adds the configuration info to the bean factory.
 *
 * @since 2.2
 * @version $Id$
 */
public class ConfigurationInfoFactoryBean
    implements FactoryBean {

    protected ConfigurationInfo info;

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
        return ConfigurationInfo.class;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }

    public ConfigurationInfo getConfigurationInfo() {
        return info;
    }

    public void setConfigurationInfo(ConfigurationInfo info) {
        this.info = info;
    }
}
