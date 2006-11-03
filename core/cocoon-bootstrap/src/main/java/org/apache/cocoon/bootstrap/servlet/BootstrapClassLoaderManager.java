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
package org.apache.cocoon.bootstrap.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.cocoon.classloader.ClassLoaderConfiguration;
import org.apache.cocoon.classloader.ClassLoaderFactory;
import org.apache.cocoon.classloader.DefaultClassLoaderFactory;

/**
 * This class creates a singleton instance of the bootstrap class loader used
 * by Cocoon.
 *
 * @version $Id$
 * @since 2.2
 */
public class BootstrapClassLoaderManager {

    private static final String WEB_INF_COCOON_LIB = "WEB-INF/cocoon/lib";

    private static final String WEB_INF_COCOON_CLASSES = "WEB-INF/cocoon/classes";

    private static final String WEB_INF_LIB = "WEB-INF/lib";

    private static final String WEB_INF_CLASSES = "WEB-INF/classes";

    protected static ClassLoader bootstrapClassLoader;

    protected static final String CONTEXT_PREFIX = "context:";

    protected static final String FILE_PREFIX = "file:";

    public static synchronized ClassLoader getClassLoader(ServletContext servletContext)
    throws ServletException {
        if ( bootstrapClassLoader == null ) {
            // Create configuration
            ClassLoaderConfiguration config = new ClassLoaderConfiguration();

            try {
                if ( servletContext.getResource("/" + WEB_INF_COCOON_CLASSES) != null ) {
                    config.addClassDirectory(WEB_INF_COCOON_CLASSES);
                }
                if ( servletContext.getResource("/" + WEB_INF_COCOON_LIB) != null ) {
                    config.addLibDirectory(WEB_INF_COCOON_LIB);
                }
                if ( servletContext.getResource("/" + WEB_INF_CLASSES) != null ) {
                    config.addClassDirectory(WEB_INF_CLASSES);
                }
                if ( servletContext.getResource("/" + WEB_INF_LIB) != null ) {
                    config.addLibDirectory(WEB_INF_LIB);
                }
            } catch (MalformedURLException mue) {
                throw new ServletException("", mue);
            }
            final String externalClasspath = servletContext.getInitParameter("bootstrap-classpath-file");
            if ( externalClasspath != null ) {
                getClassPath(externalClasspath, servletContext, config);
            }

            final String classLoaderFactoryName = servletContext.getInitParameter("bootstrap-classloader-factory");
            bootstrapClassLoader = createClassLoader(classLoaderFactoryName, servletContext, config);
        }
        return bootstrapClassLoader;
    }

    /**
     * Log a debug message to the log of the servlet context.
     * This method first checks if the init parameter "bootstrap-classloader-debug" has the value
     * true before it logs.
     * @param servletContext The servlet context.
     * @param message        The message to log.
     */
    public static void logDebug(ServletContext servletContext, String message) {
        if ( servletContext.getInitParameter("bootstrap-classloader-debug") != null
             && servletContext.getInitParameter("bootstrap-classloader-debug").equalsIgnoreCase("true") ) { 
            servletContext.log(message);
        }
    }

    protected static void getClassPath(final String                   externalClasspath, 
                                       final ServletContext           servletContext,
                                       final ClassLoaderConfiguration config)
    throws ServletException {
        BootstrapClassLoaderManager.logDebug(servletContext, "Adding classpath from " + externalClasspath);

        InputStream is = servletContext.getResourceAsStream(externalClasspath);
        if ( is == null ) {
            throw new ServletException("Classpath file " + externalClasspath + " can't be found.");
        }

        try {
            LineNumberReader lineReader = new LineNumberReader(new InputStreamReader(is));

            String line;
            do {
                line = lineReader.readLine();
                if (line != null) {
                    if (line.startsWith("class-dir:")) {
                        line = line.substring("class-dir:".length()).trim();
                        if (line.startsWith(CONTEXT_PREFIX)) {
                            line = line.substring(CONTEXT_PREFIX.length());
                        }
                        config.addClassDirectory(line);
                    } else if (line.startsWith("lib-dir:")) {
                        line = line.substring("lib-dir:".length()).trim();
                        if (line.startsWith(CONTEXT_PREFIX)) {
                            line = line.substring(CONTEXT_PREFIX.length());
                        }
                        config.addLibDirectory(line);
                    } else if (line.startsWith("#")) {
                        // skip it (consider it as comment)
                    } else {
                        // ignore it
                    }
                }
            } while (line != null);
            lineReader.close();
        } catch (IOException io) {
            throw new ServletException(io);
        }
    }

    protected static ClassLoader createClassLoader(String                   className,
                                                   ServletContext           servletContext,
                                                   ClassLoaderConfiguration config)
    throws ServletException {
        String factoryClassName = className;
        if ( factoryClassName == null ) {
            factoryClassName = DefaultClassLoaderFactory.class.getName();
        }
        try {
            final Class classLoaderFactoryClass = Class.forName(factoryClassName);
            ClassLoaderFactory factory = (ClassLoaderFactory)classLoaderFactoryClass.newInstance();
            return factory.createClassLoader(config.getClass().getClassLoader(), config, servletContext);
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
        } catch (Exception e) {
            throw new ServletException("", e);
        }
    }
}
