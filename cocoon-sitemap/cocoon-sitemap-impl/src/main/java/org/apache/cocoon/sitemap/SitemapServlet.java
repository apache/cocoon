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
package org.apache.cocoon.sitemap;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.core.Core;
import org.apache.cocoon.core.servlet.CoreUtil;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.util.ClassUtils;

/**
 * @version $Id$
 */
public class SitemapServlet
    extends HttpServlet
    implements Configurable, LogEnabled, Serviceable { 

    private String containerEncoding;
    private String contextURL;

    private Logger logger;
    private ServiceManager serviceManager;
    private Configuration config;
    private Processor processor;

    // Life cycle

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    public void service(ServiceManager serviceManager) throws ServiceException {
        this.serviceManager = serviceManager;
    }

    public void configure(Configuration config) throws ConfigurationException {
        this.config = config;
    }

    protected final Logger getLogger() {
        return this.logger;
    }

    // Servlet methods

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        this.containerEncoding = this.getInitParameter("container-encoding");
        if (this.containerEncoding == null) {
            this.containerEncoding = "ISO-8859-1";
        }
        
        Core core = null;
        try {
            core = (Core) this.serviceManager.lookup(Core.ROLE);
        } catch (ServiceException e) {
            throw new ServletException("Could not find a Core object from the parent service manager", e);
        }
        
        String sitemapPath = null;
        try {
            sitemapPath = this.config.getAttribute("file");
        } catch (ConfigurationException e) {
            throw new ServletException("Faulty sitemap configuration ", e);
        }

        // FIXME: All context relative paths are supposed to start with '/' but of some
        // reason that doesn't work for the sitemap engine, or maybe a different covention
        // applies there.
        if (sitemapPath != null && sitemapPath.charAt(0) != '/')
            sitemapPath = "/" + sitemapPath;

        Context context = CoreUtil.createContext(config, core.getSettings(), sitemapPath);
        this.contextURL = CoreUtil.getContextURL(this.getServletContext(), sitemapPath);
        
        try {
            this.processor = (Processor) ClassUtils.newInstance(TreeProcessor.class.getName());
        } catch (Exception e) {
            throw new ServletException(e);
        }
        try {
            LifecycleHelper.setupComponent(this.processor,
                    this.getLogger(),
                    context,
                    this.serviceManager,
                    this.config);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#destroy()
     */
    public void destroy() {
        // TODO Auto-generated method stub
        super.destroy();
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Request to the own block
        String uri = request.getPathInfo();

        if (uri.length() > 0 && uri.charAt(0) == '/') {
            uri = uri.substring(1);
        }

        String formEncoding = request.getParameter("cocoon-form-encoding");
        if (formEncoding == null) {
            formEncoding = "ISO-8859-1";
            // FIXME formEncoding = this.settings.getFormEncoding();
        }
        HttpEnvironment env =
            new HttpEnvironment(uri,
                    this.contextURL,
                    request,
                    response,
                    this.getServletContext(),
                    new HttpContext(this.getServletContext()),
                    this.containerEncoding,
                    formEncoding);
        env.enableLogging(getLogger());
        
        try {
            this.processor.process(env);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
        env.commitResponse();       
    }

    /* (non-Javadoc)
     * @see javax.servlet.GenericServlet#getServletInfo()
     */
    public String getServletInfo() {
        return "SitemapServlet";
    }

}
