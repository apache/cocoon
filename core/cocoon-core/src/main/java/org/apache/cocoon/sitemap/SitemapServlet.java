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
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.core.container.spring.avalon.AvalonUtils;
import org.springframework.beans.factory.BeanCreationException;

/**
 * Use this servlet as entry point to Cocoon. It wraps the {@link TreeProcessor}
 * and delegates all requests to it.
 *
 * @version $Id$
 */
public class SitemapServlet extends HttpServlet {

    protected RequestProcessor processor;

    /**
     * Initialize the servlet. The main purpose of this method is creating a
     * configured {@link TreeProcessor}.
     */
    public void init() throws ServletException {
        super.init();

        this.processor = new RequestProcessor(this.getServletContext());
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
        private static final String DEFAULT_SITEMAP_PATH = "/sitemap.xmap";
        private static final String SITEMAP_PATH_PROPERTY = "sitemapPath";

        private Configuration treeProcessorConfiguration;

        public RequestProcessor(ServletContext servletContext) {
            super(servletContext);
        }

        protected boolean rethrowExceptions() {
            return true;
        }

        protected Processor getProcessor() {
            ServiceManager serviceManager =
                (ServiceManager) this.cocoonBeanFactory.getBean(AvalonUtils.SERVICE_MANAGER_ROLE);
            try {
                this.treeProcessorConfiguration =
                    this.createTreeProcessorConfiguration(this.servletContext);
            } catch (IOException e) {
                throw new BeanCreationException("Could not create configuration for TreeProcesoor", e);
            }

            Processor processor;
            // create the tree processor
            try {
                TreeProcessor treeProcessor =  new TreeProcessor();
                // TODO (DF/RP) The treeProcessor doesn't need to be a managed component at all.
                processor = (Processor) LifecycleHelper.setupComponent(treeProcessor,
                        this.log,
                        null,
                        serviceManager,
                        this.treeProcessorConfiguration);
            } catch (Exception e) {
                throw new BeanCreationException("Could not create TreeProcessor", e);
            }
            return processor;
        }

        protected void destroy() {
            if ( this.processor != null ) {
                ContainerUtil.dispose(this.processor);
                this.processor = null;
            }
        }

        /* (non-Javadoc)
         * @see org.apache.cocoon.servlet.RequestProcessor#getURI(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
         */
        // The original implementation prepend the servlet context path which doesn't work
        // in the tree processor if there actually is a servlet context path
        protected String getURI(HttpServletRequest request, HttpServletResponse res) throws IOException {
            String uri = request.getPathInfo();
            if (uri == null)
                return "";
            else if (uri.length() > 0 && uri.charAt(0) == '/')
                return uri.substring(1);
            else
                return uri;
        }

        /**
         * Create an Avalon Configuration @link {@link Configuration} that configures the tree processor.
         * @throws IOException
         */
        private Configuration createTreeProcessorConfiguration(ServletContext servletContext)
        throws IOException {
            // get the uri to the sitemap location and resolve it in the curent servlet context,
            // observere that it is very important that the Treeprocessor get a resolved uri,
            // just providing a relative uri relative to the current context is not enough
            // and doesn't work
            String sitemapPath = servletContext.getInitParameter(SITEMAP_PATH_PROPERTY);
            if (sitemapPath== null)
                sitemapPath= DEFAULT_SITEMAP_PATH;

            String sitemapURI;
            URL uri = servletContext.getResource(sitemapPath);
            if (uri == null)
                throw new IOException("Couldn't find the sitemap " + sitemapPath);
            sitemapURI = uri.toExternalForm();

            DefaultConfiguration treeProcessorConf = new DefaultConfiguration("treeProcessorConfiguration");
            treeProcessorConf.setAttribute("check-reload", true);
            treeProcessorConf.setAttribute("file", sitemapURI);
            return treeProcessorConf;
        }

        /* (non-Javadoc)
         * @see org.apache.cocoon.servlet.RequestProcessor#cleanup()
         */
        // The cleanup should be done in the end of the request processing. In the
        // block servlet context this is not certain to happen in this servlet as
        // it can have been called from a block protocol.
        protected void cleanup() {
        }
    }
}
