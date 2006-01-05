/*
 * Copyright 2005 The Apache Software Foundation.
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
package org.apache.cocoon.core.logging;

import org.apache.avalon.excalibur.logger.LogKitLoggerManager;
import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.cocoon.environment.Environment;
import org.apache.log.ContextMap;
import org.apache.log.Hierarchy;

/**
 * This is an extension of the {@link LoggerManager}. It can be used to
 * initialize a logging context on a per thread basis. This allows the
 * logging implementation to access and log information about the current
 * request.
 * 
 * @version $Id$
 * @since 2.2
 */
public class CocoonLogKitLoggerManager 
    extends LogKitLoggerManager
    implements PerRequestLoggerManager {
    
    public CocoonLogKitLoggerManager() {
        // Use the default hierarchy, which is also used by commons-logging
        super(Hierarchy.getDefaultHierarchy());
    }

    /**
     * @see org.apache.cocoon.core.logging.PerRequestLoggerManager#initializePerRequestLoggingContext(org.apache.cocoon.environment.Environment)
     */
    public Object initializePerRequestLoggingContext(Environment env) {
        ContextMap ctxMap;
        // Initialize a fresh log context containing the object model: it
        // will be used by the CocoonLogFormatter
        ctxMap = ContextMap.getCurrentContext();
        // Add thread name (default content for empty context)
        String threadName = Thread.currentThread().getName();
        ctxMap.set("threadName", threadName);
        // Add the object model
        ctxMap.set("objectModel", env.getObjectModel());
        // Add a unique request id (threadName + currentTime
        ctxMap.set("request-id", threadName + System.currentTimeMillis());
        
        return ctxMap;
    }

    /**
     * @see org.apache.cocoon.core.logging.PerRequestLoggerManager#cleanPerRequestLoggingContext(java.lang.Object)
     */
    public void cleanPerRequestLoggingContext(Object ctxMap) {
        if ( ctxMap != null ) {
            ((ContextMap)ctxMap).clear();
        }
    }

}
