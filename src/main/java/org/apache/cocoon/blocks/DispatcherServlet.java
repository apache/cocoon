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
package org.apache.cocoon.blocks;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet that dispatch to managed sevlets from the context Spring container.
 * It dispatch to servlets that has the property mountPath, and dispatches to the
 * servlet with the longest prefix of the request pathInfo.
 * 
 * This servlet will also initialize and destroy all the servlets that it finds
 * from the context container. This means that there must only be one dispatcher
 * servlet, otherwise the managed servlets will be initialized several times.
 *
 * @version $Id$
 */
public class DispatcherServlet
    extends HttpServlet {

    private static final String MOUNT_PATH = "mountPath";

    /** All registered mountable servlets. */
    private Map mountableServlets = new HashMap();

    private ListableBeanFactory beanFactory;
    
    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init()
     */
    public void init() throws ServletException {
        this.beanFactory =
            WebApplicationContextUtils.getRequiredWebApplicationContext(this.getServletContext());
        this.log("DispatcherServlet: initializing");
        // the returned map contains the bean names as key and the beans as values
        final Map servlets = 
            BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, Servlet.class);
        // register and initialize the servlets that has a mount path property
        final Iterator i = servlets.values().iterator();
        while ( i.hasNext() ) {
            final Servlet servlet = (Servlet) i.next();
            this.log("DispatcherServlet: initializing servlet " + servlet);
            BeanWrapperImpl wrapper = new BeanWrapperImpl(servlet);
            if (wrapper.isReadableProperty(MOUNT_PATH)) {
                String mountPath = (String) wrapper.getPropertyValue(MOUNT_PATH);
                this.log("DispatcherServlet: initializing servlet at " + mountPath);
                this.mountableServlets.put(mountPath, servlet);
                servlet.init(super.getServletConfig());
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#destroy()
     */
    public void destroy() {
        super.destroy();
        final Iterator i = this.mountableServlets.values().iterator();
        while (i.hasNext()) {
            final Servlet servlet = (Servlet) i.next();
            servlet.destroy();
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getPathInfo();
        path = path == null ? "" : path;
        // find the servlet which mount path is the longest prefix of the path info
        int index = path.length();
        Servlet servlet = null;
        while (servlet == null && index != -1) {
            path = path.substring(0, index);
            servlet = (Servlet) mountableServlets.get(path);
            index = path.lastIndexOf('/');
        }
        if (servlet == null) {
            throw new ServletException("No block for " + req.getPathInfo());
        }

        // Move the mount path from the start of the path info to the end of the
        // servlet path to get reasonable values in the called servlet
        final String mountPath = path;
        HttpServletRequest request =
            new HttpServletRequestWrapper(req) {

                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getServletPath()
                 */
                public String getServletPath() {
                    return super.getServletPath() + mountPath;
                }

                /* (non-Javadoc)
                 * @see javax.servlet.http.HttpServletRequestWrapper#getPathInfo()
                 */
                public String getPathInfo() {
                    String pathInfo = super.getPathInfo().substring(mountPath.length()); 
                    return pathInfo.length() == 0 ? null : pathInfo;
                }
            
        };
        this.log("DispatcherServlet: service servlet=" + servlet +
                " mountPath=" + mountPath +
                " servletPath=" + request.getServletPath() +
                " pathInfo=" + request.getPathInfo());
        servlet.service(request, res);
    }
}
