/*
 * Copyright 1999-2004 The Apache Software Foundation.
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
package org.apache.butterfly.servlet;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.butterfly.environment.Environment;
import org.apache.butterfly.environment.http.HttpEnvironment;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Description of ButterflyServlet.
 * 
 * @version CVS $Id$
 */
public class ButterflyServlet extends HttpServlet {

    protected static final Log logger = LogFactory.getLog(ButterflyServlet.class);
    private ServletContext servletContext;
    private String containerEncoding;
    private String defaultFormEncoding;
    /** The Spring application context */
    private WebApplicationContext applicationContext;

    /* (non-Javadoc)
     * @see javax.servlet.Servlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig conf) throws ServletException {
        this.servletContext = conf.getServletContext();
        this.applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(this.servletContext);
        /* FIXME: GenericServlet.getInitParameter causes an NPE. WTF???
        this.containerEncoding = getInitParameter("container-encoding", "ISO-8859-1");
        this.defaultFormEncoding = getInitParameter("form-encoding", "ISO-8859-1");
        */
        this.containerEncoding = "ISO-8859-1";
        this.defaultFormEncoding = "ISO-8859-1";
    }
    
    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void service(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        String uri = req.getServletPath();
        if (uri == null) {
            uri = "";
        }
        String pathInfo = req.getPathInfo();
        if (pathInfo != null) {
            // VG: WebLogic fix: Both uri and pathInfo starts with '/'
            // This problem exists only in WL6.1sp2, not in WL6.0sp2 or WL7.0b.
            if (uri.length() > 0 && uri.charAt(0) == '/') {
                uri = uri.substring(1);
            }
            uri += pathInfo;
        }

        if (uri.length() == 0) {
            /* empty relative URI
                 -> HTTP-redirect from /cocoon to /cocoon/ to avoid
                    StringIndexOutOfBoundsException when calling
                    "".charAt(0)
               else process URI normally
            */
            String prefix = req.getRequestURI();
            if (prefix == null) {
                prefix = "";
            }

            res.sendRedirect(res.encodeRedirectURL(prefix + "/"));
            return;
        }
        if (uri.charAt(0) == '/') {
            uri = uri.substring(1);
        }
        // Pass uri into environment without URLDecoding, as it is already decoded.

        Environment env = getEnvironment(uri, req, res);
        
        // Process the request with the given environment
        ClassLoader parent = getClass().getClassLoader();
        GroovyClassLoader loader = new GroovyClassLoader(parent);
        try {
            /* Class pipelineClass = */ loader.parseClass(getClass().getResourceAsStream("Pipeline.groovy"));
            // Parse the main sitemap
            FileInputStream fis = new FileInputStream(this.servletContext.getResource("sitemap.groovy").getFile());
            Class sitemapClass = loader.parseClass(fis);
            GroovyObject sitemap = (GroovyObject) sitemapClass.newInstance();
            sitemap.setProperty("beanFactory", this.applicationContext);
            Object[] args = { env };
            Boolean retval = (Boolean) sitemap.invokeMethod("setup", args);
            if (retval.booleanValue()) {
                sitemap.invokeMethod("process", new Object[] { env });
            } else {
                logger.info("Sitemap has no match for URI [" + uri + "].");
            }
        } catch (CompilationFailedException e) {
            logger.error("Cannot compile Groovy sitemap.", e);
            throw new ServletException(e);
        } catch (InstantiationException e) {
            logger.error("Cannot instantiate sitemap.", e);
            throw new ServletException(e);
        } catch (IllegalAccessException e) {
            logger.error("Cannot access sitemap method.", e);
            throw new ServletException(e);
        }

    }

    /**
     * @param uri
     * @param req
     * @param res
     * @throws MalformedURLException
     * @throws IOException
     */
    protected Environment getEnvironment(String uri, HttpServletRequest req, HttpServletResponse res) throws MalformedURLException, IOException {
        String formEncoding = req.getParameter("cocoon-form-encoding");
        if (formEncoding == null) {
            formEncoding = this.defaultFormEncoding;
        }
         return new HttpEnvironment(uri,
                null, // this.servletContextURL,
                req,
                res,
                this.servletContext,
                null, // (HttpContext) this.appContext.get(Constants.CONTEXT_ENVIRONMENT_CONTEXT),
                this.containerEncoding,
                formEncoding);
    }

    /**
     * Get an initialisation parameter. The value is trimmed, and null is returned if the trimmed value
     * is empty.
     */
    public String getInitParameter(String name) {
        String result = super.getInitParameter(name);
        if (result != null) {
            result = result.trim();
            if (result.length() == 0) {
                result = null;
            }
        }

        return result;
    }

    /** Convenience method to access servlet parameters */
    protected String getInitParameter(String name, String defaultValue) {
        String result = getInitParameter(name);
        if (result == null) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(name + " was not set - defaulting to '" + defaultValue + "'");
            }
            return defaultValue;
        } else {
            return result;
        }
    }

    /** Convenience method to access boolean servlet parameters */
    protected boolean getInitParameterAsBoolean(String name, boolean defaultValue) {
        String value = getInitParameter(name);
        if (value == null) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(name + " was not set - defaulting to '" + defaultValue + "'");
            }
            return defaultValue;
        }

        return BooleanUtils.toBoolean(value);
    }

    protected int getInitParameterAsInteger(String name, int defaultValue) {
        String value = getInitParameter(name);
        if (value == null) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug(name + " was not set - defaulting to '" + defaultValue + "'");
            }
            return defaultValue;
        } else {
            return Integer.parseInt(value);
        }
    }
}
