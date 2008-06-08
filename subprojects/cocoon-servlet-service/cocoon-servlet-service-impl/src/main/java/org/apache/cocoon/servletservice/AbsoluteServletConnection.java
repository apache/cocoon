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
package org.apache.cocoon.servletservice;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.cocoon.callstack.environment.CallFrameHelper;
import org.apache.cocoon.servletservice.util.ServletServiceRequest;
import org.apache.cocoon.servletservice.util.ServletServiceResponse;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Create a connection to a servlet service. In order to use it, the fully qualified service name must be available.
 * 
 * @version $Id$
 * @since 1.0.0
 */
public final class AbsoluteServletConnection extends AbstractServletConnection {

    public static String ABSOLUTE_SERVLET_SOURCE_POSTFIX = "+";

    private Servlet servlet;

    /**
     * Create an absolute connection to a servlet service.
     * 
     * @param serviceName The fully qualified service name (= the name of the Spring bean).
     * @param path The requested path of the service.
     * @param queryString The query parameters formatted as HTTP request query string.
     */
    public AbsoluteServletConnection(String serviceName, String path, String queryString) {
        if (serviceName == null) {
            throw new IllegalArgumentException("The serviceName parameter must be passed.");
        }
        this.context = CallStackHelper.getBaseServletContext();
        final ApplicationContext applicationContext = WebApplicationContextUtils
                .getRequiredWebApplicationContext(this.context);
        try {
            this.servlet = (Servlet) applicationContext.getBean(serviceName);
        } catch (ClassCastException cce) {
            throw new IllegalArgumentException("The service '" + serviceName + "' is not of type "
                    + Servlet.class.getName() + ".");
        }
        if (this.servlet == null) {
            throw new IllegalArgumentException("The service '" + serviceName + "' does not exist.");
        }

        URI reqUri = null;
        try {
            this.uri = new URI(serviceName + ABSOLUTE_SERVLET_SOURCE_POSTFIX, null, path, queryString, null);
            this.uri = new URI("servlet", this.uri.toASCIIString(), null);
            reqUri = new URI("servlet", null, path, queryString, null);
        } catch (URISyntaxException e) {
            IllegalArgumentException iae = new IllegalArgumentException("Can't create a URI using the passed path '"
                    + path + "' and query string '" + queryString + "' values.");
            iae.initCause(e);
            throw iae;
        }
        this.request = new ServletServiceRequest(reqUri, CallFrameHelper.getRequest());
        this.response = new ServletServiceResponse();
    }

    /**
     * Perform the actual connect that invokes the servlet service.
     */
    protected void performConnect() throws ServletException, IOException {
        try {
            CallStackHelper.enterServlet(this.context, this.request, this.response);
            this.servlet.service(this.request, this.response);
        } finally {
            CallStackHelper.leaveServlet();
        }
    }

}
