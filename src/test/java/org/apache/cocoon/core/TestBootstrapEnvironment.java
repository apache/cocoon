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
                                    String processorClassName) {
        this.configuration = configuration;
        this.contextPath = contextPath;
        this.environmentContext = environmentContext;
        this.processorClassName = processorClassName;
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
