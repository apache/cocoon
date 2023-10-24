/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cocoon.blockdeployment;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;

import javax.servlet.ServletContext;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.WebApplicationContext;

public class BlockContextURLStreamHandlerFactory implements URLStreamHandlerFactory, ApplicationContextAware {

    private Map<String, String> blockContexts;

    /**
     * {@inheritDoc}
     * 
     * @see java.net.URLStreamHandlerFactory#createURLStreamHandler(java.lang.String)
     */
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if ("blockcontext".equalsIgnoreCase(protocol)) {
            return new BlockContextURLStreamHandler(this.blockContexts);
        }

        return null;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @SuppressWarnings("unchecked")
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;
        ServletContext servletContext = webApplicationContext.getServletContext();
        this.blockContexts = (Map<String, String>) servletContext
                .getAttribute(BlockDeploymentServletContextListener.BLOCK_CONTEXT_MAP);
    }
}
