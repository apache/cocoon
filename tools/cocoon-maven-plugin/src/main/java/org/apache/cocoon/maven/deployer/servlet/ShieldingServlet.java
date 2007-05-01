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
package org.apache.cocoon.maven.deployer.servlet;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

/**
 * This servlet builds a classloading sandbox and runs another servlet inside
 * that sandbox. The purpose is to shield the libraries and classes shipped with
 * the web application from any other classes with the same name that may exist
 * in the system, such as Xerces and Xalan versions included in JDK 1.4.
 * <p>
 * This servlet propagates all initialisation parameters to the sandboxed
 * servlet, and requires the parameter <code>servlet-class</code>.
 * <ul>
 * <li><code>servlet-class</code> defines the sandboxed servlet class.</li>
 * </ul>
 *
 * @version $Id$
 */
public class ShieldingServlet extends HttpServlet {

    protected Servlet servlet;

    protected ClassLoader classloader;

    /**
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        // Get the classloader
        this.classloader = ShieldedClassLoaderManager.getClassLoader(config.getServletContext());

        String servletName = config.getInitParameter("servlet-class");
        if (servletName == null) {
            throw new ServletException("ShieldingServlet: Init-Parameter 'servlet-class' is missing.");
        }
        ShieldedClassLoaderManager.logDebug(config.getServletContext(),
                                             "ShieldingServlet: Loading servlet class " + servletName);

        // Create the servlet
        try {

            Class servletClass = this.classloader.loadClass(servletName);
            this.servlet = (Servlet) servletClass.newInstance();

        } catch (Exception e) {
            throw new ServletException("Cannot load servlet " + servletName, e);
        }

        // Always set the context classloader. JAXP uses it to find a
        // ParserFactory,
        // and thus fails if it's not set to the webapp classloader.
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classloader);

            // Inlitialize the actual servlet
            this.servlet.init(this.getServletConfig());
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Service the request by delegating the call to the real servlet
     */
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classloader);
            this.servlet.service(request, response);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Destroy the actual servlet
     */
    public void destroy() {
        if (this.servlet != null) {
            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.classloader);
                this.servlet.destroy();
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
        super.destroy();
    }
}