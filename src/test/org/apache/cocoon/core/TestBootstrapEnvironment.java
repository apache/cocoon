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
package org.apache.cocoon.core;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.Settings;
import org.apache.cocoon.environment.Context;
import org.apache.cocoon.util.log.DeprecationLogger;
import org.apache.log.LogTarget;

public class TestBootstrapEnvironment
    implements BootstrapEnvironment {
    
    private final String configuration;
    private final ClassLoader classLoader;
    private final String contextPath;
    public Logger logger;
    private final Context environmentContext;

    public TestBootstrapEnvironment(String configuration,
                                    ClassLoader classLoader,
                                    String contextPath,
                                    Context environmentContext,
				    Logger logger) {
        this.configuration = configuration;
        this.classLoader = classLoader;
        this.contextPath = contextPath;
        this.environmentContext = environmentContext;

        this.logger = logger;
        DeprecationLogger.logger = this.logger;
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
        settings.setConfiguration(this.configuration);
        settings.setWorkDirectory("work");
    }

    public void configureLoggingContext(DefaultContext context) {
        // simply do nothing
    }

    public void configure(DefaultContext context) {
    }

    public ClassLoader getInitClassLoader() {
        return this.classLoader;
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

    public LogTarget getDefaultLogTarget() {
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
        return new URL(this.contextPath + configFileName);
    }

    public String getClassPath(Settings settings) {
        return null;
    }
}
