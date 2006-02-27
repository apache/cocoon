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
package org.apache.cocoon.blocks.osgi;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.osgi.service.log.LogService;

/**
 * @version $Id$
 */
public class Activator {
    
    private LogService log;
    private HttpService httpService;
    private ComponentContext context;

    protected void setLog(LogService logService) {
        this.log = logService;
        System.out.println("Got log");
    }

    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
        System.out.println("Got http service");
    }

    protected void setServlet(ServiceReference reference) throws ServletException, NamespaceException {
        String path = (String) reference.getProperty("path");
        Servlet servlet = (Servlet) this.context.locateService("Servlet", reference);
        this.httpService.registerServlet(path, servlet, null, null);
        this.log.log(LogService.LOG_DEBUG, "Register Servlet at " + path);
    }

    protected void unsetServlet(ServiceReference reference) {
        String path = (String) reference.getProperty("path");
        this.httpService.unregister(path);
        this.log.log(LogService.LOG_DEBUG, "Unregister Servlet at " + path);
    }

    protected void activate(ComponentContext context) {
        this.context = context;
        System.out.println("Cocoon Start");
        this.log.log(LogService.LOG_DEBUG, "Cocoon start");
    }

    protected void deactivate(ComponentContext context) {
        System.out.println("Cocoon Stop");
        this.log.log(LogService.LOG_DEBUG, "Cocoon stop");
    }    
}
