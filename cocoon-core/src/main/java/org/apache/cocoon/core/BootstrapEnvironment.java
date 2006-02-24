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

import java.net.URL;

import org.apache.avalon.framework.context.DefaultContext;
import org.apache.avalon.framework.logger.Logger;

/**
 * The BootstrapEnvironment is the connection between the real environment
 * (servlet, cli etc.) and the Cocoon core. The core uses this object to
 * access information from the real environment and to pass several objects
 * back.
 * A BootstrapEnvironment can be used to create a new Cocoon system using
 * the {@link CoreUtil}.
 * 
 * @version $Id$
 * @since 2.2
 */
public interface BootstrapEnvironment {

    /**
     * Pass the root logger back to the environment. As soon as the
     * logging system is set up, this method is called.
     * @param rootLogger The root logger.
     */
    void setLogger(Logger rootLogger);

    /**
     * This callback can be used by the environment to add environment specific
     * settings. For example the servlet environment parsed the web.xml and adjusts
     * the settings based on the parameters.
     * @param settings The settings for Cocoon.
     */
    void configure(MutableSettings settings);

    /**
     * This callback can be used by the environment to add environment specific
     * information.
     * @param context The context passed to all Avalon based components that are context aware.
     */
    void configure(DefaultContext context);

    /**
     * Create the context object of the environment.
     * @return The context object.
     */
    org.apache.cocoon.environment.Context getEnvironmentContext();

    /**
     * Returns the URL to the application context.
     */
    String getContextURL();

    /**
     * Set the ConfigFile for the Cocoon object.
     *
     * @param configFileName The file location for the cocoon.xconf
     *
     * @throws Exception
     */
    URL getConfigFile(String configFileName)
    throws Exception;
}