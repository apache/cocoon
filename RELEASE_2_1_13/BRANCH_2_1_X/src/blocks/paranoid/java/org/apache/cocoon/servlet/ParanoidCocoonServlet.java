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
package org.apache.cocoon.servlet;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
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
 * This servlet builds a classloading sandbox and runs another servlet inside
 * that sandbox. The purpose is to shield the libraries and classes shipped with
 * the web application from any other classes with the same name that may exist
 * in the system, such as Xerces and Xalan versions included in JDK 1.4.
 * <p>
 * This servlet propagates all initialisation parameters to the sandboxed
 * servlet, and accepts the parameters <code>servlet-class</code> and
 * <code>paranoid-classpath</code>.
 * <ul>
 * <li><code>servlet-class</code> defines the sandboxed servlet class, the
 * default is {@link CocoonServlet}
 * <li><code>paranoid-classpath</code> expects the name of a text file that
 * can contain lines begining with
 * <code>class-dir:<code> (directory containing classes),
 *      <code>lib-dir:<code> (directory containing JAR or ZIP libraries) and <code>#</code>
 *      (for comments). <br/>
 *      All other lines are considered as URLs.
 *      <br/>
 *      It is also possible to use a the pseudo protocol prefix<code>context:/<code> which 
 *      is resolved to the basedir of the servlet context.
 * </ul>
 *
 * @author <a href="mailto:bloritsch@apache.org">Berin Loritsch</a>
 * @author <a href="http://www.apache.org/~sylvain/">Sylvain Wallez</a>
 * @author <a href="mailto:tcurdt@apache.org">Torsten Curdt</a>
 * @version CVS $Id$
 */

public class ParanoidCocoonServlet extends HttpServlet {

    /**
     * The name of the actual servlet class.
     */
    public static final String DEFAULT_SERVLET_CLASS = "org.apache.cocoon.servlet.CocoonServlet";

    protected static final String CONTEXT_PREFIX = "context:";

    protected static final String FILE_PREFIX = "file:";

    protected Servlet servlet;

    protected ClassLoader classloader;

    public void init(ServletConfig config) throws ServletException {

        super.init(config);

        // Create the classloader in which we will load the servlet
        // this can either be specified by an external file configured
        // as a parameter in web.xml or (the default) all jars and
        // classes from WEB-INF/lib and WEB-INF/classes are used.
        final String externalClasspath = config.getInitParameter("paranoid-classpath");
        final URL[] classPath = (externalClasspath == null)
            ? getClassPath(getContextDir())
            : getClassPath(externalClasspath, getContextDir());

            
        final String classLoaderName = config.getInitParameter("classloader-class");
        if (classLoaderName != null) {
            log("Using classloader " + classLoaderName);
        }
        this.classloader = createClassLoader(classLoaderName, classPath);

        
        String servletName = config.getInitParameter("servlet-class");
        if (servletName == null) {
            servletName = DEFAULT_SERVLET_CLASS;
        }
        log("Loading servlet class " + servletName);

        
        // Create the servlet
        try {

            Class servletClass = this.classloader.loadClass(servletName);
            this.servlet = (Servlet) servletClass.newInstance();

        } catch (Exception e) {
            throw new ServletException("Cannot load servlet " + servletName, e);
        }

        // Always set the context classloader. JAXP uses it to find a
        // ParserFactory,
        // and thus fails if it's not set to the webapp classloader.
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classloader);

            // Inlitialize the actual servlet
            this.initServlet();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Initialize the wrapped servlet. Subclasses (see {@link BootstrapServlet}
     * change the <code>ServletConfig</code> given to the servlet.
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

    protected URL[] getClassPath(final File contextDir) throws ServletException {
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
            File[] libraries = libDir.listFiles(new JarFileFilter());

            for (int i = 0; i < libraries.length; i++) {
                URL lib = libraries[i].toURL();
                log("Adding class library " + lib);
                urlList.add(lib);
            }
        } catch (MalformedURLException mue) {
            throw new ServletException(mue);
        }

        URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);

        return urls;
    }
    
    protected URL[] getClassPath(final String externalClasspath, final File contextDir) throws ServletException {
        final List urlList = new ArrayList();

        File file = new File(externalClasspath);
        if (!file.isAbsolute()) {
            file = new File(contextDir, externalClasspath);
        }

        log("Adding classpath from " + file);
        try {
            FileReader fileReader = new FileReader(file);
            LineNumberReader lineReader = new LineNumberReader(fileReader);

            String line;
            do {
                line = lineReader.readLine();
                if (line != null) {
                    if (line.startsWith("class-dir:")) {
                        line = line.substring("class-dir:".length()).trim();
                        if (line.startsWith(CONTEXT_PREFIX)) {
                            line = contextDir + line.substring(CONTEXT_PREFIX.length());
                        }
                        URL url = new File(line).toURL();
                        log("Adding class directory " + url);
                        urlList.add(url);

                    } else if (line.startsWith("lib-dir:")) {
                        line = line.substring("lib-dir:".length()).trim();
                        if (line.startsWith(CONTEXT_PREFIX)) {
                            line = contextDir + line.substring(CONTEXT_PREFIX.length());
                        }
                        File dir = new File(line);
                        File[] libraries = dir.listFiles(new JarFileFilter());
                        log("Adding " + libraries.length + " libraries from " + dir.toURL());
                        for (int i = 0; i < libraries.length; i++) {
                            URL url = libraries[i].toURL();
                            urlList.add(url);
                        }
                    } else if (line.startsWith("#")) {
                        // skip it (consider it as comment)
                    } else {
                        // Consider it as a URL
                        final URL lib;
                        if (line.startsWith(CONTEXT_PREFIX)) {
                            line = FILE_PREFIX + "/" + contextDir + line.substring(CONTEXT_PREFIX.length()).trim();
                        }
                        if (line.indexOf(':') == -1) {
                            File entry = new File(line);
                            lib = entry.toURL();
                        } else {
                            lib = new URL(line);
                        }
                        log("Adding class URL " + lib);
                        urlList.add(lib);
                    }
                }
            } while (line != null);
            lineReader.close();
        } catch (IOException io) {
            throw new ServletException(io);
        }

        URL[] urls = (URL[]) urlList.toArray(new URL[urlList.size()]);

        return urls;
    }

    protected ClassLoader createClassLoader(final String className, final URL[] classPath) throws ServletException {
        if (className != null) {
            try {
                final Class classLoaderClass = Class.forName(className);
                final Class[] parameterClasses = new Class[] { ClassLoader.class };
                final Constructor constructor = classLoaderClass.getConstructor(parameterClasses);
                final Object[] parameters = new Object[] { this.getClass().getClassLoader() };
                final ClassLoader classloader = (ClassLoader) constructor.newInstance(parameters);
                return classloader;
            } catch (InstantiationException e) {
                throw new ServletException("", e);
            } catch (IllegalAccessException e) {
                throw new ServletException("", e);
            } catch (ClassNotFoundException e) {
                throw new ServletException("", e);
            } catch (SecurityException e) {
                throw new ServletException("", e);
            } catch (NoSuchMethodException e) {
                throw new ServletException("", e);
            } catch (IllegalArgumentException e) {
                throw new ServletException("", e);
            } catch (InvocationTargetException e) {
                throw new ServletException("", e);
            }
        } else {
            return ParanoidClassLoader.newInstance(classPath, this.getClass().getClassLoader());
        }
    }

    
    /**
     * Get the classloader that will be used to create the actual servlet. Its
     * classpath is defined by the WEB-INF/classes and WEB-INF/lib directories
     * in the context dir.
     * @deprecated
     */
    protected ClassLoader getClassLoader(File contextDir) throws ServletException {
        return createClassLoader(null, getClassPath(contextDir));
    }

    /**
     * Get the classloader that will be used to create the actual servlet. Its
     * classpath is defined by an external file.
     * @deprecated
     */
    protected ClassLoader getClassLoader(final String externalClasspath, final File contextDir) throws ServletException {
        return createClassLoader(null, getClassPath(externalClasspath, contextDir));
    }

    /**
     * Service the request by delegating the call to the real servlet
     */
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {

        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classloader);
            this.servlet.service(request, response);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Destroy the actual servlet
     */
    public void destroy() {

        if (this.servlet != null) {
            final ClassLoader old = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(this.classloader);
                this.servlet.destroy();
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }

        super.destroy();
    }

    private static class JarFileFilter implements FilenameFilter {
        public boolean accept(File dir, String name) {
            return name.endsWith(".zip") || name.endsWith(".jar");
        }
    }

}

