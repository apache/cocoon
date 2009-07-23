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
package org.apache.cocoon.sitemap;

import java.io.IOException;
import java.net.URL;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.container.ContainerUtil;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.BeanCreationException;

import org.apache.cocoon.Processor;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.core.container.spring.avalon.AvalonUtils;
import org.apache.cocoon.servlet.RequestUtil;

/**
 * Use this servlet as an entry point to Cocoon. It wraps the {@link TreeProcessor}
 * and delegates all requests to it.
 *
 * @version $Id$
 */
public class SitemapServlet extends HttpServlet {

    /**
     * Name of the 'sitemap-path' servlet init parameter. Value should point
     * to the location of sitemap file, defaults to '/sitemap.xmap'.
     */
    private static final String PARAM_SITEMAP_PATH = "sitemap-path";
    private static final String DEFAULT_SITEMAP_PATH = "/sitemap.xmap";

    /**
     * Name of the 'check-reload' servlet init parameter. Value should be
     * one of 'true', 'yes', 'false', 'no'.
     */
    private static final String PARAM_CHECK_RELOAD = "check-reload";

    /**
     * Name of the 'pass-through' servlet init parameter. Value should be
     * one of 'true', 'yes', 'false', 'no'.
     */
    private static final String PARAM_PASS_THROUGH = "pass-through";

    protected RequestProcessor processor;

    /**
     * Initialize the servlet. The main purpose of this method is creating a
     * configured {@link TreeProcessor}.
     */
    public void init() throws ServletException {
        super.init();
        this.processor = new RequestProcessor(getServletContext());
    }

    /**
     * Process the incoming request using the Cocoon tree processor.
     */
    protected void service(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        this.processor.service(request, response);
    }

    /**
     * @see javax.servlet.GenericServlet#destroy()
     */
    public void destroy() {
        if (this.processor != null) {
            this.processor.destroy();
        }
        super.destroy();
    }

    protected class RequestProcessor extends org.apache.cocoon.servlet.RequestProcessor {

        public RequestProcessor(ServletContext servletContext) {
            super(servletContext);
        }

        protected boolean rethrowExceptions() {
            return true;
        }

        protected Processor getProcessor() {
            // read tree processor configuration
            Configuration config;
            try {
                config = createTreeProcessorConfiguration(this.servletContext);
            } catch (IOException e) {
                throw new BeanCreationException("Could not create configuration for TreeProcessor", e);
            }

            ServiceManager serviceManager =
                (ServiceManager) this.cocoonBeanFactory.getBean(AvalonUtils.SERVICE_MANAGER_ROLE);

            // create the tree processor
            Processor processor;
            try {
                processor = new TreeProcessor();
                ContainerUtil.service(processor, serviceManager);
                ContainerUtil.configure(processor, config);
                ContainerUtil.initialize(processor);
            } catch (Exception e) {
                throw new BeanCreationException("Could not create TreeProcessor", e);
            }

            return processor;
        }

        protected void destroy() {
            if (this.processor != null) {
                ContainerUtil.dispose(this.processor);
                this.processor = null;
            }
        }

        /**
         * @see org.apache.cocoon.servlet.RequestProcessor#getURI(HttpServletRequest, HttpServletResponse)
         */
        protected String getURI(HttpServletRequest request, HttpServletResponse response) throws IOException {
            return RequestUtil.getCompleteBlockUri(request, response);
        }

        /**
         * Create an Avalon Configuration @link {@link Configuration} that configures the tree processor.
         * @throws IOException
         */
        private Configuration createTreeProcessorConfiguration(ServletContext servletContext)
        throws IOException {
            // Get the uri to the sitemap location and resolve it in the curent servlet context.
            // Please note that it is very important that the Treeprocessor receives a resolved
            // uri, simply providing a uri relative to the current context is not enough
            // and doesn't work
            String sitemapPath = getInitParameter(PARAM_SITEMAP_PATH);
            if (sitemapPath == null) {
                sitemapPath = DEFAULT_SITEMAP_PATH;
            }

            URL uri = servletContext.getResource(sitemapPath);
            if (uri == null) {
                throw new IOException("Couldn't find the sitemap " + sitemapPath);
            }
            String sitemapURI = uri.toExternalForm();

            // Create configuration
            DefaultConfiguration config = new DefaultConfiguration("sitemap");
            config.setAttribute("file", sitemapURI);

            // Set check-reload attribute
            String checkReloadStr = getInitParameter(PARAM_CHECK_RELOAD);
            if (checkReloadStr != null) {
                boolean checkReload = BooleanUtils.toBoolean(checkReloadStr);
                config.setAttribute("check-reload", checkReload);
            }

            // Set pass-through attribute
            String passThroughStr = getInitParameter(PARAM_PASS_THROUGH);
            if (passThroughStr != null) {
                boolean passThrough = BooleanUtils.toBoolean(passThroughStr);
                config.setAttribute("pass-through", passThrough);
            }

            // Done
            return config;
        }
    }
}
