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
package org.apache.cocoon.processing.impl;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.cocoon.processing.ProcessInfoProvider;

/**
 * Default implementation of the process info provider.
 *
 * @version $Id$
 * @since 2.2
 */
public class ProcessInfoProviderImpl
    implements ProcessInfoProvider {

    protected ServletContext servletContext;

    protected Map getCurrentObjectModel() {
        final Environment env = EnvironmentHelper.getCurrentEnvironment();
        if ( env == null ) {
            throw new IllegalStateException("Unable to locate current environment.");
        }
        return env.getObjectModel();
    }

    /**
     * Set the dependency to the servlet context.
     * @param context The servlet context.
     */
    public void setServletContext(ServletContext context) {
        this.servletContext = context;
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getRequest()
     */
    public HttpServletRequest getRequest() {
        return (HttpServletRequest) this.getCurrentObjectModel().get(HttpEnvironment.HTTP_REQUEST_OBJECT);
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getResponse()
     */
    public HttpServletResponse getResponse() {
        return (HttpServletResponse) this.getCurrentObjectModel().get(HttpEnvironment.HTTP_RESPONSE_OBJECT);
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getServletContext()
     */
    public ServletContext getServletContext() {
        return this.servletContext;
    }

    /**
     * @see org.apache.cocoon.processing.ProcessInfoProvider#getObjectModel()
     */
    public Map getObjectModel() {
        return this.getCurrentObjectModel();
    }
}
