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
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;

/**
 * This servlet builds a classloading sandbox and runs another servlet inside that
 * sandbox. The purpose is to shield the libraries and classes shipped with the web
 * application from any other classes with the same name that may exist in the system,
 * such as Xerces and Xalan versions included in JDK 1.4.
 * <p>
 * This servlet propagates all initialisation parameters to the sandboxed servlet, and
 * accept only one additional parameter, <code>servlet-class</code>, which defined the
 * sandboxed servlet class. The default is {@link CocoonServlet}.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @version CVS $Id: ParanoidCocoonServlet.java,v 1.4 2003/07/02 18:33:38 cziegeler Exp $
 */

public class ParanoidCocoonServlet extends HttpServlet {

	/**
	 * The name of the actual servlet class.
	 */
	public static final String DEFAULT_SERVLET_CLASS = "org.apache.cocoon.servlet.CocoonServlet";
    
	protected Servlet servlet;
    
    protected ClassLoader classloader;
    
	public void init(ServletConfig config) throws ServletException {
		
		super.init(config);

		// Create the classloader in which we will load the servlet
		this.classloader = getClassLoader(this.getContextDir());
        
        String servletName = config.getInitParameter("servlet-class");
        if (servletName == null) {
            servletName = DEFAULT_SERVLET_CLASS;
        }
        
        // Create the servlet
		try {
			Class servletClass = this.classloader.loadClass(servletName);
            
			this.servlet = (Servlet)servletClass.newInstance();

		} catch(Exception e) {
			throw new ServletException("Cannot load servlet " + servletName, e);
		}
        
		// Always set the context classloader. JAXP uses it to find a ParserFactory,
		// and thus fails if it's not set to the webapp classloader.
		Thread.currentThread().setContextClassLoader(this.classloader);
        
		// Inlitialize the actual servlet
		this.initServlet();
        
	}
	
	/**
	 * Initialize the wrapped servlet. Subclasses (see {@link BootstrapServlet} change the
	 * <code>ServletConfig</code> given to the servlet.
	 * 
	 * @throws ServletException
	 */
	protected void initServlet() throws ServletException {
		this.servlet.init(this.getServletConfig());
	}
	
	/**
	 * Get the web application context directory.
	 * 
	 * @return the context dir
	 * @throws ServletException
	 */
	protected File getContextDir() throws ServletException {
		String result = getServletContext().getRealPath("/");
		if (result == null) {
			throw new ServletException(this.getClass().getName() + " cannot run in an undeployed WAR file");
		}
		return new File(result);
	}
    
	/**
	 * Get the classloader that will be used to create the actual servlet. Its classpath is defined
	 * by the WEB-INF/classes and WEB-INF/lib directories in the context dir.
	 */
	protected ClassLoader getClassLoader(File contextDir) throws ServletException {
		List urlList = new ArrayList();
        
		try {
			File classDir = new File(contextDir + "/WEB-INF/classes");
			if (classDir.exists()) {
				if (!classDir.isDirectory()) {
					throw new ServletException(classDir + " exists but is not a directory");
				}
            
				URL classURL = classDir.toURL();
				log("Adding class directory " + classURL);
				urlList.add(classURL);
                
			}
            
            // List all .jar and .zip
			File libDir = new File(contextDir + "/WEB-INF/lib");
			File[] libraries = libDir.listFiles(
				new FilenameFilter() {
	                public boolean accept(File dir, String name) {
	                    return name.endsWith(".zip") || name.endsWith(".jar");
	                }
				}
			);

			for (int i = 0; i < libraries.length; i++) {
				URL lib = libraries[i].toURL();
				log("Adding class library " + lib);
				urlList.add(lib);
			}
		} catch (MalformedURLException mue) {
			throw new ServletException(mue);
		}
        
		URL[] urls = (URL[])urlList.toArray(new URL[urlList.size()]);
        
		return ParanoidClassLoader.newInstance(urls, this.getClass().getClassLoader());
	}
    
	/**
	 * Service the request by delegating the call to the real servlet
	 */
	public void service(ServletRequest request, ServletResponse response)
	  throws ServletException, IOException {

		Thread.currentThread().setContextClassLoader(this.classloader);
		this.servlet.service(request, response);
	}
    
	/**
	 * Destroy the actual servlet
	 */
	public void destroy() {

		Thread.currentThread().setContextClassLoader(this.classloader);
		this.servlet.destroy();

		super.destroy();
	}
}

