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

import org.apache.cocoon.components.classloader.RepositoryClassLoader;
import org.apache.cocoon.util.IOUtils;

import javax.servlet.ServletException;
import java.io.File;
import java.net.URL;

/**
 * This is the entry point for Cocoon execution as an HTTP Servlet.
 * It also creates a buffer by loading the whole servlet inside a ClassLoader.
 * It has been changed to extend <code>CocoonServlet</code> so that it is
 * easier to add and change functionality between the two servlets.
 * The only real differences are the ClassLoader and instantiating Cocoon inside
 * of it.
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @version CVS $Id: ParanoidCocoonServlet.java,v 1.1 2003/03/09 00:09:37 pier Exp $
 */

public class ParanoidCocoonServlet extends CocoonServlet {

    protected RepositoryClassLoader repositoryLoader;
    
    public ParanoidCocoonServlet() {
        super();
        // Override the parent class classloader
        this.repositoryLoader = new RepositoryClassLoader(new URL[] {}, this.getClass().getClassLoader());
        super.classLoader = this.repositoryLoader;
    }

    /**
     * This builds the important ClassPath used by this Servlet.  It
     * does so in a Servlet Engine neutral way.  It uses the
     * <code>ServletContext</code>'s <code>getRealPath</code> method
     * to get the Servlet 2.2 identified classes and lib directories.
     * It iterates through every file in the lib directory and adds
     * it to the classpath.
     *
     * Also, we add the files to the ClassLoader for the Cocoon system.
     * In order to protect ourselves from skitzofrantic classloaders,
     * we need to work with a known one.
     *
     * @param context  The ServletContext to perform the lookup.
     *
     * @throws ServletException
     */
     protected String getClassPath()
     throws ServletException {

        StringBuffer buildClassPath = new StringBuffer();
        String classDirPath = getInitParameter("class-dir");
        String libDirPath = getInitParameter("lib-dir");
        String classDir;
        File root;

        if ((classDirPath != null) && !classDirPath.trim().equals("")) {
            classDir = classDirPath;
        } else {
            classDir = this.servletContext.getRealPath("/WEB-INF/classes");
        }

        if ((libDirPath != null) && !libDirPath.trim().equals("")) {
            root = new File(libDirPath);
        } else {
            root = new File(this.servletContext.getRealPath("/WEB-INF/lib"));
        }

        addClassLoaderDirectory(classDir);

        buildClassPath.append(classDir);

        if (root.isDirectory()) {
            File[] libraries = root.listFiles();

            for (int i = 0; i < libraries.length; i++) {
            	String fullName = IOUtils.getFullFilename(libraries[i]);
                buildClassPath.append(File.pathSeparatorChar).append(fullName);

                addClassLoaderDirectory(fullName);
            }
        }

        buildClassPath.append(File.pathSeparatorChar)
                      .append(System.getProperty("java.class.path"));

        buildClassPath.append(File.pathSeparatorChar)
                      .append(getExtraClassPath());

        return buildClassPath.toString();
     }

    /**
     * Adds an URL to the classloader.
     */
    protected void addClassLoaderURL(URL url) {
        try {
            this.repositoryLoader.addURL(url);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not add URL" + url, e);
            }
        }
    }

    /**
     * Adds a directory to the classloader.
     */
    protected void addClassLoaderDirectory(String dir) {
        try {
            this.repositoryLoader.addDirectory(new File(dir));
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("Could not add directory" + dir, e);
            }
        }
    }
}

