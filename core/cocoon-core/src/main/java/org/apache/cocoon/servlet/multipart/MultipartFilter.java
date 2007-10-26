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
package org.apache.cocoon.servlet.multipart;

import java.io.File;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.support.WebApplicationContextUtils;

import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.servlet.RequestUtil;
import org.apache.cocoon.servlet.ServletSettings;
import org.apache.cocoon.util.AbstractLogEnabled;

/**
 * Servlet filter for handling multi part MIME uploads
 * 
 * @version $Id$
 */
public class MultipartFilter extends AbstractLogEnabled
                             implements Filter{

    /**
     * The RequestFactory is responsible for wrapping multipart-encoded
     * forms and for handing the file payload of incoming requests
     */
    protected RequestFactory requestFactory;

    /** Root Cocoon Bean Factory. */
    protected BeanFactory cocoonBeanFactory;

    /** The root settings. */
    protected Settings settings;

    /** The special servlet settings. */
    protected ServletSettings servletSettings;

    /** The servlet context. */
    protected ServletContext servletContext;

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    public void init(FilterConfig config) throws ServletException {
        this.servletContext = config.getServletContext();
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    public void destroy() {
        // nothing to do
    }

    protected synchronized void configure() {
        if (this.cocoonBeanFactory == null) {
            this.cocoonBeanFactory = WebApplicationContextUtils.getRequiredWebApplicationContext(this.servletContext);
            this.settings = (Settings) this.cocoonBeanFactory.getBean(Settings.ROLE);
            this.servletSettings = new ServletSettings(this.settings);
            String containerEncoding;
            final String encoding = this.settings.getContainerEncoding();
            if (encoding == null) {
                containerEncoding = "ISO-8859-1";
            } else {
                containerEncoding = encoding;
            }

            final MultipartConfigurationHelper config = new MultipartConfigurationHelper();
            config.configure(this.settings, getLogger());

            this.requestFactory = new RequestFactory(config.isAutosaveUploads(),
                                                     new File(config.getUploadDirectory()),
                                                     config.isAllowOverwrite(),
                                                     config.isSilentlyRename(),
                                                     config.getMaxUploadSize(),
                                                     containerEncoding);
        }
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain filterChain)
    throws IOException, ServletException {
        if (this.cocoonBeanFactory == null) {
            this.configure();
        }

        // get the request (wrapped if contains multipart-form data)
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        try{

            try {
                request = this.requestFactory.getServletRequest(request);
            } catch (Exception e) {
                if (getLogger().isErrorEnabled()) {
                    getLogger().error("Problem in multipart filter. Unable to create request.", e);
                }

                RequestUtil.manageException(request, response, null, null,
                                            HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                                            "Problem in creating the Request",
                                            null, null, e, this.servletSettings, getLogger(), this);
            }

            filterChain.doFilter(request, response);

        } finally {
            try {
                if (request instanceof MultipartHttpServletRequest) {
                    if (getLogger().isDebugEnabled()) {
                        getLogger().debug("Deleting uploaded file(s).");
                    }
                    ((MultipartHttpServletRequest) request).cleanup();
                }
            } catch (IOException e) {
                getLogger().error("MultipartFilter got an exception while trying to cleanup the uploaded files.", e);
            }
        }
    }
}
