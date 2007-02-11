/*

 ============================================================================
                   The Apache Software License, Version 1.1
 ============================================================================

 Copyright (C) 1999-2003 The Apache Software Foundation. All rights reserved.

 Redistribution and use in source and binary forms, with or without modifica-
 tion, are permitted provided that the following conditions are met:

 1. Redistributions of  source code must  retain the above copyright  notice,
    this list of conditions and the following disclaimer.

 2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.

 3. The end-user documentation included with the redistribution, if any, must
    include  the following  acknowledgment:  "This product includes  software
    developed  by the  Apache Software Foundation  (http://www.apache.org/)."
    Alternately, this  acknowledgment may  appear in the software itself,  if
    and wherever such third-party acknowledgments normally appear.

 4. The names "Apache Cocoon" and  "Apache Software Foundation" must  not  be
    used to  endorse or promote  products derived from  this software without
    prior written permission. For written permission, please contact
    apache@apache.org.

 5. Products  derived from this software may not  be called "Apache", nor may
    "Apache" appear  in their name,  without prior written permission  of the
    Apache Software Foundation.

 THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS  FOR A PARTICULAR  PURPOSE ARE  DISCLAIMED.  IN NO  EVENT SHALL  THE
 APACHE SOFTWARE  FOUNDATION  OR ITS CONTRIBUTORS  BE LIABLE FOR  ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL,  EXEMPLARY, OR CONSEQUENTIAL  DAMAGES (INCLU-
 DING, BUT NOT LIMITED TO, PROCUREMENT  OF SUBSTITUTE GOODS OR SERVICES; LOSS
 OF USE, DATA, OR  PROFITS; OR BUSINESS  INTERRUPTION)  HOWEVER CAUSED AND ON
 ANY  THEORY OF LIABILITY,  WHETHER  IN CONTRACT,  STRICT LIABILITY,  OR TORT
 (INCLUDING  NEGLIGENCE OR  OTHERWISE) ARISING IN  ANY WAY OUT OF THE  USE OF
 THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 This software  consists of voluntary contributions made  by many individuals
 on  behalf of the Apache Software  Foundation and was  originally created by
 Stefano Mazzocchi  <stefano@apache.org>. For more  information on the Apache
 Software Foundation, please see <http://www.apache.org/>.

*/
package org.apache.cocoon.servlet;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * A bootstrap servlet to allow Cocoon to run in servlet engines that aren't fully
 * compliant with the servlet 2.2 spec.
 * <p>
 * This servlet adds a mandatory "context-dir" parameter to those accepted by {@link CocoonServlet},
 * which should point to Cocoon's context directory (e.g. "<code>/path-to-webapp/cocoon</code>").
 * This directory is used to :
 * <ul>
 * <li>build a classloader with the correct class path with the contents of
 *     <code>WEB-INF/classes</code> and <code>WEB-INF/lib</code> (see
 *     {@link ParanoidClassLoader}),</li>
 * <li>resolve paths for context resources.
 * </ul>
 *
 * @author <a href="mailto:sylvain@apache.org">Sylvain Wallez</a>
 * @version CVS $Id: BootstrapServlet.java,v 1.4 2003/07/30 02:21:31 joerg Exp $
 */

public class BootstrapServlet extends ParanoidCocoonServlet {
    
    protected File contextDir;
    
	protected File getContextDir() throws ServletException {
		
		ServletContext context = getServletContext();
		ServletConfig config = getServletConfig();
		
		log("getRealPath(\"/\") = " + context.getRealPath("/"));

		String contextDirParam = config.getInitParameter("context-directory");
		
		if (contextDirParam == null) {
				throw new ServletException("The 'context-directory' parameter must be set to the root of the servlet context");
		}
        
		// Ensure context dir doesn't end with a "/" (servlet spec says that paths for
		// getResource() should start by a "/")
		if (contextDirParam.endsWith("/")) {
			contextDirParam = contextDirParam.substring(0, contextDirParam.length() - 1);
		}
        
		// Ensure context dir exists and is a directory
		this.contextDir = new File(contextDirParam);
		if (!this.contextDir.exists()) {
			String msg = "Context dir '" + this.contextDir + "' doesn't exist";
			log(msg);
			throw new ServletException(msg);
		}

		if (!this.contextDir.isDirectory()) {
			String msg = "Context dir '" + this.contextDir + "' should be a directory";
			log(msg);
			throw new ServletException(msg);
		}
        
		context.log("Context dir set to " + this.contextDir);
		
		return this.contextDir;
	}


    protected void initServlet() throws ServletException {
        
        ServletContext newContext = new ContextWrapper(getServletContext(), this.contextDir);
        ServletConfig newConfig = new ConfigWrapper(getServletConfig(), newContext);
        
        this.servlet.init(newConfig);        
    }

    //-------------------------------------------------------------------------
    /**
     * Implementation of <code>ServletConfig</code> passed to the actual servlet.
     * It wraps the original config object and returns the new context.
     */
    public static class ConfigWrapper implements ServletConfig {
        ServletConfig config;
        ServletContext context;
        
        /**
         * Builds a <code>ServletConfig</code> using a servlet name and
         * a <code>ServletContext</code>.
         */
        public ConfigWrapper(ServletConfig config, ServletContext context) {
            this.config = config;
            this.context = context;
        }
        public String getServletName() {
            return config.getServletName();
        }
        
        public Enumeration getInitParameterNames() {
            return this.config.getInitParameterNames();
        }
        
        public ServletContext getServletContext() {
            return this.context;
        }
        
        public String getInitParameter(String name) {
            return config.getInitParameter(name);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Wrapper for the <code>ServletContext</code> passed to the actual servlet.
     * It implements all resource-related methods using the provided context
     * root directory. Other calls are delegated to the wrapped context.
     */
    public static class ContextWrapper implements ServletContext {
        ServletContext context;
        File contextRoot;
        
        /**
         * Builds a wrapper around an existing context, and handle all
         * resource resolution relatively to <code>contextRoot</code>
         */
        public ContextWrapper(ServletContext context, File contextRoot) {
            this.context = context;
            this.contextRoot = contextRoot;
        }
        
        public ServletContext getContext(String param) {
            return this.context.getContext(param);
        }
    
        public int getMajorVersion() {
            return this.context.getMajorVersion();
        }
    
        public int getMinorVersion() {
            return this.context.getMinorVersion();
        }
    
        public String getMimeType(String param) {
            return this.context.getMimeType(param);
        }

        /**
         * Returns the resource URL by appending <code>path</code> to the context
         * root. If this doesn't point to an existing file, <code>null</code> is
         * returned.
         */
        public URL getResource(String path) throws MalformedURLException {
            File file = new File(this.contextRoot, path);
            if (file.exists()) {
                URL result = file.toURL();
                //this.context.log("getResource(" + path + ") = " + result);
                return result;
            } else {
                //this.context.log("getResource(" + path + ") = null");
                return null;
            }
        }
    
        /**
         * Returns the stream for the result of <code>getResource()</code>, or
         * <code>null</code> if the resource doesn't exist.
         */
        public InputStream getResourceAsStream(String path) {
            try {
                URL url = getResource(path);
                return (url == null) ? null : url.openStream();
            } catch(Exception e) {
                this.context.log("getResourceAsStream(" + path + ") failed", e);
                return null;
            }
        }
    
        public RequestDispatcher getRequestDispatcher(String param) {
            return this.context.getRequestDispatcher(param);
        }
    
        public RequestDispatcher getNamedDispatcher(String param) {
            return this.context.getNamedDispatcher(param);
        }

        /**
         * @deprecated The method BootstrapServlet.ContextWrapper.getServlet(String)
         *             overrides a deprecated method from ServletContext.
         * @see <a href="http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/ServletContext.html#getServlet(java.lang.String)">ServletContext#getServlet(java.lang.String)</a>
         */
        public Servlet getServlet(String param) throws ServletException {
            return this.context.getServlet(param);
        }

        /**
         * @deprecated The method BootstrapServlet.ContextWrapper.getServlets()
         *             overrides a deprecated method from ServletContext.
         * @see <a href="http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/ServletContext.html#getServlets()">ServletContext#getServlets()</a>
         */
        public Enumeration getServlets() {
            return this.context.getServlets();
        }

        /**
         * @deprecated The method BootstrapServlet.ContextWrapper.getServletNames()
         *             overrides a deprecated method from ServletContext.
         * @see <a href="http://java.sun.com/j2ee/sdk_1.3/techdocs/api/javax/servlet/ServletContext.html#getServletNames()">ServletContext#getServletNames()</a>
         */
        public Enumeration getServletNames() {
            return this.context.getServletNames();
        }
    
        public void log(String msg) {
            this.context.log(msg);
        }

        /** @deprecated use {@link #log(String message, Throwable throwable)} instead. */
        public void log(Exception ex, String msg) {
            this.context.log(ex, msg);
        }
    
        public void log(String msg, Throwable thr) {
            this.context.log(msg, thr);
        }

        /**
         * Appends <code>path</code> to the context root.
         */
        public String getRealPath(String path) {
            String result = this.contextRoot + path;
            //this.context.log("getRealPath(" + path + ") = " + result);
            return result;
        }
    
        public String getServerInfo() {
            return this.context.getServerInfo();
        }
    
        public String getInitParameter(String param) {
            return this.context.getInitParameter(param);
        }
    
        public Enumeration getInitParameterNames() {
            return this.context.getInitParameterNames();
        }
    
        public Object getAttribute(String param) {
            Object result = this.context.getAttribute(param);
            //this.context.log("getAttribute(" + param + ") = " + result);
            return result;
        }
    
        public Enumeration getAttributeNames() {
            return this.context.getAttributeNames();
        }
    
        public void setAttribute(String name, Object value) {
            this.context.setAttribute(name, value);
        }
    
        public void removeAttribute(String name) {
            this.context.removeAttribute(name);
        }
        
        // Implementation of Servlet 2.3 methods. This is not absolutely required
        // for real usage since this servlet is targeted at 2.2, but is needed
        // for successful compilation
        public Set getResourcePaths(String param) {
            return null;
        }
        
        public String getServletContextName() {
            return "Cocoon context";
        }
    }
}

