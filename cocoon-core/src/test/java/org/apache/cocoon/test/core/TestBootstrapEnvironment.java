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
package org.apache.cocoon.test.core;

import java.io.File;
import java.net.URL;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.MutableSettings;
import org.apache.cocoon.environment.Context;

public class TestBootstrapEnvironment
    implements BootstrapEnvironment {
    
    private final String configuration;
    private final String contextPath;
    public Logger logger;
    private final Context environmentContext;
    private String processorClassName;

    public TestBootstrapEnvironment(String configuration,
                                    String contextPath,
                                    Context environmentContext,
				                    Logger logger,
                                    String processorClassName) {
        this.configuration = configuration;
        this.contextPath = contextPath;
        this.environmentContext = environmentContext;
        this.logger = logger;
        this.processorClassName = processorClassName;
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
     * @see org.apache.cocoon.core.BootstrapEnvironment#configure(org.apache.cocoon.core.MutableSettings)
     */
    public void configure(MutableSettings settings) {
        settings.setConfiguration(this.configuration);
        settings.setWorkDirectory("work");
        settings.setProcessorClassName(this.processorClassName);
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
        return new URL(this.contextPath + configFileName);
    }

}
