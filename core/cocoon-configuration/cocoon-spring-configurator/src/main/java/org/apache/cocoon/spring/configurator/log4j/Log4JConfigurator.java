/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.spring.configurator.log4j;

import org.apache.cocoon.configuration.PropertyHelper;
import org.apache.cocoon.configuration.Settings;
import org.apache.log4j.LogManager;
import org.apache.log4j.xml.DOMConfigurator;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;

/**
 * This is a configurator for log4j that supports variable substitution
 * from the settings object.
 *
 * @version $Id$
 * @since 1.0
 */
public class Log4JConfigurator
    extends DOMConfigurator
    implements InitializingBean {

    /** The settings object that is used to substitute variable values. */
    protected Settings settings;

    /** The configuration resources. */
    protected Resource resource;

    /**
     * Inject the settings object.
     * @param s The settings bean.
     */
    public void setSettings(final Settings s) {
        this.settings = s;
    }

    /**
     * Set the configuration resource.
     * @param r The resource.
     */
    public void setResource(final Resource r) {
        this.resource = r;
    }

    /**
     * This method is called after the instance is setup and before it is used.
     * @throws Exception If anything during configuration goes wrong an exception is thrown.
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception {
        this.doConfigure(this.resource.getInputStream(), LogManager.getLoggerRepository());
    }

    /**
     * Substitute variables in the string value.
     * @param value The value which might contain variable references.
     * @return The new value without variables (if defined).
     * @see org.apache.log4j.xml.DOMConfigurator#subst(java.lang.String)
     */
    protected String subst(final String value) {
        return PropertyHelper.replace(value, this.settings);
    }
}
