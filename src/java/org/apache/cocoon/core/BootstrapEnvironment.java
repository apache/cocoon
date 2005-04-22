/*
 * Copyright 2005 The Apache Software Foundation
 * Licensed  under the  Apache License,  Version 2.0  (the "License");
 * you may not use  this file  except in  compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed  under the  License is distributed on an "AS IS" BASIS,
 * WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
 * implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.core;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;
import org.apache.log.LogTarget;

/**
* The BootstrapEnvironment is the connection between the real environment
* (servlet, cli etc.) and the Cocoon core. The core uses this object to
* access information from the real environment and to pass several objects
* back.
* A BootstrapEnvironment can be used to create a new Cocoon system using
* the {@link CoreUtil}.
*
* @version SVN $Id$
* @since 2.2
*/
public interface BootstrapEnvironment {

    /** Log a message during bootstrapping. This is used to log
     * information before the logging system is setup.
     * @param message A message.
     */
    void log(String message);

    /** Log a message during bootstrapping. This is used to log
     * information before the logging system is setup.
     * @param message A message.
     * @param error   An error.
     */
    void log(String message, Throwable error);

    /**
     * Pass the root logger back to the environment. As soon as the
     * logging system is set up, this method is called.
     * @param rootLogger The root logger.
     */
    void setLogger(Logger rootLogger);

    InputStream getInputStream(String path);
    
    void configure(Settings settings);
    void configureLoggingContext(DefaultContext context);

    void configure(DefaultContext context);

    ClassLoader getInitClassLoader();

    org.apache.cocoon.environment.Context getEnvironmentContext();
    
    /**
     * Returns the URL to the application context.
     */
    String getContextURL();

    /**
     * Returns a file to the application context.
     * @return A file pointing to the context or null if the context is not
     *         writeable.
     */
    File getContextForWriting();

    LogTarget getDefaultLogTarget();

    /**
     * Set the ConfigFile for the Cocoon object.
     *
     * @param configFileName The file location for the cocoon.xconf
     *
     * @throws Exception
     */
    URL getConfigFile(String configFileName)
    throws Exception;

    String getClassPath(Settings settings);        
}