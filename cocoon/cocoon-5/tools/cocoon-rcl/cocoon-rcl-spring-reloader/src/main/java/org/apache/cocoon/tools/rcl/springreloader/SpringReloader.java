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
package org.apache.cocoon.tools.rcl.springreloader;

import javax.servlet.ServletContext;

import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * <p>
 * This class is used by the ReloadingSpringFilter to reload a Spring
 * web application context. In order not to run into {@link NoClassDefFoundError}s,
 * is is important that this class is loaded by the reloading classloader.
 * </p>
 *
 * @version $Id$
 */
public class SpringReloader {

    public synchronized void reload(ServletContext servletContext) {
        SynchronizedConfigureableWebApplicationContext ac = (SynchronizedConfigureableWebApplicationContext)
                WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
        ac.reload();
    }

}
