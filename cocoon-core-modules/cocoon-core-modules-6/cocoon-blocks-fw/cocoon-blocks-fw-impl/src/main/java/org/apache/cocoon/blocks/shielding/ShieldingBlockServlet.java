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
package org.apache.cocoon.blocks.shielding;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.cocoon.blocks.BlockServlet;

import org.springframework.aop.framework.ProxyFactory;

/**
 * A servlet for use in cocoon blocks that adds shielded classloading support.
 */
public class ShieldingBlockServlet extends BlockServlet {

    /**
     * Spring property, name of the group that will get the same classloader.
     */
    protected String shieldedGroup;

    /**
     * Spring property, path to the shielded libraries (jars) inside COB-INF/
     */
    protected String shieldedLib;

    /**
     * Spring property, path to the shielded classes inside COB-INF/
     */
    protected String shieldedClasses;

    /**
     * Spring property, name of the classloader factory
     */
    protected String classLoaderFactory;

    protected ClassLoader classLoader;

    public void setShieldedGroup(String shieldedGroup) {
        this.shieldedGroup = shieldedGroup;
    }

    public void setShieldedLib(String shieldedLib) {
        if (!shieldedLib.startsWith("/")) {
            this.shieldedLib = "/" + shieldedLib;
        } else {
            this.shieldedLib = shieldedLib;
        }
    }

    public void setShieldedClasses(String shieldedClasses) {
        if (!shieldedClasses.startsWith("/")) {
            this.shieldedClasses = "/" + shieldedClasses;
        } else {
            this.shieldedClasses = shieldedClasses;
        }
    }

    public void setClassLoaderFactory(String classLoaderFactory) {
        this.classLoaderFactory = classLoaderFactory;
    }

    protected Servlet createEmbeddedServlet(String embeddedServletClassName, ServletConfig servletConfig) 
    throws ServletException {
        // create the classloader
        Map parameters = new HashMap();
        parameters.put(ShieldedGroupClassLoaderManager.SHIELDED_GROUP_PARAM,
                this.shieldedGroup);
        parameters.put(ShieldedGroupClassLoaderManager.SHIELDED_LIB_PARAM,
                this.shieldedLib);
        parameters.put(ShieldedGroupClassLoaderManager.SHIELDED_CLASSES_PARAM,
                this.shieldedClasses);
        parameters.put(ShieldedGroupClassLoaderManager.CLASSLOADER_FACTORY_PARAM,
                this.classLoaderFactory);
        
        this.classLoader = 
            ShieldedGroupClassLoaderManager.getClassLoader(getBlockContext(), parameters);

        // Create the servlet
        final ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classLoader);
            
            Class servletClass = this.classLoader.loadClass(embeddedServletClassName);
            Servlet embeddedServlet = (Servlet) servletClass.newInstance();
            ProxyFactory proxyFactory = new ProxyFactory(embeddedServlet);
            proxyFactory.addAdvice(new ShieldingClassLoaderInterceptor(this.classLoader));
            return (Servlet) proxyFactory.getProxy();
        } catch (Exception e) {
            throw new ServletException(
                    "Loading class for embedded servlet failed " + embeddedServletClassName, e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }
    }

}
