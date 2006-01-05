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
package org.apache.cocoon.core.osgi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.environment.impl.AbstractContext;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.log.LogService;

/**
 * @version $Id$
 * @since 2.2
 */
public class OSGiBootstrapEnvironment implements BootstrapEnvironment {

    private final String configuration = "/WEB-INF/block.xml";

    public Logger logger = null;
    private final String contextPath;
    private final Context environmentContext;


    public OSGiBootstrapEnvironment(BundleContext bc)
        throws Exception {

        // Create a logger manager that delegates to OSGi
        // FIXME: have the maximum level as a property of the bundle
        LoggerManager logManager = new OSGiLoggerManager(bc, LogService.LOG_DEBUG);
        this.logger = logManager.getDefaultLogger();

        Bundle bundle = bc.getBundle();
        if (bundle == null) {
            throw new Exception("No sitemap bundle");
        }

        // Try to figure out the path of the root from that of /WEB-INF/block.xml
        URL pathURL = bundle.getResource(this.configuration);
        if (pathURL == null) {
            throw new FileNotFoundException("Unable to get resource '/WEB-INF/block.xml' from bundle ." + bundle);
        }

        String path = pathURL.toString();
        path = path.substring(0, path.length() - (this.configuration.length() - 1));
        this.contextPath = path;

        this.environmentContext = new OSGiContext(bundle);
    }

    /**
     * @see org.apache.cocoon.core.BootstrapEnvironment#getBootstrapLogger(org.apache.cocoon.core.BootstrapEnvironment.LogLevel)
     */
    public Logger getBootstrapLogger(LogLevel logLevel) {
        return this.logger;
    }

    /** Log a message during bootstrapping. This is used to log
     * information before the logging system is setup.
     * @param message A message.
     */
    public void log(String message) {
        this.logger.info(message);
    }

    /** Log a message during bootstrapping. This is used to log
     * information before the logging system is setup.
     * @param message A message.
     * @param error   An error.
     */
    public void log(String message, Throwable error) {
        this.logger.info(message, error);
    }

    /**
     * Pass the root logger back to the environment. As soon as the
     * logging system is set up, this method is called.
     * @param rootLogger The root logger.
     */
    public void setLogger(Logger rootLogger) {
        this.logger = rootLogger;
    }

    /**
     * Get the input stream from a resource at the given
     * path. Only paths relative to the bootstrap context are
     * supported. Returns null if no resource exists at the
     * specified path
     */
    public InputStream getInputStream(String path) {
        try {
            return (new URL(this.contextPath + path)).openStream();
        } catch (IOException e) {
            this.log("Couldn't open " + this.contextPath + path);
            return null;
        }
    }

    public void configure(MutableSettings settings) {
        // FIXME: Should be found from block.xml
        settings.setConfiguration("/WEB-INF/cocoon.xconf");
        settings.setWorkDirectory("work");
        settings.setLoggingConfiguration("/WEB-INF/logkit.xconf");
    }

    public void configureLoggingContext(DefaultContext context) {
        // simply do nothing
    }

    public void configure(DefaultContext context) {
    }

    public Context getEnvironmentContext() {
        return this.environmentContext;
    }

    /**
     * Returns the URL to the application context.
     */
    public String getContextURL() {
        return this.contextPath;
    }

    /**
     * Returns a file to the application context.
     * @return A file pointing to the context or null if the context is not
     *         writeable.
     */
    public File getContextForWriting() {
        return null;
    }

    /**
     * Set the ConfigFile for the Cocoon object.
     *
     * @param configFileName The file location for the cocoon.xconf
     *
     * @throws Exception
     */
    public URL getConfigFile(String configFileName) throws Exception {
        this.logger.debug("getConfigFile: contextPath=" + this.contextPath +
                          " configFileName=" + configFileName);
        return new URL(this.contextPath + configFileName);
    }

    public class OSGiContext extends AbstractContext {

        private Bundle bundle;
        private Hashtable attributes = new Hashtable();
        private Hashtable initparameters = new Hashtable();

        public OSGiContext(Bundle bundle) {
            this.bundle = bundle;
            this.initparameters.put("work-directory", "work");
        }

        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        public Enumeration getAttributeNames() {
            return attributes.keys();
        }

        public URL getResource(String path) throws MalformedURLException {
            return this.bundle.getResource(path);
        }

        public String getRealPath(String path) {
            // Everything is zipped up, no path.
            return null;
        }

        public String getMimeType(String file) {
            // TODO Implement
            throw new UnsupportedOperationException("Not Implemented");
        }

        public String getInitParameter(String name) {
            return (String) initparameters.get(name);
        }

        public InputStream getResourceAsStream(String path) {
            try {
                return bundle.getResource(path).openStream();
            } catch (IOException e) {
                // FIXME Error handling
                e.printStackTrace();
                return null;
            }
        }
    }
}
