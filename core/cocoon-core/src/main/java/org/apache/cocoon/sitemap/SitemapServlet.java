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
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.ProcessingUtil;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Use this servlet as entry point to Cocoon. It wraps the @link {@link TreeProcessor} and delegates
 * all requests to it.
 * 
 * @version $Id$
 */
public class SitemapServlet extends HttpServlet {

    private static final String DEFAULT_CONTAINER_ENCODING = "ISO-8859-1";
	private static final String DEFAULT_SITEMAP_PATH = "/sitemap.xmap";	
	private static final String SITEMAP_PATH_PROPERTY = "sitemapPath";

	private Logger logger;
    protected Context cocoonContext;
	private Processor processor;	
	private Settings settings;

    /**
     * Initialize the servlet. The main purpose of this method is creating a configured @link {@link TreeProcessor}.
     */
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        
        // Get a bean factory from the servlet context
        WebApplicationContext container =
            WebApplicationContextUtils.getRequiredWebApplicationContext(config.getServletContext());
        
        // get components from the beanFactory
        this.logger = (Logger) container.getBean(ProcessingUtil.LOGGER_ROLE);
        this.settings = (Settings) container.getBean(Settings.ROLE);
        ServiceManager serviceManager = (ServiceManager) 
        container.getBean(ProcessingUtil.SERVICE_MANAGER_ROLE);     
        
        // create the Cocoon context out of the Servlet context
        this.cocoonContext = new HttpContext(config.getServletContext());

        // get the uri to the sitemap location and resolve it in the curent servlet context,
        // observere that it is very important that the Treeprocessor get a resolved uri,
        // just providing a relative uri relative to the current context is not enough
        // and doesn't work
        String sitemapPath = this.getServletContext().getInitParameter(SITEMAP_PATH_PROPERTY);
        if (sitemapPath== null)
            sitemapPath= DEFAULT_SITEMAP_PATH;

        String sitemapURI;
        try {
            URL uri = this.getServletContext().getResource(sitemapPath);
            if (uri == null)
                throw new ServletException("Couldn't find the sitemap " + sitemapPath);
            sitemapURI = uri.toExternalForm();
        } catch (MalformedURLException e) {
            throw new ServletException(e);
        }

        // create the tree processor
        try {
            TreeProcessor treeProcessor =  new TreeProcessor();
            // TODO (DF/RP) The treeProcessor doesn't need to be a managed component at all. 
            this.processor = (Processor) LifecycleHelper.setupComponent(treeProcessor,
                    this.logger,
                    null,
                    serviceManager,
                    createTreeProcessorConfiguration(sitemapURI));
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
    
	/**
     * Process the incoming request using the Cocoon tree processor.
     */
	protected void service(HttpServletRequest request, HttpServletResponse response) 
		throws ServletException, IOException {
		
		Environment environment = createCocoonEnvironment(request, response);
		try {
	        EnvironmentHelper.enterProcessor(this.processor, environment);			
			this.processor.process(environment);
            environment.commitResponse();
		} catch (Exception e) {
            environment.tryResetResponse();
			throw new ServletException(e);
		} finally { 
            EnvironmentHelper.leaveProcessor();
            environment.finishingProcessing();
	    } 
	}    
	
	/**
	 * This method takes the servlet request and response and creates a Cocoon 
	 * environment (@link {@link Environment}) out of it.
	 */
    protected Environment createCocoonEnvironment(HttpServletRequest req, 
    		HttpServletResponse res) throws IOException  {
    	
		String uri = req.getPathInfo();
		if(uri != null) {
			uri = uri.substring(1);
		} else {
			uri = "";
		}

		String formEncoding = req.getParameter("cocoon-form-encoding");
		if (formEncoding == null) {
			formEncoding = this.settings.getFormEncoding();
		}
		HttpEnvironment env = new HttpEnvironment(uri, req, res, this.getServletContext(),
				this.cocoonContext, DEFAULT_CONTAINER_ENCODING, formEncoding);
		
		env.enableLogging(this.logger);
		return env;
	}
    
    /**
     * Create an Avalon Configuration @link {@link Configuration} that configures the tree processor.
     */
	private Configuration createTreeProcessorConfiguration(String sitemapURI) {
		DefaultConfiguration treeProcessorConf = new DefaultConfiguration("treeProcessorConfiguration");
        treeProcessorConf.setAttribute("check-reload", true);
        treeProcessorConf.setAttribute("file", sitemapURI);
		return treeProcessorConf;
	}	    
}
