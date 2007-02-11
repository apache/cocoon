/*
 * Copyright 1999-2004 The Apache Software Foundation.
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

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.avalon.framework.logger.Logger;
import org.apache.cocoon.core.BootstrapEnvironment;
import org.apache.cocoon.core.CoreUtil;

public class TestCoreUtil extends CoreUtil {
    public TestCoreUtil(BootstrapEnvironment env) throws Exception {
        super(env);
        this.classloader = TestCoreUtil.class.getClassLoader();
    }

    // Simplified logging
    protected void initLogger() {
        this.log = ((TestBootstrapEnvironment)this.env).logger;
        this.loggerManager = new DefaultLoggerManager(this.log);
    }

    // Simplified classloader handling
    protected void updateEnvironment() throws Exception {}

    /**
     * We use this simple logger manager that sends all output to the console (logger)
     */
    protected static class DefaultLoggerManager implements LoggerManager {
        
        private Logger logger;
        
        public DefaultLoggerManager(Logger logger) {
            this.logger = logger;
        }
        /* (non-Javadoc)
         * @see org.apache.avalon.excalibur.logger.LoggerManager#getDefaultLogger()
         */
        public Logger getDefaultLogger() {
            return this.logger;
        }
        /* (non-Javadoc)
         * @see org.apache.avalon.excalibur.logger.LoggerManager#getLoggerForCategory(java.lang.String)
         */
        public Logger getLoggerForCategory(String arg0) {
            return this.logger;
        }
    }
}
