/*
 * Copyright 1999-2005 The Apache Software Foundation.
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
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;

/**
 * @version $Id$
 */
public class Activator {

    private LogService log;
    private HttpService httpService;
    private HashMap servlets = new HashMap();
    private ComponentContext context;

    protected void setLog(LogService logService) {
        this.log = logService;
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void setServlet(ServiceReference reference)
            throws ServletException, NamespaceException {
        String path = (String) reference.getProperty("path");
        if (path != null) {
            this.servlets.put(path, reference);
            // If there are servlets that allready are registred when this class
            // is activated, this method will be called before the activate method.
            if (this.context != null)
                this.registerServlet(path, reference);
        }
    }

    protected void unsetServlet(ServiceReference reference) {
        String path = (String) reference.getProperty("path");
        if (path != null) {
            this.servlets.remove(path);
            this.unregisterServlet(path);
        }
    }

    protected void activate(ComponentContext context)
        throws ServletException, NamespaceException {
        this.context = context;
        this.log.log(LogService.LOG_DEBUG, "Cocoon start");
        Iterator entries = this.servlets.entrySet().iterator();
        while (entries.hasNext()) {
            Entry e = (Entry) entries.next();
            this.registerServlet((String) e.getKey(), (ServiceReference) e.getValue());
        }
    }

    protected void deactivate(ComponentContext context) {
    }
    
    private void registerServlet(String path, final ServiceReference reference)
        throws ServletException, NamespaceException {
        Servlet servlet = (Servlet) this.context.locateService("Servlet", reference);
        
        // Create a context that resolves resources in the bundle context where
        // the servlet origins from
        HttpContext httpContext = new HttpContext() {
            private Bundle bundle = reference.getBundle();
            public boolean handleSecurity(HttpServletRequest request, HttpServletResponse response) throws IOException {
                return true;
            }

            public URL getResource(String name) {
                return this.bundle.getEntry(name);
            }

            public String getMimeType(String name) {
                return null;
            }
        };
        this.httpService.registerServlet(path, servlet, null, httpContext);
        this.log.log(LogService.LOG_DEBUG, "Register Servlet at " + path);    
    }

    private void unregisterServlet(String path) {
        this.httpService.unregister(path);
        this.log.log(LogService.LOG_DEBUG, "Unregister Servlet at " + path);
    }
}
