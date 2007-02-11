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

import org.apache.avalon.excalibur.logger.LoggerManager;
import org.apache.cocoon.environment.Environment;

/**
 * This is an extension of the {@link LoggerManager}. It can be used to
 * initialize a logging context on a per thread basis. This allows the
 * logging implementation to access and log information about the current
 * request.
 * 
 * @version $Id:$
 * @since 2.2
 */
public interface PerRequestLoggerManager extends LoggerManager {

    /**
     * Initialize the context for logging.
     */
    Object initializePerRequestLoggingContext(Environment env);

    /**
     * Clean up the logging context.
     */
    void cleanPerRequestLoggingContext(Object ctxMap);
}
