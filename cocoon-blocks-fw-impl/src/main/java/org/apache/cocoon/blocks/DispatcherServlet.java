/*
 * Copyright 2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.DirectFieldAccessor;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * A servlet that dispatch to managed sevlets from the context Spring container.
 * It dispatch to servlets that has the property mountPath, and dispatches to the
 * servlet with the longest prefix of the request pathInfo.
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
        // the returned map contains the bean names as key and the beans as values
        final Map servlets = 
            BeanFactoryUtils.beansOfTypeIncludingAncestors(this.beanFactory, Servlet.class);
        final Iterator i = servlets.values().iterator();
        while ( i.hasNext() ) {
            final Servlet servlet = (Servlet) i.next();
            DirectFieldAccessor accessor = new DirectFieldAccessor(servlet);
            if (accessor.isReadableProperty(MOUNT_PATH)) {
                this.mountableServlets.put(accessor.getPropertyValue(MOUNT_PATH), servlet);
            }
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String path = req.getPathInfo();
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
        // FIXME: the request object should be modified so that pathInfo
        // and the rest of the parts of the request uri are correct 
        servlet.service(req, res);
    }
}
