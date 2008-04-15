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
package org.apache.cocoon.corona.servlet;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.cocoon.corona.servlet.node.MimeTypeCollector;
import org.apache.cocoon.corona.servlet.node.StatusCodeCollector;
import org.apache.cocoon.corona.servlet.util.HttpContextHelper;
import org.apache.cocoon.corona.sitemap.Invocation;
import org.apache.cocoon.corona.sitemap.InvocationImpl;
import org.apache.cocoon.corona.sitemap.SitemapBuilder;
import org.apache.cocoon.corona.sitemap.node.Sitemap;
import org.apache.excalibur.sourceresolve.jnet.DynamicURLStreamHandlerFactory;
import org.apache.excalibur.sourceresolve.jnet.URLStreamHandlerFactoryInstaller;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class SitemapServlet extends HttpServlet implements BeanFactoryAware {

    private static final long serialVersionUID = 1L;

    private BeanFactory beanFactory;
    private Sitemap sitemap;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        try {
            SitemapBuilder sitemapBuilder = (SitemapBuilder) this.beanFactory.getBean(SitemapBuilder.class.getName());
            URL url = servletConfig.getServletContext().getResource("/sitemap.xmap");
            this.sitemap = sitemapBuilder.build(url);

            URLStreamHandlerFactoryInstaller.setURLStreamHandlerFactory(new DynamicURLStreamHandlerFactory());
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    public void invoke(String requestURI, Map<String, Object> parameters, OutputStream outputStream) {
        InvocationImpl invocation = (InvocationImpl) this.beanFactory.getBean(Invocation.class.getName());

        System.out.println("Starting invocation for RequestURI " + requestURI);
        invocation.setRequestURI(requestURI);
        invocation.setParameters(parameters);
        invocation.setOutputStream(outputStream);

        this.sitemap.invoke(invocation);
    }

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            DynamicURLStreamHandlerFactory.push(new ServletURLStreamHandlerFactory());
            SitemapDelegator.setSitemapServlet(this);

            // assemble parameters
            Map<String, Object> parameters = this.getInvocationParameters(request);
            HttpContextHelper.storeRequest(request, parameters);
            HttpContextHelper.storeResponse(response, parameters);

            this.invoke(request.getRequestURI(), parameters, response.getOutputStream());

            response.setStatus(StatusCodeCollector.getStatusCode());
            response.setContentType(MimeTypeCollector.getMimeType());
            response.setContentType("text/html;charset=UTF-8");
        } catch (Exception e) {
            PrintWriter writer = new PrintWriter(response.getOutputStream());
            e.printStackTrace(writer);
            writer.close();
        } finally {
            SitemapDelegator.removeSitemapServlet();
            DynamicURLStreamHandlerFactory.pop();
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getInvocationParameters(HttpServletRequest req) {
        Map<String, Object> invocationParameters = new HashMap<String, Object>();

        for (Enumeration<String> names = req.getParameterNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            invocationParameters.put(name, req.getParameter(name));
        }

        return invocationParameters;
    }
}
