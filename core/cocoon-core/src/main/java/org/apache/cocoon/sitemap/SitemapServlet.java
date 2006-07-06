/*
 * Copyright 2006 The Apache Software Foundation.
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
import java.net.MalformedURLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.DefaultConfiguration;
import org.apache.avalon.framework.logger.Logger;
import org.apache.avalon.framework.service.ServiceManager;
import org.apache.cocoon.Processor;
import org.apache.cocoon.components.LifecycleHelper;
import org.apache.cocoon.components.treeprocessor.TreeProcessor;
import org.apache.cocoon.configuration.Settings;
import org.apache.cocoon.core.CoreUtil;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.Environment;
import org.apache.cocoon.environment.http.HttpContext;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.environment.internal.EnvironmentHelper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.web.context.WebApplicationContext;

/**
 * Use this servlet as entry point to Cocoon. It wraps the @link {@link TreeProcessor} and delegates
 * all requests to it.
 * 
 * @version $Id$
 */
public class SitemapServlet extends HttpServlet {

	private static final String MANIFEST_FILE = "/META-INF/MANIFEST.MF";
    private static final String DEFAULT_CONTAINER_ENCODING = "ISO-8859-1";
	private static final String DEFAULT_SITEMAP_PATH = "/COB-INF/sitemap.xmap";	
	private static final String SITEMAP_PATH_PROPERTY = "sitemapPath";

	private BeanFactory beanFactory;
	private Logger logger;
	private String sitemapPath;
    protected Context cocoonContext;
	private Processor processor;	
	private Settings settings;
    private String contextUrl;

	/**
	 * Initialize the servlet. The main purpose of this method is creating a configured @link {@link TreeProcessor}.
	 */
    public void init(ServletConfig config) throws ServletException {
    	super.init(config);

        // Get a bean factory from the servlet context
        this.beanFactory =
            (BeanFactory) this.getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
        if (this.beanFactory == null)
            throw new ServletException("No BeanFactory in the context");
        
        this.sitemapPath = this.getServletContext().getInitParameter(SITEMAP_PATH_PROPERTY);
        if (this.sitemapPath == null)
            this.sitemapPath = DEFAULT_SITEMAP_PATH;
        
    	// get components from the beanFactory
    	this.logger = (Logger) this.beanFactory.getBean("org.apache.avalon.framework.logger.Logger");
    	this.settings = (Settings) this.beanFactory.getBean(Settings.ROLE);
        ServiceManager serviceManager = (ServiceManager) 
    		this.beanFactory.getBean("org.apache.avalon.framework.service.ServiceManager");    	
    	
    	// create the Cocoon context out of the Servlet context
        this.cocoonContext = new HttpContext(config.getServletContext());
        
        // create the Avalon context
        this.contextUrl = CoreUtil.getContextUrl(this.cocoonContext, MANIFEST_FILE);
        org.apache.avalon.framework.context.Context avalonContext;
        try {
            avalonContext = CoreUtil.createContext(this.settings, this.cocoonContext, this.contextUrl);
        } catch (MalformedURLException e) {
            throw new ServletException(e);
        }
        
        // create the tree processor
        try {
			TreeProcessor treeProcessor =  new TreeProcessor();
            treeProcessor.setBeanFactory(this.beanFactory);			
            // TODO (DF/RP) The treeProcessor doesn't need to be a managed component at all. 
            this.processor = (Processor) LifecycleHelper.setupComponent(treeProcessor,
                    this.logger,
                    avalonContext,
                    serviceManager,
                    createTreeProcessorConfiguration());
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
		} catch (Exception e) {
			throw new ServletException(e);
		} finally { 
            EnvironmentHelper.leaveProcessor();	
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
	private Configuration createTreeProcessorConfiguration() {
		DefaultConfiguration treeProcessorConf = new DefaultConfiguration("treeProcessorConfiguration");
        treeProcessorConf.setAttribute("check-reload", true);
        treeProcessorConf.setAttribute("file", this.sitemapPath);
		return treeProcessorConf;
	}	    
}
