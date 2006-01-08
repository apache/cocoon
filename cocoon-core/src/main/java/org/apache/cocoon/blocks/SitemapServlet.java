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

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.activity.Disposable;
import org.apache.avalon.framework.activity.Initializable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.context.Context;
import org.apache.avalon.framework.context.ContextException;
import org.apache.avalon.framework.context.Contextualizable;
import org.apache.avalon.framework.logger.LogEnabled;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceException;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.avalon.framework.service.Serviceable;
import org.apache.cocoon.Processor;
import org.apache.cocoon.blocks.util.CoreUtil;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.container.CocoonServiceManager;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.apache.excalibur.source.Source;
import org.apache.excalibur.source.SourceResolver;

/**
 * @version $Id$
 */
public class SitemapServlet
    extends HttpServlet
    implements Configurable, Contextualizable, Disposable, Initializable, LogEnabled, Serviceable { 

    private String containerEncoding;
    private String contextURL;

    private Logger logger;
    private Context context;
    private ServiceManager parentServiceManager;
    private ServiceManager serviceManager;
    private Configuration config;
    private Processor processor;

    // Life cycle

    public void enableLogging(Logger logger) {
        this.logger = logger;
    }

    public void contextualize(Context context) throws ContextException {
        this.context = context;
    }

    public void service(ServiceManager manager) throws ServiceException {
        this.parentServiceManager = manager;
    }

    public void configure(Configuration config)
        throws ConfigurationException {
        this.config = config;
    }

    public void initialize() throws Exception {
        this.contextURL = CoreUtil.getContextURL(this.getServletContext(), "COB-INF/block.xml");

        // Create an own service manager
        this.serviceManager = new CocoonServiceManager(this.parentServiceManager);

        String sitemapPath = this.config.getAttribute("src");

        // Hack to put a sitemap configuration for the main sitemap of
        // the block into the service manager
        getLogger().debug("SitemapServlet: create sitemap " + sitemapPath);
        DefaultConfiguration sitemapConf =
            new DefaultConfiguration("sitemap", "SitemapServlet sitemap: " + " for " + sitemapPath);
        sitemapConf.setAttribute("file", sitemapPath);
        sitemapConf.setAttribute("check-reload", "yes");
        // The source resolver must be defined in this service
        // manager, otherwise the root path will be the one from the
        // parent manager
        DefaultConfiguration resolverConf =
            new DefaultConfiguration("source-resolver", "SitemapServlet source resolver");
        DefaultConfiguration conf =
            new DefaultConfiguration("components", "SitemapServlet components");
        conf.addChild(sitemapConf);
        conf.addChild(resolverConf);

        LifecycleHelper.setupComponent(this.serviceManager,
                                       this.getLogger(),
                                       this.context,
                                       null,
                                       conf);

        SourceResolver sourceResolver = (SourceResolver)this.serviceManager.lookup(SourceResolver.ROLE);
        final Processor processor = EnvironmentHelper.getCurrentProcessor();
        if (processor != null) {
            getLogger().debug("processor context" + processor.getContext());
        }
        Source sitemapSrc = sourceResolver.resolveURI(sitemapPath);
        getLogger().debug("Sitemap Source " + sitemapSrc.getURI());
        sourceResolver.release(sitemapSrc);
        this.serviceManager.release(sourceResolver);

        // Get the Processor and keep it
        this.processor = (Processor)this.serviceManager.lookup(Processor.ROLE);
    }

    public void dispose() {
        LifecycleHelper.dispose(this.serviceManager);
        this.serviceManager = null;
        this.parentServiceManager = null;
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

        if (uri.charAt(0) == '/') {
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
